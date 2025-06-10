package Models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "lastName", nullable = false, length = 30)
    private String lastName;

    @Column(name = "userName", nullable = false, unique = true, length = 100)
    private String userName;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "userRole", nullable = false, length = 20)
    private String userRole;

    public User() {
    }

    public User(Long id, String name, String lastName, String userName, String password, String userRole) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
        this.userRole = userRole;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    @Transient
    public String getFullName() {
        return name + " " + lastName;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.userRole);
    }
}
