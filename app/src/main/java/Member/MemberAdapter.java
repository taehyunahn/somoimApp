package Member;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.somoim.R;

import java.util.List;

import Meetup.MeetupData;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    List<MemberData> memberDataList;
    Context context;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private static final String TAG = "MemberAdapter";


    public MemberAdapter(List<MemberData> memberDataList, Context context) {
        this.memberDataList = memberDataList;
        this.context = context;
        //주석
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        MemberData memberData = memberDataList.get(position);

        Log.i(TAG, "onBindViewHolder에서 프로필주소 = " + memberDataList.get(position).getUserProfileImage());

        if(memberDataList.get(position).getUserProfileImage() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(memberData.getUserProfileImage())
                    .centerCrop()
                    .into(holder.iv_profileImage);
        }

        if(memberData.getUserSeq().equals(memberData.getLeaderSeq())){
            // 모임장인 경우
            holder.tv_name.setText(memberData.getUserName() + "   ☆모임장");
        } else {
            // 일반 멤버인 경우
            holder.tv_name.setText(memberData.getUserName());
        }

        holder.tv_intro.setText(memberData.getUserIntro());


    }

    @Override
    public int getItemCount() {
        return memberDataList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        //viewHolder의 view 안에 글자가 표시되도록 textView 2개를 추가되도록 할 것이다

        ImageView iv_profileImage;
        TextView tv_name, tv_intro, tv_status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_profileImage = itemView.findViewById(R.id.iv_profileImage); //
            tv_name = itemView.findViewById(R.id.tv_name); //
            tv_intro = itemView.findViewById(R.id.tv_intro); //
            tv_status = itemView.findViewById(R.id.tv_status); //

        }
    }
}


