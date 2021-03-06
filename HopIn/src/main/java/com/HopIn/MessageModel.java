package com.HopIn;

import java.util.Date;

/**
 * Message object used when using the chat function of the app.
 *
 */


public class MessageModel {

    public String message;
    public String userType;
    public int messageType;
    public Date messageTime = new Date();

    public MessageModel(String message, int messageType, String userType) {
        this.message = message;
        this.messageType = messageType;
        this.userType = userType;
    }

    public MessageModel() {
        this.message = null;
        this.messageType = 0;

    }
}