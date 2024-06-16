package com.example.shopgpt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

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
    public String showConversation(Model model) {
        return setupAndGetConversationTemplate(model, new Message(), new Message());
    }

    @PostMapping("/conversation")
    public String listConversation(Model model, Message prevMessage) {
        messageRepo.save(prevMessage);
        return setupAndGetConversationTemplate( model, prevMessage, new Message());
    }

    private String setupAndGetConversationTemplate( Model model, Message prevMessage, Message attributeValue) {
        List<Message> listMessages = messageRepo.findAll();
        model.addAttribute("listMessages", listMessages);
        model.addAttribute("prevMessage", prevMessage);
        model.addAttribute("message", attributeValue);
        return "conversation";
    }


}

