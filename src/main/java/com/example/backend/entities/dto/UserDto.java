package com.example.backend.entities.dto;

import com.example.backend.entities.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "DTO representing user data.")
public class UserDto {
    @Schema(description = "The unique ID of the user.")
    private Long id;
    @Schema(description = "The username of the user.", example = "john.doe")
    private String username;
    @Schema(description = "The email of the user.", example = "john@example.com")
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "The raw password (write-only). Will be hashed before storage.")
    private String password;
    @NotNull
    @Schema(description = "The role assigned to this user. Must include a valid role ID.")
    private Role role;
    @NotNull
    @Schema(description = "The group ID the user belongs to. Must not be null.")
    private Long groupId;
    @Schema(description = "Optional list of permissions (derived from role).")
    private List<String> permissions;
    private String groupeName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creationDate;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    public UserDto(Long id, String username, String email, String password, Role role, Long groupId, List<String> permissions) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.groupId = groupId;
        this.permissions = permissions;
    }
}


