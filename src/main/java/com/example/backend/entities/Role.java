package com.example.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity(name = "role")
@Table(schema = "fraude_management",name = "role")
public class Role {
    @Id
    @Column(name = "r_id", unique = true)
    private Long id;

    @Column(name = "role", unique = true)
    private String role;

    @OneToMany(mappedBy="role")
    @JsonIgnore
    private List<User> users;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Role() {
    }
}
