package io.ap1.backendlesschattest;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by admin on 18/03/16.
 */
public class ViewHolderChatMessage extends RecyclerView.ViewHolder {

    public TextView tvSelfPadding;
    public TextView tvChatUserName;
    public TextView tvChatMsgTimestamp;
    public ImageView ivChatUserProfileImage;
    public TextView tvChatMsgContent;

    public LinearLayout cellMsgInList;

    public int selfPosition;

    public ViewHolderChatMessage(View rootView){
        super(rootView);

        tvSelfPadding = (TextView) rootView.findViewById(R.id.tv_self_padding);
        tvChatUserName = (TextView) rootView.findViewById(R.id.tv_chat_user_name);
        tvChatMsgTimestamp = (TextView) rootView.findViewById(R.id.tv_chat_timestamp);
        tvChatMsgContent = (TextView) rootView.findViewById(R.id.tv_chat_content);

        ivChatUserProfileImage = (ImageView) rootView.findViewById(R.id.iv_chat_profile_image);

        cellMsgInList = (LinearLayout) rootView.findViewById(R.id.cell_message_in_list);

    }
}
