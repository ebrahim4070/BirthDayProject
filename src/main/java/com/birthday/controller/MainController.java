package com.birthday.controller;

import com.birthday.model.Birthday;
import com.birthday.util.EmailUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML private TableView<Birthday> birthdayTable;
    @FXML private TableColumn<Birthday, String> idColumn;
    @FXML private TableColumn<Birthday, String> nameColumn;
    @FXML private TableColumn<Birthday, LocalDate> birthDateColumn;
    @FXML private TableColumn<Birthday, String> phoneColumn;
    @FXML private TableColumn<Birthday, String> emailColumn;

    @FXML private TextField idField, nameField, phoneField, emailField;
    @FXML private DatePicker datePicker;
    @FXML private TextField searchField;

    private Connection conn;
    private ObservableList<Birthday> birthdayList;

    @FXML
    public void initialize() {
        connectDatabase();
        if (conn != null) {
            initColumns();
            loadBirthdays();
            birthdayTable.setOnMouseClicked(this::handleTableClick);
        }
    }

    private void connectDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/BirthdayModify", "root", "ebrahim");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "ডাটাবেস কানেকশন ব্যর্থ", e.getMessage());
        }
    }

    private void initColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void loadBirthdays() {
        birthdayList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM birthdays";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Birthday b = new Birthday(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getDate("birth_date").toLocalDate(),
                        rs.getString("phone"),
                        rs.getString("email")
                );
                birthdayList.add(b);
            }
            birthdayTable.setItems(birthdayList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "ডাটা লোডিং ব্যর্থ", e.getMessage());
        }
    }

    @FXML
    private void addBirthday() {
        String id = idField.getText();
        String name = nameField.getText();
        LocalDate birthDate = datePicker.getValue();
        String phone = phoneField.getText();
        String email = emailField.getText();

        if (id.isEmpty() || name.isEmpty() || birthDate == null || phone.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "অনুপস্থিত ইনপুট", "সকল ফিল্ড পূরণ করুন।");
            return;
        }

        String sql = "INSERT INTO birthdays (id, name, birth_date, phone, email, email_sent) VALUES (?, ?, ?, ?, ?, 0)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setDate(3, Date.valueOf(birthDate));
            ps.setString(4, phone);
            ps.setString(5, email);
            ps.executeUpdate();
            loadBirthdays();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "সফল", "নতুন জন্মদিন যোগ করা হয়েছে।");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "যোগ ব্যর্থ", e.getMessage());
        }
    }

    @FXML
    private void updateBirthday() {
        String id = idField.getText();
        if (id.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "ভুল আইডি", "সঠিক আইডি দিন।");
            return;
        }

        String name = nameField.getText();
        LocalDate birthDate = datePicker.getValue();
        String phone = phoneField.getText();
        String email = emailField.getText();

        String sql = "UPDATE birthdays SET name=?, birth_date=?, phone=?, email=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDate(2, Date.valueOf(birthDate));
            ps.setString(3, phone);
            ps.setString(4, email);
            ps.setString(5, id);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                showAlert(Alert.AlertType.WARNING, "আপডেট ব্যর্থ", "এই আইডির কোন রেকর্ড পাওয়া যায়নি।");
            } else {
                loadBirthdays();
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "সফল", "জন্মদিন আপডেট করা হয়েছে।");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "আপডেট ব্যর্থ", e.getMessage());
        }
    }

    @FXML
    private void deleteBirthday() {
        String id = idField.getText();
        if (id.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "ভুল আইডি", "সঠিক আইডি দিন।");
            return;
        }

        String sql = "DELETE FROM birthdays WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int deleted = ps.executeUpdate();
            if (deleted == 0) {
                showAlert(Alert.AlertType.WARNING, "ডিলিট ব্যর্থ", "এই আইডির কোন রেকর্ড পাওয়া যায়নি।");
            } else {
                loadBirthdays();
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "সফল", "জন্মদিন মুছে ফেলা হয়েছে।");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "ডিলিট ব্যর্থ", e.getMessage());
        }
    }

    @FXML
    private void searchBirthdays() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            loadBirthdays();
            return;
        }

        List<Birthday> filtered = new ArrayList<>();
        for (Birthday b : birthdayList) {
            if (b.getId().toLowerCase().contains(searchText) ||
                    b.getName().toLowerCase().contains(searchText) ||
                    (b.getBirthDate() != null && b.getBirthDate().getMonth().toString().toLowerCase().contains(searchText))) {
                filtered.add(b);
            }
        }
        birthdayTable.setItems(FXCollections.observableArrayList(filtered));
    }

    // ---------------------
    // ইমেইল একবারে পাঠানো এবং email_sent ফ্ল্যাগ সেট করা
    @FXML
    private void showTodayBirthdays() {
        LocalDate today = LocalDate.now();
        List<Birthday> todayBirthdays = new ArrayList<>();
        StringBuilder names = new StringBuilder();

        for (Birthday b : birthdayList) {
            if (b.getBirthDate() != null &&
                    b.getBirthDate().getMonth() == today.getMonth() &&
                    b.getBirthDate().getDayOfMonth() == today.getDayOfMonth()) {

                todayBirthdays.add(b);

                // চেক করো ইমেইল আগেই গেছে কিনা
                if (!hasEmailBeenSent(b.getId())) {
                    String subject = "শুভ জন্মদিন!";
                    String body = "প্রিয় " + b.getName() + ",\n\nশুভ জন্মদিন! আপনার জীবন আনন্দ ও সাফল্যে ভরে উঠুক।\n\n-- MD.EBRAHIM";
                    EmailUtil.sendEmail(b.getEmail(), subject, body);

                    // ইমেইল পাঠানোর পরে flag update করো
                    setEmailSentFlag(b.getId(), true);
                }

                names.append("- ").append(b.getName()).append("\n");
            }
        }

        birthdayTable.setItems(FXCollections.observableArrayList(todayBirthdays));

        if (todayBirthdays.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "আজকের জন্মদিন", "আজ কারো জন্মদিন নেই।");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "আজকের জন্মদিন",
                    todayBirthdays.size() + " জনের জন্মদিন আজ:\n\n" + names.toString());
        }
    }

    private boolean hasEmailBeenSent(String id) {
        String sql = "SELECT email_sent FROM birthdays WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("email_sent");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setEmailSentFlag(String id, boolean sent) {
        String sql = "UPDATE birthdays SET email_sent = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, sent);
            ps.setString(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------------
    // প্রতিদিন email_sent ফ্ল্যাগ রিসেট করার জন্য
    public void resetEmailSentFlags() {
        String sql = "UPDATE birthdays SET email_sent = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int updatedRows = ps.executeUpdate();
            System.out.println("Reset email_sent flags for " + updatedRows + " records.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "ডাটাবেজ ত্রুটি", "email_sent রিসেট করতে সমস্যা হয়েছে: " + e.getMessage());
        }
    }

    private void handleTableClick(MouseEvent event) {
        Birthday selected = birthdayTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            idField.setText(selected.getId());
            nameField.setText(selected.getName());
            datePicker.setValue(selected.getBirthDate());
            phoneField.setText(selected.getPhone());
            emailField.setText(selected.getEmail());
        }
    }

    private void clearFields() {
        idField.clear();
        nameField.clear();
        datePicker.setValue(null);
        phoneField.clear();
        emailField.clear();
        searchField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
