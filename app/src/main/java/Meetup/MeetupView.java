package Meetup;

import static Common.StaticVariable.serverAddress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.somoim.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Common.NetworkClient;
import Common.UploadApis;
import Member.MemberAdapter;
import Member.MemberData;
import MoimCreate.MoimUpdate;
import MoimDetail.MoimDetailMain;
import retrofit2.Call;
import retrofit2.Callback;

public class MeetupView extends AppCompatActivity {

    TextView tv_meetupTitle, tv_meetupDate, tv_meetupTime, tv_address, tv_fee, tv_memberCount;
    private RecyclerView member_rv;
    private RecyclerView.Adapter member_adapter;
    private RecyclerView.LayoutManager member_layoutManager;
    List<MemberData> memberDataList = new ArrayList<>();

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    // 레트로핏 통신 공통
    NetworkClient networkClient;
    UploadApis uploadApis;

    private static final String TAG = "MeetupView(정모 상세보기)";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_view);
        Log.i(TAG, "(onCreate()) 1. 호출");

        // UI 연결
        tv_meetupTitle = findViewById(R.id.tv_meetupTitle);
        tv_meetupDate = findViewById(R.id.tv_meetupDate);
        tv_meetupTime = findViewById(R.id.tv_meetupTime);
        tv_address = findViewById(R.id.tv_address);
        tv_fee = findViewById(R.id.tv_fee);
        tv_memberCount = findViewById(R.id.tv_memberCount);
        member_rv = findViewById(R.id.member_rv);

        // member 리사이클러뷰 세팅
        member_rv.setHasFixedSize(true);
        member_layoutManager = new LinearLayoutManager(getApplicationContext());
        member_rv.setLayoutManager(member_layoutManager);
        member_adapter = new MemberAdapter(memberDataList, getApplicationContext());
        member_rv.setAdapter(member_adapter);

