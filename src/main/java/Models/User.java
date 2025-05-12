package Models;
import jakarta.persistence.*;
import jakarta.persistence.Table;
@Entity                      
@Table(name = "users")    
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) 
@DiscriminatorColumn(name = "role")
public class User {
    @Id                    
    @Column(name = "user_name", length = 50)
    private String userName;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "last_name", nullable = false, length = 30)
    private String lastName;

    @Column(name = "identification", unique = true, length = 9)
    private String identification;

    @Column(name = "password", nullable = false, length = 8)  
    private String password;

    @Column(name = "role", insertable = false, updatable = false)
    private String role;
    public User() {
    }
  
    public User(String name, String lastName, String id,String userName, String password) {
        this.userName = userName;
        this.name = name;
        this.lastName = lastName;
        this.identification = id;
        this.password = password;
    }

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
    public void updatePassword(String newPassword){
        password = newPassword;
    }
     public String getRole() {
        return role;
    }
     
    @Transient
    public boolean isAdmin() {
        return "admin".equals(role);
    }
}
