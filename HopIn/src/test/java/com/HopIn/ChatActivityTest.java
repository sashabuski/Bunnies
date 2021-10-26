package com.HopIn;

import junit.framework.TestCase;

public class ChatActivityTest extends TestCase {


    /**
     * This test exhibits the functionality of the filterIncomingMessages of the ChatActivity Class.
     * The user type of the activity is set as driver, a messageList with all messageType of 2 (outgoing messages)
     * is created. The purpose of this function is to change the messageType to 1 (incoming messages) if the userType
     * of the message model is (in this case) a Rider (and vice versa if the user type of the activity is set to rider).
     *
     * This test asserts that the change in messageType is successful.
     *
     */

    public void testFilterIncomingMessages() {

        ChatActivity cA = new ChatActivity();
        cA.setUserType("Driver");
        MessageModel a = new MessageModel("testicle", 2, "Rider");
        MessageModel b = new MessageModel("testicle", 2, "Rider");
        MessageModel c = new MessageModel("testicle", 2, "Driver");

        MessageList ml = new MessageList();

        ml.getMessagesList().add(a);
        ml.getMessagesList().add(b);
        ml.getMessagesList().add(c);

        cA.filterIncomingMessages(ml);

        assertEquals(1 , ml.getMessagesList().get(0).messageType);
        assertEquals(1 , ml.getMessagesList().get(1).messageType);
        assertEquals(2 , ml.getMessagesList().get(2).messageType);
    }
}