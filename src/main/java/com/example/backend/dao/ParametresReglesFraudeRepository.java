package com.example.backend.dao;

import com.example.backend.entities.ParametresReglesFraude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParametresReglesFraudeRepository extends JpaRepository<ParametresReglesFraude, Long> {
}
