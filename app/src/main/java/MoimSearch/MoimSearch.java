package MoimSearch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.somoim.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Common.DataClass;
import Common.NetworkClient;
import Common.RecyclerViewEmptySupport;
import Common.UploadApis;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class MoimSearch extends AppCompatActivity {
    private static final String TAG = "MoimSearch(모임검색)";
    List<DataClass> moimDataList = new ArrayList<>(); // 리사이클러뷰 어댑터에 사용할 값 형식 → 모임 목록 출력
    private RecyclerViewEmptySupport recyclerView; // 리사이클러뷰
    private RecyclerView.Adapter adapter;  // 리사이클러뷰 어댑터
    private RecyclerView.LayoutManager layoutManager;  // 리사이클러뷰 레이아웃 매니저

    // 레트로핏 통신 공통 변수(클래스 연결)
    NetworkClient networkClient; // 레트로핏 객체 생성 클래스
    UploadApis uploadApis; // 레트로핏 통신을 위한 인터페이스

    // sharedPreference 세팅
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    ProgressBar progressBar;
    NestedScrollView nestedScrollView;
    // 1페이지에 3개씩 데이터를 불러온다
    int page = 1;
    int limit = 10;

    TextInputLayout til_search;
    TextInputEditText et_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moim_search2);

        Log.i(TAG, "(onCreate()) 1. 호출");
        recyclerView = findViewById(R.id.recyclerView);

        // sharedPreference 세팅
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onCreate()) 2. sharedPreference(login) 값, userSeq : " + userSeq);

        // 리사이클러뷰 세팅 ※ 의미 공부 필요
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MoimAdapter(moimDataList, getApplicationContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setEmptyView(findViewById(R.id.list_empty));

        // 서버로 보낼 값 (onCreate 안에 선언)
        HashMap hashmap = new HashMap<String, String>();
        hashmap.put("userSeq", userSeq);
        hashmap.put("page", page);
        hashmap.put("limit", limit);
        Log.i(TAG, "(onCreate()) 3. HTTP 통신에 사용할 hashmap 객체 생성, hashmap : " + String.valueOf(hashmap));

        Log.i(TAG, "(onCreate()) 4. requestMoimList(hashmap) 호출 - 모든 모임 목록 요청");
        requestMoimList(hashmap); // 모든 모임 목록 요청

        nestedScrollView = findViewById(R.id.nestedScrollView);
        progressBar = findViewById(R.id.progress_bar);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    Log.i(TAG, "(nestedScrollView) 1. 아래로 스크롤함");
                    page++;
                    Log.i(TAG, "(nestedScrollView) 2. page++ 결과, page : " + page);
                    progressBar.setVisibility(View.VISIBLE);
//                    BackgroundThread thread = new BackgroundThread();
//                    thread.start();
                    HashMap hashmap = new HashMap<String, String>();
                    hashmap.put("userSeq", userSeq);
                    hashmap.put("page", page);
                    hashmap.put("limit", limit);

                    Log.i(TAG, "(nestedScrollView) 3. 서버로 보낼 hashmap 객체 생성, hashmap : " + String.valueOf(hashmap));
                    Log.i(TAG, "(nestedScrollView) 4. requestMoimList(hashmap) 호출");
                    requestMoimList(hashmap);
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
                hashmap.put("userSeq", userSeq);
                hashmap.put("page", page);
                hashmap.put("limit", limit);
                Log.i(TAG, "(onCreate()) 3. HTTP 통신에 사용할 hashmap 객체 생성, hashmap : " + String.valueOf(hashmap));

                Log.i(TAG, "(onCreate()) 4. requestMoimList(hashmap) 호출 - 모든 모임 목록 요청");
                requestMoimList(hashmap); // 모든 모임 목록 요청

                /* 업데이트가 끝났음을 알림 */
                swiperefresh.setRefreshing(false);
            }
        });




    }


    // HTTP통신 메서드 1. 모임 목록 요청
    private void requestMoimList(HashMap<String, String> hashmap) {

        Log.i(TAG, "(requestMoimList) 1. 호출 - 모임 목록 요청");
        // 레트로핏 생성하여 인터페이스와연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        textToServer.put("page", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("page"))));
        textToServer.put("limit", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("limit"))));

        Log.i(TAG, "(requestMoimList) 2. 서버로 보내는 값, hashmap : " + String.valueOf(hashmap));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<List<DataClass>> call = uploadApis.requestMoimList(textToServer);
        call.enqueue(new Callback<List<DataClass>>() {
            @Override
            public void onResponse(Call<List<DataClass>> call, retrofit2.Response<List<DataClass>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    Log.i(TAG, "(requestMoimList) ");
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


    // HTTP통신 메서드 2. 검색한 키워드 모임 목록 요청
    private void requestMoimListSearch(HashMap<String, String> hashmap) {
        Log.i(TAG, "(requestMoimListSearch) 1. 호출 - 검색한 키워드 해당 모임 목록 요청");

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        Log.i(TAG, "(requestMoimListSearch) 2. 서버로 보내는 값, userSeq : " + hashmap.get("userSeq"));
        textToServer.put("inputSearch", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("inputSearch")));
        Log.i(TAG, "(requestMoimListSearch) 2. 서버로 보내는 값, inputSearch(검색어) : " + hashmap.get("inputSearch"));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<List<DataClass>> call = uploadApis.requestMoimListSearch(textToServer);
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


}