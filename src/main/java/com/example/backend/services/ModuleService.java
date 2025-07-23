package com.example.backend.services;

import com.example.backend.entities.Module;
import com.example.backend.entities.SubModule;

import java.util.List;


public interface ModuleService {

    Module addModule(Module module) ;
    Module editModule(Module module,Long id);
    List<Module> getListModule() ;

    Module findById(Long Id);

    List<Module> findModuleByGroup(Long Id);

    void deleteModule(Long Id) ;

    List<SubModule> getSubmodulesForModule(Long moduleId);
}
