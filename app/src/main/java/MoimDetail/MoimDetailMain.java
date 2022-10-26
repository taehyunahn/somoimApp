package MoimDetail;

import static Common.StaticVariable.serverAddress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Chat.ChatData;
import Chat.ViewType;
import Common.DataClass;
import Common.NetworkClient;
import Common.UploadApis;
import MoimCreate.MoimUpdate;
import MoimDetail.Board.BoardUpdate;
import MoimSearch.MoimData;
import MoimSearch.MoimSearchActivity;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class MoimDetailMain extends AppCompatActivity {

    // 레트로핏 통신 공통
    NetworkClient networkClient;
    UploadApis uploadApis;

    TabLayout tabLayout;
    static ViewPager tabViewPager;
    TabViewPagerAdapter tabViewPagerAdapter;

    static String staticUserSeq;


    public static Handler mHandler;

    TextView tv_edit, tv_title;
    ImageView iv_like;
    public static String moimSeq;
    public static String moveToChat;

    private static final String TAG = "MoimDetailMain(모임상세정보)";
    ImageView iv_share;

    // sharedPreference 세팅

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    ImageView iv_more; // 모임 화면에서 더보기 버튼

    TextView tv_moimSeq, tv_userSeq, tv_userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moim_detail_main);
        Log.i(TAG, "(onCreate()) 1. 호출");

        // UI 연결
        tabLayout = findViewById(R.id.main_tab_layout);
        tabViewPager = findViewById(R.id.view_pager_content);
        tv_edit = findViewById(R.id.tv_edit);
        iv_like = findViewById(R.id.iv_like);
        tv_title = findViewById(R.id.tv_title);
        iv_share = findViewById(R.id.iv_share);
        tv_moimSeq = findViewById(R.id.tv_moimSeq);
        tv_userSeq = findViewById(R.id.tv_userSeq);
        tv_userName = findViewById(R.id.tv_userName);

        //텝레이아웃에 메뉴를 추가한다.
        tabLayout.addTab(tabLayout.newTab().setText("정보"));
        tabLayout.addTab(tabLayout.newTab().setText("게시판"));
        tabLayout.addTab(tabLayout.newTab().setText("사진첩"));
        tabLayout.addTab(tabLayout.newTab().setText("채팅"));

        //뷰페이저어뎁터 선언 및 텝레이아웃과 연결 (stickode TabViewPagerAdapter참고)
        tabViewPagerAdapter = new TabViewPagerAdapter(
                getSupportFragmentManager(), tabLayout.getTabCount(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        //뷰페이저에 어댑터 세팅
        tabViewPager.setAdapter(tabViewPagerAdapter);

        //리스너 선언
        tabViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            //탭이 선택되었을때의 이벤트 동작
            public void onTabSelected(TabLayout.Tab tab) {
                tabViewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        String moimSeqFromNoti = sp.getString("moimSeqFromNoti", "");


        Boolean moveToChat = sp.getBoolean("moveToChat" + userSeq, false);

        staticUserSeq = userSeq;

        Log.i(TAG, "(onCreate()) 2. sharedPreference에서 불러온 값, userSeq : " + userSeq + " / moimSeqFromNoti : " + moimSeqFromNoti + " / " +  moveToChat);
        Log.i(TAG, "(onCreate()) 3. static 전역변수에 값 저장, staticUserSeq : " + staticUserSeq);


        // 인텐트로 전달받은 모임 번호
        Intent intent = getIntent();
        moimSeq = intent.getStringExtra("moimSeq");
        Boolean fromMoimAdapter = intent.getBooleanExtra("fromMoimAdapter", false);
        Boolean fromMoimCreate = intent.getBooleanExtra("fromMoimCreate", false);
        Boolean fromMoimJoin = intent.getBooleanExtra("fromMoimJoin", false);
        Boolean fromMoimUpdate = intent.getBooleanExtra("fromMoimUpdate", false);
        Boolean fromMeetupCreate = intent.getBooleanExtra("fromMeetupCreate", false);
        Boolean fromAlbumView = intent.getBooleanExtra("fromAlbumView", false);
        Boolean fromBoardView = intent.getBooleanExtra("fromBoardView", false);


        Log.i(TAG, "(onCreate()) 4. 인텐트로 수신하는 값, moimSeq : " + moimSeq + " / fromMoimAdapter : " + fromMoimAdapter + " / fromMoimCreate : " + fromMoimCreate);

//
//        // 인텐트에서 넘어온거라면, 네번째 탭으로 이동해라
//        if(moveToChat){
//            tabViewPager.setCurrentItem(3);
//        } else {
//
//        }

        // 모임 가입완료 후, 채팅 화면으로 이동하도록 설정
            if(intent.getBooleanExtra("intentToChat", false)){
        tabViewPager.setCurrentItem(3);
    }


        if(fromMoimAdapter){
            // MoimAdapter에서 온 경우는 노티피케이션으로 인한 영향을 받지 않도록 한다

        } else if (fromMoimCreate) {
            // MoimCteate에서 온 경우는 노티피케이션으로 인한 영향을 받지 않도록 한다
        } else if (fromMoimJoin) {
            // InfoFragment에서 모임가입하고 온 경우는 노티피케이션으로 인한 영향을 받지 않도록 한다
        } else if (fromMoimUpdate) {
            // InfoFragment에서 모임수정하고 온 경우는 노티피케이션으로 인한 영향을 받지 않도록 한다
        } else if (fromMeetupCreate || fromAlbumView || fromBoardView) {
            // 정모를 만들고 온 경우는 노티피케이션으로 인한 영향을 받지 않도록 한다
        } else {
            moimSeq = moimSeqFromNoti;
            Log.i(TAG, "노티피케이션으로 전달받은 값을 전역변수에 넣음, moimSeq = " + moimSeq);
            //노티피케이션 이용했으면, 그안에 들어있었던 값은 삭제한다.
            editor.remove("moimSeqFromNoti" + userSeq);
            editor.commit();
        }


        Log.i(TAG, "(onCreate()) 5. getMoimTitle(moimSeq) 매서드 호출");
        getMoimTitle(moimSeq);

        Log.i(TAG, "(sharedPreference에서 사용하는 정보) 2. moveToChat = " + moveToChat);

        if(moveToChat){
            // 채팅 화면으로 이동
            tabViewPager.setCurrentItem(3);
            Log.i(TAG, "(sharedPreference에서 사용하는 정보) moveToChat 사용 결과 = tabViewPager.setCurrentItem(3) 동작함 - 채팅 탭으로 이동");
            editor.remove("moveToChat" + userSeq);
            editor.remove("moimSeqFromNoti" + userSeq);
            editor.commit();
        } else {
            Log.i(TAG, "(sharedPreference에서 사용하는 정보) moveToChat 사용 결과 = tabViewPager.setCurrentItem(3) 동작하지 않음");
        }


        // 편집 클릭시
        tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MoimUpdate.class);
                intent.putExtra("moimSeq", moimSeq);
                startActivity(intent);
            }
        });

        // 더보기 버튼 -> 게시글 수정 또는 삭제 메뉴
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
                inflater.inflate(R.menu.menu_moim, popup.getMenu());
                // + Handling click events
                // To perform an action when the user selects a menu item,
                // you must implement the PopupMenu.OnMenuItemClickListener interface and register it with your PopupMenu by calling setOnMenuItemclickListener().
                // When the user selects an item, the system calls the onMenuItemClick() callback in your interface.
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.editMoim){
                            Log.i(TAG, "모임 수정 클릭");
                            Intent intent = new Intent(getApplicationContext(), MoimUpdate.class);
                            intent.putExtra("moimSeq", moimSeq);
                            startActivity(intent);
                        } else if(item.getItemId() == R.id.deleteMoim){
                            Log.i(TAG, "모임 삭제 클릭");
                            // 서버로 보낼 값 (onCreate 안에 선언)
                            deleteMoim(moimSeq);
                            // 게시글의 고유값을 서버에 보내서 삭제한다. 그리고 모임 상세정보의 게시판 목록으로 간다 (모임의 고유값 필요)

                        }
                        return false;
                    }
                });

                // 3. Call PopupMenu.show().
                popup.show();

            }
        });
    }
    @Override
    public void onBackPressed() {
        if (tabViewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
//            super.onBackPressed();
            Intent intent = new Intent(getApplicationContext(), MoimSearchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
        } else {
            // Otherwise, select the previous step.
//            tabViewPager.setCurrentItem(tabViewPager.getCurrentItem() - 1);
            tabViewPager.setCurrentItem(0);

        }
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

    public void moimLeaderSetting() {
//        tv_edit.setVisibility(View.VISIBLE);
//        iv_like.setVisibility(View.INVISIBLE);
        iv_more.setVisibility(View.VISIBLE);

    }

    public void moimUserSetting() {
//        tv_edit.setVisibility(View.INVISIBLE);
//        iv_like.setVisibility(View.VISIBLE);
    }

    // 프래그먼트 교체 작업을 위한 메서드 (현재 사용 안하는 듯, 추후 삭제 필요)
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.view_pager_content, fragment).commit();      // Fragment로 사용할 MainActivity내의 layout공간을 선택합니다.
    }

    // 모임 타이틀을 서버에서 불러오는 메서드
    private void getMoimTitle(String moimSeq){
        Log.i(TAG, "(getMoimTitle) 1. 호출");

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = serverAddress + "somoim/moimManage/getMoimTitle.php";
        Log.i(TAG, "(getMoimTitle) 2. HTTP 통신 url : " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "(getMoimTitle) 4. HTTP 통신 수신한 값, response : " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);

                    String moimTitle = jsonResponse.getString("moimTitle");
                    Log.i(TAG, "moimTitle = " + moimTitle);
                    Log.i(TAG, "(getMoimTitle) 5. HTTP 통신 수신한 값, moimTitle : " + moimTitle);


                    sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
                    String userSeq = sp.getString("userSeq", "");
                    String userName = sp.getString("userName", "");
                    Log.i(TAG, "(onCreate()) sharedPreference(login) 값, userSeq : " + userSeq);


                    // 테스트 목적으로 방번호와 계정번호를 띄움
                    tv_title.setText(moimTitle);
                    tv_moimSeq.setText("m:" + moimSeq);
                    tv_userSeq.setText("u:" + userSeq);
                    tv_userName.setText(userName);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "(getMoimTitle) JSONException, e : " + e);

                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // textView.setText("That didn't work!");
                Log.e("(getMoimTitle) error : ", String.valueOf(error));
            }
        }) {
            // 포스트 파라미터 넣기
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("moimSeq", moimSeq);
                Log.i(TAG, "(getMoimTitle) 3. HTTP 통신 보내는 값, moimSeq : " + moimSeq);
                return params;
            }

        };
        queue.add(stringRequest);
    }

    // HTTP통신 매서드 1. 모임 삭제 - moim 테이블에서 moimSeq에 해당하는 행 삭제
    // (통신방식) volley
    // (클라이언트→서버) moimSeq
    // (서버→클라이언트) message

    private void deleteMoim(String moimSeq){
        Log.i(TAG, "(deleteMoim) 1. 호출");

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = serverAddress + "somoim/moimManage/deleteMoim.php";
        Log.i(TAG, "(deleteMoim) 2. HTTP 통신 url : " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "(deleteMoim) 4. HTTP 통신 수신한 값, response : " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);

                    String message = jsonResponse.getString("message");
                    Log.i(TAG, "(deleteMoim) 5. HTTP 통신 수신한 값, message : " + message);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "(deleteMoim) JSONException, e : " + e);

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
                params.put("moimSeq", moimSeq);
                Log.i(TAG, "(deleteMoim) 3. HTTP 통신 보내는 값, moimSeq : " + moimSeq);
                return params;
            }

        };
        queue.add(stringRequest);
    }



}
