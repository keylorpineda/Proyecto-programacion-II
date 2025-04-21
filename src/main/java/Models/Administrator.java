package Models;
import jakarta.persistence.*; 
@Entity
@DiscriminatorValue("admin")
public class Administrator extends User {

    public Administrator() {
        super();
    }
    
    public Administrator(String userName, String name, String lastName, String identification, String password) {
        super(userName, name, lastName, identification, password);
    }
    public void manageUsers(){}
    public void manageSpaces(){}
    public void manageReservations(){}

}
