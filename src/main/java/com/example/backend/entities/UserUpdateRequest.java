package com.example.backend.entities;

public class UserUpdateRequest {

    private String username;
    private String uMail;
    private String nomUtilisateur;
    private String oldPassword;
    private String newPassword;

    // Constructors, getters, and setters

    public UserUpdateRequest() {
    }

    public UserUpdateRequest(String username, String uMail, String nomUtilisateur, String newPassword,String oldPassword) {
        this.username = username;
        this.uMail = uMail;
        this.nomUtilisateur = nomUtilisateur;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUMail() {
        return uMail;
    }

    public void setUMail(String uMail) {
        this.uMail = uMail;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }


}
