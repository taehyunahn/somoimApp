package MyMoim;

import static Common.StaticVariable.serverAddress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class AllRecommendedMoim extends AppCompatActivity {
    private static final String TAG = "AllRecommendedMoim";


    List<DataClass> moimDataList = new ArrayList<>();
    // 레트로핏 통신 공통 변수(클래스 연결)
    NetworkClient networkClient;
    UploadApis uploadApis;

    // sharedPreference 세팅
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    String joinedMoimAll;

    String interestAll;

    private RecyclerViewEmptySupport recommended_rv;
    private RecyclerView.Adapter recommended_adapter;
    private RecyclerView.LayoutManager recommended_layoutManager;

    ProgressBar progressBar;
    NestedScrollView nestedScrollView;
    // 1페이지에 3개씩 데이터를 불러온다
    int page = 1;
    int limit = 10;

    TextView tv_title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_recommended_moim);
        Log.i(TAG, "(onCreate()) 1. 호출");

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

        // 서버로 보낼 값 (onCreate 안에 선언)
        HashMap hashmap = new HashMap<String, String>();
        hashmap.put("userSeq", sp.getString("userSeq",""));


        // 현재 user의 가입한 모임과 관심분야 정보 수신
        getMyMoimInfo(userSeq);

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

                    getMyMoimInfo(userSeq);


                    Log.i(TAG, "onScrollChange page = " + page);
                }
            }
        });

    }


    // 모임 목록을 요청하는 메서드
    private void recommendedMoimList(HashMap<String, String> hashmap) {

        Log.i(TAG, "(recommendedMoimList) 1. 호출");
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Log.i(TAG, "(recommendedMoimList) 1. hashmap : " + String.valueOf(hashmap));
        Log.i(TAG, "(recommendedMoimList) 2. hashmap.get(\"page\") : " + String.valueOf(hashmap.get("page")));


        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
//        textToServer.put("interestCount", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("interestCount")));
        textToServer.put("interestAll", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("interestAll")));
        textToServer.put("page", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("page"))));
        textToServer.put("limit", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("limit"))));

        Log.i(TAG, "(recommendedMoimList) 2. HTTP통신 보내는 값, userSeq : " + hashmap.get("userSeq") + " / interestAll : " + hashmap.get("interestAll"));

//        int interestCount = Integer.parseInt(hashmap.get("interestCount"));
//        Log.i(TAG, "(recommendedMoimList) interestCount : " + interestCount);
//
//        for(int i = 0; i < interestCount; i++){
//            if(hashmap.get("moim"+i) != null){
//                textToServer.put("interest" + i, RequestBody.create(MediaType.parse("text/plain"), hashmap.get("interest" + i)));
//                Log.i(TAG, "(recommendedMoimList) 3. HTTP통신 보내는 값, interest" +  i + " : " + hashmap.get("interest" + i));
//            }
//        }

        Call<List<DataClass>> call = uploadApis.recommendedMoimListPaging(textToServer);
        call.enqueue(new Callback<List<DataClass>>() {
            @Override
            public void onResponse(Call<List<DataClass>> call, retrofit2.Response<List<DataClass>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    progressBar.setVisibility(View.GONE);

                    List<DataClass> responseBody = response.body();
                    for (int i = 0; i < responseBody.size(); i++) {
                        moimDataList.add(responseBody.get(i));
                    }


                    int a = responseBody.size();
                    if(a > 0){
                        a = 0;
                        String recommendItemCount = responseBody.get(a).getRecommendItemCount();
                        Log.i(TAG, "(recommendedMoimList) 6. 모임 추천 리사이클러뷰 아이템 전체 개수, recommendItemCount : " + recommendItemCount);
                        tv_title.setText("모임 추천 (" + recommendItemCount + ")");
                    }


                    recommended_adapter = new MoimAdapter(moimDataList, getApplicationContext());
                    recommended_rv.setAdapter(recommended_adapter);
                    recommended_adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<DataClass>> call, Throwable t) {
                Log.i(TAG, "(recommendedMoimList) 4. HTTP통신 onFailure : " + t);

            }
        });
    }


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

                    HashMap hashmap = new HashMap<String, String>();
                    hashmap.put("userSeq", sp.getString("userSeq",""));
                    hashmap.put("interestAll",interestAll); // 관심분야의 개수
                    hashmap.put("page", page);
                    hashmap.put("limit", limit);


                    Log.i(TAG, "(getMyMoimInfo) 5. HTTP 통신 수신한 값을 hashmap 저장, hashmap : " + String.valueOf(hashmap));
                    //계정의 관심분야 맞춤 목록 요청 메서드
                    Log.i(TAG, "(getMyMoimInfo) 6. recommendedMoimList(hashmap) 메서드 호출");
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



}