package com.example.shopgpt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MessageRepository messageRepo;

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

    @GetMapping("/conversation")
    public String showConversation(Model model, Principal principal) {
        User user = getUserByPrincipal(principal);
        return getString(model, user);
    }

    @PostMapping("/conversation")
    public String listConversation(Model model, @ModelAttribute("message") Message prevMessage, Principal principal) {
        User user = getUserByPrincipal(principal);
        prevMessage.setUser(user); // Associate message with the logged-in user
        messageRepo.save(prevMessage);
        return getString(model, user);
    }

    private User getUserByPrincipal(Principal principal) {
        return userRepo.findByEmail(principal.getName());
    }

    private void setupModelAttributes(Model model, User user, List<Message> listMessages, Message newMessage) {
        model.addAttribute("user", user);
        model.addAttribute("listMessages", listMessages);
        model.addAttribute("message", newMessage);
    }

    private String getString(Model model, User user) {
        List<Message> listMessages = messageRepo.findMessagesByUserId(user.getUserId());
        setupModelAttributes(model, user, listMessages, new Message());
        return "conversation";
    }

    @GetMapping("/conversation/{email}")
    public String showUserConversationForAdmin(Model model, @PathVariable String email) {
        User user = userRepo.findByEmail(email);
        return getString(model, user);
    }



}

