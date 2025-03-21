package pfe.HumanIQ.HumanIQ.services.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.ChatRoom;
import pfe.HumanIQ.HumanIQ.models.Message;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.ChatRoomRepository;
import pfe.HumanIQ.HumanIQ.repositories.IMessageRepo;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.util.List;

@Service
public class MessageServiceImpl  {

    @Autowired
    private IMessageRepo iMessageRepo;

    @Autowired
    private UserRepo iUserRepo;

    @Autowired
    private ChatRoomRepository chatRoomRepository;



    // Récupérer les messages entre deux utilisateurs
    public List<Message> getMessages(String senderId, Long receiverId) {
        User sender= iUserRepo.findByUsername(senderId).orElse(null);

        return iMessageRepo.findBySenderIdAndReceiverIdOrderBySentAtAsc(sender.getId(), receiverId);
    }
    public List<Message> getMessagesbyroom(Long chatRoomId) {
        ChatRoom chatRoom =chatRoomRepository.findById(chatRoomId).get();
        return iMessageRepo.findByChatRoom_IdOrderBySentAtAsc(chatRoomId);
    }


    public Message saveMessage(Long Chatroomid ,String senderId, Long receiverId, String content) {
        Message message = new Message();
        ChatRoom chatRoom= chatRoomRepository.findById(Chatroomid).orElse(null);
        User sender= iUserRepo.findByUsername(senderId).orElse(null);
        User receiver= iUserRepo.findById(receiverId).orElse(null);
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        return iMessageRepo.save(message);
    }
}
