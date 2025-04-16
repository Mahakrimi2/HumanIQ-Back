package pfe.HumanIQ.HumanIQ.DTO.request;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;

public class MessageDto {
    private String senderId;
    private Long receiverId;
    private Long chatroomId;
    @Lob
    @Column(columnDefinition = "text")
    private String content;
    public Long getChatroomId() {
        return chatroomId;
    }

    public MessageDto setChatroomId(Long chatroomId) {
        this.chatroomId = chatroomId;
        return this;
    }

    public String getSenderId() {
        return senderId;
    }

    public MessageDto setSenderId(String senderId) {
        this.senderId = senderId;
        return this;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public MessageDto setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public MessageDto setContent(String content) {
        this.content = content;
        return this;
    }
}
