
package com.mycompany.proyectoprogramacionii;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;


public class DataBaseManager {
     private static final EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("CoworkingProgram"); 

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
