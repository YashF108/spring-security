package com.example.client.controller;

import com.example.client.entity.User;
import com.example.client.entity.VerificationToken;
import com.example.client.event.RegistrationCompleteEvent;
import com.example.client.model.PasswordModel;
import com.example.client.model.UserModel;
import com.example.client.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {
        User user = userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(
                user,
                applicationUrl(request)
        ));
        return "Success";
    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {
        String result = userService.validatedVerificationToken(token);
        if ("Valid".equalsIgnoreCase(result)) {
            return "Verification Completed";
        }
        return result;
    }

    @GetMapping("/resendVerifyToken")
    public String resentVerificationToken(@RequestParam("token") String oldToken, HttpServletRequest request) {
        VerificationToken verificationToken = userService.generateNewVerificationToken(oldToken);
//        User user = verificationToken.getUser();
        resendVerificationUrl(applicationUrl(request), verificationToken);
        return "Verification Url Sent";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request){
        User user = userService.findUserByEmail(passwordModel.getEmail());
        String url = "";
        if(user!=null){
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            url = passwordResetUrl(applicationUrl(request), token);
        }
        return "Password Reset URL Sent";
    }

    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token, @RequestBody PasswordModel passwordModel){
        String result = userService.validatedPasswordResetToken(token);
        if (!("Valid".equalsIgnoreCase(result))) {
            return result;
        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if (user.isPresent()) {
            userService.changePassword(user.get(), passwordModel.getNewPassword());
            return "Password Reset Successfully";
        } else {
            return "Invalid Token";
        }
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel){
        User user = userService.findUserByEmail(passwordModel.getEmail());
        if (!userService.checkIfValidOldPassword(user, passwordModel.getOldPassword())) {
            return "Invalid Old Password";
        }

        userService.changePassword(user, passwordModel.getNewPassword());
        return "Password Changed Successfully";
    }

    private String passwordResetUrl(String applicationUrl, String token) {
        String url = applicationUrl + "/savePassword?token=" + token;

        // sendVerificationEmail()
        log.info("Click the link to reset your password: {}", url);

        return url;
    }

    private void resendVerificationUrl(String applicationUrl, VerificationToken verificationToken) {
        String url = applicationUrl + "/verifyRegistration?token=" + verificationToken.getToken();

        // sendVerificationEmail()
        log.info("Click the link to verify your account: {}", url);
    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
