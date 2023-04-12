package com.example.client.service;

import com.example.client.entity.User;
import com.example.client.entity.VerificationToken;
import com.example.client.model.PasswordModel;
import com.example.client.model.UserModel;

import java.util.Optional;

public interface UserService {
    User registerUser(UserModel userModel);

    void saveVerificationTokenForUser(User user, String token);

    String validatedVerificationToken(String token);

    VerificationToken generateNewVerificationToken(String oldToken);

    User findUserByEmail(String email);

    void createPasswordResetTokenForUser(User user, String token);

    String validatedPasswordResetToken(String token);

    Optional<User> getUserByPasswordResetToken(String token);

    void changePassword(User user, String newPassword);

    Boolean checkIfValidOldPassword(User user, String oldPassword);
}
