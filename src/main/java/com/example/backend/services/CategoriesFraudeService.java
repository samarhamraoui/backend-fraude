package com.example.backend.services;

import com.example.backend.dao.CategoriesRespository;
import com.example.backend.entities.CategoriesFraude;
import com.example.backend.entities.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriesFraudeService {

    @Autowired
    private CategoriesRespository categoriesRespository;

    public List<CategoriesFraude> getCategories() {
        return categoriesRespository.findAll();
    }

    public CategoriesFraude getCategory(Integer id) {
        return categoriesRespository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    public CategoriesFraude addCategory(CategoriesFraude category) {
        return categoriesRespository.save(category);
    }

    public CategoriesFraude updateCategory(Integer id, CategoriesFraude category) {
        CategoriesFraude existing = categoriesRespository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        existing.setNomCategorie(category.getNomCategorie());
        return categoriesRespository.save(existing);
    }

    public void deleteCategory(Integer id) {
        if (!categoriesRespository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoriesRespository.deleteById(id);
    }
}
