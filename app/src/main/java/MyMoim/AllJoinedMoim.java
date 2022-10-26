package MyMoim;

import static Common.StaticVariable.serverAddress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.google.gson.Gson;

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

public class AllJoinedMoim extends AppCompatActivity {

    private static final String TAG = "AllJoinedMoim";

    // 레트로핏 통신 공통 변수(클래스 연결)
    NetworkClient networkClient;
    UploadApis uploadApis;
    List<DataClass> joinedMoimDataList = new ArrayList<>();



    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private RecyclerViewEmptySupport joined_rv;
    private RecyclerView.Adapter joined_adapter;
    private RecyclerView.LayoutManager joined_layoutManager;


    TextView tv_title;
    ProgressBar progressBar;
    NestedScrollView nestedScrollView;
    // 1페이지에 3개씩 데이터를 불러온다
    int page = 1;
    int limit = 7;

    String interestAll;
    String interestCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_joined_moim);
        Log.i(TAG, "(onCreate()) 1. 호출");

        tv_title = findViewById(R.id.tv_title);
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

        // 서버로 보낼 값 (onCreate 안에 선언)
        HashMap hashmap = new HashMap<String, String>();
        hashmap.put("userSeq", userSeq);
        hashmap.put("page", page);
        hashmap.put("limit", limit);




        // 현재 user의 가입한 모임과 관심분야 정보 수신
        //가입한 모임 목록 요청 메서드
        joinedMoimListPaging(hashmap);


        // notifyDataSetChanged할때 리사이클러뷰가 깜빡이는 애니메이션을 없애줌.
        RecyclerView.ItemAnimator animator = joined_rv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        // 화면 닫기
        ImageView iv_close = (ImageView) findViewById(R.id.iv_close);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        nestedScrollView = findViewById(R.id.nestedScrollView);
        progressBar = findViewById(R.id.progress_bar);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;

                    // 서버로 보낼 값 (onCreate 안에 선언)
                    HashMap hashmap = new HashMap<String, String>();
                    hashmap.put("userSeq", userSeq);
                    hashmap.put("page", page);
                    hashmap.put("limit", limit);

                    progressBar.setVisibility(View.VISIBLE);
//                    BackgroundThread thread = new BackgroundThread();
//                    thread.start();
                    joinedMoimListPaging(hashmap);




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
                Log.i(TAG, "(swipe refresh) 1. joinedMoimDataList를 비운다(clear)");
                joinedMoimDataList.clear();
                page = 1;

                Log.i(TAG, "(swipe refresh) 2. getMyMoimInfo 메서드를 다시 호출한다");
                // 서버에 등록된 모임 목록 요청
                joinedMoimListPaging(hashmap);


                /* 업데이트가 끝났음을 알림 */
                swiperefresh.setRefreshing(false);
            }
        });


    }

    // HTTP통신 메서드 1. 로그인한 계정이 가입한 모임, 관심분야 값 수신
    // (통신방식) Volley
    // (클라이언트→서버) userSeq
    // (서버→클라이언트) interestAll
    // (이후 동작) recommendedMoimList(hashmap) 호출

//    private void getMyMoimInfo(String userSeq){
//        Log.i(TAG, "(getMyMoimInfo) 1. 호출");
//
//        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//        String url = serverAddress + "somoim/moimManage/getMyMoimInfo.php";
//        Log.i(TAG, "(getMyMoimInfo) 2. HTTP 통신 url : " + url);
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//
//
//                Log.i(TAG, "(getMyMoimInfo) 4. HTTP 통신 수신한 값, response : " + response);
//                try {
//                    JSONObject jsonResponse = new JSONObject(response);
//
//                    interestAll = jsonResponse.getString("interestAll");
//                    Log.i(TAG, "(getMyMoimInfo) 5. HTTP 통신 수신한 값, interestAll : " + interestAll);
//                    Log.i(TAG, "(getMyMoimInfo) 5. 전역변수에 저장, interestAll : " + interestAll);
//
//                    String[] interestList = interestAll.split(";");
//                    interestCount = String.valueOf(interestList.length);
//
//
//
//                    HashMap hashmap = new HashMap<String, String>();
//                    hashmap.put("userSeq", sp.getString("userSeq",""));
//                    hashmap.put("interestAll", interestAll); //
//                    hashmap.put("interestCount", interestCount); // 관심분야의 개수
//                    hashmap.put("page", page);
//                    hashmap.put("limit", limit);
//
//                    Log.i(TAG, "(getMyMoimInfo) 6. 또 다른 매서드 호출을 위해 hashmap 객체 생성, hashmap : " + String.valueOf(hashmap));
//
//                    //계정의 관심분야 맞춤 목록 요청 메서드
//                    Log.i(TAG, "(getMyMoimInfo) 7. recommendedMoimList(hashmap) 메서드 호출");
//                    //가입한 모임 목록 요청 메서드
//                    joinedMoimListPaging(hashmap);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    Log.i(TAG, "(getMyMoimInfo) JSONException, e : " + e);
//
//                }
//            }
//        }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                // textView.setText("That didn't work!");
//                Log.e(TAG,   "(getMyMoimInfo) error : "+ String.valueOf(error));
//            }
//        }) {
//            // 포스트 파라미터 넣기
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError
//            {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("userSeq", userSeq);
//                Log.i(TAG, "(getMyMoimInfo) 3. HTTP 통신 보내는 값, userSeq : " + userSeq);
//                return params;
//            }
//
//        };
//        queue.add(stringRequest);
//    }





    // 모임 목록을 요청하는 메서드
    private void joinedMoimListPaging(HashMap<String, String> hashmap) {
        Log.i(TAG, "(joinedMoimList) 1. 호출");

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        textToServer.put("page", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("page"))));
        textToServer.put("limit", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("limit"))));



        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<List<DataClass>> call = uploadApis.joinedMoimListPaging(textToServer);
        call.enqueue(new Callback<List<DataClass>>() {
            @Override
            public void onResponse(Call<List<DataClass>> call, retrofit2.Response<List<DataClass>> response) {
                Log.i(TAG, "(joinedMoimList) 4. HTTP통신 통신 결과, response : " + response);
                if(response.isSuccessful() && response.body() != null){
                    progressBar.setVisibility(View.GONE);

                    List<DataClass> responseBody = response.body();

                    for(int i = 0; i < responseBody.size(); i++) {
                        joinedMoimDataList.add(responseBody.get(i));
                        Gson gson = new Gson();
                        String object = gson.toJson(responseBody.get(i));
                        Log.i(TAG, "(joinedMoimList) 5. HTTP통신 통신결과 수신하는 값, object " + i + " : " + object);
                    }

                    int a = responseBody.size();
                    if(a > 0){
                        a = 0;
                        String itemCount = responseBody.get(a).getItemCount();
                        Log.i(TAG, "(joinedMoimList) 6. 리사이클러뷰 아이템 전체 개수, itemCount : " + itemCount);
                        tv_title.setText("가입한 모임 (" + itemCount + ")");
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

}