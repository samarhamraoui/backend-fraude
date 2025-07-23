package com.example.backend.dao;

import com.example.backend.entities.Group;
import com.example.backend.entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ModuleRepository extends JpaRepository<Module,Long> {
   // List<Module> findModulesByGroupId(Long gId);


}
