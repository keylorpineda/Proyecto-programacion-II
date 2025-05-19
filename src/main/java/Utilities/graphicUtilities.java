/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utilities;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class graphicUtilities {
    
    public void showAlert(AlertType type, String title, String message) {
    Alert alert = new Alert(type); 
    alert.setTitle(title);
    alert.setHeaderText(null); 
    alert.setContentText(message);
    alert.showAndWait();
}
}
