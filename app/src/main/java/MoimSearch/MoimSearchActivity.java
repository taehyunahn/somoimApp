package MoimSearch;

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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

import java.io.File;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Chat.ChatService;
import Common.BackKeyHandler;
import Common.DataClass;
import Common.NetworkClient;
import Common.RecyclerViewEmptySupport;
import Common.UploadApis;
import MoimCreate.MoimCreate;
import MoimDetail.MoimDetailMain;
import MyMoim.MyMoimActivity;
import MyProfile.ProfileActivity;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class MoimSearchActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    MoimAdapter moimAdapter;
//    List<MoimData> moimDataList = new ArrayList<>();
    List<DataClass> moimDataList = new ArrayList<>();

    LinearLayout category1, category2, category3, category4, category5, category6, category7, category8;
    private BackKeyHandler backKeyHandler = new BackKeyHandler(this);

    // 레트로핏 통신 공통 변수(클래스 연결)
    NetworkClient networkClient;
    UploadApis uploadApis;

    // sharedPreference 세팅
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    ImageView iv_search;

    private SwipeRefreshLayout swiperefresh;

    ProgressBar progressBar;
    NestedScrollView nestedScrollView;
    // 1페이지에 3개씩 데이터를 불러온다
    int page = 1;
    int limit = 5;

    private static final String TAG = "MoimSearchActivity";

    String interestAll;
    String interestCount;

    private RecyclerViewEmptySupport recommended_rv;
    private RecyclerView.Adapter recommended_adapter;
    private RecyclerView.LayoutManager recommended_layoutManager;

    TextView tv_title;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moim_search);
        Log.i(TAG, "(onCreate()) 1. 호출");

        // UI 연결
        bottomNavigationView = findViewById(R.id.bottom_navigator);
        recommended_rv = findViewById(R.id.recommended_rv);
        category1 = findViewById(R.id.category1);
        category2 = findViewById(R.id.category2);
        category3 = findViewById(R.id.category3);
        category4 = findViewById(R.id.category4);
        category5 = findViewById(R.id.category5);
        category6 = findViewById(R.id.category6);
        category7 = findViewById(R.id.category7);
        category8 = findViewById(R.id.category8);
        iv_search = findViewById(R.id.iv_search);
        swiperefresh = findViewById(R.id.swiperefresh);
        tv_title = findViewById(R.id.tv_title);

        // sharedPreference 세팅
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onCreate()) 2. sharedPreference(login) 값, userSeq : " + userSeq);


        // 추천 모임 목록을 위한 리사이클러뷰 세팅
        recommended_rv = findViewById(R.id.recommended_rv);
        recommended_rv.setHasFixedSize(true);
        recommended_layoutManager = new LinearLayoutManager(getApplicationContext());
        recommended_rv.setLayoutManager(recommended_layoutManager);
        recommended_adapter = new MoimAdapter(moimDataList, getApplicationContext());
        recommended_rv.setAdapter(recommended_adapter);
        recommended_rv.setEmptyView(findViewById(R.id.recommended_list_empty));

//        // 서버로 보낼 값 (onCreate 안에 선언)
//        HashMap hashmap = new HashMap<String, String>();
//        hashmap.put("userSeq", userSeq);
//
//
//        Log.i(TAG, "(onCreate()) 3. hashmap 객체 생성 후 서버로 보낼 값 저장, hashmap : " + hashmap);
//        // 현재 user의 가입한 모임과 관심분야 정보 수신
//        Log.i(TAG, "(onCreate()) 4. getMyMoimInfo(userSeq) 호출");
//        getMyMoimInfo(userSeq);


        // ProgressDialog 생성
        dialog = new ProgressDialog(MoimSearchActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("잠시만 기다려주세요.");
        dialog.show();

        nestedScrollView = findViewById(R.id.nestedScrollView);
        nestedScrollView.setVisibility(View.INVISIBLE);

        progressBar = findViewById(R.id.progress_bar);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;

                    // 서버로 보낼 값 (onCreate 안에 선언)
                    HashMap hashmap = new HashMap<String, String>();
                    hashmap.put("userSeq", sp.getString("userSeq",""));
                    hashmap.put("interestAll",interestAll);
                    hashmap.put("interestCount",interestCount); // 관심분야의 개수

                    hashmap.put("page", sp.getString("page", String.valueOf(page)));
                    hashmap.put("limit", sp.getString("limit", String.valueOf(limit)));
                    Log.i(TAG, "(onResume()) 1. hashmap에 값 저장 : " + hashmap);
                    Log.i(TAG, "(onResume()) 서버로 보낼 값(hashmap) = " + String.valueOf(hashmap));

                    progressBar.setVisibility(View.VISIBLE);
//                    BackgroundThread thread = new BackgroundThread();
//                    thread.start();

                    recommendedMoimList(hashmap);


                    Log.i(TAG, "onScrollChange page = " + page);
                }
            }
        });

            swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Log.i(TAG, "swipe refresh의 onRefresh() 메서드 호출");
                Log.i(TAG, "(swipe refresh) 화면을 스와이프하면 새로 고침 동작");
                Log.i(TAG, "(swipe refresh) 1. moimDataList를 비운다(removeAll)");
                moimDataList.removeAll(moimDataList);
                page = 1;

                Log.i(TAG, "(swipe refresh) 2. requestMoimList 메서드를 다시 호출한다");
                // 서버에 등록된 모임 목록 요청
                getMyMoimInfo(userSeq);

                /* 업데이트가 끝났음을 알림 */
                swiperefresh.setRefreshing(false);
            }
        });




        // 검색 아이콘 클릭 시
        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "검색 아이콘 클릭");
                Intent intent = new Intent(getApplicationContext(), MoimSearch.class);
                startActivity(intent);
            }
        });





        // 하단 메뉴 설정
        bottomNavigationView.setSelectedItemId(R.id.search);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.search:
                        return true;
                    case R.id.myList:
