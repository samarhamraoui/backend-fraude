package com.example.backend.services;

import com.example.backend.dao.ModuleRepository;
import com.example.backend.dao.SubModuleRepository;
import com.example.backend.entities.Group;
import com.example.backend.entities.Module;
import com.example.backend.entities.SubModule;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    ModuleRepository moduleRepository;
    @Autowired
    SubModuleRepository subModuleRepository;
    @Override
    public Module addModule(Module module) {
        List<SubModule> subModules = new ArrayList<>();
//        for (SubModule subModule : module.getList_sub_modules()) {
//            SubModule existingSubModule = subModuleRepository.getOne(subModule.getId());
//            existingSubModule.setModule(module);
//            subModules.add(existingSubModule);
//        }
        module.setList_sub_modules(subModules);
        return moduleRepository.save(module);
    }

    @Override
    public Module editModule(Module module, Long id) {
        System.out.println(module.getModuleName());
        Module ex = moduleRepository.getOne(id);
        if (ex != null) {
            ex.setModuleName(module.getModuleName());
            ex.setGroup_module(module.getGroup_module());
            List<SubModule> subModules = new ArrayList<>();
            if(module.getList_sub_modules()!=null){
                for (SubModule subModule : module.getList_sub_modules()) {
                    SubModule existingSubModule = subModuleRepository.getOne(subModule.getId());
                    existingSubModule.setModule(ex);
                    subModules.add(existingSubModule);
                }
                // Remove submodules that were previously selected but are now deselected
                List<SubModule> existingSubModules = ex.getList_sub_modules();
                for (SubModule existingSubModule : existingSubModules) {
                    if (!subModules.contains(existingSubModule)) {
                        existingSubModule.setModule(null);
                    }
                }
                ex.setList_sub_modules(subModules);
            }
        }
        return moduleRepository.saveAndFlush(ex);
    }



    @Override
    public List<Module> getListModule() {
        return moduleRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    public void deleteModule(Long Id) {
        Module module = moduleRepository.getOne(Id);
        for (Group g: module.getGroup_module()) {
            g.getModule_groups().remove(this);
        }
        if (module != null)
        {
            moduleRepository.delete(module);
        }

    }






    @Override
    public Module findById(Long Id) {
        return moduleRepository.getOne(Id);
    }

    @Override
    public List<Module> findModuleByGroup(Long Id) {
        return moduleRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream().filter
                (x -> x.getGroup_module().stream().anyMatch(t -> t.getgId() == Id)).collect(Collectors.toList());
    }

    public List<SubModule> getSubmodulesForModule(Long moduleId) {
        Module module = moduleRepository.findById(moduleId).get();
              //  .orElseThrow(() -> new NotFoundException("Module not found with id: " + moduleId));

        return module.getList_sub_modules();
    }
}


