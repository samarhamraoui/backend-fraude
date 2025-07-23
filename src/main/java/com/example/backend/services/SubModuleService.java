package com.example.backend.services;

import com.example.backend.entities.Module;
import com.example.backend.entities.SubModule;

import java.util.List;

public interface SubModuleService {

    SubModule addSubModule(SubModule subModule) ;
    List<SubModule> getListSubModule() ;
    void deleteSubModule(Long Id) ;

    SubModule findById(Long Id);
    SubModule editSubModule(Long Id,SubModule subModule);
}
