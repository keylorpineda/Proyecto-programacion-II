
package Models;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "id")
public class Customer extends User {

    public Customer() { }

    public Customer(Long id, String firstName, String lastName, String email, String password) {
        super(id, firstName, lastName, email, password, "CUSTOMER");
    }
}


