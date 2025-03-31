package Models;

public class Administrator extends User {
    public Administrator(String userName, String name, String lastName, String identification, String password, String userRol) {
        super(userName, name, lastName, identification, password, userRol);
    }
    public void manageUsers(){}
    public void manageSpaces(){}
    public void manageReservations(){}

}
