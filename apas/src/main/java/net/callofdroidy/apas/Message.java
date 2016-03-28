package net.callofdroidy.apas;

import java.util.HashMap;

/**
 * Created by admin on 23/03/16.
 */
public class Message {
    private HashMap<String, String> headers;
    private String body;

    public Message(HashMap<String, String> headers, String body){
        this.headers = headers;
        this.body = body;
    }

    public String getBody(){
        return body;
    }

    public HashMap<String, String> getHeaders(){
        return headers;
    }

    public void setHeaders(HashMap<String, String > headers){
        this.headers = headers;
    }

    public void setBody(String body){
        this.body = body;
    }
}
