package MoimDetail.Board;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import MoimDetail.MoimDetailMain;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ViewHolder> {

    List<BoardData> boardDataList;
    Context context;
    private static final String TAG = "BoardAdapter";

    public BoardAdapter(List<BoardData> boardDataList, Context context) {
        this.boardDataList = boardDataList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        //게시글 작성자 이름
        holder.tv_name.setText(boardDataList.get(position).getName());
        //게시글 제목
        holder.tv_title.setText("제목 : " + String.valueOf(boardDataList.get(position).getTitle()));
        //게시글 내용
        holder.tv_content.setText("내용\n" + String.valueOf(boardDataList.get(position).getContent()));
        //게시된 날짜
        String dateValue = boardDataList.get(position).getDate();
        String monthOnly = dateValue.substring(5, 7);
        String dateOnly = dateValue.substring(8, 10);
        String showDate = monthOnly + "월 " + dateOnly + "일 ";
        String timeOnly = "";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = null;
        try {
            date = formatter.parse(dateValue);
            SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
            String date2 = formatter2.format(date);
            String amOrPm = date2.substring(20, 22);
            String minAndSecond = date2.substring(11, 16);
            timeOnly = amOrPm + " " + minAndSecond;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String revisedDate = showDate + timeOnly;
        holder.tv_date.setText(revisedDate);



        //게시글 프로필 이미지
        if(boardDataList.get(position).getProfileImage() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(boardDataList.get(position).getProfileImage())
                    .centerCrop()
                    .into(holder.iv_profileImage);
        }

        //게시글 대표 이미지 (등록된 이미지 중에서 최신 것 1개)
        if(boardDataList.get(position).getMainImageUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(boardDataList.get(position).getMainImageUrl())
                    .centerCrop()
                    .into(holder.iv_mainImage);
        }

        //좋아요 버튼 클릭시
        holder.iv_likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //좋아요 숫자
        holder.tv_likeCount.setText("좋아요 " + boardDataList.get(position).getLikeCount());
        //댓글 버튼 클릭시
        //댓글 숫자
        holder.tv_commentCount.setText("댓글 " + boardDataList.get(position).getCommentCount());
        //게시글 카테고리
//        holder.tv_boardCategory.setText(String.valueOf(boardDataList.get(position).getDate()));
        //게시글 작성자 지위
        //if(모임장의 고유값과 게시물 작성자의 고유값이 동일하다면 모임장이라는 텍스트를 보여줌)
        holder.tv_status.setText("모임장");


        //아이템 전체 레이아웃 클릭시
        holder.containerBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "(게시글 아이템 클릭) 1. 클릭함 - BoardView(게시글 상세화면)로 이동");

                Intent intent = new Intent(context, BoardView.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


                intent.putExtra("boardSeq", boardDataList.get(position).getBoardSeq());
                Log.i(TAG, "(게시글 아이템 클릭) 2. intent로 보내는 값, boardSeq : " + boardDataList.get(position).getBoardSeq());
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return boardDataList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        //viewHolder의 view 안에 글자가 표시되도록 textView 2개를 추가되도록 할 것이다
        LinearLayout containerBoard;
        TextView tv_name, tv_status, tv_title, tv_content , tv_date , tv_likeCount, tv_commentCount , tv_boardCategory;
        ImageView iv_profileImage, iv_mainImage, iv_likeIcon, iv_commentIcon;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            containerBoard = itemView.findViewById(R.id.containerBoard); //
            tv_name = itemView.findViewById(R.id.tv_name); //
            tv_status = itemView.findViewById(R.id.tv_status); //
            tv_title = itemView.findViewById(R.id.tv_title); //
            tv_content = itemView.findViewById(R.id.tv_content); //
            tv_date = itemView.findViewById(R.id.tv_date); //
            tv_likeCount = itemView.findViewById(R.id.tv_likeCount); //
            tv_commentCount = itemView.findViewById(R.id.tv_commentCount); //
            tv_boardCategory = itemView.findViewById(R.id.tv_boardCategory); //
            iv_profileImage = itemView.findViewById(R.id.iv_profileImage); //
            iv_mainImage = itemView.findViewById(R.id.iv_mainImage); //
            iv_likeIcon = itemView.findViewById(R.id.iv_likeIcon); //
            iv_commentIcon = itemView.findViewById(R.id.iv_commentIcon); //
        }

    }
}


