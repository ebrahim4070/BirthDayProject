package com.birthday.model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/Birthday";
    private static final String USER = "root";
    private static final String PASSWORD = "ebrahim";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static List<Birthday> getAllBirthdays() throws SQLException {
        List<Birthday> birthdays = new ArrayList<>();
        String sql = "SELECT * FROM birthdays";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                birthdays.add(mapResultSetToBirthday(rs));
            }
        }
        return birthdays;
    }

    public static List<Birthday> getTodayBirthdays() throws SQLException {
        List<Birthday> birthdays = new ArrayList<>();
        String sql = "SELECT * FROM birthdays WHERE MONTH(birth_date) = ? AND DAY(birth_date) = ?";
        LocalDate today = LocalDate.now();

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, today.getMonthValue());
            pstmt.setInt(2, today.getDayOfMonth());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    birthdays.add(mapResultSetToBirthday(rs));
                }
            }
        }
        return birthdays;
    }

    public static void addBirthday(Birthday birthday) throws SQLException {
        if (existsById(birthday.getId())) {
            throw new SQLException("এই ID ইতোমধ্যে বিদ্যমান আছে। দয়া করে অন্য ID ব্যবহার করুন।");
        }

        String sql = "INSERT INTO birthdays (id, name, birth_date, phone, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, birthday.getId());
            pstmt.setString(2, birthday.getName());
            pstmt.setDate(3, Date.valueOf(birthday.getBirthDate()));
            pstmt.setString(4, birthday.getPhone());
            pstmt.setString(5, birthday.getEmail());

            pstmt.executeUpdate();
        }
    }

    public static void updateBirthday(Birthday birthday) throws SQLException {
        String sql = "UPDATE birthdays SET name = ?, birth_date = ?, phone = ?, email = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, birthday.getName());
            pstmt.setDate(2, Date.valueOf(birthday.getBirthDate()));
            pstmt.setString(3, birthday.getPhone());
            pstmt.setString(4, birthday.getEmail());
            pstmt.setString(5, birthday.getId());

            pstmt.executeUpdate();
        }
    }

    public static void deleteBirthday(String id) throws SQLException {
        String sql = "DELETE FROM birthdays WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    public static List<Birthday> searchBirthdays(String searchTerm) throws SQLException {
        List<Birthday> birthdays = new ArrayList<>();
        String sql;
        String englishMonth = convertMonthName(searchTerm);

        if (englishMonth != null) {
            // মাসের নাম পাওয়া গেছে
            sql = "SELECT * FROM birthdays WHERE MONTHNAME(birth_date) = ?";
        } else {
            // নাম অথবা ID দিয়ে সার্চ
            sql = "SELECT * FROM birthdays WHERE id LIKE ? OR name LIKE ?";
        }

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (englishMonth != null) {
                pstmt.setString(1, englishMonth);
            } else {
                pstmt.setString(1, "%" + searchTerm + "%");
                pstmt.setString(2, "%" + searchTerm + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    birthdays.add(mapResultSetToBirthday(rs));
                }
            }
        }
        return birthdays;
    }

    public static boolean existsById(String id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM birthdays WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private static Birthday mapResultSetToBirthday(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        LocalDate birthDate = rs.getDate("birth_date").toLocalDate();
        String phone = rs.getString("phone");
        String email = rs.getString("email");

        return new Birthday(id, name, birthDate, phone, email);
    }

    private static String convertMonthName(String name) {
        switch (name.trim().toLowerCase()) {
            case "january": case "জানুয়ারি": return "January";
            case "february": case "ফেব্রুয়ারি": return "February";
            case "march": case "মার্চ": return "March";
            case "april": case "এপ্রিল": return "April";
            case "may": case "মে": return "May";
            case "june": case "জুন": return "June";
            case "july": case "জুলাই": return "July";
            case "august": case "আগস্ট": return "August";
            case "september": case "সেপ্টেম্বর": return "September";
            case "october": case "অক্টোবর": return "October";
            case "november": case "নভেম্বর": return "November";
            case "december": case "ডিসেম্বর": return "December";
            default: return null;
        }
    }
}
