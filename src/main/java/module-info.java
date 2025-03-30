module com.mycompany.proyectoprogramacionii {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    opens Controllers to javafx.fxml; 
    opens com.mycompany.proyectoprogramacionii to javafx.fxml;
    exports com.mycompany.proyectoprogramacionii;
    exports Models;
    opens Models to javafx.fxml;
}
