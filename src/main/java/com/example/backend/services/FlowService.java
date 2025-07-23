package com.example.backend.services;

import com.example.backend.dao.FlowRepository;
import com.example.backend.entities.Flow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlowService {
    @Autowired
    private FlowRepository flowRepository;

    public List<Flow> getFlowsByStatusAndTypeId(Long status, List<Integer> flowTypeIds){
        return flowRepository.findFlowsByStatusAndTypeId(status,flowTypeIds);
    }

    public List<Flow> findFlowsByFlowTypeAndProc(Long idFlow){
        return flowRepository.findFlowsByFlowTypeAndProcInSubquery(idFlow);
    }
}
