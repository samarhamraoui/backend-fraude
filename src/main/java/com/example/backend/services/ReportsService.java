package com.example.backend.services;

import com.example.backend.dao.SubModuleReportRepository;
import com.example.backend.entities.RepRapports;
import com.example.backend.entities.SubModuleReport;
import com.example.backend.entities.dto.RepRapportDTO;
import com.example.backend.entities.dto.SubModuleReportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportsService {
    @Autowired
    private SubModuleReportRepository subModuleReportRepository;

    public List<SubModuleReportDTO> getAllSubModuleReports() {
        return subModuleReportRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(SubModuleReport::getOrder)) // Sorting by 'order' field
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<SubModuleReportDTO> getAllReportsBySubModuleId(Long subModuleId) {
        return subModuleReportRepository.findBySubModuleId(subModuleId)
                .stream()
                .sorted(Comparator.comparing(SubModuleReport::getOrder)) // Sorting by 'order' field
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private SubModuleReportDTO toDTO(SubModuleReport entity) {
        SubModuleReportDTO dto = new SubModuleReportDTO();
        dto.setOrder(entity.getOrder());
        RepRapports rapport = entity.getRepRapports();

        RepRapportDTO rapportDTO = new RepRapportDTO();
        rapportDTO.setId(rapport.getId());
        rapportDTO.setTitle(rapport.getTitle());
        rapportDTO.setName(rapport.getName());


        dto.setRepRapports(rapportDTO);
        return dto;
    }

}
