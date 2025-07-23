package com.example.backend.dao;

import com.example.backend.entities.BlocageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloacageLogRepository extends JpaRepository<BlocageLog, Long> {
}
