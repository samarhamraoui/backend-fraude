package com.example.backend.services;

import com.example.backend.dao.FiltresFraudeRepository;
import com.example.backend.entities.CategoriesFraude;
import com.example.backend.entities.FiltresFraude;
import com.example.backend.entities.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FilterService {
    @Autowired
    private FiltresFraudeRepository filresFraudeRepository;
    public List<FiltresFraude> getAllFilters() {
        return filresFraudeRepository.findAll();
    }

    public FiltresFraude getFilter(Integer id) {
        return filresFraudeRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Filter not found with id: " + id));
    }

    public FiltresFraude addFilter(FiltresFraude filtresFraude) {
        return filresFraudeRepository.save(filtresFraude);
    }

    public FiltresFraude updateFilter(Integer id, FiltresFraude filtresFraudeDetails) {
        FiltresFraude existingFilter = filresFraudeRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Filter not found with id: " + id));

        existingFilter.setFiltre(filtresFraudeDetails.getFiltre());
        return filresFraudeRepository.save(existingFilter);
    }

}
