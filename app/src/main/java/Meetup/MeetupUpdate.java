package Meetup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.somoim.R;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.HashMap;

import Common.NetworkClient;
import Common.UploadApis;
import MoimDetail.MoimDetailMain;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class MeetupUpdate extends AppCompatActivity {

    EditText et_meetupTitle, et_address, et_fee, et_memberCount;
    TextView tv_meetupDate, tv_meetupTime;
    Button btn_save;
    ImageView iv_back;

    // 레트로핏 통신 공통 변수(클래스 연결)
    NetworkClient networkClient;
    UploadApis uploadApis;

    private static final String TAG = "MeetupUpdate(정모 수정)";

    DatePickerDialog.OnDateSetListener setListener;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_update);

        Log.i(TAG, "(onCreate()) 1. 호출");

        // UI 연결
        et_meetupTitle = findViewById(R.id.et_meetupTitle);
        et_address = findViewById(R.id.et_address);
        et_fee = findViewById(R.id.et_fee);
        et_memberCount = findViewById(R.id.et_memberCount);
        tv_meetupDate = findViewById(R.id.tv_meetupDate);
        tv_meetupTime = findViewById(R.id.tv_meetupTime);
        btn_save = findViewById(R.id.btn_save);
        iv_back = findViewById(R.id.iv_back);

        // sharedPreference 세팅
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();

        Intent intent = getIntent();
        String moimSeq = intent.getStringExtra("moimSeq");
        Log.i(TAG, "(onCreate()) 3. moimSeq : " + moimSeq);
        String meetupTitle = intent.getStringExtra("meetupTitle");
        Log.i(TAG, "(onCreate()) 3. meetupTitle : " + meetupTitle);
        String meetupDate = intent.getStringExtra("meetupDate");
        Log.i(TAG, "(onCreate()) 3. meetupDate : " + meetupDate);
        String meetupTime = intent.getStringExtra("meetupTime");
        Log.i(TAG, "(onCreate()) 3. meetupTime : " + meetupTime);
        String address = intent.getStringExtra("address");
        Log.i(TAG, "(onCreate()) 3. address : " + address);
        String fee = intent.getStringExtra("fee");
        Log.i(TAG, "(onCreate()) 3. fee : " + fee);
        String meetupSeq = intent.getStringExtra("meetupSeq");
        Log.i(TAG, "(onCreate()) 3. meetupSeq : " + meetupSeq);

        et_meetupTitle.setText(meetupTitle);
        tv_meetupDate.setText(meetupDate);
        tv_meetupTime.setText(meetupTime);
        et_address.setText(address);
        et_fee.setText(fee);


        Calendar calendar = Calendar.getInstance();
//        final int year = calendar.get(Calendar.YEAR);
//        final int month = calendar.get(Calendar.MONTH);
//        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);


//        캘린더 선택형
        tv_meetupDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        MeetupUpdate.this, new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month + 1;
//                        String date = day+"/"+month + "/" + year;
                        String date = year + "년 " + month + "월 " + day + "일";
                        tv_meetupDate.setText(date);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        tv_meetupTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        MeetupUpdate.this, android.R.style.Theme_Holo_Light_Dialog, new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        if (hourOfDay > 12) {
                            hourOfDay -= 12;
                            tv_meetupTime.setText("오후 " + hourOfDay + ":" + String.format("%02d", minute));
                        } else {
                            tv_meetupTime.setText("오전 " + hourOfDay + ":" + String.format("%02d", minute));
                        }


