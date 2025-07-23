package com.example.backend.services;

import com.example.backend.conf.JwtTokenUtil;
import com.example.backend.dao.ResetPasswordTokenRepository;
import com.example.backend.dao.UserRepository;
import com.example.backend.entities.*;
import com.example.backend.entities.Module;
import com.example.backend.entities.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ResetPasswordTokenRepository tokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private EmailService emailService;

    public LoginResponse doLogin(@RequestBody LoginRequest loginRequest) throws Exception{
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Optional<User> userOptional = userService.findByUsername(loginRequest.getUsername());

        if(!userOptional.isPresent()) {
            return null;
        }
        User user = userOptional.get();
        if(!user.isEnabled()) {
            throw new Exception("Your account has been locked. Please contact your administrator!");
        }
        List<String> roles = new ArrayList<>();
        roles.add(user.getRole().getRole());

        UserDto userDto = new UserDto(user.getId(), user.getUsername(),user.getEmail(),user.getPassword(),user.getRole(),user.getUser_group().getgId(),roles);

        String token = jwtTokenUtil.generateToken(user);
        return new LoginResponse(user.getUsername(), userDto, token, roles);
    }

    public User getUserData(String token){
        final Optional<User> user = userService.findByUsername(jwtTokenUtil.getUsernameFromToken(token));
        if (token != null) {
            List<Module> sortedModules = cloneAndSortModules(user.get().getUser_group().getModule_groups());
            User sortedUser = new User();
            sortedUser.setId(user.get().getId());
            sortedUser.setUsername(user.get().getUsername());
            sortedUser.setEmail(user.get().getEmail());
            sortedUser.setRole(user.get().getRole());
            sortedUser.setUser_group(user.get().getUser_group());
            sortedUser.getUser_group().setModule_groups(sortedModules);
            return sortedUser;
        }
        return null;
    }

    public List<ModuleDTO> getUserMenus(String token) {
        final Optional<User> user = userService.findByUsername(jwtTokenUtil.getUsernameFromToken(token));
        List<ModuleDTO> result = new ArrayList<>();
        if (token != null && user.isPresent()) {
            Group grp = user.get().getUser_group();
            // Group submodules by module
            Map<Module, List<SubModule>> submodulesByModule = grp.getListe_submodule()
                    .stream()
                    .collect(Collectors.groupingBy(SubModule::getModule));

            for (Map.Entry<Module, List<SubModule>> moduleEntry : submodulesByModule.entrySet()) {
                Module module = moduleEntry.getKey();

                ModuleDTO moduleDTO = new ModuleDTO();
                moduleDTO.setId(module.getId());
                moduleDTO.setModuleName(module.getModuleName());

                List<SubModuleDTO> subModuleDTOs = moduleEntry.getValue().stream()
                        .map(subModule -> {
                            SubModuleDTO subModuleDTO = new SubModuleDTO();
                            subModuleDTO.setId(subModule.getId());
                            subModuleDTO.setSubModuleName(subModule.getSubModuleName());
                            return subModuleDTO;
                        })
                        .collect(Collectors.toList());

                // Sort submodules inside each module
                subModuleDTOs.sort(Comparator.comparing(SubModuleDTO::getId));

                moduleDTO.setListSubModule(subModuleDTOs);

                result.add(moduleDTO);
            }
            result.sort(Comparator.comparing(ModuleDTO::getId));

        }
        return result;
    }

    public List<MenuDTO> getUserMenusV2(String token) {
        if (token == null || token.isEmpty()) {
            return Collections.emptyList();
        }

        String username = jwtTokenUtil.getUsernameFromToken(token);
        Optional<User> optUser = userService.findByUsername(username);
        if (!optUser.isPresent()) {
            return Collections.emptyList();
        }

        User user = optUser.get();
        String userRole = user.getRole().getRole().toUpperCase();

        Group grp = user.getUser_group();
        if (grp == null) {
            return Collections.emptyList();
        }

        // Group submodules by their module
        Map<Module, List<SubModule>> submodulesByModule = grp.getListe_submodule().stream()
                .collect(Collectors.groupingBy(SubModule::getModule));

        List<MenuDTO> menuList = new ArrayList<>();

        for (Map.Entry<Module, List<SubModule>> entry : submodulesByModule.entrySet()) {
            Module module = entry.getKey();
            List<SubModule> subModules = entry.getValue();

            MenuDTO topMenu = new MenuDTO();
            topMenu.setId(module.getId());
            topMenu.setName(module.getModuleName());
            topMenu.setRoute(module.getModuleName());
            topMenu.setType("sub");
            topMenu.setIcon(module.getIcon());
            topMenu.setOrder(module.getOrder());
            List<MenuChildrenItemDTO> childList = new ArrayList<>();

            if ("ADMIN".equalsIgnoreCase(userRole)) {
                MenuPermissionsDTO modulePerms = new MenuPermissionsDTO();
                modulePerms.setOnly(Collections.singletonList("ADMIN"));
                topMenu.setPermissions(modulePerms);

                for (SubModule subModule : subModules) {
                    MenuChildrenItemDTO child = new MenuChildrenItemDTO();
                    child.setId(subModule.getId());
                    child.setName(subModule.getSubModuleName());
                    child.setRoute(subModule.getSubModuleName());
                    child.setIcon(subModule.getIcon());
                    child.setOrder(subModule.getOrder());

                    MenuPermissionsDTO childPerms = new MenuPermissionsDTO();
                    childPerms.setOnly(Collections.singletonList("ADMIN"));
                    child.setPermissions(childPerms);

                    // 1) Check if there are any reports for this SubModule
                    List<SubModuleReport> subModuleReports = subModule.getSubModuleReports();
                    if (subModuleReports != null && !subModuleReports.isEmpty()) {
                        child.setType("link");
                        // 2) Transform SubModuleReport -> ReportDTO
                        List<ReportDTO> reportDTOs = subModuleReports.stream()
                                .map(smr -> {
                                    RepRapports rap = smr.getRepRapports();
                                    ReportDTO dto = new ReportDTO();
                                    dto.setId(rap.getId());
                                    dto.setTitle(rap.getTitle());
                                    dto.setName(rap.getName());
                                    return dto;
                                })
                                .collect(Collectors.toList());
                        // 3) Attach to the child
                        child.setReports(reportDTOs);
                    } else {
                        // Fallback: no reports => "link"
                        child.setType("link");
                    }

                    childList.add(child);
                }
            } else {
                if (!"GUEST".equalsIgnoreCase(module.getPermission())) {
                    continue;
                }

                MenuPermissionsDTO modulePerms = new MenuPermissionsDTO();
                modulePerms.setOnly(Collections.singletonList("GUEST"));
                topMenu.setPermissions(modulePerms);

                for (SubModule subModule : subModules) {
                    if (!"GUEST".equalsIgnoreCase(subModule.getPermission())) {
                        continue;
                    }
                    MenuChildrenItemDTO child = new MenuChildrenItemDTO();
                    child.setId(subModule.getId());
                    child.setName(subModule.getSubModuleName());
                    child.setRoute(subModule.getSubModuleName());
                    child.setIcon(subModule.getIcon());
                    child.setOrder(subModule.getOrder());

                    MenuPermissionsDTO childPerms = new MenuPermissionsDTO();
                    childPerms.setOnly(Collections.singletonList("GUEST"));
                    child.setPermissions(childPerms);

                    // Same check for reports
                    List<SubModuleReport> subModuleReports = subModule.getSubModuleReports();
                    if (subModuleReports != null && !subModuleReports.isEmpty()) {
                        child.setType("link");
                        List<ReportDTO> reportDTOs = subModuleReports.stream()
                                .map(smr -> {
                                    RepRapports rap = smr.getRepRapports();
                                    ReportDTO dto = new ReportDTO();
                                    dto.setId(rap.getId());
                                    dto.setTitle(rap.getTitle());
                                    dto.setName(rap.getName());
                                    return dto;
                                })
                                .collect(Collectors.toList());
                        child.setReports(reportDTOs);
                    } else {
                        child.setType("link");
                    }

                    childList.add(child);
                }
                if (childList.isEmpty()) {
                    continue;
                }
            }

            childList.sort(Comparator.comparingInt(MenuChildrenItemDTO::getOrder));
            topMenu.setChildren(childList);
            menuList.add(topMenu);
        }
        menuList.sort(Comparator.comparingInt(MenuDTO::getOrder));
        return menuList;
    }

    private List<Module> cloneAndSortModules(List<Module> modules) {
        List<Module> sortedModules = new ArrayList<>(modules);
        sortedModules.sort(Comparator.comparingLong(Module::getId));
        sortedModules.forEach(this::sortModule);
        return sortedModules;
    }

    private void sortModule(Module module) {
        module.getList_sub_modules().sort(Comparator.comparingLong(SubModule::getId));
    }

    public boolean checkToken(String token) {
        if (token != null) {
            final String username = jwtTokenUtil.getUsernameFromToken(token);
            final Optional<User> user = userService.findByUsername(username);
            if (username.equals(user.get().getUsername()) && !jwtTokenUtil.isTokenExpired(token)) {
                return true;

            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void forgotPassword(String userIdentifier) {
        User user = userRepository.findUserByUsernameOrEmail(userIdentifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
        ResetPasswordToken resetToken = new ResetPasswordToken(
                tokenValue,
                expiry,
                user
        );
        tokenRepository.save(resetToken);
        try{
            emailService.sendResetLinkToUser(user, tokenValue);
        }catch(Exception e){
            throw new RuntimeException("Error sending email to user");
        }
    }

    public void resetPassword(String tokenValue, String newPlainPassword) {
        ResetPasswordToken resetToken = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Token already used");
        }
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPlainPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}

