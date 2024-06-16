package com.example.shopgpt;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class EndToEndTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void greetingShouldReturnDefaultMessage() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("Welcome to ShopGpt")));
    }

    @Test
    void registerUser() throws Exception {
        var user = new User();
        String CLIENT_NAME = "hassan";
        String CLIENT_PASSWORD = "hassanpass";

        user.setEmail(CLIENT_NAME);
        user.setPassword(CLIENT_PASSWORD);
        user.setFirstName("hassan1");
        user.setLastName("hassan-1");

//		mockMvc.perform(post("/process_register").with(csrf())
//						.contentType("application/x-www-form-urlencoded")
//				.content("email=hassan%40daf.cfd&password=bbbasdf&firstName=ttt&lastName=bbb")
//				).andExpect(status().isOk());

    }

    @Test
    @WithMockUser(username = "user1", password = "pwd", roles = "USER")
    void showUsers() throws Exception {
        mockMvc.perform(
                get("/users")
        ).andExpect(status().isOk());
    }

    @Test
    void showConversationButton() throws Exception{
        mockMvc.perform(
                get("/")
                ).andExpect(content().string(containsString("Conversation")));
    }

    @Test
    void showConversation() throws Exception {
        mockMvc.perform(
                get("/conversation")
        ).andExpect(status().isOk()).andExpect(content().string(containsString("Submit")));
    }

    @Test
    void whenSubmitShowConversationInPage() throws Exception {
        String inputMessage = "Hi";
        mockMvc.perform(
                post("/conversation")
                        .with(csrf()).param("text", inputMessage)
        ).andExpect(status().isOk()).andExpect(content().string(containsString(inputMessage)));
    }


}
