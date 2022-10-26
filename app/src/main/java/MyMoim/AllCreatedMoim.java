package MyMoim;

import static Common.StaticVariable.serverAddress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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

import Common.DataClass;
import Common.NetworkClient;
import Common.RecyclerViewEmptySupport;
import Common.UploadApis;
import MoimSearch.MoimAdapter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class AllCreatedMoim extends AppCompatActivity {
    List<DataClass> createdMoimDataList = new ArrayList<>();

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

    private static final String TAG = "AllCreatedMoim";

    ProgressDialog dialog;


    TextView tv_title;
    ProgressBar progressBar;
    NestedScrollView nestedScrollView;
    // 1페이지에 3개씩 데이터를 불러온다
    int page = 1;
    int limit = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_created_moim);
        Log.i(TAG, "(onCreate()) 1. 호출");

        tv_title = findViewById(R.id.tv_title);
        btn_createdMoimMore = findViewById(R.id.btn_createdMoimMore);

        // sharedPreference 세팅
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onCreate()) 2. sharedPreference(login) 값, userSeq : " + userSeq);


        // 추천 모임 목록을 위한 리사이클러뷰 세팅
        created_rv = findViewById(R.id.created_rv);
        created_rv.setHasFixedSize(true);
        created_layoutManager = new LinearLayoutManager(getApplicationContext());
        created_rv.setLayoutManager(created_layoutManager);
        created_adapter = new MoimAdapter(createdMoimDataList, getApplicationContext());
        created_rv.setAdapter(created_adapter);
        created_rv.setEmptyView(findViewById(R.id.created_list_empty));



        nestedScrollView = findViewById(R.id.nestedScrollView);
        progressBar = findViewById(R.id.progress_bar);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;
//                    progressBar.setVisibility(View.VISIBLE);
//                    BackgroundThread thread = new BackgroundThread();
//                    thread.start();
                    HashMap hashmap = new HashMap<String, String>();
                    hashmap.put("userSeq", sp.getString("userSeq",""));
                    hashmap.put("page", page);
                    hashmap.put("limit", limit);


                    createdMoimList(hashmap);


                    Log.i(TAG, "onScrollChange page = " + page);
                }
            }
        });

        SwipeRefreshLayout swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Log.i(TAG, "swipe refresh의 onRefresh() 메서드 호출");
                Log.i(TAG, "(swipe refresh) 화면을 스와이프하면 새로 고침 동작");
                Log.i(TAG, "(swipe refresh) 1. createdMoimDataList 비운다(clear)");
                createdMoimDataList.clear();
                page = 1;

                Log.i(TAG, "(swipe refresh) 2. createdMoimList 메서드를 다시 호출한다");
                // 서버에 등록된 모임 목록 요청
                // 서버로 보낼 값 (onCreate 안에 선언)
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("userSeq", sp.getString("userSeq",""));
                hashmap.put("page", page);
                hashmap.put("limit", limit);
                createdMoimList(hashmap);

                /* 업데이트가 끝났음을 알림 */
                swiperefresh.setRefreshing(false);
            }
        });


    }


    // 내가 개설한 모임 목록 요청
    private void createdMoimList(HashMap<String, String> hashmap) {

        Log.i(TAG, "(createdMoimList) 1. 호출 - HTTP통신으로 개설한 모임 목록 요청");
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        textToServer.put("page", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("page"))));
        textToServer.put("limit", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("limit"))));

        Log.i(TAG, "(createdMoimList) 2. 서버로 보내는 값, textToServer : " +  String.valueOf(hashmap));

        Call<List<DataClass>> call = uploadApis.createdMoimListPaging(textToServer);
        call.enqueue(new Callback<List<DataClass>>() {
            @Override
            public void onResponse(Call<List<DataClass>> call, retrofit2.Response<List<DataClass>> response) {
                Log.i(TAG, "(createdMoimList) 3. 서버로부터 데이터 수신, response : " +  response);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                            Log.i(TAG, "(createdMoimList) 4. 로딩 다이얼로그를 없애는 스레드 동작");
                            dialog.dismiss();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                progressBar.setVisibility(View.GONE);
                List<DataClass> responseBody = response.body();
                for(int i = 0; i < responseBody.size(); i++) {
                    createdMoimDataList.add(responseBody.get(i));
                }

                int a = responseBody.size();
                if(a > 0){
                    a = 0;
                    String itemCount = responseBody.get(a).getItemCount();
                    Log.i(TAG, "(createdMoimList) 6. 해당 모임 리사이클러뷰 아이템 전체 개수, itemCount : " + itemCount);
                    tv_title.setText("개설한 모임 (" + itemCount + ")");
                }

                created_adapter = new MoimAdapter(createdMoimDataList, getApplicationContext());
                created_rv.setAdapter(created_adapter);
                created_adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<DataClass>> call, Throwable t) {
                Log.i(TAG, "(recommendedMoimList) 4. HTTP통신 onFailure : " + t);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                            Log.i(TAG, "(createdMoimList) 에러발생시에도 로딩 다이얼로그를 없애는 스레드 동작");
                            dialog.dismiss();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "(onResume()) 1. 호출");

        createdMoimDataList.clear();

        // ProgressDialog 생성
        dialog = new ProgressDialog(AllCreatedMoim.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("잠시만 기다려주세요.");
        dialog.show();

        // 서버로 보낼 값 (onCreate 안에 선언)
        HashMap hashmap = new HashMap<String, String>();
        hashmap.put("userSeq", sp.getString("userSeq",""));
        hashmap.put("page", page);
        hashmap.put("limit", limit);

        Log.i(TAG, "(onResume()) 2. hashmap 객체 생성 값, hashmap : " + String.valueOf(hashmap));

        Log.i(TAG, "(onResume()) 3. createdMoimList(hashmap) 메서드 호출 - 개설한 모임 목록 요청 ");
        createdMoimList(hashmap);

    }
}