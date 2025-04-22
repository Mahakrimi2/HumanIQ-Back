package pfe.HumanIQ.HumanIQ.services.serviceUser;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import pfe.HumanIQ.HumanIQ.DTO.request.ChangePasswordRequest;
import pfe.HumanIQ.HumanIQ.models.*;
import pfe.HumanIQ.HumanIQ.repositories.*;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImp implements UserService, UserDetailsService {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepo tokenRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;

    public UserServiceImp(UserRepo userRepository, PasswordEncoder passwordEncoder, TokenRepo tokenRepository, RoleRepository roleRepository, DepartmentRepository departmentRepository, ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    @Override
    public List<User> getAllUsersemp() {
        Role role = roleRepository.findByName(UserRole.ROLE_EMPLOYEE);
        return userRepository.findByRoles(role);
    }

    @Override
    public User createEmployee(User user,Long id) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("username already exists");
        }
        Department department = departmentRepository.findById(id).orElse(null);
        if (department == null) {
            return null;
        }
        System.out.println(id);
        Role role = roleRepository.findByName(UserRole.ROLE_EMPLOYEE);
        ////user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(role);
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println(role);
        newUser.setRoles(userRoles);
        newUser.setFullname(user.getFullname());
        newUser.setAddress(user.getAddress());
        newUser.setNationalID(user.getNationalID());
        newUser.setHireDate(user.getHireDate());
        newUser.setGender(user.getGender());
        newUser.setPosition(user.getPosition());
        newUser.setTelNumber(user.getTelNumber());
        newUser.setDepartment(department);
        return userRepository.save(newUser);
    }
    @Override
    public User createUser(User user,Long id) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("username already exists");
        }

        Department department = departmentRepository.findById(id).orElse(null);
        if (department == null) {
            return null;
        }

        Set<Role> userRoles = new HashSet<>();
        System.out.println(user.getRoles());
        for (Role roleRequest : user.getRoles()) {
            Role role = roleRepository.findByName(roleRequest.getName());
            System.out.println(role.getName());
            if (role != null) {
                System.out.println(role.getName());
                userRoles.add(role);
            } else {

                throw new RuntimeException("Role not found: " + roleRequest.getName());
            }
        }


        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));

        newUser.setRoles(userRoles);
        newUser.setFullname(user.getFullname());
        newUser.setAddress(user.getAddress());
        newUser.setNationalID(user.getNationalID());
        newUser.setHireDate(user.getHireDate());
        newUser.setGender(user.getGender());
        newUser.setPosition(user.getPosition());
        newUser.setTelNumber(user.getTelNumber());
        newUser.setDepartment(department);
        return userRepository.save(newUser);
    }




    @Override
    public User updateUser(User user,Long id) {
        User existingUser = findUserById(id);
        if(existingUser != null){
            Optional<User> userWithEmail = userRepository.findByUsername(user.getUsername());
            existingUser.setUsername(user.getUsername());
            // Mise à jour des rôles

            Set<Role> userRoles = new HashSet<>();
            for (Role roleRequest : user.getRoles()) {
                System.out.println(user.getRoles().size());
                Role role = roleRepository.findByName(roleRequest.getName());
                System.out.println(role);
                if (role != null) {
                    userRoles.add(role);
                } else {
                    throw new RuntimeException("Role not found: " + roleRequest.getName());
                }
            }

            existingUser.setRoles(userRoles);
            existingUser.setFullname(user.getFullname());
            existingUser.setAddress(user.getAddress());
            existingUser.setPosition(user.getPosition() );
            existingUser.setTelNumber(user.getTelNumber());
            return userRepository.save(existingUser);
        } else {
            throw new RuntimeException("User does not exist");
        }
    }

    public Role createRoleIfNotExist(UserRole roleName) {
        Role existingRole = roleRepository.findByName(roleName);
        if (existingRole == null) {
            Role newRole = new Role();
            newRole.setName(roleName);
            return roleRepository.save(newRole);
        }
        return existingRole;
    }


    @Override
    public void deleteUser(Long id) {
       User user = userRepository.findById(id).orElse(null);
       if (user == null) {
           throw new RuntimeException("user does not exist");
       }
        List<Project> projects = projectRepository.findByProjectManagerId(id);
        for (Project p : projects) {
            p.setProjectManager(null);
        }
        projectRepository.saveAll(projects);
        userRepository.delete(user);

    }

    public void enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsDisabled(false);
        userRepository.save(user);
    }

    public void disactivateUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new RuntimeException("user does not exist");
        }
        user.setIsDisabled(true);
        userRepository.save(user);

    }




    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }


    public Token createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = createdAt.plusHours(24); // Token valid for 24 hours

        Token verificationToken = new Token();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setCreatedAt(createdAt);
        verificationToken.setExpiresAt(expiresAt);
        verificationToken.setValidatedAt(null); // Token is not validated yet

        tokenRepository.save(verificationToken);
        return verificationToken;
    }
    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    public void changePassword(ChangePasswordRequest request) {
        User currentUser = getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());


        currentUser.setPassword(hashedNewPassword);
        userRepository.save(currentUser);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids);
    }
    public List<User> getAllUsersWithRoles() {
        return userRepository.findAllWithRoles();
    }


    public Map<String, Long> getCountOfUsersByRole() {
        List<Object[]> results = userRepository.countUsersByRole();
        Map<String, Long> countByRole = new HashMap<>();

        for (Object[] result : results) {
            UserRole role = (UserRole) result[0]; // Récupère l'énumération UserRole
            String roleName = role.name(); // Convertit l'énumération en chaîne de caractères
            Long count = (Long) result[1];
            countByRole.put(roleName, count);
        }

        return countByRole;
    }
}


