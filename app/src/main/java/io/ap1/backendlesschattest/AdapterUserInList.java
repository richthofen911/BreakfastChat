package io.ap1.backendlesschattest;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.backendless.BackendlessUser;
import com.squareup.picasso.Picasso;

/**
 * Created by admin on 17/03/16.
 */
public class AdapterUserInList extends RecyclerView.Adapter<ViewHolderUserInList>{
    private Context context;

    public AdapterUserInList(Context context){
        this.context = context;
    }

    @Override
    public ViewHolderUserInList onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_in_list, viewGroup, false);
        return new ViewHolderUserInList(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderUserInList detectedUser, final int position){
        MyBackendlessUser userTmp = DataStore.userList.get(position);
        /*
        String userName = (String) userTmp.getProperty("name");
        String userBio = (String) userTmp.getProperty("bio");
        String userColor = "#" + userTmp.getProperty("color");
        Log.e("adpater color", userColor);
        String userPictureURL = (String) userTmp.getProperty("pictureUrl");
        String pictureUrl = Constants.PROFILE_IMAGE_PATH_ROOT + userPictureURL;
        Log.e("picasso", pictureUrl);
        */
        String userName = userTmp.getName();
        String userBio = userTmp.getBio();
        String userColor = userTmp.getColor();
        String userPictureUrl = Constants.PROFILE_IMAGE_PATH_ROOT + userTmp.getPictureUrl();
        detectedUser.tvDetectedUserColor.setBackgroundColor(Color.parseColor(userColor));
        if(userTmp.getMessageList().size() == 0)
            detectedUser.tvDetectedUserMsgNotify.setVisibility(View.GONE);
        else{
            int newSize = userTmp.getMessageList().size();
            detectedUser.tvDetectedUserMsgNotify.setVisibility(View.VISIBLE);
            detectedUser.tvDetectedUserMsgNotify.setText(Integer.toString(newSize));
        }

        Picasso.with(context).load(userPictureUrl).into(detectedUser.ivDetectedUserProfileImage);
        detectedUser.tvDetectedUserName.setText(userName);
        detectedUser.tvDetectedUserBio.setText(userBio);

        detectedUser.selfPosition = position;
    }

    @Override
    public int getItemCount() {
        return DataStore.userList.size();
    }
}
