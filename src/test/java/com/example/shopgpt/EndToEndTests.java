package com.example.shopgpt;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class EndToEndTests {

    @Autowired
    private MockMvc mockMvc;

    static boolean userHasBeenCreated = false;

    @BeforeEach
    void setup() throws Exception {
        if (!userHasBeenCreated){
            mockMvc.perform(post("/process_register").with(csrf())
                    .contentType("application/x-www-form-urlencoded")
                    .content("email=user%40hassan.com&password=psw&firstName=hassanFirstName&lastName=bbb")
            );
            mockMvc.perform(post("/process_register").with(csrf())
                    .contentType("application/x-www-form-urlencoded")
                    .content("email=ali%40hassan.com&password=psw&firstName=AliFirstName&lastName=bbb")
            );
            userHasBeenCreated = true;
        }
    }

    @Test
    void greetingShouldReturnDefaultMessage() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("Welcome to ShopGpt")));
    }

    @Test
    void registerUser() throws Exception {

		mockMvc.perform(post("/process_register").with(csrf())
						.contentType("application/x-www-form-urlencoded")
				.content("email=hassan%40daf.cfd&password=bbbasdf&firstName=hassanFirstName&lastName=bbb")
				).andExpect(status().isOk());

        mockMvc.perform(get("/users").with(user("admin").roles("ADMIN").password("pass"))).andExpect(status().isOk()).andExpect(content().string(containsString("hassanFirstName")));
    }

    @Test
    @WithMockUser(username = "user@hassan.com", password = "pwd", roles = "USER")
    void doNotShowUsersToNotAdmin() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }
    @Test
    @WithMockUser(username = "admin", password = "pass", roles = "ADMIN")
    void showUsersOnlyToAdmin() throws Exception {
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
    @WithMockUser(username = "user@hassan.com", password = "pwd", roles = "USER")
    void showConversation() throws Exception {
        mockMvc.perform(
                get("/conversation")
        ).andExpect(status().isOk()).andExpect(content().string(containsString("Submit")));
    }

    @Test
    @WithMockUser(username = "user@hassan.com", password = "pwd", roles = "USER")
    void whenSubmitShowConversationInPage() throws Exception {
        String inputMessage = "Hi";

        mockMvc.perform(
                post("/conversation")
                        .with(csrf()).param("content", inputMessage)
        ).andExpect(status().isOk()).andExpect(content().string(containsString(inputMessage)));
    }

    @Test
    @WithMockUser(username = "user@hassan.com", password = "pwd", roles = "USER")
    void whenSubmitTwoMessagesShowBoth() throws Exception {
        String inputMessage1 = "Hi1";
        String inputMessage2 = "Hi2";
        mockMvc.perform(
                post("/conversation")
                        .with(csrf()).param("content", inputMessage1)
        );
        mockMvc.perform(
                post("/conversation")
                        .with(csrf()).param("content", inputMessage2)
        ).andExpect(status().isOk()).andExpect(content().string(containsString(inputMessage1))).andExpect(content().string(containsString(inputMessage2)));
    }

    @Test
    void getAnErroingWhenTryingToAccessConversationWithoutLogin() throws Exception {

        mockMvc.perform(get("/conversation").with(csrf())

        ).andExpect(status().is3xxRedirection());

    }

    @Test
    void whenUsersCanSeeTheirOwnConversations() throws Exception {
        String inputMessage1 = "Hassan Hi 2";
        mockMvc.perform(
                post("/conversation").with(csrf()).with(user("user@hassan.com").password("kachal"))
                        .param("content", inputMessage1)
        );

        mockMvc.perform(get("/conversation")
                        .with(user("ali@hassan.com").password("kochooloo"))
                ).andExpect(status().isOk()).andExpect(
                        result -> {
                            assertThat(result.getResponse().getContentAsString()).doesNotContain(inputMessage1);
                        });
    }


}