//                        String time = hourOfDay + "시 " + minute + "분";
//                        tv_meetupTime.setText(time);
                    }
                }, 0, 0, false);
                timePickerDialog.show();
            }
        });


        // 수정완료 버튼 클릭
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "(정모수정완료 버튼) 1. 클릭함");

                String meetupTitle = et_meetupTitle.getText().toString();
                String address = et_address.getText().toString();
                String fee = et_fee.getText().toString();
                String meetupDate = tv_meetupDate.getText().toString();
                String meetupTime = tv_meetupTime.getText().toString();
                String userSeq = sp.getString("userSeq","");


                // 서버로 보낼 값 (onCreate 안에 선언)
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("meetupTitle", meetupTitle);
                hashmap.put("meetupDate", meetupDate);
                hashmap.put("meetupTime", meetupTime);
                hashmap.put("address", address);
                hashmap.put("fee", fee);
                hashmap.put("userSeq", userSeq);
                hashmap.put("moimSeq", moimSeq);
                hashmap.put("meetupSeq", meetupSeq);
                Log.i(TAG, "(정모수정완료 버튼) 2. 서버로 보낼 hashmap 객체 생성, hashmap : " + String.valueOf(hashmap));
                Log.i(TAG, "(정모수정완료 버튼) 3. updateMeetup(hashmap) 호출");
                updateMeetup(hashmap);


            }
        });

    }

    // HTTP통신 매서드 1. meetup 테이블 특정 행 업데이트
    // (통신방식) Retrofit
    // (클라이언트→서버) meetupTitle, address, fee, memberCount, meetupDate, meetupTime, userSeq, moimSeq
    // (서버→클라이언트) MeetupData 객체의 값(success, moimSeq)
    private void updateMeetup(HashMap<String, String> hashmap) {
        Log.i(TAG, "(createMeetup) 1. 호출 - meetup 테이블에 새로운 행 추가 (정모 저장)");

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("meetupTitle", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("meetupTitle")));
        Log.i(TAG, "(createMeetup) 2. 서버로 보내는 값, hashmap.get(\"meetupTitle\") : " + hashmap.get("meetupTitle"));
        textToServer.put("address", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("address")));
        Log.i(TAG, "(createMeetup) 2. 서버로 보내는 값, hashmap.get(\"address\") : " + hashmap.get("address"));
        textToServer.put("fee", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("fee")));
        Log.i(TAG, "(createMeetup) 2. 서버로 보내는 값, hashmap.get(\"fee\") : " + hashmap.get("fee"));
        textToServer.put("meetupDate", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("meetupDate")));
        Log.i(TAG, "(createMeetup) 2. 서버로 보내는 값, hashmap.get(\"meetupDate\") : " + hashmap.get("meetupDate"));
        textToServer.put("meetupTime", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("meetupTime")));
        Log.i(TAG, "(createMeetup) 2. 서버로 보내는 값, hashmap.get(\"meetupTime\") : " + hashmap.get("meetupTime"));
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        Log.i(TAG, "(createMeetup) 2. 서버로 보내는 값, hashmap.get(\"userSeq\") : " + hashmap.get("userSeq"));
        textToServer.put("moimSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("moimSeq")));
        Log.i(TAG, "(createMeetup) 2. 서버로 보내는 값, hashmap.get(\"moimSeq\") : " + hashmap.get("moimSeq"));
        textToServer.put("meetupSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("meetupSeq")));
        Log.i(TAG, "(createMeetup) 2. 서버로 보내는 값, hashmap.get(\"meetupSeq\") : " + hashmap.get("meetupSeq"));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<MeetupData> call = uploadApis.updateMeetup(textToServer);
        call.enqueue(new Callback<MeetupData>() {
            @Override
            public void onResponse(Call<MeetupData> call, retrofit2.Response<MeetupData> response) {
                Log.i(TAG, "(createMeetup) 3. 서버에서 받은 응답, response : " + response);

                MeetupData responseBody = response.body();
                String success = responseBody.getSuccess();
                String meetupSeq = responseBody.getMeetupSeq();

                Gson gson = new Gson();
                String object = gson.toJson(responseBody);
                Log.i(TAG, "(createMeetup) 3. 서버에서 받은 응답, object : " + object);

                if(success.equals("1")){
                    Log.i(TAG, "(createMeetup) 4. 정모 행 추가에 성공한 경우");
                    Toast.makeText(MeetupUpdate.this, "정모를 개설했습니다", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "(createMeetup) 5. 현재 액티비티 종료(finish)");
                    finish();
                    Log.i(TAG, "(createMeetup) 6. MoimDetailMain 화면 이동");
                    Intent intent = new Intent(getApplicationContext(), MeetupView.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("meetupSeq", meetupSeq); // 모임의 고유값을 전달 (모임 상세보기)
                    intent.putExtra("meetupDate", hashmap.get("meetupDate")); //
                    intent.putExtra("moimSeq", hashmap.get("moimSeq")); //
                    intent.putExtra("meetupSeq", hashmap.get("meetupSeq")); //
                    intent.putExtra("meetupTime", hashmap.get("meetupTime")); //
                    intent.putExtra("address", hashmap.get("address")); //
                    intent.putExtra("fee", hashmap.get("fee")); //
                    intent.putExtra("meetupTitle", hashmap.get("meetupTitle")); //

                    startActivity(intent);





                }
            }

            @Override
            public void onFailure(Call<MeetupData> call, Throwable t) {
                Log.i(TAG, "(createMeetup) 통신에 실패한 경우, t : " + t);
            }
        });
    }

}