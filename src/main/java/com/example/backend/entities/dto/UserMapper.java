package com.example.backend.entities.dto;

import com.example.backend.entities.User;
import org.springframework.stereotype.Component;


@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreationDate(user.getCreationDate());
        dto.setAccountNonExpired(user.isAccountNonExpired());
        dto.setAccountNonLocked(user.isAccountNonLocked());
        dto.setCredentialsNonExpired(user.isCredentialsNonExpired());
        dto.setEnabled(user.isEnabled());
        if (user.getUser_group() != null) {
            dto.setGroupeName(user.getUser_group().getgName());
            dto.setGroupId(user.getUser_group().getgId());
        }

        return dto;
    }

    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setPassword(dto.getPassword());
        user.setCreationDate(dto.getCreationDate());
        user.setAccountNonExpired(dto.isAccountNonExpired());
        user.setAccountNonLocked(dto.isAccountNonLocked());
        user.setCredentialsNonExpired(dto.isCredentialsNonExpired());
        user.setEnabled(dto.isEnabled());
        return user;
    }
}
