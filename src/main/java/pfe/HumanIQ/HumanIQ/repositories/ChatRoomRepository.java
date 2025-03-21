package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pfe.HumanIQ.HumanIQ.models.ChatRoom;
import pfe.HumanIQ.HumanIQ.models.User;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
    @Query("SELECT c FROM ChatRoom c WHERE :user1 MEMBER OF c.users AND :user2 MEMBER OF c.users")
    ChatRoom findByUsers(@Param("user1") User user1, @Param("user2") User user2);

    List<ChatRoom> findAllByUsers(User user);
}
