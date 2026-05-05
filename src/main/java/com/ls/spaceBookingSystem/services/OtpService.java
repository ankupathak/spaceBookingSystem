package com.ls.spaceBookingSystem.services;

import com.ls.spaceBookingSystem.constants.OtpTypes;
import com.ls.spaceBookingSystem.database.entity.Otp;
import com.ls.spaceBookingSystem.database.entity.User;
import com.ls.spaceBookingSystem.common.errors.ErrorCode;
import com.ls.spaceBookingSystem.common.exceptions.AppException;
import com.ls.spaceBookingSystem.database.repository.OtpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private  EmailTemplateService emailTemplateService;

    @Autowired
    private EmailService emailService;

    public String getEmailOtp(User user,  OtpTypes otpTypeCode) {
        validateOtpSendRules(user.getUserId(), otpTypeCode.getId());

        Otp emailOtp = createOtp(user.getUserId(), otpTypeCode.getId());

        String html = emailTemplateService.renderCreateAccountOtpEmail(user.getFullName(), emailOtp.getOtpCode());
        emailService.sendMail(user.getEmail(),"Otp for Account Creation for Space Booking System", html);
        return "Otp is sent to "+user.getEmail()+". Pls check spam folder as well in case you don't receive the otp";
    }

    private void validateOtpSendRules(long userId, int otpTypeId) {
        if (isLimitCrossed(userId, Instant.now().minus(1, ChronoUnit.MINUTES), otpTypeId)) {
            throw new AppException(ErrorCode.OTP_MAX_ATTEMPTS);
        }
        if (!hasMinimumGapBetweenOtps(userId, otpTypeId)) {
            throw new AppException(ErrorCode.OTP_RESEND_TOO_SOON);
        }
    }

    private boolean isLimitCrossed(long userId, Instant fromTime, int otpTypeId) {
        int count = otpRepository.countOtpsSentInWindow(
                userId,
                otpTypeId,
                Instant.now().minus(1, ChronoUnit.HOURS)
        );
        System.out.println("count =" +count);
        return count >= 5;
    }

    private boolean hasMinimumGapBetweenOtps(long userId, int otpTypeId) {
        Optional<Otp> lastOtpOpt = otpRepository.findLatestOtp(
                userId,
                otpTypeId,
                Instant.now()
        );
        if(lastOtpOpt.isPresent()) {
            Instant lastTime = lastOtpOpt.get().getCreatedAt();
            return !lastTime.isAfter(Instant.now().minus(1, ChronoUnit.MINUTES));
        }

        return true;
    }

    private Otp createOtp(long userId, int otpTypeId) {
        String otp = generateOtp();
        Otp emailOtp = new Otp();
        emailOtp.setOtpCode(otp);
        emailOtp.setUserId(userId);
        emailOtp.setOtpTypeId(otpTypeId);
        emailOtp.setExpiredAt(Instant.now().plus(10,ChronoUnit.MINUTES));
        otpRepository.save(emailOtp);

        return emailOtp;
    }

    private String generateOtp() {
        return String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 999999)
        );
    }

    @Transactional(noRollbackFor = AppException.class, propagation = Propagation.REQUIRES_NEW)
    public void verifyOtp(long userId, int otpTypeId, String otpCode) {
        Otp otp = otpRepository.findLatestOtp(userId, otpTypeId, Instant.now())
                .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_FOUND));

        if (otp.getRemainingAttempts() <= 0) {
            throw new AppException(ErrorCode.OTP_MAX_ATTEMPTS);
        }

        if (!otp.getOtpCode().equals(otpCode)) {
            log.error("remainingAttempts={} otpId={}",String.valueOf(otp.getRemainingAttempts()-1),String.valueOf(otp.getOtpId()));

            otpRepository.decrementAttempts((byte)(otp.getRemainingAttempts()-1),otp.getOtpId());
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        otpRepository.decrementAttempts((byte)0, otp.getOtpId());
    }
}
