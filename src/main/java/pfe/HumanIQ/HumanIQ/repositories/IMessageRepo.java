package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pfe.HumanIQ.HumanIQ.models.ChatRoom;
import pfe.HumanIQ.HumanIQ.models.Message;

import java.util.List;

@Repository
public interface IMessageRepo extends JpaRepository<Message,Integer> {
    List<Message> findBySenderIdAndReceiverIdOrderBySentAtAsc(Long senderId, Long receiverId);


    List<Message> findMessageByChatRoom(ChatRoom chatRoom);
    List<Message> findByChatRoom_IdOrderBySentAtAsc(Long chatRoomId);

}
