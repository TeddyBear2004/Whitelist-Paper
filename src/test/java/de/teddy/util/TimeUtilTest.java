package de.teddy.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class TimeUtilTest {

    @Test
    void parseMillis(){

    }

    @Test
    void parseMinutes(){

    }

    @Test
    void stringToMinutes(){
        assertEquals(1, TimeUtil.stringToMinutes("1m"));
        assertEquals(2, TimeUtil.stringToMinutes("2m"));
        assertEquals(5, TimeUtil.stringToMinutes("5m"));
        assertEquals(10, TimeUtil.stringToMinutes("10m"));

        assertEquals(60, TimeUtil.stringToMinutes("1h"));
        assertEquals(120, TimeUtil.stringToMinutes("2h"));
        assertEquals(300, TimeUtil.stringToMinutes("5h"));
        assertEquals(600, TimeUtil.stringToMinutes("10h"));

        assertEquals(11 * 60 + 5, TimeUtil.stringToMinutes("11h5m"));
        assertEquals(2 * 1440 + 11 * 60 + 5, TimeUtil.stringToMinutes("2d11h5m"));
        assertEquals(2 * 1440 + 5, TimeUtil.stringToMinutes("2d5m"));
        assertEquals(2 * 1440, TimeUtil.stringToMinutes("2d"));

        //test edge cases
        assertEquals(0, TimeUtil.stringToMinutes("0m"));
        assertEquals(0, TimeUtil.stringToMinutes("0h"));
        assertEquals(0, TimeUtil.stringToMinutes("0h0m"));
        assertThrowsExactly(IllegalArgumentException.class, () -> TimeUtil.stringToMinutes("0m0s"));
        assertThrowsExactly(IllegalArgumentException.class, () -> TimeUtil.stringToMinutes("0h0m0s0ms"));
        assertThrowsExactly(IllegalArgumentException.class, () -> TimeUtil.stringToMinutes("0h0m0s0ms0"));
        assertThrowsExactly(IllegalArgumentException.class, () -> TimeUtil.stringToMinutes("hmsms"));
        assertEquals(2 * 1440, TimeUtil.stringToMinutes("2dhm"));
        assertThrowsExactly(IllegalArgumentException.class, () -> TimeUtil.stringToMinutes("a"));
        assertThrowsExactly(IllegalArgumentException.class, () -> TimeUtil.stringToMinutes("3b"));
        assertEquals(3, TimeUtil.stringToMinutes("h3m"));
        assertEquals(0, TimeUtil.stringToMinutes("m"));
        assertEquals(0, TimeUtil.stringToMinutes("h"));
        assertEquals(4 * 1440 + 3, TimeUtil.stringToMinutes("3m4d"));
        assertEquals(4 * 1440, TimeUtil.stringToMinutes("m4d"));
        assertThrowsExactly(IllegalArgumentException.class, () -> TimeUtil.stringToMinutes("22.23h"));
    }

    @Test
    void completeTime(){
        assertEquals(List.of("1d", "1h", "1m"), TimeUtil.completeTime("1"));
        assertEquals(List.of("1d2h", "1d2m"), TimeUtil.completeTime("1d2"));
        assertEquals(List.of("1d2m4h"), TimeUtil.completeTime("1d2m4"));
    }
}