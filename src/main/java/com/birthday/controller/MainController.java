package com.birthday.controller;

import com.birthday.model.Birthday;
import com.birthday.model.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MainController {
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private DatePicker datePicker;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField searchField;
    @FXML private TableView<Birthday> birthdayTable;

    private ObservableList<Birthday> birthdayData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up table columns
        birthdayTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        birthdayTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        birthdayTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        birthdayTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("phone"));
        birthdayTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("email"));

        // Load all birthdays
        loadAllBirthdays();

        // Check for today's birthdays
        checkTodayBirthdays();

        // Add listener to table selection
        birthdayTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showBirthdayDetails(newValue));
    }

    private void loadAllBirthdays() {
        try {
            List<Birthday> birthdays = Database.getAllBirthdays();
            birthdayData.setAll(birthdays);
            birthdayTable.setItems(birthdayData);
        } catch (SQLException e) {
            showAlert("ডাটাবেস ত্রুটি", "সমস্ত জন্মদিন লোড করতে সমস্যা হয়েছে: " + e.getMessage());
        }
    }

    private void checkTodayBirthdays() {
        try {
            List<Birthday> todayBirthdays = Database.getTodayBirthdays();
            if (!todayBirthdays.isEmpty()) {
                StringBuilder message = new StringBuilder("আজকের জন্মদিন:\n");
                for (Birthday b : todayBirthdays) {
                    message.append(b.getName()).append("\n");
                }
                showAlert("জন্মদিনের শুভেচ্ছা!", message.toString());
            }
        } catch (SQLException e) {
            showAlert("ডাটাবেস ত্রুটি", "আজকের জন্মদিন চেক করতে সমস্যা হয়েছে: " + e.getMessage());
        }
    }

    private void showBirthdayDetails(Birthday birthday) {
        if (birthday != null) {
            idField.setText(birthday.getId());
            nameField.setText(birthday.getName());
            datePicker.setValue(birthday.getBirthDate());
            phoneField.setText(birthday.getPhone());
            emailField.setText(birthday.getEmail());
        }
    }

    @FXML
    private void addBirthday() {
        if (validateFields()) {
            try {
                Birthday birthday = new Birthday(
                        idField.getText(),
                        nameField.getText(),
                        datePicker.getValue(),
                        phoneField.getText(),
                        emailField.getText()
                );
                Database.addBirthday(birthday);
                loadAllBirthdays();
                clearFields();
                showAlert("সফল", "জন্মদিন সফলভাবে যোগ করা হয়েছে");
            } catch (SQLException e) {
                showAlert("ডাটাবেস ত্রুটি", "জন্মদিন যোগ করতে সমস্যা হয়েছে: " + e.getMessage());
            }
        }
    }

    @FXML
    private void updateBirthday() {
        if (validateFields()) {
            try {
                Birthday birthday = new Birthday(
                        idField.getText(),
                        nameField.getText(),
                        datePicker.getValue(),
                        phoneField.getText(),
                        emailField.getText()
                );
                Database.updateBirthday(birthday);
                loadAllBirthdays();
                showAlert("সফল", "জন্মদিন সফলভাবে আপডেট করা হয়েছে");
            } catch (SQLException e) {
                showAlert("ডাটাবেস ত্রুটি", "জন্মদিন আপডেট করতে সমস্যা হয়েছে: " + e.getMessage());
            }
        }
    }

    @FXML
    private void deleteBirthday() {
        String id = idField.getText();
        if (id.isEmpty()) {
            showAlert("ত্রুটি", "মুছতে একটি আইডি নির্বাচন করুন");
            return;
        }

        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("নিশ্চিতকরণ");
            alert.setHeaderText("আপনি কি নিশ্চিত যে আপনি এই জন্মদিন মুছতে চান?");
            alert.setContentText("আইডি: " + id);

            if (alert.showAndWait().get() == ButtonType.OK) {
                Database.deleteBirthday(id);
                loadAllBirthdays();
                clearFields();
                showAlert("সফল", "জন্মদিন সফলভাবে মুছে ফেলা হয়েছে");
            }
        } catch (SQLException e) {
            showAlert("ডাটাবেস ত্রুটি", "জন্মদিন মুছতে সমস্যা হয়েছে: " + e.getMessage());
        }
    }

    @FXML
    private void searchBirthdays() {
        String searchTerm = searchField.getText();
        if (searchTerm.isEmpty()) {
            loadAllBirthdays();
            return;
        }

        try {
            List<Birthday> birthdays = Database.searchBirthdays(searchTerm);
            birthdayData.setAll(birthdays);
            birthdayTable.setItems(birthdayData);
        } catch (SQLException e) {
            showAlert("ডাটাবেস ত্রুটি", "জন্মদিন খুঁজতে সমস্যা হয়েছে: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (idField.getText().isEmpty() || nameField.getText().isEmpty() || datePicker.getValue() == null) {
            showAlert("ত্রুটি", "আইডি, নাম এবং জন্ম তারিখ অবশ্যই পূরণ করতে হবে");
            return false;
        }
        return true;
    }

    private void clearFields() {
        idField.clear();
        nameField.clear();
        datePicker.setValue(null);
        phoneField.clear();
        emailField.clear();
    }
    @FXML
    private void showTodayBirthdays() {
        try {
            List<Birthday> todayBirthdays = Database.getTodayBirthdays();
            if (!todayBirthdays.isEmpty()) {
                birthdayData.setAll(todayBirthdays);
                birthdayTable.setItems(birthdayData);
                showAlert("আজকের জন্মদিন", "আজকের জন্মদিনের তালিকা দেখানো হচ্ছে।");
            } else {
                showAlert("আজকের জন্মদিন", "আজ কারো জন্মদিন নেই।");
            }
        } catch (SQLException e) {
            showAlert("ডাটাবেস ত্রুটি", "আজকের জন্মদিন লোড করতে সমস্যা হয়েছে: " + e.getMessage());
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}