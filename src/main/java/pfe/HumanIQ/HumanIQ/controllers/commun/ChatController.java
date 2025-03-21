package pfe.HumanIQ.HumanIQ.controllers.commun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pfe.HumanIQ.HumanIQ.DTO.request.MessageDto;
import pfe.HumanIQ.HumanIQ.models.Message;
import pfe.HumanIQ.HumanIQ.services.chat.ChatService;
import pfe.HumanIQ.HumanIQ.services.chat.MessageServiceImpl;

import java.util.List;
import java.util.Set;

@Controller
@RestController
public class ChatController {


        @Autowired
        private MessageServiceImpl messageService;


    @GetMapping("/messages/{senderId}/{receiverId}")
    public List<Message> getMessages(@PathVariable String senderId, @PathVariable Long receiverId) {
        return messageService.getMessages(senderId, receiverId);
    }
    @GetMapping("/messages/{roomId}")
    public List<Message> getMessagesbyroom(@PathVariable Long roomId) {
        return messageService.getMessagesbyroom(roomId);
    }
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public MessageDto sendMessage(@Payload MessageDto message) {
        messageService.saveMessage(message.getChatroomId(),message.getSenderId(), message.getReceiverId(), message.getContent());
        return message;
    }


}
