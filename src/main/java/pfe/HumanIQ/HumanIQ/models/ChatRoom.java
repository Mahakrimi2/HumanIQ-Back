package pfe.HumanIQ.HumanIQ.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "chatroom_user",
            joinColumns = @JoinColumn(name = "chatroom_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();
    @ManyToOne
    @JsonIgnore
    private User owner ;

    public User getOwner() {
        return owner;
    }

    public ChatRoom setOwner(User owner) {
        this.owner = owner;
        return this;
    }

    @OneToMany(mappedBy = "chatRoom",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Message> messages = new HashSet<>();

    public Long getId() {
        return id;
    }

    public ChatRoom setId(Long id) {
        this.id = id;
        return this;
    }

    public Set<User> getUsers() {
        return users;
    }

    public ChatRoom setUsers(Set<User> users) {
        this.users = users;
        return this;
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public ChatRoom setMessages(Set<Message> messages) {
        this.messages = messages;
        return this;
    }


}
