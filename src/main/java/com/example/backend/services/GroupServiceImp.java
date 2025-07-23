package com.example.backend.services;

import com.example.backend.dao.GroupRepository;
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
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class GroupServiceImp implements GroupService{
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    ModuleRepository moduleRepository;
    @Autowired
    SubModuleRepository subModuleRepository;
    @Override
    public Group addGroup(Group group) {
        System.out.println(group.getgName());

        List<SubModule> fun = new ArrayList<>();
        for (SubModule f:group.getListe_submodule()) {
            if (!fun.contains(f)){
                Optional<SubModule> ff = subModuleRepository.findById(f.getId());
                fun.add(ff.get());
            }
        }
        group.setListe_submodule(fun);


        List<Module> mod = new ArrayList<>();
        for (Module m:group.getModule_groups()) {
            Optional<Module> mm = moduleRepository.findById(m.getId());
            mod.add(mm.get());
        }
        group.setModule_groups(mod);





        return groupRepository.save(group);
    }

//        group.setDateCreation(new Date());
//
//        // Process modules and submodules
  //      for (Module module : group.getModule_groups()) {
//            // Add the group to the module's group list
    //       module.getGroup_module().add(group);

//            // Process submodules
      //      for (SubModule submodule : module.getList_sub_modules()) {
//                // Set the module for each submodule
        //       submodule.setModule(module);
//
//                // Process functions
//                List<Function> functions = new ArrayList<>();
//                for (Function function : submodule.getFunctions()) {
//                    // Check if the function is already present in the group's functions
//                    if (function.getId() != null && !functions.contains(function)) {
//                        functions.add(function);
//                    }
//                }
//                submodule.setFunctions(functions);
          // }
       // }


        // Save the group
       // return groupRepository.save(group);






    @Override
    public Group editGroup(Group group,Long id) {
        Group g=this.findById(id);
        if(g.getgId()==id){
            g.setgName(group.getgName());
            g.setgDescription(group.getgDescription());
            g.setDateModif(group.getDateModif());
            g.setEtat(group.getEtat());
            g.setModule_groups(group.getModule_groups());
            return groupRepository.save(g);
        }
        return null;
    }

    @Override
    public List<Group> getListGroup() {
        return groupRepository.findAll(Sort.by(Sort.Direction.DESC,"dateCreation"));
    }

    @Override
    public void deleteGroup(Long gId) {
        Group group = groupRepository.getOne(gId);
        if (group != null)
        {
            groupRepository.delete(group);
        }

    }

    @Override
    public Group findById(Long gId) {
        return groupRepository.findById(gId).get();
    }

    @Override
    public List<Group> findGroupByModule(Long Id){
        return  groupRepository.findAll().stream().filter
                (x->x.getModule_groups().stream().anyMatch(t->t.getId()==Id)).collect(Collectors.toList());
    }



    @Transactional
    public void removeModuleFromGroup(Long groupId, Long moduleId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        Module module = group.getModule_groups().stream()
                .filter(m -> m.getId().equals(moduleId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Module not found"));
        group.getModule_groups().remove(module);
        groupRepository.save(group);
    }



    public List<Module> getModulesByGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
        return group.getModule_groups();
    }




    @Transactional
    public void assignModuleToGroup(Long groupId, Long moduleId) {
        // Retrieve the group entity from the database
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException("Group not found"));

        // Retrieve the module entity from the database
        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new EntityNotFoundException("Module not found"));

        // Add the module to the group's list of modules
        group.getModule_groups().add(module);
        groupRepository.save(group);

    }









}
