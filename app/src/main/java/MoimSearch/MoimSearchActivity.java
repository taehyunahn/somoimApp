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

    // ???????????? ?????? ?????? ??????(????????? ??????)
    NetworkClient networkClient;
    UploadApis uploadApis;

    // sharedPreference ??????
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    ImageView iv_search;

    private SwipeRefreshLayout swiperefresh;

    ProgressBar progressBar;
    NestedScrollView nestedScrollView;
    // 1???????????? 3?????? ???????????? ????????????
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
        Log.i(TAG, "(onCreate()) 1. ??????");

        // UI ??????
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

        // sharedPreference ??????
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onCreate()) 2. sharedPreference(login) ???, userSeq : " + userSeq);


        // ?????? ?????? ????????? ?????? ?????????????????? ??????
        recommended_rv = findViewById(R.id.recommended_rv);
        recommended_rv.setHasFixedSize(true);
        recommended_layoutManager = new LinearLayoutManager(getApplicationContext());
        recommended_rv.setLayoutManager(recommended_layoutManager);
        recommended_adapter = new MoimAdapter(moimDataList, getApplicationContext());
        recommended_rv.setAdapter(recommended_adapter);
        recommended_rv.setEmptyView(findViewById(R.id.recommended_list_empty));

//        // ????????? ?????? ??? (onCreate ?????? ??????)
//        HashMap hashmap = new HashMap<String, String>();
//        hashmap.put("userSeq", userSeq);
//
//
//        Log.i(TAG, "(onCreate()) 3. hashmap ?????? ?????? ??? ????????? ?????? ??? ??????, hashmap : " + hashmap);
//        // ?????? user??? ????????? ????????? ???????????? ?????? ??????
//        Log.i(TAG, "(onCreate()) 4. getMyMoimInfo(userSeq) ??????");
//        getMyMoimInfo(userSeq);


        // ProgressDialog ??????
        dialog = new ProgressDialog(MoimSearchActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("????????? ??????????????????.");
        dialog.show();

        nestedScrollView = findViewById(R.id.nestedScrollView);
        nestedScrollView.setVisibility(View.INVISIBLE);

        progressBar = findViewById(R.id.progress_bar);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;

                    // ????????? ?????? ??? (onCreate ?????? ??????)
                    HashMap hashmap = new HashMap<String, String>();
                    hashmap.put("userSeq", sp.getString("userSeq",""));
                    hashmap.put("interestAll",interestAll);
                    hashmap.put("interestCount",interestCount); // ??????????????? ??????

                    hashmap.put("page", sp.getString("page", String.valueOf(page)));
                    hashmap.put("limit", sp.getString("limit", String.valueOf(limit)));
                    Log.i(TAG, "(onResume()) 1. hashmap??? ??? ?????? : " + hashmap);
                    Log.i(TAG, "(onResume()) ????????? ?????? ???(hashmap) = " + String.valueOf(hashmap));

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

                Log.i(TAG, "swipe refresh??? onRefresh() ????????? ??????");
                Log.i(TAG, "(swipe refresh) ????????? ?????????????????? ?????? ?????? ??????");
                Log.i(TAG, "(swipe refresh) 1. moimDataList??? ?????????(removeAll)");
                moimDataList.removeAll(moimDataList);
                page = 1;

                Log.i(TAG, "(swipe refresh) 2. requestMoimList ???????????? ?????? ????????????");
                // ????????? ????????? ?????? ?????? ??????
                getMyMoimInfo(userSeq);

                /* ??????????????? ???????????? ?????? */
                swiperefresh.setRefreshing(false);
            }
        });




        // ?????? ????????? ?????? ???
        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "?????? ????????? ??????");
                Intent intent = new Intent(getApplicationContext(), MoimSearch.class);
                startActivity(intent);
            }
        });





        // ?????? ?????? ??????
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
                        Log.i(TAG, "bottomNavigationView?????? myList ??????");
                        Log.i(TAG, "--MyMoimActivity ??????????????? ??????");

                        Intent intent = new Intent(getApplicationContext(), MyMoimActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(intent);

                        overridePendingTransition(0,0);
                        return true;
                    case R.id.profile:
                        Log.i(TAG, "bottomNavigationView?????? profile ??????");
                        Log.i(TAG, "--ProfileActivity ??????????????? ??????");
//                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));

                        Intent intent2 = new Intent(getApplicationContext(), ProfileActivity.class);
//                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent2);

                        overridePendingTransition(0,0);
                        return true;
                    case R.id.create:
                        Log.i(TAG, "bottomNavigationView?????? create ??????");
                        Log.i(TAG, "--MoimCreate ??????????????? ??????");
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


        // ????????????1(??????/??????) ????????? ????????? ????????????
        category1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","??????/??????");
                // FLAG_ACTIVITY_CLEAR_TOP = ????????? ???????????? ?????? ?????? ???????????? ?????????.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // ????????????2(??????/??????) ????????? ????????? ????????????
        category2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","??????/??????");
                // FLAG_ACTIVITY_CLEAR_TOP = ????????? ???????????? ?????? ?????? ???????????? ?????????.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        category3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","????????????/??????");
                // FLAG_ACTIVITY_CLEAR_TOP = ????????? ???????????? ?????? ?????? ???????????? ?????????.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        category4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","??????/?????????");
                // FLAG_ACTIVITY_CLEAR_TOP = ????????? ???????????? ?????? ?????? ???????????? ?????????.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        category5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","?????????/???");
                // FLAG_ACTIVITY_CLEAR_TOP = ????????? ???????????? ?????? ?????? ???????????? ?????????.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        category6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","??????/??????");
                // FLAG_ACTIVITY_CLEAR_TOP = ????????? ???????????? ?????? ?????? ???????????? ?????????.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        category7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","??????/??????");
                // FLAG_ACTIVITY_CLEAR_TOP = ????????? ???????????? ?????? ?????? ???????????? ?????????.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        category8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimListActivity.class);
                intent.putExtra("category","??????/??????");
                // FLAG_ACTIVITY_CLEAR_TOP = ????????? ???????????? ?????? ?????? ???????????? ?????????.
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
        Log.i(TAG, "(onResume() 1. ?????????)");

        bottomNavigationView.setSelectedItemId(R.id.search);


        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onResume()) 2. sharedPreference(login) ???, userSeq : " + userSeq);

        moimDataList.clear();