//                        startActivity(new Intent(getApplicationContext(), MyMoimActivity.class));
                        Log.i(TAG, "bottomNavigationView에서 myList 선택");
                        Log.i(TAG, "--MyMoimActivity 액티비티로 이동");

                        Intent intent = new Intent(getApplicationContext(), MyMoimActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(intent);

                        overridePendingTransition(0,0);
                        return true;
                    case R.id.profile:
                        Log.i(TAG, "bottomNavigationView에서 profile 선택");
                        Log.i(TAG, "--ProfileActivity 액티비티로 이동");
//                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));

                        Intent intent2 = new Intent(getApplicationContext(), ProfileActivity.class);
//                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent2);

                        overridePendingTransition(0,0);
                        return true;
                    case R.id.create:
                        Log.i(TAG, "bottomNavigationView에서 create 선택");
                        Log.i(TAG, "--MoimCreate 액티비티로 이동");
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


        // 카테고리1(외국/언어) 아이콘 클릭시 화면이동
        category1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","외국/언어");
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // 카테고리2(외국/언어) 아이콘 클릭시 화면이동
        category2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","가족/결혼");
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        category3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","아웃도어/여행");
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        category4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","운동/스포츠");
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        category5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","인문학/책");
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        category6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","업종/직무");
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        category7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","문화/공연");
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        category8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","음악/악기");
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


    }
    @Override
    public void onBackPressed() {

        backKeyHandler.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "(onResume() 1. 호출됨)");

        bottomNavigationView.setSelectedItemId(R.id.search);


        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onResume()) 2. sharedPreference(login) 값, userSeq : " + userSeq);

        moimDataList.clear();
//
        // 서버로 보낼 값 (onCreate 안에 선언)
        HashMap hashmap = new HashMap<String, String>();
        hashmap.put("userSeq", userSeq);
        hashmap.put("page", page);
        hashmap.put("limit", limit);

        Log.i(TAG, "(onResume()) 1. hashmap에 값 저장 : " + hashmap);

        // 서버에 등록된 모임 목록 요청
        Log.i(TAG, "(onResume()) 2. requestMoimList 메서드 호출");
        getMyMoimInfo(userSeq);

