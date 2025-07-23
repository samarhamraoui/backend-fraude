package com.example.backend.Controllers;

import com.example.backend.entities.Module;
import com.example.backend.entities.SubModule;
import com.example.backend.services.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/module")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @RequestMapping(value="/add",method= RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addModule(@RequestBody Module module) {
        moduleService.addModule(module);
    }

    @RequestMapping(value="/edit/{id}",method= RequestMethod.PUT,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> editModule(@RequestBody Module module, @PathVariable Long id) {
        System.out.println(module.getModuleName());
        if(module==null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else{
            moduleService.editModule(module,id);
            return new ResponseEntity<>(module,HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/list")
    public List<Module> getModule(){
        return moduleService.getListModule();
    }

    @RequestMapping(value = "/delete/{id}",method = RequestMethod.DELETE)
    public void delete(@PathVariable Long id){
        moduleService.deleteModule(id);
    }

    @RequestMapping(value = "/{id}",method=RequestMethod.GET)
    public Module findById(@PathVariable Long id){
        return moduleService.findById(id);
    }

    @RequestMapping(value = "/group/{Id}",method=RequestMethod.GET)
    public List<Module> findModuleByGroup(@PathVariable Long Id){
        return moduleService.findModuleByGroup(Id);
    }
    @GetMapping("/{moduleId}/submodules")
    public List<SubModule> getSubmodulesForModule(@PathVariable Long moduleId) {
        return moduleService.getSubmodulesForModule(moduleId);
    }
}
