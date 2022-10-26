package MoimSearch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.somoim.R;
import com.google.gson.Gson;

import java.util.List;

import Common.DataClass;
import MoimDetail.MoimDetailMain;

public class MoimAdapter extends RecyclerView.Adapter<MoimAdapter.ViewHolder> {

    List<DataClass> moimDataList; //DataClass가 가진 값 = 회원정보 + 모임정보
    Context context;

    public MoimAdapter(List<DataClass> moimDataList, Context context) {
        this.moimDataList = moimDataList;
        this.context = context;
    }
    private static final String TAG = "MoimAdapter";

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Log.i(TAG, "1. (onCreateViewHolder()) 호출");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.moim_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        //Log.i(TAG, "2. (onBindViewHolder()) 호출");
        DataClass moimData = moimDataList.get(position);
        holder.tv_interest.setText("분야: " + moimData.getInterest()); // 모임 카테고리
        holder.tv_address.setText("장소: " + moimData.getAddress()); // 모임 주소
        holder.tv_title.setText("모임명: " + moimData.getTitle()); // 모임 제목
        holder.tv_memberCount.setText("인원: " +  moimData.getJoinedCount() + " / " +  moimData.getMemberCount()); // 모임 정원

        holder.parentLayout.setOnClickListener(new View.OnClickListener() { // 모임 레이아웃 클릭 시, 모임의 세부 화면으로 이동(Activity -> Activity -> Fragment)
            @Override
            public void onClick(View v) {
                //Log.i(TAG, "2. (onBindViewHolder()) " + position+ "번째 모임 정보 아이템 클릭함");
                //Log.i(TAG, "2. (onBindViewHolder()) 1. Intent 객체 생성 - MoimDetailMain(모임 상세 정보)으로 이동");
                Intent intent = new Intent(context, MoimDetailMain.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 2022.09.28.
                //Log.i(TAG, "2. (onBindViewHolder()) 2. intent.putExtra(), key : moimSeq / value : " + moimData.getMoimSeq());
                intent.putExtra("moimSeq", moimData.getMoimSeq()); // 모임의 고유값을 전달 (모임 상세보기)
                intent.putExtra("fromMoimAdapter", true);
                context.startActivity(intent);
                //Log.i(TAG, "2. (onBindViewHolder()) 3. intent로 moimSeq를 전달하면, MoimDetailMain에서 해당 모임 정보를 표시한다 ");
            }
        });

        if(moimData.getMainImage() != null) { // 메인이미지
            Glide.with(holder.itemView.getContext())
                    .load(moimData.getMainImage())
                    .centerCrop()
                    .into(holder.iv_mainImage);
        }

        Gson gson = new Gson();
        String object = gson.toJson(moimData);
        //Log.i(TAG, "3. (onBindViewHolder()) " + position+ "번째 리사이클러뷰 아이템 객체 정보 : " + object);
        //Log.i(TAG, "---(onBindViewHolder()) 값이 정상적으로 들어와있는지 확인 필요 ");
    }

    @Override
    public int getItemCount() {
        return moimDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //viewHolder의 view 안에 글자가 표시되도록 textView 2개를 추가되도록 할 것이다
        CardView parentLayout;
        ImageView iv_mainImage;
        TextView tv_address, tv_title, tv_memberCount, tv_interest;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parentLayout = itemView.findViewById(R.id.parentLayout); // 모임 리사이클러뷰 아이템 전체 레이아웃
            iv_mainImage = itemView.findViewById(R.id.iv_mainImage); // 모임 썸네일 이미지
            tv_address = itemView.findViewById(R.id.tv_address); // 모임 지역
            tv_title = itemView.findViewById(R.id.tv_title); // 모임 타이틀
            tv_memberCount = itemView.findViewById(R.id.tv_memberCount); // 모임 참가자수
            tv_interest = itemView.findViewById(R.id.tv_interest); // 모임의 카테고리(관심분야)
        }
    }
}


