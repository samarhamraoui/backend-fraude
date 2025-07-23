package com.example.backend.dao;

import com.example.backend.entities.Group;
import com.example.backend.entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group,Long> {

   // Optional<Group> findGroupByModule(Long Id);
}
