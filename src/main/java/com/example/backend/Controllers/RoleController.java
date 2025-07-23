package com.example.backend.Controllers;

import com.example.backend.dao.RoleRepository;
import com.example.backend.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository ;

    @RequestMapping(value = "/list")
    public List<Role> getRole(){
        return roleRepository.findAll();
    }
}
