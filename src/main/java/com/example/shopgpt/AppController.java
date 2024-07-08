package com.example.shopgpt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private ConversationRepository conversationRepo;

    @RequestMapping({"", "/", "/index"})
    public String viewHomePage() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());

        return "signup_form";
    }

    @PostMapping("/process_register")
    public String processRegister(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(user.getPassword());

        user.setPassword(encodedPassword);

        userRepo.save(user);

        return "register_success";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> listUsers = userRepo.findAll();
        model.addAttribute("listUsers", listUsers);

        return "users";
    }


    @GetMapping("/chat")
    public String showConversationInChat(Model model, Principal principal) {
        User user = getUserByPrincipal(principal);
        var loggedInUser = user;
        prepareModelForChat(model, user, loggedInUser);
        return "chatPage";
    }

    @GetMapping("/admin_chat/{email}")
    public String showUserChatPageToAdmin(Model model, Principal principal, @PathVariable String email) {
        User user = userRepo.findByEmail(email);
        var loggedInUser = getUserByPrincipal(principal);
        prepareModelForChat(model, user, loggedInUser);
        return "chatPage";
    }

    @GetMapping({"/admin_conversation/{conversationId}"})
    public String showUserSpecificConversationToAdmin(Model model, Principal principal, @PathVariable("conversationId") Long conversationId) {
        Long theId = conversationId;
        var loggedInUser = getUserByPrincipal(principal);
        List<Message> listMessages = messageRepo.findMessagesByConversationId(theId);
        var conversation = conversationRepo.findByConversationId(theId);
        var user = conversation.getUser();

        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("user", user);
        model.addAttribute("listMessages", listMessages);
        model.addAttribute("formMessage", new FormMessage("", theId.toString()));
        return "conversation";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/admin_conversation/{conversationId}")
    public String submitMessageWhenItIsAdmin(Model model, Principal principal, @PathVariable("conversationId") Long conversationId, @ModelAttribute FormMessage preMessage) {

        User admin = getUserByPrincipal(principal);
        var conversation = conversationRepo.findByConversationId(conversationId);
        var user = conversation.getUser();

        var messageToSave = new Message();
        messageToSave.setConversation(conversation);
        messageToSave.setContent(preMessage.content);
        messageToSave.setItFromAdmin(true);

        messageRepo.save(messageToSave);
        List<Message> listMessages = messageRepo.findMessagesByConversationId(conversationId);

        model.addAttribute("loggedInUser", admin);
        model.addAttribute("user", user);
        model.addAttribute("listMessages", listMessages);
        model.addAttribute("formMessage", new FormMessage("", conversationId.toString()));
        return "conversation";

    }

    @GetMapping({"/conversation/{conversationId}", "/conversation"})
    public String showSpecificConversation(Model model, Principal principal, @PathVariable("conversationId") Optional<Long> conversationId) {
        Long theId;
        if (conversationId.isPresent()) {
            theId = conversationId.get();
        } else {
            User user = getUserByPrincipal(principal);
            var conversation = createNewConversation(user);
            theId = conversation.getConversationId();
        }

        return renderConversationTemplate(theId, principal, model, "formMessage");
    }

    @PostMapping("/conversation")
    public String submitMessage(Model model, Principal principal, @ModelAttribute FormMessage preMessage) {
        var inputConversationId = Long.parseLong(preMessage.conversationId());
        var conversation = conversationRepo.findByConversationId(inputConversationId);
        var messageToSave = new Message();
        messageToSave.setConversation(conversation);
        messageToSave.setContent(preMessage.content);
        messageRepo.save(messageToSave);
        return renderConversationTemplate(inputConversationId, principal, model, "message");
    }

    private String renderConversationTemplate(Long theId, Principal principal, Model model, String formMessage) {
        List<Message> listMessages = messageRepo.findMessagesByConversationId(theId);
        User user = getUserByPrincipal(principal);
        model.addAttribute("loggedInUser", user);
        model.addAttribute("user", user);
        model.addAttribute("listMessages", listMessages);
        model.addAttribute("formMessage", new FormMessage("", theId.toString()));
        return "conversation";
    }

    record FormMessage(String content, String conversationId){  }

    private Conversation createNewConversation(User user) {
        Conversation con = new Conversation();
        con.setUser(user);
        return conversationRepo.save(con);
    }


    private void prepareModelForChat(Model model, User user, User loggedInUser) {
        model.addAttribute("user", user);
        model.addAttribute("loggedInUser", loggedInUser);
        List<Conversation> conversationsByUserId = conversationRepo.findConversationsByUserId(user.getUserId());
        List<Long> convIds = conversationsByUserId.stream().map(conversation -> conversation.getConversationId()).toList();
        model.addAttribute("listConversations", convIds);
    }


    private User getUserByPrincipal(Principal principal) {
        return userRepo.findByEmail(principal.getName());
    }

    private String getString(Model model, User user) {
//        List<Message> listMessages = messageRepo.findMessagesByUserId(user.getUserId());
        List<Message> listMessages = null;
        setupModelAttributes(model, user, listMessages, new Message());
        return "conversation";
    }

    private void setupModelAttributes(Model model, User user, List<Message> listMessages, Message newMessage) {
        model.addAttribute("user", user);
        model.addAttribute("listMessages", listMessages);
        model.addAttribute("message", newMessage);
    }

}