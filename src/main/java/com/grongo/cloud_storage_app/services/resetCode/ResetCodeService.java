package com.grongo.cloud_storage_app.services.resetCode;


import com.grongo.cloud_storage_app.exceptions.resetCode.ExistingResetCodeException;
import com.grongo.cloud_storage_app.exceptions.resetCode.InvalidCodeException;
import com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException;
import com.grongo.cloud_storage_app.models.resetCode.CheckResetCodeRequest;
import com.grongo.cloud_storage_app.models.resetCode.ResetCodeMemory;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.cache.CacheKeys;
import com.grongo.cloud_storage_app.services.cache.impl.ResetCodeMemoryCache;
import com.grongo.cloud_storage_app.services.email.EmailService;
import static com.grongo.cloud_storage_app.services.cache.CacheKeys.*;
import lombok.RequiredArgsConstructor;
import org.apache.james.mime4j.dom.datetime.DateTime;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class ResetCodeService {

    private final EmailService emailService;
    private final ResetCodeMemoryCache resetCodeMemoryCache;
    private final UserRepository userRepository;

    private final Duration TTL = Duration.ofMinutes(5);

    public void createCode(String email){
        userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Couldn't find user with email " + email));

        ResetCodeMemory foundCodeMemory = resetCodeMemoryCache.getKey(CacheKeys.resetCodeKey(email));
        if (foundCodeMemory != null) throw new ExistingResetCodeException("A reset password code has already been sent to your email.");

        ResetCodeMemory codeMemory = ResetCodeMemory.builder()
                .code(generateStringCode())
                .createdAt(new Date())
                .isChecked(false)
                .build();

        resetCodeMemoryCache.setKey(CacheKeys.resetCodeKey(email), codeMemory, TTL);

        SimpleMailMessage message = emailService.message()
                .setSubject("Reset password code")
                .setTo(email)
                .setText("A reset password code has been sent to your email. If it wasn't you, ignore the email. Code: " + codeMemory.getCode())
                .build();

        emailService.send(message);
    }

    public String generateStringCode(){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 10; i++){
            int curNum = (int) Math.ceil(Math.random() * 10);
            stringBuilder.append(curNum);
        }

        return stringBuilder.toString();
    }

    public void checkCode(CheckResetCodeRequest resetCodeRequest){
        ResetCodeMemory resetCodeMemory = resetCodeMemoryCache.getKey(CacheKeys.resetCodeKey(resetCodeRequest.getEmail()));
        if (resetCodeMemory == null){
            throw new InvalidCodeException("Code has expired. Try requesting another one.");
        }

        if (!resetCodeMemory.getCode().equals(resetCodeRequest.getCode())){
            throw new InvalidCodeException("Code doesn't match.");
        }

        resetCodeMemory.setChecked(true);
        resetCodeMemoryCache.setKey(
            CacheKeys.resetCodeKey(resetCodeRequest.getEmail()),
            resetCodeMemory,
            TTL
        );
    }


}