//
//        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//
//                Log.i(TAG, "swipe refresh의 onRefresh() 메서드 호출");
//                Log.i(TAG, "(swipe refresh) 화면을 스와이프하면 새로 고침 동작");
//                Log.i(TAG, "(swipe refresh) 1. moimDataList를 비운다(removeAll)");
//                moimDataList.removeAll(moimDataList);
//
//                Log.i(TAG, "(swipe refresh) 2. requestMoimList 메서드를 다시 호출한다");
//                // 서버에 등록된 모임 목록 요청
//                requestMoimList(hashmap);
//
//                /* 업데이트가 끝났음을 알림 */
//                swiperefresh.setRefreshing(false);
//            }
//        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "(onStop()) 1. 호출됨");
//        Log.i(TAG, "(onStop()) 2. moimDataList를 비운다(removeAll)");
//        moimDataList.removeAll(moimDataList);
//        Log.i(TAG, "(onStop()) 3. adapter.notifyDataSetChanged() 호출하여 리사이클러뷰 갱신");
//        recommended_adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "(onPause()) 호출");
        page = 1;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "(onDestroy()) 호출");

    }




    // HTTP통신 메서드 1. 로그인한 계정이 가입한 모임, 관심분야 값 수신
    // (통신방식) Volley
    // (클라이언트→서버) userSeq
    // (서버→클라이언트) interestAll
    // (이후 동작) recommendedMoimList(hashmap) 호출

    private void getMyMoimInfo(String userSeq){
        Log.i(TAG, "(getMyMoimInfo) 1. 호출");

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = serverAddress + "somoim/moimManage/getMyMoimInfo.php";
        Log.i(TAG, "(getMyMoimInfo) 2. HTTP 통신 url : " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                Log.i(TAG, "(getMyMoimInfo) 4. HTTP 통신 수신한 값, response : " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);

                    interestAll = jsonResponse.getString("interestAll");
                    Log.i(TAG, "(getMyMoimInfo) 5. HTTP 통신 수신한 값, interestAll : " + interestAll);
                    Log.i(TAG, "(getMyMoimInfo) 5. 전역변수에 저장, interestAll : " + interestAll);

                    String[] interestList = interestAll.split(";");
                    interestCount = String.valueOf(interestList.length);


                    HashMap hashmap = new HashMap<String, String>();
                    hashmap.put("userSeq", sp.getString("userSeq",""));
                    hashmap.put("interestAll", interestAll); //
                    hashmap.put("interestCount", interestCount); // 관심분야의 개수
                    hashmap.put("page", page);
                    hashmap.put("limit", limit);

                    Log.i(TAG, "(getMyMoimInfo) 6. 또 다른 매서드 호출을 위해 hashmap 객체 생성, hashmap : " + String.valueOf(hashmap));

                    //계정의 관심분야 맞춤 목록 요청 메서드
                    Log.i(TAG, "(getMyMoimInfo) 7. recommendedMoimList(hashmap) 메서드 호출");
                    recommendedMoimList(hashmap);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "(getMyMoimInfo) JSONException, e : " + e);

                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // textView.setText("That didn't work!");
                Log.e(TAG,   "(getMyMoimInfo) error : "+ String.valueOf(error));
            }
        }) {
            // 포스트 파라미터 넣기
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userSeq", userSeq);
                Log.i(TAG, "(getMyMoimInfo) 3. HTTP 통신 보내는 값, userSeq : " + userSeq);
                return params;
            }

        };
        queue.add(stringRequest);
    }

    // HTTP통신 메서드 2. 모임 목록을 요청
    private void recommendedMoimList(HashMap<String, String> hashmap) {

        Log.i(TAG, "(recommendedMoimList) 1. 호출 - 모임 목록을 요청");
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        Log.i(TAG, "(recommendedMoimList) 2. HTTP통신 서버로 보내는 값, userSeq : " + hashmap.get("userSeq"));
        textToServer.put("interestCount", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("interestCount")));
        Log.i(TAG, "(recommendedMoimList) 2. HTTP통신 서버로 보내는 값, interestCount : " + hashmap.get("interestCount"));
        textToServer.put("interestAll", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("interestAll")));
        Log.i(TAG, "(recommendedMoimList) 2. HTTP통신 서버로 보내는 값, interestAll : " + hashmap.get("interestAll"));
        textToServer.put("page", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("page"))));
        Log.i(TAG, "(recommendedMoimList) 2. HTTP통신 서버로 보내는 값, page : " + String.valueOf(hashmap.get("page")));
        textToServer.put("limit", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("limit"))));
        Log.i(TAG, "(recommendedMoimList) 2. HTTP통신 서버로 보내는 값, limit : " + String.valueOf(hashmap.get("limit")));

        Call<List<DataClass>> call = uploadApis.recommendedMoimListPaging(textToServer);
        call.enqueue(new Callback<List<DataClass>>() {
            @Override
            public void onResponse(Call<List<DataClass>> call, retrofit2.Response<List<DataClass>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
                                dialog.dismiss();
                                progressBar.setVisibility(View.GONE);
                                nestedScrollView.setVisibility(View.VISIBLE);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    List<DataClass> responseBody = response.body();
                    for (int i = 0; i < responseBody.size(); i++) {
                        moimDataList.add(responseBody.get(i));
                        Gson gson = new Gson();
                        String object = gson.toJson(responseBody.get(i));
                        Log.i(TAG, "(recommendedMoimList) 3. 서버에서 받은 객체, object : " + object);
                    }

                    int a = responseBody.size();
                    if(a > 0){
//                        a = 0;
//                        String recommendItemCount = responseBody.get(a).getRecommendItemCount();
                        Log.i(TAG, "(recommendedMoimList) 3. 모임 추천 리사이클러뷰 아이템 전체 개수, recommendItemCount : " + a);
                        tv_title.setText("관심분야 모임 (" + a + ")");
                    }
                    recommended_adapter = new MoimAdapter(moimDataList, getApplicationContext());
                    recommended_rv.setAdapter(recommended_adapter);
                    recommended_adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<DataClass>> call, Throwable t) {
                Log.i(TAG, "(recommendedMoimList) HTTP통신 onFailure : " + t);

            }
        });
    }



}