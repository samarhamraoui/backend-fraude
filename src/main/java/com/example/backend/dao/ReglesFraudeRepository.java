package com.example.backend.dao;

import com.example.backend.entities.ReglesFraude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReglesFraudeRepository extends JpaRepository<ReglesFraude, Integer> {
    @Query("SELECT r FROM regles_fraudes r ORDER BY r.etat ASC")
    List<ReglesFraude> findAllSortedByEtat();
}
