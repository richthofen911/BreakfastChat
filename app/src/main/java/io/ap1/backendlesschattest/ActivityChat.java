package io.ap1.backendlesschattest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class ActivityChat extends AppCompatActivity {

    private String userObjectIdMe;
    private String userObjectIdOther;
    private MyBackendlessUser otherBackendlessUser;

    public AdapterChatMsgList adapterChatMsgList;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        int selectedUserIndex = getIntent().getIntExtra("selectedIndex", 0);
        otherBackendlessUser = DataStore.userList.get(selectedUserIndex);

        adapterChatMsgList = new AdapterChatMsgList(this, otherBackendlessUser.getMessageList(), userObjectIdMe, userObjectIdOther);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_chat_msg_list);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterChatMsgList);
    }
}
