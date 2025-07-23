package com.example.backend.dao;

import com.example.backend.entities.KpiMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KpiRepository extends JpaRepository<KpiMetadata, Long> {
    List<KpiMetadata> findByUserId(Long userId);
}

