package com.example.backend.services;

import com.example.backend.dao.UserRepository;
import com.example.backend.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //System.out.println("Loading user by username: " + username);
        Optional<User> optionalUser = userRepository.findUserByUsername(username);
        optionalUser.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        User user = optionalUser.get();
        //System.out.println("User found: " + user.getUsername() + ", Password: " + user.getPassword());

        // Return the UserDetails object
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRole() != null
                        ? Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getRole()))
                        : Collections.emptyList()
        );
    }
}