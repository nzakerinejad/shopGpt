package com.example.shopgpt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.security.Principal;
import java.sql.SQLOutput;
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

//    @GetMapping("/conversation")
//    public String showConversation(Model model, Principal principal) {
//        User user = getUserByPrincipal(principal);
//        return getString(model, user);
//    }

//    @PostMapping("/conversation")
//    public String listConversation(Model model, @ModelAttribute("message") Message prevMessage, Principal principal) {
//        User user = getUserByPrincipal(principal);
//        prevMessage.setUser(user); // Associate message with the logged-in user
//        messageRepo.save(prevMessage);
//        return getString(model, user);
//    }

//    @GetMapping("/conversation/{email}")
//    public String showUserConversationForAdmin(Model model, @PathVariable String email) {
//        User user = userRepo.findByEmail(email);
//        return getString(model, user);
//    }

//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @PostMapping("/conversation/{email}")
//    public String sendMessageInUSerPageByAdmin(Model model, @PathVariable String email, @ModelAttribute("message") Message prevMessage, Principal principal) {
//
//        User admin = getUserByPrincipal(principal);
//        User user = userRepo.findByEmail(email);
//        prevMessage.setUser(user); // Associate message with the logged-in user
//        prevMessage.setItFromAdmin(true);
//        messageRepo.save(prevMessage);
//        List<Message> listMessages = messageRepo.findMessagesByUserId(user.getUserId());
//
//        model.addAttribute("user", user);
//        model.addAttribute("listMessages", listMessages);
//        model.addAttribute("message", new Message());
//
//        return "conversation";
//    }


    @GetMapping("/chat")
    public String showConversationInChat(Model model, Principal principal) {
        User user = getUserByPrincipal(principal);
        prepareModelForChat(model, user);
        return "chatPage";
    }

    @GetMapping("/admin_chat/{email}")
    public String showUserChatPageToAdmin(Model model, @PathVariable String email) {
        User user = userRepo.findByEmail(email);
        prepareModelForChat(model, user);
        return "chatPage";
    }

    @GetMapping({"/admin_conversation/{conversationId}"})
    public String showUserSpecificConversationToAdmin(Model model, Principal principal, @PathVariable("conversationId") Long conversationId) {
        Long theId = conversationId;

        List<Message> listMessages = messageRepo.findMessagesByConversationId(theId);
        var c = conversationRepo.findByConversationId(theId);
        System.out.println(c.getConversationId());
        var user = c.getUser();
        System.out.println(user.getFirstName());


        model.addAttribute("user", user);
        model.addAttribute("listMessages", listMessages);
        model.addAttribute("formMessage", new FormMessage("", theId.toString()));
        return "conversation";

    }

    @GetMapping({"/conversation/{conversationId}","/conversation"})
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


    private void prepareModelForChat(Model model, User user) {
        model.addAttribute("user", user);
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

