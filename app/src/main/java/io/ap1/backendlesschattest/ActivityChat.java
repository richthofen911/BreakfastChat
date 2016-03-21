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

    private ArrayList<Message> currentDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        etMsgInput = (EditText) findViewById(R.id.et_msg_input);
        btnSend = (Button) findViewById(R.id.btn_send_msg);

        int selectedUserIndex = getIntent().getIntExtra("selectedIndex", 0);
        otherBackendlessUser = DataStore.userList.get(selectedUserIndex);
        targetUserObjectId = otherBackendlessUser.getUserObjectId();
        myUserObjectId = ActivityMain.myUserObjectId;

        currentDataSource = otherBackendlessUser.getMessageList();

        adapterChatMsgList = new AdapterChatMsgList(this, currentDataSource, myUserObjectId, targetUserObjectId);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_chat_msg_list);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterChatMsgList);

        messageIOCenterChatConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binderMessageIO = (ServiceMessageIOCenter.BinderMessageIO) service;
                binderMessageIO.setMyAdapterChatMsgList(adapterChatMsgList);
                Log.e("parsedTargetUserId", otherBackendlessUser.getUserObjectId());
                binderMessageIO.setTargetPubChannel(otherBackendlessUser.getUserObjectId());

                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        binderMessageIO.pubToTargetChannel(Constants.CHAT_MESSAGE_TYPE_NORMAL, etMsgInput.getText().toString());
                        adapterChatMsgList.notifyItemInserted(currentDataSource.size());
                        etMsgInput.setText("");
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e("Service MessageCenter", "Disconnected, name: " + name);

            }
        };

        /*
        Bundle bundle = new Bundle();
        bundle.putBoolean("isChatList", true);
        */
        bindService(new Intent(ActivityChat.this, ServiceMessageIOCenter.class), messageIOCenterChatConn, BIND_AUTO_CREATE);
    }

    public void onDestroy(){
        super.onDestroy();

        DataStore.userList.clear();
        adapterChatMsgList.notifyItemRangeRemoved(0, currentDataSource.size() - 1);

        if(binderMessageIO != null && binderMessageIO.isBinderAlive())
            this.unbindService(messageIOCenterChatConn);

    }

}
