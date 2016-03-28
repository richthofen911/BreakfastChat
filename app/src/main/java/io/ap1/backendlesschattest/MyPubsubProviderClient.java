package io.ap1.backendlesschattest;

import android.util.Log;

import com.google.gson.Gson;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import net.callofdroidy.apas.Message;
import net.callofdroidy.apas.PubsubProviderClient;

/**
 * Created by admin on 24/03/16.
 */
public class MyPubsubProviderClient implements PubsubProviderClient<AppPubsubCallback>{
    private Pubnub pubnub;
    private Gson gson;

    public MyPubsubProviderClient(Pubnub pubnub){
        this.pubnub = pubnub;
        gson = new Gson();
    }

    @Override
    public void subscribeToChannel(String channelName, AppPubsubCallback appPubsubCallback){
        try{
            pubnub.subscribe(channelName, appPubsubCallback);
        }catch (PubnubException e){
            Log.e("SUBSCRIBE", "Error: " + e.toString());
        }
    }

    @Override
    public void publishToChannel(String channelName, Message message, AppPubsubCallback appPubsubCallback){
        pubnub.publish(channelName, gson.toJson(message), appPubsubCallback);
    }
}
