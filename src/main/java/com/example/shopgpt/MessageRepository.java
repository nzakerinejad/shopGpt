package com.example.shopgpt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.conversation.conversationId = :conversationId")
    List<Message> findMessagesByConversationId(@Param("conversationId") Long conversationId);
}
