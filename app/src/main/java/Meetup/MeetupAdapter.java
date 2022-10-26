package Meetup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;

import java.util.List;

import Common.DataClass;
import Common.NetworkClient;
import Common.UploadApis;
import Member.MemberData;
import MoimDetail.MoimDetailMain;
import retrofit2.Call;
import retrofit2.Callback;

public class MeetupAdapter extends RecyclerView.Adapter<MeetupAdapter.ViewHolder> {
    // 레트로핏 통신 공통 변수(클래스 연결)
    NetworkClient networkClient;
    UploadApis uploadApis;

    List<MeetupData> meetupDataList;
    Context context;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private static final String TAG = "MeetupAdapter(정모 어댑터)";


    public MeetupAdapter(List<MeetupData> meetupDataList, Context context) {
        this.meetupDataList = meetupDataList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meetup_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.tv_meetupDate.setText(meetupDataList.get(position).getMeetupDate());
        holder.tv_meetupTime.setText(meetupDataList.get(position).getMeetupTime());
        holder.tv_address.setText(meetupDataList.get(position).getAddress());
        holder.tv_fee.setText(meetupDataList.get(position).getFee() + "원");
        holder.tv_meetupTitle.setText(meetupDataList.get(position).getMeetupTitle());

        sp = context.getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        String currentMeetupSeq = meetupDataList.get(position).getMeetupSeq();
        String currentMoimSeq = meetupDataList.get(position).getMoimSeq();
        Boolean clickedOrNot = sp.getBoolean(currentMeetupSeq + userSeq, false);

        Log.i(TAG, "currentMeetupSeq = " + currentMeetupSeq);
        Log.i(TAG, "currentMoimSeq = " + currentMoimSeq);

        if(clickedOrNot){
            holder.btn_join.setVisibility(View.GONE);
            holder.btn_out.setVisibility(View.VISIBLE);
        }

//        //모임 가입여부를 판단해서, 가입 안되어있으면 버튼 비활성화
//        memberCheck(currentMoimSeq, userSeq, currentMeetupSeq);


        // 레이아웃 클릭시
        holder.container_meetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MeetupView.class);
                intent.putExtra("moimSeq", currentMoimSeq);
                intent.putExtra("meetupSeq", currentMeetupSeq);
                intent.putExtra("meetupDate", meetupDataList.get(position).getMeetupDate());
                intent.putExtra("meetupTime", meetupDataList.get(position).getMeetupTime());
                intent.putExtra("address", meetupDataList.get(position).getAddress());
                intent.putExtra("fee", meetupDataList.get(position).getFee());
                intent.putExtra("meetupTitle", meetupDataList.get(position).getMeetupTitle());
                context.startActivity(intent);
            }
        });


        // 참석 버튼 클릭시
        holder.btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder ad = new AlertDialog.Builder(context); // context부분에 getAplicationContext를 쓰면 안된다! 기억할 것
                ad.setMessage("정모에 참석하시겠습니까?");
                ad.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                ad.setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // 내가 클릭한 버튼이 속해있는 meetup의 고유값을 사용한다
                        // 어떤 이름으로 저장해야 할까?
                        // meetup 고유값을 key로 boolean 값을 저장하면 어떨까?


                        editor.putBoolean(currentMeetupSeq + userSeq , true); // 어떤 meetup 아이템이 클릭되어있는지 저장
                        editor.commit();

                        holder.btn_join.setVisibility(View.GONE);
                        holder.btn_out.setVisibility(View.VISIBLE);

                        joinMeetup(currentMoimSeq, userSeq, currentMeetupSeq);
                        Toast.makeText(context, "정모 참석을 신청하셨습니다", Toast.LENGTH_SHORT).show();


                    }
                });
                ad.show();


            }
        });

        holder.btn_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder ad = new AlertDialog.Builder(context); // context부분에 getAplicationContext를 쓰면 안된다! 기억할 것
                ad.setMessage("정모에 참석을 취소하시겠습니까?");
                ad.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                ad.setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        editor.remove(currentMeetupSeq + userSeq);
                        editor.commit();
                        holder.btn_join.setVisibility(View.VISIBLE);
                        holder.btn_out.setVisibility(View.GONE);

                        outMoim(currentMoimSeq, userSeq, currentMeetupSeq);
                        Toast.makeText(context, "정모 참석을 취소하셨습니다", Toast.LENGTH_SHORT).show();

                    }
                });
                ad.show();






            }
        });



    }

    @Override
    public int getItemCount() {
        return meetupDataList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        //viewHolder의 view 안에 글자가 표시되도록 textView 2개를 추가되도록 할 것이다

        LinearLayout container_meetup;
        Button btn_join, btn_out;
        TextView tv_meetupDate, tv_meetupTime, tv_address, tv_fee, tv_meetupTitle ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btn_out = itemView.findViewById(R.id.btn_out); //
            btn_join = itemView.findViewById(R.id.btn_join); //
            tv_meetupDate = itemView.findViewById(R.id.tv_meetupDate); //
            tv_address = itemView.findViewById(R.id.tv_address); //
            tv_meetupTime = itemView.findViewById(R.id.tv_meetupTime); //
            tv_fee = itemView.findViewById(R.id.tv_fee); //
            container_meetup = itemView.findViewById(R.id.container_meetup); //
            tv_meetupTitle = itemView.findViewById(R.id.tv_meetupTitle);

        }
    }


    // HTTP통신 매서드 1. 정모에 참여
    // (통신방식) Retrofit
    // (클라이언트→서버) moimSeq, userSeq, meetupSeq
    // (서버→클라이언트) MemberData 객체의 값()

    private void joinMeetup(String moimSeq, String userSeq, String meetupSeq) {
        Log.i(TAG, "");

        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Call<MemberData> call = uploadApis.joinMeetup(moimSeq, userSeq, meetupSeq);
        call.enqueue(new Callback<MemberData>() {
            @Override
            public void onResponse(Call<MemberData> call, retrofit2.Response<MemberData> response) {
                Log.d("onResponse", response.body().toString());

                if(!response.isSuccessful()){
                    return;
                }
            }

            @Override
            public void onFailure(Call<MemberData> call, Throwable t) {
                Log.i(TAG, "onFailure = " + t);
            }
        });
    }

    // 모임 탈퇴 메서드
    private void outMoim(String moimSeq, String userSeq, String meetupSeq) {

        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Call<MemberData> call = uploadApis.outMeetup(moimSeq, userSeq, meetupSeq);
        call.enqueue(new Callback<MemberData>() {
            @Override
            public void onResponse(Call<MemberData> call, retrofit2.Response<MemberData> response) {
                Log.d("onResponse", response.body().toString());

                if(!response.isSuccessful()){
                    return;
                }
            }

            @Override
            public void onFailure(Call<MemberData> call, Throwable t) {
                Log.i(TAG, "onFailure = " + t);
            }
        });
    }

    // 모임 가입 여부 확인
    private void memberCheck(String moimSeq, String userSeq, String meetupSeq) {
        Log.i(TAG, "(memberCheck) 1. 호출 - HTTP 통신, 현재 모임의 멤버인지 확인");
        Log.i(TAG, "(memberCheck) 2. 서버로 보내는 값, moimSeq : " + moimSeq + " / userSeq : " + userSeq + " / meetupSeq : " + meetupSeq);
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Call<MemberData> call = uploadApis.memberCheck(moimSeq, userSeq, meetupSeq);
        call.enqueue(new Callback<MemberData>() {
            @Override
            public void onResponse(Call<MemberData> call, retrofit2.Response<MemberData> response) {
                Log.d("onResponse", response.body().toString());
                if(!response.isSuccessful()){
                    return;
                }
            }
            @Override
            public void onFailure(Call<MemberData> call, Throwable t) {
                Log.i(TAG, "onFailure = " + t);
            }
        });
    }
}




