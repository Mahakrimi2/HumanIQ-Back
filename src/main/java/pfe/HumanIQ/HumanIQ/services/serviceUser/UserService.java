package pfe.HumanIQ.HumanIQ.services.serviceUser;

import org.springframework.security.core.userdetails.UserDetailsService;
import pfe.HumanIQ.HumanIQ.models.User;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    List<User> getAllUsers();
    User createUser(User user);
    public User updateUser(User user,Long id);
    void deleteUser(Long id);
    public List<User> getAllUsersemp() ;

    User findUserById(Long id);

    Optional<User> findByUsername(String username);
}
