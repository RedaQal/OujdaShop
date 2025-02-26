package com.example.oujdashop.model;

public class User {

    private int id;
    private String email;
    private String password;
    private String nom;
    private String prenom;

    public int getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public User(String nom , String prenom , String email , String password){
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
    }
    public User(int id,String nom , String prenom , String email , String password){
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
    }
}
