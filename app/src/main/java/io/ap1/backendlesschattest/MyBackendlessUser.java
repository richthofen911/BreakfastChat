package io.ap1.backendlesschattest;

import com.backendless.BackendlessUser;
import com.backendless.messaging.Message;

import java.util.ArrayList;

/**
 * Created by admin on 18/03/16.
 */
public class MyBackendlessUser{
    private BackendlessUser backendlessUser;
    private ArrayList<Message> messageList;

    public MyBackendlessUser(BackendlessUser backendlessUser){
        this.backendlessUser = backendlessUser;
        messageList = new ArrayList<>();
    }

    public String getName(){
        return (String) backendlessUser.getProperty("name");
    }

    public String getBio(){
        return (String) backendlessUser.getProperty("bio");
    }

    public String getColor(){
        return "#" + backendlessUser.getProperty("color");
    }

    public String getPictureUrl(){
        return (String) backendlessUser.getProperty("pictureUrl");
    }

    public String getUserObjectId(){
        return backendlessUser.getObjectId();
    }

    public void addToMessageList(Message newMessage){
        messageList.add(newMessage);
    }

    public void clearMessageList(){
        messageList.clear();
    }

    public ArrayList<Message> getMessageList(){
        return messageList;
    }
}
