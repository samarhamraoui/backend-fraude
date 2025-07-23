package com.example.backend.Controllers;

import com.example.backend.entities.Group;
import com.example.backend.entities.Module;
import com.example.backend.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @RequestMapping(value="/add",method= RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Group> addGroup(@RequestBody Group group) {
            groupService.addGroup(group);
            return new ResponseEntity<>(group,HttpStatus.OK);
    }

    @RequestMapping(value="/edit/{id}",method= RequestMethod.PUT,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> editGroup(@RequestBody Group group,@PathVariable Long id) {
        if(group==null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else{
            groupService.editGroup(group,id);
            return new ResponseEntity<>(group,HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/list")
    public List<Group> getGroup(){
        return groupService.getListGroup();
    }

    @RequestMapping(value = "/delete/{id}",method = RequestMethod.DELETE)
    public void delete(@PathVariable Long id){
        groupService.deleteGroup(id);

    }

    @RequestMapping(value = "/{gId}",method=RequestMethod.GET)
    public Group findById(@PathVariable Long gId){
        return groupService.findById(gId);
    }


    @RequestMapping(value = "/module/{Id}",method=RequestMethod.GET)
    public List<Group> findGroupByModule(@PathVariable Long Id){
        return groupService.findGroupByModule(Id);
    }


    @DeleteMapping("/{groupId}/modules/{moduleId}")
    public ResponseEntity<Void> removeModuleFromGroup(@PathVariable Long groupId, @PathVariable Long moduleId) {
        groupService.removeModuleFromGroup(groupId, moduleId);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/{groupId}/modules")
    public List<Module> getModulesByGroup(@PathVariable Long groupId) {
        return groupService.getModulesByGroup(groupId);
    }


    @PostMapping("/{groupId}/modules/{moduleId}")
    public ResponseEntity<String> assignModuleToGroup(@PathVariable Long groupId, @PathVariable Long moduleId) {
        try {
            groupService.assignModuleToGroup(groupId, moduleId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while assigning the module to the group");
        }
    }
















}