//
        // ????????? ?????? ??? (onCreate ?????? ??????)
        HashMap hashmap = new HashMap<String, String>();
        hashmap.put("userSeq", userSeq);
        hashmap.put("page", page);
        hashmap.put("limit", limit);

        Log.i(TAG, "(onResume()) 1. hashmap??? ??? ?????? : " + hashmap);

        // ????????? ????????? ?????? ?????? ??????
        Log.i(TAG, "(onResume()) 2. requestMoimList ????????? ??????");
        getMyMoimInfo(userSeq);

//
//        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//
//                Log.i(TAG, "swipe refresh??? onRefresh() ????????? ??????");
//                Log.i(TAG, "(swipe refresh) ????????? ?????????????????? ?????? ?????? ??????");
//                Log.i(TAG, "(swipe refresh) 1. moimDataList??? ?????????(removeAll)");
//                moimDataList.removeAll(moimDataList);
//
//                Log.i(TAG, "(swipe refresh) 2. requestMoimList ???????????? ?????? ????????????");
//                // ????????? ????????? ?????? ?????? ??????
//                requestMoimList(hashmap);
//
//                /* ??????????????? ???????????? ?????? */
//                swiperefresh.setRefreshing(false);
//            }
//        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "(onStop()) 1. ?????????");
//        Log.i(TAG, "(onStop()) 2. moimDataList??? ?????????(removeAll)");
//        moimDataList.removeAll(moimDataList);
//        Log.i(TAG, "(onStop()) 3. adapter.notifyDataSetChanged() ???????????? ?????????????????? ??????");
//        recommended_adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "(onPause()) ??????");
        page = 1;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "(onDestroy()) ??????");

    }




    // HTTP?????? ????????? 1. ???????????? ????????? ????????? ??????, ???????????? ??? ??????
    // (????????????) Volley
    // (????????????????????????) userSeq
    // (????????????????????????) interestAll
    // (?????? ??????) recommendedMoimList(hashmap) ??????

    private void getMyMoimInfo(String userSeq){
        Log.i(TAG, "(getMyMoimInfo) 1. ??????");

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = serverAddress + "somoim/moimManage/getMyMoimInfo.php";
        Log.i(TAG, "(getMyMoimInfo) 2. HTTP ?????? url : " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                Log.i(TAG, "(getMyMoimInfo) 4. HTTP ?????? ????????? ???, response : " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);

                    interestAll = jsonResponse.getString("interestAll");
                    Log.i(TAG, "(getMyMoimInfo) 5. HTTP ?????? ????????? ???, interestAll : " + interestAll);
                    Log.i(TAG, "(getMyMoimInfo) 5. ??????????????? ??????, interestAll : " + interestAll);

                    String[] interestList = interestAll.split(";");
                    interestCount = String.valueOf(interestList.length);


                    HashMap hashmap = new HashMap<String, String>();
                    hashmap.put("userSeq", sp.getString("userSeq",""));
                    hashmap.put("interestAll", interestAll); //
                    hashmap.put("interestCount", interestCount); // ??????????????? ??????
                    hashmap.put("page", page);
                    hashmap.put("limit", limit);

                    Log.i(TAG, "(getMyMoimInfo) 6. ??? ?????? ????????? ????????? ?????? hashmap ?????? ??????, hashmap : " + String.valueOf(hashmap));

                    //????????? ???????????? ?????? ?????? ?????? ?????????
                    Log.i(TAG, "(getMyMoimInfo) 7. recommendedMoimList(hashmap) ????????? ??????");
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
            // ????????? ???????????? ??????
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userSeq", userSeq);
                Log.i(TAG, "(getMyMoimInfo) 3. HTTP ?????? ????????? ???, userSeq : " + userSeq);
                return params;
            }

        };
        queue.add(stringRequest);
    }

    // HTTP?????? ????????? 2. ?????? ????????? ??????
    private void recommendedMoimList(HashMap<String, String> hashmap) {

        Log.i(TAG, "(recommendedMoimList) 1. ?????? - ?????? ????????? ??????");
        // ???????????? ???????????? ?????????????????? ??????
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // ????????? ????????? ???????????? ??????
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        Log.i(TAG, "(recommendedMoimList) 2. HTTP?????? ????????? ????????? ???, userSeq : " + hashmap.get("userSeq"));
        textToServer.put("interestCount", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("interestCount")));
        Log.i(TAG, "(recommendedMoimList) 2. HTTP?????? ????????? ????????? ???, interestCount : " + hashmap.get("interestCount"));
        textToServer.put("interestAll", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("interestAll")));
        Log.i(TAG, "(recommendedMoimList) 2. HTTP?????? ????????? ????????? ???, interestAll : " + hashmap.get("interestAll"));
        textToServer.put("page", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("page"))));
        Log.i(TAG, "(recommendedMoimList) 2. HTTP?????? ????????? ????????? ???, page : " + String.valueOf(hashmap.get("page")));
        textToServer.put("limit", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(hashmap.get("limit"))));
        Log.i(TAG, "(recommendedMoimList) 2. HTTP?????? ????????? ????????? ???, limit : " + String.valueOf(hashmap.get("limit")));

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
                        Log.i(TAG, "(recommendedMoimList) 3. ???????????? ?????? ??????, object : " + object);
                    }

                    int a = responseBody.size();
                    if(a > 0){
//                        a = 0;
//                        String recommendItemCount = responseBody.get(a).getRecommendItemCount();
                        Log.i(TAG, "(recommendedMoimList) 3. ?????? ?????? ?????????????????? ????????? ?????? ??????, recommendItemCount : " + a);
                        tv_title.setText("???????????? ?????? (" + a + ")");
                    }
                    recommended_adapter = new MoimAdapter(moimDataList, getApplicationContext());
                    recommended_rv.setAdapter(recommended_adapter);
                    recommended_adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<DataClass>> call, Throwable t) {
                Log.i(TAG, "(recommendedMoimList) HTTP?????? onFailure : " + t);

            }
        });
    }



}