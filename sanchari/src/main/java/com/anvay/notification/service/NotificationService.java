package com.anvay.notification.service;

import com.anvay.notification.sms.SmsGateway;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SmsGateway sms;

    public NotificationService(SmsGateway sms) {
        this.sms = sms;
    }

    public void sendOtp(String phone, String code) {
        sms.send(phone, code + " is your Anvay verification code. It is valid for 5 minutes. Do not share it with anyone.");
    }
}
