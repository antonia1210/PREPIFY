package com.anto.backend.config;

import com.anto.backend.model.Permission;
import com.anto.backend.model.Role;
import com.anto.backend.model.User;
import com.anto.backend.repository.PermissionRepository;
import com.anto.backend.repository.RoleRepository;
import com.anto.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public DataInitializer(RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        if (roleRepository.count() > 0) return;

        Permission createRecipe = permissionRepository.save(new Permission("CREATE_RECIPE"));
        Permission editOwnRecipe = permissionRepository.save(new Permission("EDIT_OWN_RECIPE"));
        Permission deleteOwnRecipe = permissionRepository.save(new Permission("DELETE_OWN_RECIPE"));
        Permission deleteAnyRecipe = permissionRepository.save(new Permission("DELETE_ANY_RECIPE"));
        Permission rateRecipe = permissionRepository.save(new Permission("RATE_RECIPE"));
        Permission viewStats = permissionRepository.save(new Permission("VIEW_STATS"));
        Permission manageUsers = permissionRepository.save(new Permission("MANAGE_USERS"));

        Role userRole = new Role("USER");
        userRole.setPermissions(List.of(createRecipe, editOwnRecipe, deleteOwnRecipe, rateRecipe));
        roleRepository.save(userRole);

        Role adminRole = new Role("ADMIN");
        adminRole.setPermissions(List.of(createRecipe, editOwnRecipe, deleteOwnRecipe,
                deleteAnyRecipe, rateRecipe, viewStats, manageUsers));
        roleRepository.save(adminRole);

        if (userRepository.count() == 0) {
            User alice = new User();
            alice.setName("Alice Smith");
            alice.setEmail("alice@gmail.com");
            alice.setUsername("alice");
            alice.setPassword("Password1!");
            alice.setPreferences("vegan");
            alice.setRoles(List.of(userRole));
            userRepository.save(alice);

            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@gmail.com");
            admin.setUsername("admin");
            admin.setPassword("Admin123!");
            admin.setPreferences("none");
            admin.setRoles(List.of(adminRole));
            userRepository.save(admin);
        }
    }
}