package com.example.backend.dao;

import com.example.backend.entities.CategoriesFraude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriesRespository extends JpaRepository<CategoriesFraude,Integer> {}
