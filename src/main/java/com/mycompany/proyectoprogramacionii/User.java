/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectoprogramacionii;

/**
 *
 * @author David
 */


/**

JavaFX App*/
public abstract class User {
    protected String userName;
    protected String name;
    protected String lastName;
    protected String identification;
    protected String password;
    protected String userRol;

    public String getUserName() {
        return userName;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getIdentification() {
        return identification;
    }

    public String getPassword() {
        return password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
