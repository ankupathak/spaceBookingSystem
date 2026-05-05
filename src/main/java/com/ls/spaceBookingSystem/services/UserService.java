package com.ls.spaceBookingSystem.services;

import com.ls.spaceBookingSystem.common.errors.ErrorCode;
import com.ls.spaceBookingSystem.common.exceptions.AppException;
import com.ls.spaceBookingSystem.dtos.requests.CreateAccountRequest;
import com.ls.spaceBookingSystem.database.entity.User;
import com.ls.spaceBookingSystem.database.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired EmailTemplateService emailTemplateService;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String getUserTimezone(long userId) {
        return userRepository.findTimezoneByUserId(userId)
        .orElseThrow(() -> new AppException(ErrorCode.UNEXPECTED)
                .withDevMessage("unable to fetch timezone for user = "+userId)
        );
    }

    /*
    @Transactional
    public Optional<User> createUser(CreateAccountRequest request) {
        try {
            User user = new User();
            user.setFullName(request.getFullName());
            user.setPassword(encodePassword(request.getPassword()));
            user.setEmail(request.getEmail());
            User savedUser = userRepository.save(user);

            emailTemplateService.renderCreateAccountOtpEmail(savedUser.getFullName(), "1234");
            return Optional.of(userRepository.save(user));
        } catch(Exception e) {
            log.error("Exception at saveUser: ",e);
        }

        return Optional.empty();
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

     */
}