//        String userSeq = sp.getString("userSeq", "");
//        Log.i(TAG, "(onCreate()) 2. sharedPreference(login) 값, userSeq : " + userSeq);
        Log.i(TAG, "(onCreate()) 3. intent 전달받은 값으로 화면 값 넣기");

        Intent intent = getIntent();
        String moimSeq = intent.getStringExtra("moimSeq");
        Log.i(TAG, "(onCreate()) 3. moimSeq : " + moimSeq);
        String meetupSeq = intent.getStringExtra("meetupSeq");
        Log.i(TAG, "(onCreate()) 3. meetupSeq : " + meetupSeq);
        String meetupDate = intent.getStringExtra("meetupDate");
        Log.i(TAG, "(onCreate()) 3. meetupDate : " + meetupDate);
        String meetupTime = intent.getStringExtra("meetupTime");
        Log.i(TAG, "(onCreate()) 3. meetupTime : " + meetupTime);
        String address = intent.getStringExtra("address");
        Log.i(TAG, "(onCreate()) 3. address : " + address);
        String fee = intent.getStringExtra("fee");
        Log.i(TAG, "(onCreate()) 3. fee : " + fee);
        String meetupTitle = intent.getStringExtra("meetupTitle");
        Log.i(TAG, "(onCreate()) 3. meetupTitle : " + meetupTitle);


        tv_meetupTitle.setText(meetupTitle);
        tv_meetupDate.setText(meetupDate);
        tv_meetupTime.setText(meetupTime);
        tv_address.setText(address);
        tv_fee.setText(fee);

        Log.i(TAG, "(onCreate()) 4. meetupMemberList(moimSeq, meetupSeq) 호출 - 정모 참석 희망하는 멤버 목록 요청" );
        // 멤버 목록 보여주기
        meetupMemberList(moimSeq, meetupSeq);

        // 더보기 버튼 -> 게시글 수정 또는 삭제 메뉴
        ImageView iv_more = (ImageView) findViewById(R.id.iv_more);
        iv_more = findViewById(R.id.iv_more);
        iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "더보기 버튼 클릭");

                // PopupMenu 사용방법 (a Menu in XML를 defining해야 한다)
                // 1. Instantiate a PopupMenu with its constructor, which takes the current application Context and the View to which the menu should be anchored.
                PopupMenu popup = new PopupMenu(getApplicationContext(), v);
                // 2. Use MenuInflater to inflate your menu resource into the Menu object returned by PopupMenu.getMenu().
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_meetup, popup.getMenu());
                // + Handling click events
                // To perform an action when the user selects a menu item,
                // you must implement the PopupMenu.OnMenuItemClickListener interface and register it with your PopupMenu by calling setOnMenuItemclickListener().
                // When the user selects an item, the system calls the onMenuItemClick() callback in your interface.
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.editMeetup){
                            Log.i(TAG, "모임 수정 클릭");
                            Intent intent = new Intent(getApplicationContext(), MeetupUpdate.class);
                            intent.putExtra("moimSeq", moimSeq);
                            intent.putExtra("meetupTitle", meetupTitle);
                            intent.putExtra("meetupDate", meetupDate);
                            intent.putExtra("meetupTime", meetupTime);
                            intent.putExtra("address", address);
                            intent.putExtra("fee", fee);
                            intent.putExtra("meetupSeq", meetupSeq);


                            startActivity(intent);
                        } else if(item.getItemId() == R.id.deleteMeetup){
                            Log.i(TAG, "(모임 삭제 클릭) 1. 클릭함");
                            Log.i(TAG, "(모임 삭제 클릭) 2. deleteMeetup(meetupSeq) 호출 - 정모 삭제");

                            deleteMeetup(meetupSeq);


                        }
                        return false;
                    }
                });

                // 3. Call PopupMenu.show().
                popup.show();

            }
        });

        SwipeRefreshLayout swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                memberDataList.clear();

                meetupMemberList(moimSeq, meetupSeq);


                /* 업데이트가 끝났음을 알림 */
                swiperefresh.setRefreshing(false);
            }
        });




    }


    // HTTP통신 매서드 1. 정모 참석 희망하는 멤버 목록 요청
    // (통신방식) Retrofit
    // (클라이언트→서버) moimSeq, meetupSeq
    // (서버→클라이언트) MemberData 객체의 값(leaderSeq, address, title, mainImage, content, memberCount)

    private void meetupMemberList(String moimSeq, String meetupSeq) {

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<List<MemberData>> call = uploadApis.meetupMemberList(moimSeq, meetupSeq);
        call.enqueue(new Callback<List<MemberData>>() {
            @Override
            public void onResponse(Call<List<MemberData>> call, retrofit2.Response<List<MemberData>> response) {
                List<MemberData> responseBody = response.body();
                for(int i = 0; i < responseBody.size(); i++) {
                    memberDataList.add(responseBody.get(i));
                }
                member_adapter = new MemberAdapter(memberDataList, getApplicationContext());
                member_rv.setAdapter(member_adapter);
                member_adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<MemberData>> call, Throwable t) {
            }
        });
    }



    // HTTP통신 매서드 2. 정모 삭제 - meetup 테이블에서 meetupSeq 해당하는 행 삭제
    // (통신방식) volley
    // (클라이언트→서버) meetupSeq
    // (서버→클라이언트) message

    private void deleteMeetup(String meetupSeq){
        Log.i(TAG, "(deleteMeetup) 1. 호출");

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = serverAddress + "somoim/meetup/deleteMeetup.php";
        Log.i(TAG, "(deleteMeetup) 2. HTTP 통신 url : " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "(deleteMeetup) 4. HTTP 통신 수신한 값, response : " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);

                    String message = jsonResponse.getString("message");
                    Log.i(TAG, "(deleteMeetup) 5. HTTP 통신 수신한 값, message : " + message);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "(deleteMeetup) JSONException, e : " + e);

                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // textView.setText("That didn't work!");
                Log.e("(deleteMoim) error : ", String.valueOf(error));
            }
        }) {
            // 포스트 파라미터 넣기
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("meetupSeq", meetupSeq);
                Log.i(TAG, "(deleteMeetup) 3. HTTP 통신 보내는 값, meetupSeq : " + meetupSeq);
                return params;
            }

        };
        queue.add(stringRequest);
    }
}