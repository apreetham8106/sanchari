package com.sanchari.notification.sms;

/** Swap implementations to change SMS provider. */
public interface SmsGateway {
    void send(String phone, String message);
}
