package MoimSearch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.somoim.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Common.DataClass;
import Common.NetworkClient;
import Common.RecyclerViewEmptySupport;
import Common.UploadApis;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class MoimListActivity extends AppCompatActivity {

    MoimAdapter moimAdapter;
    //    List<MoimData> moimDataList = new ArrayList<>();
    List<DataClass> moimDataList = new ArrayList<>();

    private RecyclerViewEmptySupport recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    TextInputLayout til_search;
    TextInputEditText et_search;

    TextView tv_category; // 소모임 카테고리(interest와 동일함)
    ImageView iv_back;

    // 레트로핏 통신 공통 변수(클래스 연결)
    NetworkClient networkClient;
    UploadApis uploadApis;

    // sharedPreference 세팅
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private static final String TAG = "MoimListActivity";

    ProgressBar progressBar;
    NestedScrollView nestedScrollView;
    // 1페이지에 3개씩 데이터를 불러온다
    int page = 1;
    int limit = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moim_list);
        Log.i(TAG, "(onCreate()) 1. 호출");

        // UI연결
        recyclerView = findViewById(R.id.recyclerView);
        tv_category = findViewById(R.id.tv_category); // 모임 카테고리
        iv_back = findViewById(R.id.iv_back);

        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onCreate()) 2. sharedPreference(login) 값, userSeq : " + userSeq);

        Intent intent = getIntent();
        String category = intent.getStringExtra("category");
        tv_category.setText(category);

        // 뒤로가기 버튼
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 리사이클러뷰 세팅 ※ 의미 공부 필요
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MoimAdapter(moimDataList, getApplicationContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setEmptyView(findViewById(R.id.list_empty));


        // 서버로 보낼 값 (onCreate 안에 선언)
        HashMap hashmap = new HashMap<String, String>();
        hashmap.put("userSeq", sp.getString("userSeq",""));
        hashmap.put("category", category);
        hashmap.put("page", page);
        hashmap.put("limit", limit);
        Log.i(TAG, "서버로 보낼 값(hashmap) = " + String.valueOf(hashmap));

        // 서버에 등록된 모임 목록 요청
        requestMoimList(hashmap);

        nestedScrollView = findViewById(R.id.nestedScrollView);
        progressBar = findViewById(R.id.progress_bar);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;

                    progressBar.setVisibility(View.VISIBLE);
//                    BackgroundThread thread = new BackgroundThread();
//                    thread.start();

                    HashMap hashmap = new HashMap<String, String>();
                    hashmap.put("userSeq", userSeq);
                    hashmap.put("category", category);
                    hashmap.put("page", page);
                    hashmap.put("limit", limit);

                    requestMoimList(hashmap);


                    Log.i(TAG, "onScrollChange page = " + page);
                }
            }
        });

        til_search = findViewById(R.id.til_search);
        et_search = findViewById(R.id.et_search);

        et_search = (TextInputEditText) til_search.getEditText(); // textInputLayout과 EditText 연결
        et_search.addTextChangedListener(new TextWatcher() { //textInputLayout 안에 있는 EditText 감지
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String inputSearch = et_search.getText().toString();
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("userSeq", userSeq);
                hashmap.put("category", category);
                hashmap.put("inputSearch", inputSearch);

                moimDataList.clear();
                requestMoimListSearch(hashmap);


            }
        });

        SwipeRefreshLayout swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;

                moimDataList.clear();

                // 서버로 보낼 값 (onCreate 안에 선언)
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("userSeq", sp.getString("userSeq",""));
                hashmap.put("category", category);
                hashmap.put("page", page);
                hashmap.put("limit", limit);
                Log.i(TAG, "서버로 보낼 값(hashmap) = " + String.valueOf(hashmap));

                // 서버에 등록된 모임 목록 요청
                requestMoimList(hashmap);


                /* 업데이트가 끝났음을 알림 */
                swiperefresh.setRefreshing(false);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "(onDestroy()) 1. 호출");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "(onPause()) 1. 호출");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "(onResume()) 1. 호출");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "(onStop()) 1. 호출");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "(onStart()) 1. 호출");
    }


    private void requestMoimList(HashMap<String, String> hashmap) {

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        textToServer.put("category", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("category")));
        textToServer.put("page", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("page"))));
        textToServer.put("limit", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("limit"))));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<List<DataClass>> call = uploadApis.requestMoimList(textToServer);
        call.enqueue(new Callback<List<DataClass>>() {
            @Override
            public void onResponse(Call<List<DataClass>> call, retrofit2.Response<List<DataClass>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    progressBar.setVisibility(View.GONE);

                    List<DataClass> responseBody = response.body();
                    for (int i = 0; i < responseBody.size(); i++) {
                        moimDataList.add(responseBody.get(i));
                    }
                    adapter = new MoimAdapter(moimDataList, getApplicationContext());
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<DataClass>> call, Throwable t) {
            }
        });
    }

    private void requestMoimListSearch(HashMap<String, String> hashmap) {
        Log.i(TAG, "(requestMoimListSearch) 1. 호출");

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        textToServer.put("category", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("category")));
        textToServer.put("inputSearch", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("inputSearch")));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<List<DataClass>> call = uploadApis.requestMoimListSearch(textToServer);
        call.enqueue(new Callback<List<DataClass>>() {
            @Override
            public void onResponse(Call<List<DataClass>> call, retrofit2.Response<List<DataClass>> response) {
                Log.i(TAG, "(requestMoimListSearch) 2. response : " + response);
                Log.i(TAG, "(requestMoimListSearch) 3. response.body() : " + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    progressBar.setVisibility(View.GONE);

                    List<DataClass> responseBody = response.body();
                    for (int i = 0; i < responseBody.size(); i++) {
                        moimDataList.add(responseBody.get(i));
                        Gson gson = new Gson();
                        String object = gson.toJson(responseBody.get(i));
                        Log.i(TAG, "(requestMoimListSearch) 4. HTTP통신 결과 수신한 배열 : " +object);
                    }
                    adapter = new MoimAdapter(moimDataList, getApplicationContext());
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<DataClass>> call, Throwable t) {
                Log.i(TAG, "(requestMoimListSearch) onFailure : " + t);

            }
        });
    }

}