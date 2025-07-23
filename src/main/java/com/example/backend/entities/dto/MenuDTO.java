package com.example.backend.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuDTO {
    private Long id;
    private String route;
    private String name;
    private String type; // e.g., "link", "sub", ...
    private String icon;
    private String label; // or could be an object if needed
    private String badge;
    private List<MenuChildrenItemDTO> children = new ArrayList<>();
    private MenuPermissionsDTO permissions;
    private int order;
}
