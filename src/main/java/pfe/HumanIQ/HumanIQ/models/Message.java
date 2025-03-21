package pfe.HumanIQ.HumanIQ.models;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Lob
    @Column(columnDefinition = "text")
    private String content; // Contenu du message
    private LocalDateTime sentAt;
    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
    @ManyToOne
    @JoinColumn(name = "receiver_Id")
    private User receiver;

    public User getSender() {
        return sender;
    }

    public Message setSender(User sender) {
        this.sender = sender;
        return this;
    }

    public User getReceiver() {
        return receiver;
    }

    public Message setReceiver(User receiver) {
        this.receiver = receiver;
        return this;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public Message setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        return this;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public Message setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
        return this;
    }

    @PrePersist
    public void setCreationDateTime() {
        this.sentAt = LocalDateTime.now();
    }
    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }




    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
