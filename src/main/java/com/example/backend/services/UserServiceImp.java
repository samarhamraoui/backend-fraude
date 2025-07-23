package com.example.backend.services;

import com.example.backend.dao.GroupRepository;
import com.example.backend.dao.RoleRepository;
import com.example.backend.dao.UserRepository;
import com.example.backend.entities.Group;
import com.example.backend.entities.Role;
import com.example.backend.entities.User;
import com.example.backend.entities.dto.UserDto;
import com.example.backend.entities.dto.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getGroupId() == null) {
            throw new IllegalArgumentException("Group ID must not be null (user must belong to a group).");
        }
        User user = userMapper.toEntity(userDto);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        Group group = groupRepository.findById(userDto.getGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id=" + userDto.getGroupId()));
        user.setUser_group(group);
        if (userDto.getRole() == null || userDto.getRole().getId() == null) {
            throw new IllegalArgumentException("Role ID must not be null.");
        }
        Role role = roleRepository.findById(userDto.getRole().getId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id=" + userDto.getRole().getId()));
        user.setRole(role);

        user.setCreationDate(LocalDateTime.now());
        user.setEnabled(userDto.isEnabled());
        user.setAccountNonExpired(userDto.isAccountNonExpired());
        user.setAccountNonLocked(userDto.isAccountNonLocked());
        user.setCredentialsNonExpired(userDto.isCredentialsNonExpired());
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        if (userDto.getId() == null) {
            throw new IllegalArgumentException("User ID is required for update.");
        }

        User existingUser = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id=" + userDto.getId()));

        if (userDto.getUsername() != null) {
            existingUser.setUsername(userDto.getUsername());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        if (userDto.getGroupId() != null) {
            Group group = groupRepository.findById(userDto.getGroupId())
                    .orElseThrow(() -> new EntityNotFoundException("Group not found with id=" + userDto.getGroupId()));
            existingUser.setUser_group(group);
        }
        if (userDto.getRole() != null && userDto.getRole().getId() != null) {
            Role role = roleRepository.findById(userDto.getRole().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Role not found with id=" + userDto.getRole().getId()));
            existingUser.setRole(role);
        }
        existingUser.setEnabled(userDto.isEnabled());
        userRepository.save(existingUser);
        return userMapper.toDto(existingUser);
    }

    @Override
    public UserDto findUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id=" + userId));
        return userMapper.toDto(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id=" + userId));
        userRepository.delete(user);
    }

    @Override
    public List<UserDto> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }
}
