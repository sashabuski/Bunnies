package com.HopIn;

import junit.framework.TestCase;

/**
 * This test exhibits the functionality of the updateUser() of the Profile Class.
 * The user is allowed to update their name via the Profile page. However, leaving an empty firstname should not be allowed.
 * The purpose of this test is to pass two Strings, one with alphabets and another with spaces.
 * Without alphabets
 *
 * This test asserts false if only spaces or empty.
 *
 */
public class ProfileTest extends TestCase {

    public void testisNameValid() {

        Profile profile = new Profile();
        
		String firstname="Testing";
		String secondname=" ";

        assertFalse(profile.isNameValid(firstname));

        assertFalse(profile.isNameValid(secondname));
    }
    public void testisNameValidd() {

        Profile profile = new Profile();

        String firstname="Testing";
        String secondname=" ";

        assertTrue(profile.isNameValid(firstname));
    }

}
