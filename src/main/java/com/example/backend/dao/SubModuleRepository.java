package com.example.backend.dao;

import com.example.backend.entities.SubModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubModuleRepository extends JpaRepository<SubModule,Long> {


}
