package Dto;

public class UserDTO {

    private Long id;
    private String name;
    private String lastName;
    private String userName;
    private String password;
    private String userRole;

    public UserDTO() {
    }

    public UserDTO(Long id, String name, String lastName, String userName, String password, String userRole) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
        this.userRole = userRole;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getFullName() {
        return name + " " + lastName;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.userRole);
    }
}
