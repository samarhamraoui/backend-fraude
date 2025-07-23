package com.example.backend.services;

import com.example.backend.dao.*;
import com.example.backend.entities.*;
import com.example.backend.entities.dto.FilterDto;
import com.example.backend.entities.dto.ParameterDto;
import com.example.backend.entities.dto.RuleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class RuleService {
    @Autowired
    private ReglesFraudeRepository reglesFraudeRepository;
    @Autowired
    private CategoriesRespository categoriesFraudeRepository;
    @Autowired
    private FlowRepository flowRepository;
    @Autowired
    private FiltresFraudeRepository filtresFraudeRepository;
    @Autowired
    private FiltreReglesFraudeRepository filtreReglesFraudeRepository;
    @Autowired
    private ParametresReglesFraudeRepository parametresReglesFraudeRepository;

    public List<ReglesFraude> getAllRules(){
        return reglesFraudeRepository.findAllSortedByEtat();
    }

    @Transactional
    public ReglesFraude createRule(RuleDTO dto) {
        CategoriesFraude cat = categoriesFraudeRepository.findById(dto.getCategorie().getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Flow flow = flowRepository.findById(dto.getFlux().getId())
                .orElseThrow(() -> new RuntimeException("Flow not found"));

        ReglesFraude regle = new ReglesFraude();
        regle.setDateModif(new Timestamp(System.currentTimeMillis()));
        regle.setNom(dto.getNom());
        regle.setDescription(dto.getDescription());
        regle.setEtat(dto.getEtat());
        regle.setType(dto.getType());
        regle.setCategorie(cat);
        regle.setFlux(flow);

        List<ParametresReglesFraude> paramList = new ArrayList<>();
        if (dto.getListe_parameters() != null) {
            for (ParameterDto p : dto.getListe_parameters()) {
                Flow paramFlow = null;
                if (p.getFlow() != null) {
                    paramFlow = flowRepository.findById(p.getFlow().getId())
                            .orElseThrow(() -> new RuntimeException("ParamFlow not found"));
                }

                ParametresReglesFraude paramEntity = new ParametresReglesFraude();
                paramEntity.setDateModif(new Timestamp(System.currentTimeMillis()));
                paramEntity.setVegal(p.getVegal());
                paramEntity.setVmax(p.getVmax());
                paramEntity.setVmin(p.getVmin());
                paramEntity.setFlow(paramFlow);
                paramEntity.setRegle(regle);

                paramList.add(paramEntity);
            }
        }
        regle.setListe_parameters(paramList);

        List<FiltresReglesFraude> filterList = new ArrayList<>();
        if (dto.getListe_filters() != null) {
            for (FilterDto f : dto.getListe_filters()) {
                FiltresFraude fil = filtresFraudeRepository.findById(f.getFiltreFraude().getId())
                        .orElseThrow(() -> new RuntimeException("FiltresFraude not found"));

                FiltresReglesFraude filEntity = new FiltresReglesFraude();
                filEntity.setDateModif(new Timestamp(System.currentTimeMillis()));
                filEntity.setInegal(f.getInegal());
                filEntity.setVdef(f.getVdef());
                filEntity.setVegal(f.getVegal());
                filEntity.setVlike(f.getVlike());
                filEntity.setVnotlike(f.getVnotlike());
                filEntity.setFiltreFraude(fil);
                filEntity.setRegle(regle);

                filterList.add(filEntity);
            }
        }
        regle.setListe_filters(filterList);
        return reglesFraudeRepository.save(regle);
    }

    @Transactional
    public ReglesFraude editRuleOnly(Integer id, RuleDTO dto) {
        // load existing
        ReglesFraude existing = reglesFraudeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found with id=" + id));

        // update top-level fields
        existing.setDateModif(new Timestamp(System.currentTimeMillis()));
        existing.setNom(dto.getNom());
        existing.setDescription(dto.getDescription());
        existing.setEtat(dto.getEtat());
        existing.setType(dto.getType());

        // validate new category & flow if changed
        if (dto.getCategorie() != null) {
            CategoriesFraude cat = categoriesFraudeRepository.findById(dto.getCategorie().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            existing.setCategorie(cat);
        }

        return reglesFraudeRepository.save(existing);
    }

    @Transactional
    public ReglesFraude editRule(Integer id, RuleDTO dto, boolean updateParamsAndFilters) {
        // Load existing rule
        ReglesFraude existing = reglesFraudeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found with id=" + id));

        // Update only if values have changed
        if (!existing.getNom().equals(dto.getNom())) {
            existing.setNom(dto.getNom());
        }
        if (!existing.getDescription().equals(dto.getDescription())) {
            existing.setDescription(dto.getDescription());
        }
        if (!existing.getEtat().equals(dto.getEtat())) {
            existing.setEtat(dto.getEtat());
        }
        if (!existing.getType().equals(dto.getType())) {
            existing.setType(dto.getType());
        }

        // Compare and update category if changed
        if (!existing.getCategorie().getId().equals(dto.getCategorie().getId())) {
            CategoriesFraude newCategory = categoriesFraudeRepository.findById(dto.getCategorie().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            existing.setCategorie(newCategory);
        }

        // If no need to update parameters and filters, return updated rule
        if (!updateParamsAndFilters) {
            return reglesFraudeRepository.save(existing);
        }

        // Compare and update flow if changed
        if (!existing.getFlux().getId().equals(dto.getFlux().getId())) {
            Flow newFlow = flowRepository.findById(dto.getFlux().getId())
                    .orElseThrow(() -> new RuntimeException("Flow not found"));
            existing.setFlux(newFlow);
        }

        // Update parameters only if different
        if (!existing.getListe_parameters().equals(dto.getListe_parameters())) {
            existing.getListe_parameters().clear();
            for (ParameterDto p : dto.getListe_parameters()) {
                ParametresReglesFraude param = new ParametresReglesFraude();
                param.setDateModif(new Timestamp(System.currentTimeMillis()));
                param.setRegle(existing);
                param.setVegal(p.getVegal());
                param.setVmax(p.getVmax());
                param.setVmin(p.getVmin());

                if (p.getFlow() != null) {
                    Flow paramFlow = flowRepository.findById(p.getFlow().getId())
                            .orElseThrow(() -> new RuntimeException("Flow not found"));
                    param.setFlow(paramFlow);
                }

                existing.getListe_parameters().add(param);
            }
        }

        // Update filters only if different
        if (!existing.getListe_filters().equals(dto.getListe_filters())) {
            existing.getListe_filters().clear();
            for (FilterDto f : dto.getListe_filters()) {
                FiltresReglesFraude filEntity = new FiltresReglesFraude();
                filEntity.setDateModif(new Timestamp(System.currentTimeMillis()));
                filEntity.setRegle(existing);
                filEntity.setInegal(f.getInegal());
                filEntity.setVdef(f.getVdef());
                filEntity.setVegal(f.getVegal());
                filEntity.setVlike(f.getVlike());
                filEntity.setVnotlike(f.getVnotlike());

                Integer filId = f.getFiltreFraude().getId();
                FiltresFraude fil = filtresFraudeRepository.findById(filId)
                        .orElseThrow(() -> new RuntimeException("FiltreFraude not found"));
                filEntity.setFiltreFraude(fil);

                existing.getListe_filters().add(filEntity);
            }
        }

        return reglesFraudeRepository.save(existing);
    }

    @Transactional
    public void deleteRule(Integer ruleId) {
        ReglesFraude rule = reglesFraudeRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found with id=" + ruleId));
        rule.getListe_parameters().clear();
        rule.getListe_filters().clear();
        reglesFraudeRepository.delete(rule);
    }

    @Transactional
    public void deleteRuleParameter(Integer ruleId, Long paramId) {
        ReglesFraude rule = reglesFraudeRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found with id=" + ruleId));

        rule.getListe_parameters().removeIf(param -> param.getId().equals(paramId));
        reglesFraudeRepository.save(rule);
    }


    @Transactional
    public void deleteRuleFilter(Integer ruleId, Integer filterId) {
        ReglesFraude rule = reglesFraudeRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found with id=" + ruleId));

        rule.getListe_filters().removeIf(filter -> filter.getId().equals(filterId));
        reglesFraudeRepository.save(rule);
    }

    @Transactional
    public FiltresReglesFraude editRuleFilter(FilterDto filterDto) {
        FiltresReglesFraude filter = filtreReglesFraudeRepository.findById(filterDto.getId())
                .orElseThrow(() -> new RuntimeException("FiltreFraude not found"));

        filter.setDateModif(new Timestamp(System.currentTimeMillis()));
        filter.setInegal(filterDto.getInegal());
        filter.setVdef(filterDto.getVdef());
        filter.setVegal(filterDto.getVegal());
        filter.setVlike(filterDto.getVlike());
        filter.setVnotlike(filterDto.getVnotlike());

        Integer filId = filterDto.getFiltreFraude().getId();
        FiltresFraude filtreFraude = filtresFraudeRepository.findById(filId)
                .orElseThrow(() -> new RuntimeException("Filter not found"));
        filter.setFiltreFraude(filtreFraude);

        return filter;
    }


    @Transactional
    public ParametresReglesFraude editRuleParameter(ParameterDto paramDto) {
        ParametresReglesFraude param = parametresReglesFraudeRepository.findById(paramDto.getId())
                .orElseThrow(() -> new RuntimeException("Parameter not found with id=" + paramDto.getId()));

        param.setDateModif(new Timestamp(System.currentTimeMillis()));
        param.setVegal(paramDto.getVegal());
        param.setVmax(paramDto.getVmax());
        param.setVmin(paramDto.getVmin());

        if (paramDto.getFlow() != null) {
            Flow paramFlow = flowRepository.findById(paramDto.getFlow().getId())
                    .orElseThrow(() -> new RuntimeException("Flow not found"));
            param.setFlow(paramFlow);
        }
        return param;
    }
}
