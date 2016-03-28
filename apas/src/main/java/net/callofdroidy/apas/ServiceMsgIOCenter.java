package net.callofdroidy.apas;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ServiceMsgIOCenter<T extends PubsubProviderClient, S extends GeneralPubsubCallback> extends Service {
    private static final String TAG = "ServiceMsgIOCenter";

    protected T t;
    protected S s;

    protected String subChannelName;
    protected String pubChannelName;

    public ServiceMsgIOCenter() {
    }

    public void onCreate(){
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new BinderMsgIO();
    }

    protected void setPubsubProvider(T pubsubProviderClient){
        this.t = pubsubProviderClient;
    }

    protected void setPublishChannel(String pubChannelName){
        this.pubChannelName = pubChannelName;
    }

    protected void setPubsubCallbackClass(S pubsubCallback){
        this.s = pubsubCallback;
    }

    protected void setSubscribeChannel(String subChannelName){
        this.subChannelName = subChannelName;
    }

    protected String getSubscribeChannel(){
        return this.subChannelName;
    }

    protected String getPublishChannel(){
        return this.pubChannelName;
    }

    protected void subscribeToChannel(){
        if(t != null && subChannelName != null)
            t.subscribeToChannel(subChannelName, s);
        else
            Log.e(TAG, "PubsubProviderClient or SubChannel is null");
    }

    protected void publishToChannel(Message message){
        if(t != null && pubChannelName != null)
            t.publishToChannel(pubChannelName, message, s);
        else
            Log.e(TAG, "PubsubProviderClient or PubChannel is null");
    }

    protected void publishToAnotherChannel(String anotherPubChannelName, Message message){
        if(t != null){
            t.publishToChannel(anotherPubChannelName, message, s);
        }
    }

    public class BinderMsgIO extends Binder{

        public void setPubsubProviderClient(T pubsubProviderClient){
            setPubsubProvider(pubsubProviderClient);
        }

        public void setSubChannel(String channelName){
            setSubscribeChannel(channelName);
        }

        public void setPubChannel(String channelName){
            setPublishChannel(channelName);
        }

        public String getSubChannel(){
            return getSubscribeChannel();
        }

        public String getPubChannel(){
            return getPublishChannel();
        }

        public void setPubsubCallback(S pubsubCallback){
            setPubsubCallbackClass(pubsubCallback);
        }

        public void subToChannel(){
            subscribeToChannel();
        }

        public void pubToChannel(Message message){
            publishToChannel(message);
        }

        public void pubToAnotherChannel(String anotherChannelName, Message message){
            publishToAnotherChannel(anotherChannelName, message);
        }
    }
}
