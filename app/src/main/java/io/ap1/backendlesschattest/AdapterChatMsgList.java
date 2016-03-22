package io.ap1.backendlesschattest;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.backendless.messaging.Message;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by admin on 18/03/16.
 */
public class AdapterChatMsgList extends RecyclerView.Adapter<ViewHolderChatMessage>{
    private Context context;
    private ArrayList<Message> chatHistory;
    private String myObjectId;
    private String otherObjectId;// the person you are talking to
    private String myProfileImageUrl;
    private String otherProfileImageUrl;

    public AdapterChatMsgList(Context context, ArrayList<Message> messages, String myObjectId, String otherObjectId, String myProfileImageName, String otherProfileImageName){
        this.context = context;
        chatHistory = messages;
        this.myObjectId = myObjectId;
        this.otherObjectId = otherObjectId;
        myProfileImageUrl = Constants.PROFILE_IMAGE_PATH_ROOT + myProfileImageName;
        otherProfileImageUrl = Constants.PROFILE_IMAGE_PATH_ROOT + otherProfileImageName;
     }

    @Override
    public ViewHolderChatMessage onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_in_list, viewGroup, false);
        return new ViewHolderChatMessage(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderChatMessage newMessage, final int position){
        Message historyMsg = chatHistory.get(position);

        Log.e("myUrl", myProfileImageUrl);
        Log.e("otherUrl", otherProfileImageUrl);


        // the other one's message aligns left, mine align right and has background color
        if(historyMsg.getPublisherId().equals(otherObjectId)){
            newMessage.tvSelfPadding.setVisibility(View.GONE);
            Picasso.with(context).load(otherProfileImageUrl).into(newMessage.ivChatUserProfileImage);
        }else{
            Picasso.with(context).load(myProfileImageUrl).into(newMessage.ivChatUserProfileImage);
            newMessage.tvChatMsgContent.setBackground(context.getResources().getDrawable(R.drawable.textview_round_corner));
        }


        String userName = historyMsg.getHeaders().get("name");
        String timestamp = ActivityMain.timeFormat.format(historyMsg.getTimestamp());
        String content = (String) historyMsg.getData();

        newMessage.tvChatUserName.setText(userName);
        newMessage.tvChatMsgTimestamp.setText(timestamp);
        newMessage.tvChatMsgContent.setText(content);

        newMessage.selfPosition = position;
    }

    @Override
    public int getItemCount() {
        return chatHistory.size();
    }

    public ArrayList<Message> getChatHistory(){
        return chatHistory;
    }
}
