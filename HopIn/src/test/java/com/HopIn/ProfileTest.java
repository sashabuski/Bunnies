package com.HopIn;

import junit.framework.TestCase;


public class ProfileTest extends TestCase {


    /**
     * This test exhibits the functionality of the updateUser() of the Profile Class.
     * The user is allowed to update their name via the Profile page. However, leaving an empty firstname should not be allowed.
     * The purpose of this test is to pass two Strings, one with alphabets and another with spaces.
     * Without alphabets 
     *
     * This test asserts false if only spaces or empty.
     *
     */

    public void testProfile() {

        Profile profile = new Profile();
        
		firstname="Testing"
		secondname=" "

        assertTrue(profile.isNameValid(firstname));
		assertFalse(profile.isNameValid(secondname));
    }
}