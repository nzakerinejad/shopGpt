package com.example.shopgpt;

import jakarta.persistence.*;
import org.hibernate.type.NumericBooleanConverter;


@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Column(nullable = false, unique = false, length = 1000)
    private String content;

    @Column(columnDefinition = "boolean default false")
    private Boolean isItFromAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getItFromAdmin() {
        return isItFromAdmin;
    }

    public void setItFromAdmin(Boolean itFromAdmin) {
        isItFromAdmin = itFromAdmin;
    }
}
