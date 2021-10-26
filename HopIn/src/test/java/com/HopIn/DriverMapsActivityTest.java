package com.HopIn;

import junit.framework.TestCase;

import java.util.Date;

/**
 * This tests asserts that the isNewRequest function is working.
 *
 * This function asserts that a Ride object is LIVE. If the timestamp of a Ride is longer than 10 seconds
 * prior to the current time, the function will return false, and the Ride will not be considered LIVE
 * This is used as a precautionary function to double check that no rides created longer than 10 seconds ago
 * are detected as new requests by the Driver class.
 *
 */

public class DriverMapsActivityTest extends TestCase {

    public void testIsNewRequest() {

        DriverMapsActivity dma = new DriverMapsActivity();

        assertTrue(dma.isNewRequest(new Ride(new Date(System.currentTimeMillis()))));

        assertFalse(dma.isNewRequest(new Ride(new Date(System.currentTimeMillis() - 11000))));

    }
}