module com.mycompany.proyectoprogramacionii {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    // JPA API
    requires java.persistence;
    // Hibernate ORM
    requires org.hibernate.orm.core;
    // SLF4J (logging para Hibernate)
    requires org.slf4j.simple;
    opens Controllers to javafx.fxml; 
    opens com.mycompany.proyectoprogramacionii to javafx.fxml;
    exports com.mycompany.proyectoprogramacionii;
    exports Models;
    opens Models to javafx.fxml, org.hibernate.orm.core;
    requires jakarta.persistence;
}
