package pfe.HumanIQ.HumanIQ.services.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.Message;
import pfe.HumanIQ.HumanIQ.repositories.IMessageRepo;

import java.util.List;

@Service
public class ChatService {
    @Autowired
    private IMessageRepo messageRepository;

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }
}
