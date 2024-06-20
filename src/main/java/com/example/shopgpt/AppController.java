package com.example.shopgpt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MessageRepository messageRepo;

    @GetMapping("")
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
        List<Message> listMessages = messageRepo.findMessagesByUserId(user.getUserId());
        setupModelAttributes(model, user, listMessages, new Message());
        return "conversation";
    }

    @PostMapping("/conversation")
    public String listConversation(Model model, @ModelAttribute("message") Message prevMessage, Principal principal) {
        User user = getUserByPrincipal(principal);
        prevMessage.setUser(user); // Associate message with the logged-in user
        messageRepo.save(prevMessage);
        List<Message> listMessages = messageRepo.findMessagesByUserId(user.getUserId());
        setupModelAttributes(model, user, listMessages, new Message());
        return "conversation";
    }

    private User getUserByPrincipal(Principal principal) {
        return userRepo.findByEmail(principal.getName());
    }

    private void setupModelAttributes(Model model, User user, List<Message> listMessages, Message newMessage) {
        model.addAttribute("user", user);
        model.addAttribute("listMessages", listMessages);
        model.addAttribute("message", newMessage);
    }


}

