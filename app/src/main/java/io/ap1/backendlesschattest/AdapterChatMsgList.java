package io.ap1.backendlesschattest;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
    private String otherProfielImageUrl;

    public AdapterChatMsgList(Context context, ArrayList<Message> messages, String myObjectId, String otherObjectId){
        this.context = context;
        chatHistory = messages;
        this.myObjectId = myObjectId;
        this.otherObjectId = otherObjectId;
        myProfileImageUrl = Constants.PROFILE_IMAGE_PATH_ROOT + "/" + myObjectId + ".png";
        otherProfielImageUrl = Constants.PROFILE_IMAGE_PATH_ROOT + "/" + otherObjectId + ".png";
     }

    @Override
    public ViewHolderChatMessage onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_in_list, viewGroup, false);
        return new ViewHolderChatMessage(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderChatMessage newMessage, final int position){
        Message historyMsg = chatHistory.get(position);
        /*
        String userName = (String) userTmp.getProperty("name");
        String userBio = (String) userTmp.getProperty("bio");
        String userColor = "#" + userTmp.getProperty("color");
        Log.e("adpater color", userColor);
        String userPictureURL = (String) userTmp.getProperty("pictureUrl");
        String pictureUrl = Constants.PROFILE_IMAGE_PATH_ROOT + userPictureURL;
        Log.e("picasso", pictureUrl);
        */
        if(historyMsg.getPublisherId().equals(otherObjectId)){
            newMessage.tvSelfPadding.setVisibility(View.GONE);
            Picasso.with(context).load(otherProfielImageUrl).into(newMessage.ivChatUserProfileImage);
        }else
            Picasso.with(context).load(myProfileImageUrl).into(newMessage.ivChatUserProfileImage);

        String userName = historyMsg.getPublisherId();
        String timestamp = MainActivity.timeFormat.format(historyMsg.getTimestamp());
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
}
