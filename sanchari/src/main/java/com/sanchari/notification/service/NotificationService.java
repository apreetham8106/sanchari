package com.sanchari.notification.service;

import com.sanchari.notification.sms.SmsGateway;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SmsGateway sms;

    public NotificationService(SmsGateway sms) {
        this.sms = sms;
    }

    public void sendOtp(String phone, String code) {
        sms.send(phone, code + " is your Sanchari verification code. It is valid for 5 minutes. Do not share it with anyone.");
    }
}
