package io.ap1.backendlesschattest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.Message;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.backendless.messaging.PublishStatusEnum;

import java.util.List;

public class ServiceMessageIOCenter extends Service {

    private String myUserObjectId;
    private String targetUserObjectId;
    private String targetChannel;
    private String myChannel;

    private AdapterUserInList adapterUserInList;
    private AdapterChatMsgList adapterChatMsgList;

    private Handler handler;

    public ServiceMessageIOCenter() {
    }

    public void onCreate(){
        super.onCreate();

        handler = new Handler(getMainLooper());

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("onBind", "center");
        Bundle bundle = intent.getExtras();

        if(bundle.getString("myUserObjectId") != null){
            myUserObjectId = bundle.getString("myUserObjectId");
            myChannel = "proximity_" + myUserObjectId.replace("-", "");
        }

        return new BinderMessageIO();
    }

    public void setAdapterUserInList(AdapterUserInList adapterUserInList){
        this.adapterUserInList = adapterUserInList;
    }

    public void setAdapterChatMsgList(AdapterChatMsgList adapterChatMsgList){
        this.adapterChatMsgList = adapterChatMsgList;
    }

    private void retrieveDetectedUserObject(final String detectedUserObjectId){
        Backendless.Persistence.of( BackendlessUser.class ).findById(detectedUserObjectId, new DefaultCallback<BackendlessUser>(this) {
            @Override
            public void handleResponse(BackendlessUser response) {
                Log.e("detect user info", response.getProperty("name") + "\n" + response.getProperty("pictureUrl"));
                if (!DataStore.userList.contains(response)) {
                    //DataStore.userList.add(DataStore.userList.size(), response);
                    DataStore.userList.add(new MyBackendlessUser(response));
                    adapterUserInList.notifyItemInserted(DataStore.userList.size());
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                super.handleFault(fault);
                Log.e("Handle fault", fault.toString());
            }
        });
    }

    private void setTargetChannel(String targetUserObjectId){
        this.targetUserObjectId = targetUserObjectId;
        targetChannel = "proximity_" + targetUserObjectId.replace("-", "");
    }

    private void publishToTargetChannel(String msgType, String msgContent){
        PublishOptions publishOptions = new PublishOptions();
        publishOptions.putHeader("type", msgType);
        publishOptions.setPublisherId(myUserObjectId);
        Log.e("targetChannel", targetChannel + "");
        Backendless.Messaging.publish(targetChannel, msgContent, publishOptions, new AsyncCallback<MessageStatus>() {
            @Override
            public void handleResponse(final MessageStatus messageStatus) {
                PublishStatusEnum msgStatus = messageStatus.getStatus();
                if (msgStatus == PublishStatusEnum.FAILED) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ServiceMessageIOCenter.this, "Failed to send the message: " + messageStatus.getMessageId(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ServiceMessageIOCenter.this, "Msg sent: " + messageStatus.getMessageId(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }

            @Override
            public void handleFault(final BackendlessFault backendlessFault) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ServiceMessageIOCenter.this, "Send message, Error: " + backendlessFault.getDetail(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void subscribeToMyChannel(){
        Log.e("my channel", myChannel + "");

        Backendless.Messaging.subscribe(myChannel,
                new AsyncCallback<List<Message>>() {
                    @Override
                    public void handleResponse(List<Message> messages) {
                        if(messages != null){
                            for (Message message : messages) {
                                Log.e("msg recv", message.getData() + "");
                                String publisherId = message.getPublisherId();

                                for (int i = 0; i < DataStore.userList.size(); i++) {
                                    if (publisherId.equals(DataStore.userList.get(i).getUserObjectId())) {
                                        if(!message.getPublisherId().equals(targetUserObjectId)){
                                            DataStore.userList.get(i).addToMessageList(message);
                                            adapterUserInList.notifyItemChanged(i);
                                        }else{
                                            adapterChatMsgList.getChatHistory().add(message);
                                            adapterChatMsgList.notifyItemInserted(adapterChatMsgList.getItemCount());
                                        }
                                    }
                                }
                            }
                        }

                    }

                    @Override
                    public void handleFault(final BackendlessFault backendlessFault) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ServiceMessageIOCenter.this, "sub message, Error: " + backendlessFault.getDetail(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                },
                new AsyncCallback<Subscription>() {
                    public void handleResponse( Subscription response ) {
                        Subscription subscription = response;
                    }
                    public void handleFault(final BackendlessFault fault ) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("fault", fault.getMessage());
                                Toast.makeText(ServiceMessageIOCenter.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

    @Override
    public boolean onUnbind(Intent intent){
        super.onUnbind(intent);
        if (intent.getBooleanExtra("isChatList", false))
            adapterChatMsgList = null;

        return true;
    }

    public class BinderMessageIO extends Binder{
        public void setMyAdapterUserInList(AdapterUserInList adapterUserInList) {
            setAdapterUserInList(adapterUserInList);
        }

        public void setMyAdapterChatMsgList(AdapterChatMsgList adapterChatMsgList){
            setAdapterChatMsgList(adapterChatMsgList);
        }

        public void subToMyChannel(){
            subscribeToMyChannel();
        }

        public void setTargetPubChannel(String targetUserObjectId){
            setTargetChannel(targetUserObjectId);
        }

        public void pubToTargetChannel(String msgType, String msgContent){
            publishToTargetChannel(msgType, msgContent);
        }

        public void getUserObjectByObjectId(String detectedUserObjectId){
            retrieveDetectedUserObject(detectedUserObjectId);
        }
    }
}
