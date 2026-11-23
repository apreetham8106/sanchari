package com.sanchari.common.event;

public record OtpSendEvent(String phone, String code, String purpose) {
}
