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

    // ???????????? ?????? ?????? ??????(????????? ??????)
    NetworkClient networkClient;
    UploadApis uploadApis;

    // sharedPreference ??????
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
        Log.i(TAG, "(onCreate()) 1. ??????");

        bottomNavigationView = findViewById(R.id.bottom_navigator);
        tv_joinedMoim = findViewById(R.id.tv_joinedMoim);
        tv_createdMoim = findViewById(R.id.tv_createdMoim);
        btn_joineMoimMore = findViewById(R.id.btn_joineMoimMore);
        btn_createdMoimMore = findViewById(R.id.btn_createdMoimMore);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        container = findViewById(R.id.container);

        // sharedPreference ??????
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onCreate()) 2. sharedPreference(login) ???, userSeq : " + userSeq);

        // ????????? ?????? ????????? ?????? ?????????????????? ??????
        joined_rv = findViewById(R.id.joined_rv);
        joined_rv.setHasFixedSize(true);
        joined_layoutManager = new LinearLayoutManager(getApplicationContext());
        joined_rv.setLayoutManager(joined_layoutManager);
        joined_adapter = new MoimAdapter(joinedMoimDataList, getApplicationContext());
        joined_rv.setAdapter(joined_adapter);
        joined_rv.setEmptyView(findViewById(R.id.list_empty));


        // ?????? ?????? ????????? ?????? ?????????????????? ??????
        created_rv = findViewById(R.id.created_rv);
        created_rv.setHasFixedSize(true);
        created_layoutManager = new LinearLayoutManager(getApplicationContext());
        created_rv.setLayoutManager(created_layoutManager);
        created_adapter = new MoimAdapter(createdMoimDataList, getApplicationContext());
        created_rv.setAdapter(created_adapter);
        created_rv.setEmptyView(findViewById(R.id.created_list_empty));

        // ProgressDialog ??????
        dialog = new ProgressDialog(MyMoimActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("????????? ??????????????????.");

        nestedScrollView.setVisibility(View.INVISIBLE);
        dialog.show();


        // ????????? ?????? ??? (onCreate ?????? ??????)
        HashMap hashmap = new HashMap<String, String>();
        hashmap.put("userSeq", sp.getString("userSeq",""));


        // ???????????? ?????? ????????????
        joinedMoimList(hashmap);

        // ????????? ?????? ????????????
        createdMoimList(hashmap);

        // ????????? ?????? ?????? ??????
        TextView tv_seeAll = (TextView) findViewById(R.id.tv_seeAll);
        SpannableString spannableString = new SpannableString(tv_seeAll.getText());
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
        tv_seeAll.setText(spannableString);
        tv_seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "????????? ?????? ?????? ?????? ?????????");
                Intent intent = new Intent(getApplicationContext(), AllJoinedMoim.class);
                startActivity(intent);
            }
        });

        btn_joineMoimMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "????????? ?????? ?????????");
                Intent intent = new Intent(getApplicationContext(), AllJoinedMoim.class);
                startActivity(intent);
            }
        });


        // ????????? ?????? ?????? ??????
        TextView tv_seeAll2 = (TextView) findViewById(R.id.tv_seeAll2);
        SpannableString spannableString2 = new SpannableString(tv_seeAll2.getText());
        spannableString2.setSpan(new UnderlineSpan(), 0, spannableString2.length(), 0);
        tv_seeAll2.setText(spannableString2);
        tv_seeAll2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "????????? ?????? ?????? ?????? ?????????");
                Intent intent = new Intent(getApplicationContext(), AllCreatedMoim.class);
                startActivity(intent);
            }
        });

        btn_createdMoimMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "????????? ?????? ????????? ?????? ?????????");
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

                // ????????? ?????? ??? (onCreate ?????? ??????)
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("userSeq", sp.getString("userSeq",""));


                // ???????????? ?????? ????????????
                joinedMoimList(hashmap);

                // ????????? ?????? ????????????
                createdMoimList(hashmap);

                /* ??????????????? ???????????? ?????? */
                swiperefresh.setRefreshing(false);
            }
        });



        // ?????? ??????????????? ??? ??????
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


    // HTTP?????? ????????? 1. ???????????? ?????? ?????? ??????
    // (????????????) Retrofit
    // (????????????????????????) userSeq
    // (????????????????????????) List<DataClass> ????????? ???()
    private void joinedMoimList(HashMap<String, String> hashmap) {
        Log.i(TAG, "(joinedMoimList) 1. ?????? - ???????????? ?????? ?????? ??????");

        // ???????????? ???????????? ?????????????????? ??????
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // ????????? ????????? ???????????? ??????
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        Log.i(TAG, "(joinedMoimList) 2. ????????? ????????? ???, userSeq : " + hashmap.get("userSeq"));

                    // 1. ????????? ?????? ?????? (ex. DataClass)
                    // 2. uploadApis??? ????????? ?????? (ex. uploadApis.signUpInfoUpload)
                    Call<List<DataClass>> call = uploadApis.joinedMoimList(textToServer);
                    call.enqueue(new Callback<List<DataClass>>() {
                        @Override
                        public void onResponse(Call<List<DataClass>> call, retrofit2.Response<List<DataClass>> response) {

                            Log.i(TAG, "(joinedMoimList) 3. ??????????????? ?????? ???, response : " + response);
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
                                    Log.i(TAG, "(joinedMoimList) 4. ??????????????? ?????? ???, object(DataClass) " + i + " : " + object);

                            }

                                int a = responseBody.size();
                                if(a > 0) {
                                    a = 0;
                                    String itemCount = responseBody.get(a).getItemCount();
                                    Log.i(TAG, "(joinedMoimList) 5. ?????????????????? ????????? ?????? ??????, itemCount : " + itemCount);
                                    tv_joinedMoim.setText("???????????? ?????? (" + itemCount + ")");

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
                Log.i(TAG, "(joinedMoimList) 4. HTTP?????? onFailure : " + t);
            }
        });
    }

    // HTTP?????? ????????? 1. ?????? ????????? ?????? ?????? ??????
    // (????????????) Retrofit
    // (????????????????????????) userSeq
    // (????????????????????????) List<DataClass> ????????? ???()
    private void createdMoimList(HashMap<String, String> hashmap) {
        Log.i(TAG, "(recommendedMoimList) 1. ?????? - ?????? ????????? ?????? ?????? ?????? ");
        // ???????????? ???????????? ?????????????????? ??????
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // ????????? ????????? ???????????? ??????
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        Log.i(TAG, "(recommendedMoimList) 2. HTTP?????? ????????? ???, userSeq : " + hashmap.get("userSeq"));

        Call<List<DataClass>> call = uploadApis.createdMoimList(textToServer);
        call.enqueue(new Callback<List<DataClass>>() {
            @Override
            public void onResponse(Call<List<DataClass>> call, retrofit2.Response<List<DataClass>> response) {
                Log.i(TAG, "(recommendedMoimList) 3. ??????????????? ?????? ???, response : " + response);

                List<DataClass> responseBody = response.body();
                for(int i = 0; i < responseBody.size(); i++) {
                    createdMoimDataList.add(responseBody.get(i));
                    Gson gson = new Gson();
                    String object = gson.toJson(responseBody.get(i));
                    Log.i(TAG, "(recommendedMoimList) 4. ??????????????? ?????? ???, object(DataClass) : " + object);
                }
                int a = responseBody.size();
                if(a > 0) {
                    a = 0;
                    String itemCount = responseBody.get(a).getItemCount();
                    Log.i(TAG, "(recommendedMoimList) 5. ?????? ?????????????????? ????????? ?????? ??????, itemCount : " + itemCount);
                    tv_createdMoim.setText("????????? ?????? (" + itemCount + ")");
                }
                created_adapter = new MoimAdapter(createdMoimDataList, getApplicationContext());
                created_rv.setAdapter(created_adapter);
                created_adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<DataClass>> call, Throwable t) {
                Log.i(TAG, "(recommendedMoimList) HTTP?????? onFailure, t : " + t);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "(onResume()) ??????");
        // sharedPreference ??????



        joinedMoimDataList.clear();
        createdMoimDataList.clear();
        // ????????? ?????? ??? (onCreate ?????? ??????)
        HashMap hashmap = new HashMap<String, String>();
        hashmap.put("userSeq", sp.getString("userSeq",""));
//
//        // ???????????? ?????? ????????????
//        joinedMoimList(hashmap);
//        // ????????? ?????? ????????????
//        createdMoimList(hashmap);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "(onStop()) ??????");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "(onPause()) ??????");

        SharedPreferences sp;
        SharedPreferences.Editor editor;

        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onCreate()) sharedPreference(login) ???, userSeq : " + userSeq);
        editor.remove("moimSeqFromNoti" + userSeq);
        editor.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "(onDestroy()) ??????");

    }
}