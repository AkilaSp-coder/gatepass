package com.example.gatepass.controller;

import com.example.gatepass.model.Visitor;
import com.example.gatepass.repository.VisitorRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VisitorRepository repo;

    @GetMapping("/")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home(Model model) {

        long total = repo.count();

        long approved = repo.findAll()
                .stream()
                .filter(v -> "APPROVED".equals(v.getStatus()))
                .count();

        long rejected = repo.findAll()
                .stream()
                .filter(v -> "REJECTED".equals(v.getStatus()))
                .count();

        long pending = repo.findAll()
                .stream()
                .filter(v -> "PENDING".equals(v.getStatus()))
                .count();

        model.addAttribute("total", total);
        model.addAttribute("approved", approved);
        model.addAttribute("rejected", rejected);
        model.addAttribute("pending", pending);
        model.addAttribute("notification", pending);

        return "home";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam String keyword,
            Model model) {

        model.addAttribute(
                "list",
                repo.findByNameContainingIgnoreCase(keyword));

        return "visitors";
    }

    @PostMapping("/submit")
    public String submit(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String purpose,
            Model model) {

        Visitor visitor = new Visitor();

        visitor.setName(name);
        visitor.setEmail(email);
        visitor.setPurpose(purpose);

        visitor.setStatus("PENDING");

        visitor.setVisitDate(
                java.time.LocalDate.now().toString());

        visitor.setEntryTime(
                java.time.LocalTime.now().toString());

        repo.save(visitor);

        try {

            String acceptUrl =
                    "http://localhost:8081/accept/"
                            + visitor.getId();

            String rejectUrl =
                    "http://localhost:8081/reject/"
                            + visitor.getId();

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo("visitorsp26@gmail.com");

            helper.setSubject("New Visitor Request");

            String html = ""

            + "<div style='font-family:Arial;"
            + "padding:20px;"
            + "background:#f2f2f2;'>"

            + "<div style='background:white;"
            + "padding:20px;"
            + "border-radius:10px;'>"

            + "<img src='https://cdn-icons-png.flaticon.com/512/3135/3135715.png'"
            + " width='80'>"

            + "<h2 style='color:#3498db;'>"
            + "New Visitor Request"
            + "</h2>"

            + "<table border='1' cellpadding='10' "
            + "style='border-collapse:collapse;'>"

            + "<tr><th>Name</th><td>"
            + name
            + "</td></tr>"

            + "<tr><th>Email</th><td>"
            + email
            + "</td></tr>"

            + "<tr><th>Purpose</th><td>"
            + purpose
            + "</td></tr>"

            + "<tr><th>Status</th><td>PENDING</td></tr>"

            + "</table>"

            + "<br><br>"

            + "<a href='" + acceptUrl + "' "
            + "style='padding:12px 25px;"
            + "background:green;"
            + "color:white;"
            + "text-decoration:none;"
            + "border-radius:5px;"
            + "margin-right:10px;'>"
            + "ACCEPT</a>"

            + "<a href='" + rejectUrl + "' "
            + "style='padding:12px 25px;"
            + "background:red;"
            + "color:white;"
            + "text-decoration:none;"
            + "border-radius:5px;'>"
            + "REJECT</a>"

            + "</div>"
            + "</div>";

            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {

            e.printStackTrace();
        }

        model.addAttribute(
                "msg",
                "Visitor Added Successfully");

        return "success";
    }

    @GetMapping("/visitors")
    public String visitors(
            @RequestParam(required = false)
            String success,
            Model model) {

        model.addAttribute("list", repo.findAll());

        model.addAttribute("success", success);

        return "visitors";
    }

    @GetMapping("/accept/{id}")
    public String accept(@PathVariable Long id) {

        Visitor visitor = repo.findById(id).get();

        visitor.setStatus("APPROVED");

        visitor.setExitTime(
                java.time.LocalTime.now().toString());

        repo.save(visitor);

        return "redirect:/visitors?success=approved";
    }

    @GetMapping("/reject/{id}")
    public String reject(@PathVariable Long id) {

        Visitor visitor = repo.findById(id).get();

        visitor.setStatus("REJECTED");

        repo.save(visitor);

        return "redirect:/visitors?success=rejected";
    }

    @GetMapping("/admin")
    public String adminPage() {

        return "admin-login";
    }

    @PostMapping("/admin-login")
    public String adminLogin(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        if(username.equals("admin")
                && password.equals("admin123")) {

            return "redirect:/visitors";
        }

        model.addAttribute(
                "error",
                "Invalid Username or Password");

        return "admin-login";
    }
}