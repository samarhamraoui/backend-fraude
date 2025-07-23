package com.example.backend.dao;

import com.example.backend.entities.SubModuleReport;
import com.example.backend.entities.SubModuleReportId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubModuleReportRepository extends JpaRepository<SubModuleReport, Long> {

    @Query("SELECT s FROM SubModuleReport s WHERE s.subModule.id = :subModuleId ORDER BY s.order")
    List<SubModuleReport> findBySubModuleId(@Param("subModuleId") Long subModuleId);
}

