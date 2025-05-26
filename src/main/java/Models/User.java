package Models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {

    @Id
    @Column(name="id")
    private Long id;

    @Column(name="name",nullable = false, length = 30)
    private String name;

    @Column(name="lastName", nullable = false, length = 30)
    private String lastName;

    @Column(name="email",nullable = false, unique = true, length = 100)
    private String email;

    @Column(name="password",nullable = false, length = 100)
    private String password;

    public User() {
    }

    public User(Long id, String name, String lastName, String email, String password) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
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

    protected void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Transient
    public String getFullName() {
        return name + " " + lastName;
    }

    @Transient
    public boolean isAdmin() {
        return this instanceof Administrator;
    }
}
