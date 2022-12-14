package Chat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.somoim.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatData> chatDataList = null;
    Context context;
    private static final String TAG = "ChatAdapter";

    public ChatAdapter(Context context, List<ChatData> chatDataList) {
        this.context = context;
        this.chatDataList = chatDataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(viewType == ViewType.CENTER_JOIN)
        {
            view = inflater.inflate(R.layout.item_continer_center_join, parent, false);
            return new CenterViewHolder(view);
        }
        else if(viewType == ViewType.CENTER_JOIN_CHAT)
        {
            view = inflater.inflate(R.layout.item_continer_join_chat, parent, false);
            return new ChatJoinViewHolder(view);
        }
        else if(viewType == ViewType.LEFT_CHAT)
        {
            view = inflater.inflate(R.layout.item_continer_received_message, parent, false);
            return new LeftViewHolder(view);
        }
        else if(viewType == ViewType.RIGHT_CHAT_UNREAD)
        {
            view = inflater.inflate(R.layout.item_continer_sent_message_unread, parent, false);
            return new RightUnreadViewHolder(view);
        }
        else
        {
            view = inflater.inflate(R.layout.item_continer_sent_message, parent, false);
            return new RightViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position)
    {
        ChatData chatData = chatDataList.get(position);

        // ?????? ????????? ?????? -> ??????
        if(viewHolder instanceof CenterViewHolder) // ?????? ??????
        {
            ((CenterViewHolder) viewHolder).message.setText(chatData.getTime());

        }
        // ?????? ????????? ?????? -> ?????? ??????
        else if(viewHolder instanceof ChatJoinViewHolder)
        {
            ((ChatJoinViewHolder) viewHolder).message.setText(chatData.getMsg());
        }

        // ?????? ???????????? ?????? -> ???????????? ?????? ?????? ?????????
        else if(viewHolder instanceof LeftViewHolder)
        {
            ((LeftViewHolder) viewHolder).nickname.setText(chatData.getUserName());
            ((LeftViewHolder) viewHolder).message.setText(chatData.getMsg());
            ((LeftViewHolder) viewHolder).time.setText(chatData.getTime());
//            ((LeftViewHolder) viewHolder).time.setText(chatDataList.get(position).getTime());


            Glide.with(((LeftViewHolder) viewHolder).itemView.getContext())
                    .load(chatData.getUserProfileImage())
                    .centerCrop()
                    .into(((LeftViewHolder) viewHolder).imageProfile);

        }
        // ?????? ???????????? ?????? -> ????????? ?????? ?????? ?????????
        else if(viewHolder instanceof RightUnreadViewHolder)
        {
            ((RightUnreadViewHolder) viewHolder).message.setText(chatData.getMsg());
            ((RightUnreadViewHolder) viewHolder).time.setText(chatData.getTime());
//            ((RightUnreadViewHolder) viewHolder).readOrNot.setText(chatData.getReadOrNot());
            ((RightUnreadViewHolder) viewHolder).readOrNot.setText("?????????");
        }
        else
        {
            ((RightViewHolder) viewHolder).message.setText(chatData.getMsg());
            ((RightViewHolder) viewHolder).time.setText(chatData.getTime());
            ((RightViewHolder) viewHolder).unread.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    public int getItemCount()
    {
        return chatDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return chatDataList.get(position).getViewType();
    }

    // ?????? ????????? ?????? -> ??????
    public class CenterViewHolder extends RecyclerView.ViewHolder{
        TextView message;

        CenterViewHolder(View itemView)
        {
            super(itemView);

            message = itemView.findViewById(R.id.message);
        }
    }

    // ?????? ???????????? ?????? -> ???????????? ?????? ?????? ??????
    public class LeftViewHolder extends RecyclerView.ViewHolder{
        TextView message;
        TextView nickname;
        TextView time;
        ImageView imageProfile;

        LeftViewHolder(View itemView)
        {
            super(itemView);

            message = itemView.findViewById(R.id.message);
            nickname = itemView.findViewById(R.id.nickname);
            time = itemView.findViewById(R.id.time);
            imageProfile = itemView.findViewById(R.id.imageProfile);

        }
    }

    // ?????? ???????????? ?????? -> ????????? ?????? ?????? ?????????
    public class RightViewHolder extends RecyclerView.ViewHolder{
        TextView message;
        TextView time;
        TextView unread;

        RightViewHolder(View itemView)
        {
            super(itemView);

            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
            unread = itemView.findViewById(R.id.unread);
        }
    }

    public class RightUnreadViewHolder extends RecyclerView.ViewHolder{
        TextView message;
        TextView time;
        TextView readOrNot;

        RightUnreadViewHolder(View itemView)
        {
            super(itemView);

            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
            readOrNot = itemView.findViewById(R.id.readOrNot);
        }
    }


    // ?????? ????????? ?????? -> ??????
    public class ChatJoinViewHolder extends RecyclerView.ViewHolder{
        TextView message;

        ChatJoinViewHolder(View itemView)
        {
            super(itemView);

            message = itemView.findViewById(R.id.message);
        }
    }

}