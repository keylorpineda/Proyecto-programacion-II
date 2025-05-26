/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Mapper;
import Models.User;
import Dto.UserDTO;

public class UserMapper {
  public static UserDTO toDTO(User u) {
    UserDTO d = new UserDTO();
    d.setId(u.getId());
    d.setFullName(u.getFullName());
    d.setEmail(u.getEmail());
    return d;
  }
}

