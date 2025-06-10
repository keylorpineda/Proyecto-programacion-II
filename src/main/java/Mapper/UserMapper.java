package Mapper;

import Models.User;
import Dto.UserDTO;

public class UserMapper {

    public static UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setLastName(user.getLastName());
        dto.setUserName(user.getUserName());
        dto.setPassword(user.getPassword());
        dto.setUserRole(user.getUserRole());
        return dto;
    }

    public static User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setLastName(dto.getLastName());
        user.setUserName(dto.getUserName());
        user.setPassword(dto.getPassword());
        user.setUserRole(dto.getUserRole());
        return user;
    }
}
