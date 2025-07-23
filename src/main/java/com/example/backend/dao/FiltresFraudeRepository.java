package com.example.backend.dao;

import com.example.backend.entities.FiltresFraude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FiltresFraudeRepository extends JpaRepository<FiltresFraude,Integer> {
}
