package io.ap1.backendlesschattest;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.backendless.BackendlessUser;
import com.backendless.messaging.Message;

import java.util.ArrayList;

public class ActivityChat extends AppCompatActivity {

    private String myUserObjectId;
    private String targetUserObjectId;
    private MyBackendlessUser otherBackendlessUser;

    public AdapterChatMsgList adapterChatMsgList;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;

    private ServiceMessageIOCenter.BinderMessageIO binderMessageIO;

    private ServiceConnection messageIOCenterChatConn;

    public EditText etMsgInput;
    public Button btnSend;

    private ArrayList<Message> currentChatHistoryDataSource;

    private String myChannel;
    private String otherChannel;

    private BackendlessUser myUserObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_chat_msg_list);
        etMsgInput = (EditText) findViewById(R.id.et_msg_input);
        btnSend = (Button) findViewById(R.id.btn_send_msg);

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        int selectedUserIndex = getIntent().getIntExtra("selectedIndex", 0);
        otherBackendlessUser = DataStore.userList.get(selectedUserIndex);
        targetUserObjectId = otherBackendlessUser.getUserObjectId();
        otherBackendlessUser.getProfileImage();
        //myUserObjectId = ActivityMain.myUserObjectId;

        currentChatHistoryDataSource = otherBackendlessUser.getUnreadMessageList();
        if(currentChatHistoryDataSource == null)
            currentChatHistoryDataSource = new ArrayList<>();

        messageIOCenterChatConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binderMessageIO = (ServiceMessageIOCenter.BinderMessageIO) service;

                myUserObject = binderMessageIO.getMyUserObject();
                binderMessageIO.setRecyclerView(recyclerView);

                myUserObjectId = myUserObject.getObjectId();
                myChannel = binderMessageIO.getMyChannel();

                adapterChatMsgList = new AdapterChatMsgList(ActivityChat.this, currentChatHistoryDataSource, myUserObjectId, targetUserObjectId, (String) myUserObject.getProperty("profileImage"), otherBackendlessUser.getProfileImage());
                recyclerView.setAdapter(adapterChatMsgList);

                binderMessageIO.setMyAdapterChatMsgList(adapterChatMsgList);
                binderMessageIO.setTargetPubChannel(otherBackendlessUser.getUserObjectId());
                // this method cannot be called before setTargetChannel, or it will get null or wrong result
                otherChannel = binderMessageIO.getTargetChannel();

                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        binderMessageIO.pubToChannel(otherChannel, Constants.CHAT_MESSAGE_TYPE_NORMAL, etMsgInput.getText().toString());
                        binderMessageIO.pubToChannel(myChannel, Constants.CHAT_MESSAGE_TYPE_NORMAL, etMsgInput.getText().toString());

                        etMsgInput.setText("");
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e("Service MessageCenter", "Disconnected, name: " + name);

            }
        };

        bindService(new Intent(ActivityChat.this, ServiceMessageIOCenter.class), messageIOCenterChatConn, BIND_AUTO_CREATE);
    }

    public void onDestroy() {
        super.onDestroy();

        binderMessageIO.setTargetPubChannel(null); // clear targetPublishChannel
        otherBackendlessUser.clearMessageList();
        adapterChatMsgList.notifyItemRangeRemoved(0, 0);

        if(binderMessageIO != null && binderMessageIO.isBinderAlive())
            this.unbindService(messageIOCenterChatConn);

    }

}
