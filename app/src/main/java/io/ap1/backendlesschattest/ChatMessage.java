package io.ap1.backendlesschattest;

/**
 * Created by admin on 17/03/16.
 */
public class ChatMessage {

    private String sender;
    private int type;
    private String content;

    public ChatMessage(String sender, int type, String content){
        this.sender = sender;
        this.type = type;
        this.content = content;
    }

    public String toString(){
        content = content.replace("||", "//"); // doesn't allow || appears in content, it will break the format
        return sender + "||" + type + "||" + content;
    }
}
