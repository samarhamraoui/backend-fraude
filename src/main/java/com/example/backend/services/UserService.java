package com.example.backend.services;

import com.example.backend.entities.User;
import com.example.backend.entities.dto.UserDto;
import java.util.List;
import java.util.Optional;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto updateUser(UserDto userDto);

    UserDto findUserById(Long userId);

    void deleteUser(Long userId);

    List<UserDto> findAllUsers();

    Optional<User> findByUsername(String username);
}
