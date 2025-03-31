package Models;

import java.util.ArrayList;
import java.util.List;

public class UserManager {

    private static UserManager instance;
    protected List<User> users;

    private UserManager(){
        users = new ArrayList<>();
    }

    public static UserManager getInstance(){
        if(instance == null){
            instance = new UserManager();
        }
        return instance;
    }


    public void addUser(User user){
        users.add(user);
    }
    public void deleteUser(String username){

    }
    public User getUserByUsername(String username) {
        for(User user : users) {
            if(user.getUserName().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public boolean authenticateUser(String username, String password){
        for(User user : users){
            if(user.getUserName().equals(username) && user.getPassword().equals(password)){
                return true;
            }
        }
        return false;
    }


    public boolean changeUserData(String userName, String name, String lastName, String identification, String password, String userRol ){
        User option= getUserByUsername(name);
        if(option != null) {
            option.setName(name);
            option.setLastName(lastName);
            option.setIdentification(identification);
            option.setPassword(password);
            option.setUserRol(userRol);
            return true;
        }
        else {
            return false;
        }
    }

    public List<User> getUsers() {
        return users;
    }


}
