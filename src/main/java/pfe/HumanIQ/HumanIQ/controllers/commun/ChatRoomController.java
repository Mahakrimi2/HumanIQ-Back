package pfe.HumanIQ.HumanIQ.controllers.commun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.models.ChatRoom;
import pfe.HumanIQ.HumanIQ.models.Message;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;
import pfe.HumanIQ.HumanIQ.services.chat.ChatRoomService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/chatroom")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @PostMapping("/create")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestParam String user1Id, @RequestParam Long user2Id) {
        try {
            ChatRoom chatRoom = chatRoomService.createChatRoom(user1Id, user2Id);
            return ResponseEntity.ok(chatRoom);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{chatRoomId}/messages")
    public ResponseEntity<Message> sendMessage(@PathVariable Long chatRoomId,
                                               @RequestParam String senderId,
                                               @RequestParam String content) {
        try {
            Message message = chatRoomService.sendMessage(chatRoomId, senderId, content);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long chatRoomId) {
        try {
            List<Message> messages = chatRoomService.getMessages(chatRoomId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    @GetMapping("/{userid}/rooms")
    public ResponseEntity<List<ChatRoom>> getUserChatrooms(@PathVariable String userid) {
        try {
            List<ChatRoom> messages = chatRoomService.getUserChatrooms(userid);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    @DeleteMapping("/{chatroomid}/delete")
    public ResponseEntity<?> deletechatroom(@PathVariable Long chatroomid) {
        try {
             chatRoomService.deleteChatRoom(chatroomid);
            return ResponseEntity.status(HttpStatus.OK).body("deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    @GetMapping("/{userid}/getAllUsersByChatroom")
    public ResponseEntity<Set<User>> getAllUsersByChatroom(@PathVariable Long userid) {
        try {
            Set<User> users = chatRoomService.getAllUsersByChatroom(userid);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
