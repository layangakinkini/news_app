package com.layanga.tech_pulse;

public class HelperClass {
    private String username;
    private String email;
    private String createdAt;
    private String password;

    // Default constructor required for calls to DataSnapshot.getValue(HelperClass.class)
    public HelperClass() {
    }

    public HelperClass(String username, String email, String createdAt, String password) {
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.password = password;
    }

    // Getter and Setter methods

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
