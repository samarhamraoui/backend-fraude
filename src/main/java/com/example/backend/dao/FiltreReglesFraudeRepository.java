package com.example.backend.dao;

import com.example.backend.entities.FiltresReglesFraude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FiltreReglesFraudeRepository extends JpaRepository<FiltresReglesFraude, Integer> {
}
