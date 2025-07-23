package com.example.backend.entities.dto;


import com.example.backend.entities.Module;

import java.util.List;

public class customLoginResp {
    private LoginUser user;
    private List<Module> modules;

    public customLoginResp() {
    }

    public LoginUser getUser() {
        return user;
    }

    public void setUser(LoginUser user) {
        this.user = user;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }
}
