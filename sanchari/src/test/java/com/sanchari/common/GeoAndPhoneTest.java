package com.sanchari.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sanchari.common.exception.BadRequestException;
import com.sanchari.common.util.GeoUtils;
import com.sanchari.common.util.PhoneUtils;
import org.junit.jupiter.api.Test;

class GeoAndPhoneTest {

    @Test
    void distanceBetweenSamePointIsZero() {
        assertEquals(0.0, GeoUtils.distanceMeters(17.385, 78.4867, 17.385, 78.4867), 0.001);
    }

    @Test
    void oneDegreeOfLatitudeIsAbout111Km() {
        double d = GeoUtils.distanceMeters(17.0, 78.0, 18.0, 78.0);
        assertTrue(d > 110_000 && d < 112_500, "was " + d);
    }

    @Test
    void phoneNormalisation() {
        assertEquals("+919876543210", PhoneUtils.normalize("98765 43210"));
        assertEquals("+919876543210", PhoneUtils.normalize("919876543210"));
        assertEquals("+14155552671", PhoneUtils.normalize("+1 (415) 555-2671"));
        assertThrows(BadRequestException.class, () -> PhoneUtils.normalize("12345"));
        assertThrows(BadRequestException.class, () -> PhoneUtils.normalize(null));
    }
}
