package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pfe.HumanIQ.HumanIQ.models.Token;
import pfe.HumanIQ.HumanIQ.models.User;

import java.util.Optional;

@Repository
public interface TokenRepo extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);
    void deleteByUser(User user);
}
