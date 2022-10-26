package MyMoim;

import static Common.StaticVariable.serverAddress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Common.BackKeyHandler;
import Common.DataClass;
import Common.NetworkClient;
import Common.RecyclerViewEmptySupport;
import Common.UploadApis;
import MoimCreate.MoimCreate;
import MoimSearch.MoimAdapter;
import MoimSearch.MoimData;
import MoimSearch.MoimSearchActivity;
import MyProfile.ProfileActivity;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class MyMoimActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    List<DataClass> createdMoimDataList = new ArrayList<>();
    List<DataClass> joinedMoimDataList = new ArrayList<>();


    private BackKeyHandler backKeyHandler = new BackKeyHandler(this);

    // 레트로핏 통신 공통 변수(클래스 연결)
    NetworkClient networkClient;
    UploadApis uploadApis;

    // sharedPreference 세팅
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    String interestAll;
    String joinedMoimAll;
    TextView tv_joinedMoim, tv_createdMoim;
    Button btn_joineMoimMore, btn_createdMoimMore;


    private RecyclerViewEmptySupport created_rv;
    private RecyclerView.Adapter created_adapter;
    private RecyclerView.LayoutManager created_layoutManager;

    private RecyclerViewEmptySupport joined_rv;
    private RecyclerView.Adapter joined_adapter;
    private RecyclerView.LayoutManager joined_layoutManager;

    private static final String TAG = "MyMoimActivity";

    ProgressDialog dialog;

    NestedScrollView nestedScrollView;
    RelativeLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_moim);
        Log.i(TAG, "(onCreate()) 1. 호출");

        bottomNavigationView = findViewById(R.id.bottom_navigator);
        tv_joinedMoim = findViewById(R.id.tv_joinedMoim);
        tv_createdMoim = findViewById(R.id.tv_createdMoim);
        btn_joineMoimMore = findViewById(R.id.btn_joineMoimMore);
        btn_createdMoimMore = findViewById(R.id.btn_createdMoimMore);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        container = findViewById(R.id.container);

        // sharedPreference 세팅
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onCreate()) 2. sharedPreference(login) 값, userSeq : " + userSeq);

        // 가입한 모임 목록을 위한 리사이클러뷰 세팅
        joined_rv = findViewById(R.id.joined_rv);
        joined_rv.setHasFixedSize(true);
        joined_layoutManager = new LinearLayoutManager(getApplicationContext());
        joined_rv.setLayoutManager(joined_layoutManager);
        joined_adapter = new MoimAdapter(joinedMoimDataList, getApplicationContext());
        joined_rv.setAdapter(joined_adapter);
        joined_rv.setEmptyView(findViewById(R.id.list_empty));


        // 추천 모임 목록을 위한 리사이클러뷰 세팅
        created_rv = findViewById(R.id.created_rv);
        created_rv.setHasFixedSize(true);
        created_layoutManager = new LinearLayoutManager(getApplicationContext());
        created_rv.setLayoutManager(created_layoutManager);
        created_adapter = new MoimAdapter(createdMoimDataList, getApplicationContext());
        created_rv.setAdapter(created_adapter);
        created_rv.setEmptyView(findViewById(R.id.created_list_empty));

        // ProgressDialog 생성
        dialog = new ProgressDialog(MyMoimActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("잠시만 기다려주세요.");

        nestedScrollView.setVisibility(View.INVISIBLE);
        dialog.show();


        // 서버로 보낼 값 (onCreate 안에 선언)
        HashMap hashmap = new HashMap<String, String>();
        hashmap.put("userSeq", sp.getString("userSeq",""));


        // 참여중인 모임 불러오기
        joinedMoimList(hashmap);

        // 개설한 모임 불러오기
        createdMoimList(hashmap);

        // 가입한 모임 모두 보기
        TextView tv_seeAll = (TextView) findViewById(R.id.tv_seeAll);
        SpannableString spannableString = new SpannableString(tv_seeAll.getText());
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
        tv_seeAll.setText(spannableString);
        tv_seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "가입한 모임 모두 보기 클릭함");
                Intent intent = new Intent(getApplicationContext(), AllJoinedMoim.class);
                startActivity(intent);
            }
        });

        btn_joineMoimMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "더보기 버튼 클릭함");
                Intent intent = new Intent(getApplicationContext(), AllJoinedMoim.class);
                startActivity(intent);
            }
        });


        // 개설한 모임 모두 보기
        TextView tv_seeAll2 = (TextView) findViewById(R.id.tv_seeAll2);
        SpannableString spannableString2 = new SpannableString(tv_seeAll2.getText());
        spannableString2.setSpan(new UnderlineSpan(), 0, spannableString2.length(), 0);
        tv_seeAll2.setText(spannableString2);
        tv_seeAll2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "개설한 모임 모두 보기 클릭함");
                Intent intent = new Intent(getApplicationContext(), AllCreatedMoim.class);
                startActivity(intent);
            }
        });

        btn_createdMoimMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "개설한 모임 더보기 버튼 클릭함");
                Intent intent = new Intent(getApplicationContext(), AllCreatedMoim.class);
                startActivity(intent);
            }
        });


        SwipeRefreshLayout swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                joinedMoimDataList.clear();
                createdMoimDataList.clear();

                // 서버로 보낼 값 (onCreate 안에 선언)
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("userSeq", sp.getString("userSeq",""));


                // 참여중인 모임 불러오기
                joinedMoimList(hashmap);

                // 개설한 모임 불러오기
                createdMoimList(hashmap);

                /* 업데이트가 끝났음을 알림 */
                swiperefresh.setRefreshing(false);
            }
        });



        // 하단 네비게이션 바 세팅
        bottomNavigationView.setSelectedItemId(R.id.myList);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.search:
//                        startActivity(new Intent(getApplicationContext(), MoimSearchActivity.class));
                        Intent intent = new Intent(getApplicationContext(), MoimSearchActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        overridePendingTransition(0,0);

                        return true;
                    case R.id.myList:
                        return true;
                    case R.id.profile:
//                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        Intent intent2 = new Intent(getApplicationContext(), ProfileActivity.class);
//                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent2);

                        overridePendingTransition(0,0);
                        return true;
                    case R.id.create:
//                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        Intent intent3 = new Intent(getApplicationContext(), MoimCreate.class);
//                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent3);
                        return true;
                }

                return false;
            }
        });


    }
    @Override
    public void onBackPressed() {
        backKeyHandler.onBackPressed();
    }


    // HTTP통신 매서드 1. 참여중인 모임 목록 요청
    // (통신방식) Retrofit
    // (클라이언트→서버) userSeq
    // (서버→클라이언트) List<DataClass> 객체의 값()
    private void joinedMoimList(HashMap<String, String> hashmap) {
        Log.i(TAG, "(joinedMoimList) 1. 호출 - 참여중인 모임 목록 요청");

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        Log.i(TAG, "(joinedMoimList) 2. 서버로 보내는 값, userSeq : " + hashmap.get("userSeq"));

                    // 1. 데이터 형태 지정 (ex. DataClass)
                    // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
                    Call<List<DataClass>> call = uploadApis.joinedMoimList(textToServer);
                    call.enqueue(new Callback<List<DataClass>>() {
                        @Override
                        public void onResponse(Call<List<DataClass>> call, retrofit2.Response<List<DataClass>> response) {

                            Log.i(TAG, "(joinedMoimList) 3. 서버로부터 받은 값, response : " + response);
                            if(response.isSuccessful()){

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(500);
                                            dialog.dismiss();
                                            nestedScrollView.setVisibility(View.VISIBLE);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                List<DataClass> responseBody = response.body();

                                for(int i = 0; i < responseBody.size(); i++) {

                                    joinedMoimDataList.add(responseBody.get(i));
                                    Gson gson = new Gson();
                                    String object = gson.toJson(responseBody.get(i));
                                    Log.i(TAG, "(joinedMoimList) 4. 서버로부터 받은 값, object(DataClass) " + i + " : " + object);

                            }

                                int a = responseBody.size();
                                if(a > 0) {
                                    a = 0;
                                    String itemCount = responseBody.get(a).getItemCount();
                                    Log.i(TAG, "(joinedMoimList) 5. 리사이클러뷰 아이템 전체 개수, itemCount : " + itemCount);
                                    tv_joinedMoim.setText("참여중인 모임 (" + itemCount + ")");

//                                    int itemCountInt = Integer.parseInt(itemCount);
//                                    if (itemCountInt > 3) {
//                                        btn_joineMoimMore.setVisibility(View.VISIBLE);
//                                    }
                                }
                                joined_adapter = new MoimAdapter(joinedMoimDataList, getApplicationContext());
                                joined_rv.setAdapter(joined_adapter);
                                joined_adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<DataClass>> call, Throwable t) {
                Log.i(TAG, "(joinedMoimList) 4. HTTP통신 onFailure : " + t);
            }
        });
    }

    // HTTP통신 매서드 1. 내가 개설한 모임 목록 요청
    // (통신방식) Retrofit
    // (클라이언트→서버) userSeq
    // (서버→클라이언트) List<DataClass> 객체의 값()
    private void createdMoimList(HashMap<String, String> hashmap) {
        Log.i(TAG, "(recommendedMoimList) 1. 호출 - 내가 개설한 모임 목록 요청 ");
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        Log.i(TAG, "(recommendedMoimList) 2. HTTP통신 보내는 값, userSeq : " + hashmap.get("userSeq"));

        Call<List<DataClass>> call = uploadApis.createdMoimList(textToServer);
        call.enqueue(new Callback<List<DataClass>>() {
            @Override
            public void onResponse(Call<List<DataClass>> call, retrofit2.Response<List<DataClass>> response) {
                Log.i(TAG, "(recommendedMoimList) 3. 서버로부터 받은 값, response : " + response);

                List<DataClass> responseBody = response.body();
                for(int i = 0; i < responseBody.size(); i++) {
                    createdMoimDataList.add(responseBody.get(i));
                    Gson gson = new Gson();
                    String object = gson.toJson(responseBody.get(i));
                    Log.i(TAG, "(recommendedMoimList) 4. 서버로부터 받은 값, object(DataClass) : " + object);
                }
                int a = responseBody.size();
                if(a > 0) {
                    a = 0;
                    String itemCount = responseBody.get(a).getItemCount();
                    Log.i(TAG, "(recommendedMoimList) 5. 해당 리사이클러뷰 아이템 전체 개수, itemCount : " + itemCount);
                    tv_createdMoim.setText("개설한 모임 (" + itemCount + ")");
                }
                created_adapter = new MoimAdapter(createdMoimDataList, getApplicationContext());
                created_rv.setAdapter(created_adapter);
                created_adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<DataClass>> call, Throwable t) {
                Log.i(TAG, "(recommendedMoimList) HTTP통신 onFailure, t : " + t);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "(onResume()) 호출");
        // sharedPreference 세팅



        joinedMoimDataList.clear();
        createdMoimDataList.clear();
        // 서버로 보낼 값 (onCreate 안에 선언)
        HashMap hashmap = new HashMap<String, String>();
        hashmap.put("userSeq", sp.getString("userSeq",""));
//
//        // 참여중인 모임 불러오기
//        joinedMoimList(hashmap);
//        // 개설한 모임 불러오기
//        createdMoimList(hashmap);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "(onStop()) 호출");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "(onPause()) 호출");

        SharedPreferences sp;
        SharedPreferences.Editor editor;

        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onCreate()) sharedPreference(login) 값, userSeq : " + userSeq);
        editor.remove("moimSeqFromNoti" + userSeq);
        editor.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "(onDestroy()) 호출");

    }
}