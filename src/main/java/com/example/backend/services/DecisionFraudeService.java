package com.example.backend.services;

import com.example.backend.conf.JwtTokenUtil;
import com.example.backend.dao.DecisionFraudeRepository;
import com.example.backend.entities.CategoriesFraude;
import com.example.backend.entities.DecisionFraude;
import com.example.backend.entities.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.List;

@Service
public class DecisionFraudeService {
    @Autowired
    private DecisionFraudeRepository decisionFraudeRepository;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public DecisionFraudeService(DecisionFraudeRepository decisionFraudeRepository) {
        this.decisionFraudeRepository = decisionFraudeRepository;
    }

    public List<DecisionFraude> getAllDecisions() {
        return decisionFraudeRepository.findAll();
    }

    public List<DecisionFraude> getDecisionsBetweenDates(Timestamp startDate, Timestamp endDate) {
        return decisionFraudeRepository.findAllByDateDecisionBetween(startDate, endDate);
    }

    public DecisionFraude updateDecision(Integer id, DecisionFraude decisionFraude, HttpServletRequest request) {
        DecisionFraude existing = decisionFraudeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DecisionFraude not found with id: " + id));

        String token = JwtTokenUtil.extractToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        String username = jwtTokenUtil.getUsernameFromToken(token);

        existing.setNomUtilisateur(username);
        existing.setDecision(decisionFraude.getDecision());
        existing.setDateModif(new Timestamp(System.currentTimeMillis()));
        return decisionFraudeRepository.save(existing);
    }
}
