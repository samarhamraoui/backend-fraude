package com.example.backend.services;

import com.example.backend.entities.Group;
import com.example.backend.entities.Module;

import java.util.List;


public interface GroupService {

    Group addGroup(Group group) ;
    Group editGroup(Group group,Long id);

    List<Group> getListGroup() ;
    void deleteGroup(Long gId) ;

    Group findById(Long gId);

    List<Group> findGroupByModule(Long Id);
    void removeModuleFromGroup(Long groupId, Long moduleId);



    List<Module> getModulesByGroup(Long groupId);


    void assignModuleToGroup(Long groupId, Long moduleId);

}
