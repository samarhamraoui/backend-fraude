package com.example.backend.services;

import com.example.backend.dao.GenericRepositoryImpl;
import com.example.backend.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MultiTableRefService {

    @Autowired
    private GenericRepositoryImpl genericRepository;

    public List<Offre> getAllOffre() {
        return genericRepository.findAll(Offre.class);
    }

    public List<ListAppelant> getAllAppelant() {
        return genericRepository.findAll(ListAppelant.class);
    }

    public List<ListAppele> getAllAppele() {
        return genericRepository.findAll(ListAppele.class);
    }

    public List<ListCellid> getAllCellId() {
        return genericRepository.findAll(ListCellid.class);
    }

    public List<ListImei> getAllImei() {
        return genericRepository.findAll(ListImei.class);
    }

    public List<TypeDestination> getAllTypedest() {
        return genericRepository.findAll(TypeDestination.class);
    }

    public List<PlanTarifaire> getAllPlanTarifaire() {
        return genericRepository.findAll(PlanTarifaire.class);
    }
}
