package io.ap1.backendlesschattest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.awt.font.TextAttribute;

/**
 * Created by admin on 17/03/16.
 */
public class ViewHolderUserInList extends RecyclerView.ViewHolder{

    public TextView tvDetectedUserColor;
    public TextView tvDetectedUserMsgNotify;
    public ImageView ivDetectedUserProfileImage;
    public TextView tvDetectedUserName;
    public TextView tvDetectedUserBio;

    public RelativeLayout cellUserInList;

    public int selfPosition;

    public ViewHolderUserInList(View rootView){
        super(rootView);

        tvDetectedUserColor = (TextView) rootView.findViewById(R.id.tv_detected_user_color);
        tvDetectedUserMsgNotify = (TextView) rootView.findViewById(R.id.tv_detected_user_msg_notify);
        tvDetectedUserName = (TextView) rootView.findViewById(R.id.tv_detected_user_name);
        tvDetectedUserBio = (TextView) rootView.findViewById(R.id.tv_detected_user_bio);

        ivDetectedUserProfileImage = (ImageView) rootView.findViewById(R.id.iv_detected_profile_image);

        cellUserInList = (RelativeLayout) rootView.findViewById(R.id.cell_user_in_list);

        cellUserInList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDetectedUserMsgNotify.setText("");
                tvDetectedUserMsgNotify.setVisibility(View.GONE);
                Context context = v.getContext();
                Intent oneOnOneChat = new Intent(context, ActivityChat.class).putExtra("selectedIndex", selfPosition);
                context.startActivity(oneOnOneChat);
            }
        });
    }
}
