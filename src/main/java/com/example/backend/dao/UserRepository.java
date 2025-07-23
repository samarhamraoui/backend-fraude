package com.example.backend.dao;

import com.example.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
    @Query("SELECT u FROM user u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findUserByUsernameOrEmail(@Param("identifier") String identifier);
}
