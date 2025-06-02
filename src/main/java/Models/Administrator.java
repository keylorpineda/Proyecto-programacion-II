
package Models;

import jakarta.persistence.*;

@Entity
@Table(name = "administrators")
@PrimaryKeyJoinColumn(name = "id")
public class Administrator extends User {

    public Administrator() { }

    public Administrator(Long id, String firstName, String lastName, String email, String password) {
        super(id, firstName, lastName, email, password, "ADMIN"); 
    }
}
