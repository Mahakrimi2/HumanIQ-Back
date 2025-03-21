package pfe.HumanIQ.HumanIQ.services.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.ChatRoom;
import pfe.HumanIQ.HumanIQ.models.Message;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.ChatRoomRepository;
import pfe.HumanIQ.HumanIQ.repositories.IMessageRepo;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private IMessageRepo messageRepository;

    @Autowired
    private UserRepo userRepository; // Assurez-vous d'avoir un repository pour User

    public ChatRoom createChatRoom(String user1Id, Long user2Id) {
        User user1 = userRepository.findByUsername(user1Id).orElseThrow(() -> new RuntimeException("User not found"));
        User user2 = userRepository.findById(user2Id).orElseThrow(() -> new RuntimeException("User not found"));

        // Vérifier si la chatRoom existe déjà
        ChatRoom existingChatRoom = chatRoomRepository.findByUsers(user1, user2);
        if (existingChatRoom != null) {
            return existingChatRoom; // Retourner la chatRoom existante
        }

        // Sinon, créer une nouvelle chatRoom
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.getUsers().add(user1);
        chatRoom.getUsers().add(user2);
        return chatRoomRepository.save(chatRoom);
    }

    public Message sendMessage(Long chatRoomId, String senderId, String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new RuntimeException("ChatRoom not found"));
        User sender = userRepository.findByUsername(senderId).orElseThrow(() -> new RuntimeException("Sender not found"));
        Message message = new Message();
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        message.setContent(content);
        return messageRepository.save(message);
    }

    public List<Message> getMessages(Long chatRoomId) {
        ChatRoom chatRoom =chatRoomRepository.findById(chatRoomId).get();
        return messageRepository.findMessageByChatRoom(chatRoom);
    }


    public List<ChatRoom> getAllChatroom() {
        return chatRoomRepository.findAll();
    }

    public void deleteChatRoom(Long chatRoomId) {
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(chatRoomId);

        if (chatRoomOptional.isPresent()) {
            ChatRoom chatRoom = chatRoomOptional.get();

            // Vider la liste des utilisateurs (si nécessaire)
            chatRoom.getUsers().clear();

            // Supprimer la ChatRoom (CascadeType.ALL supprimera les messages)
            chatRoomRepository.delete(chatRoom);
        } else {
            throw new RuntimeException("ChatRoom non trouvée !");
        }
    }

    public ChatRoom addUserToChatroom(String username, Long chatroomId) {
        User user=userRepository.findByUsername(username).get();
        ChatRoom chatroom=chatRoomRepository.findById(chatroomId).get();
        if(user.getChatRooms().contains(chatroom)){
            throw new RuntimeException("user already exists in the chatroom");
        }
        user.getChatRooms().add(chatroom);
        chatroom.getUsers().add(user);
        userRepository.save(user);
        chatRoomRepository.save(chatroom);
        return chatroom;
    }


    public ResponseEntity removeUserFromChatroom(String username, Long chatroomId) {
        ChatRoom chatroom=chatRoomRepository.findById(chatroomId).get();
        User user=userRepository.findByUsername(username).get();
        if(!(chatroom.getUsers().contains(user))){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();}
        chatroom.getUsers().remove(user);
        user.getChatRooms().remove(chatroom);
        chatRoomRepository.save(chatroom);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }


    public List<ChatRoom> getUserChatrooms(String  userId)
    {
        User user = userRepository.findByUsername(userId).orElse(null);
        return chatRoomRepository.findAllByUsers(user);
    }

    public Set<User> getAllUsersByChatroom(Long chatroomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatroomId).orElse(null);
        return chatRoom.getUsers();

    }
}
