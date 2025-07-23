package com.example.backend.services;

import com.example.backend.entities.Blocage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlocageRepository extends JpaRepository<Blocage, Long> {
}
