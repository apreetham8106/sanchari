package com.anvay.common.event;

public final class Topics {
    private Topics() {
    }

    public static final String OTP_SEND = "otp.send";
    public static final String DRIVER_LOCATION = "driver.location";
    public static final String RIDE_REQUESTED = "ride.requested";
    public static final String RIDE_OFFER = "ride.offer";
    public static final String RIDE_ACCEPTED = "ride.accepted";
    public static final String RIDE_STATUS = "ride.status";
    public static final String DRIVER_KYC = "driver.kyc";
}
