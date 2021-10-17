package com.HopIn;

import java.util.ArrayList;

/**
 * Class to make reading and writing from the DB easier. Easier to use custom object containing
 * arraylist than trying to store and deal with arraylist/hashmap/arraylist/hashmap bs.
 *
 */

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
