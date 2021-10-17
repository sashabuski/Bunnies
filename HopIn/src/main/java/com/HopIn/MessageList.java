package com.HopIn;

import java.util.ArrayList;

public class MessageList {

    private ArrayList<MessageModel> messagesList;

    public ArrayList<MessageModel> getMessagesList() {
        return messagesList;
    }
    public MessageList() {
        this.messagesList = new ArrayList<>();
    }

    public void setMessagesList(ArrayList<MessageModel> messagesList) {
        this.messagesList = messagesList;
    }

    public MessageList(ArrayList<MessageModel> messagesList) {
        this.messagesList = messagesList;
    }

}
