package com.fmi.quarkus.service;


public class ChangePasswordRequest {

    public String currentPassword;
    public String newPassword;
    public String confirmPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String currentPassword, String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
}
