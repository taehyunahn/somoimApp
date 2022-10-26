package com.example.somoim;

import static Common.StaticVariable.serverAddress;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.auth.model.Prompt;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import LoginSignUp.OtherLogin;
import LoginSignUp.SignUpSMS;
import MoimSearch.MoimSearchActivity;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class KakaoLoginActivity extends AppCompatActivity {

    private Intent serviceIntent;

    ImageButton btn_kakaoLogin; //카카오로그인 버튼
    TextView tv_otherLogin; //다른 로그인 방법 선택

    // 로그인용
    SharedPreferences spLogin;
    SharedPreferences.Editor editorLogin;

    // 회원가입용
    SharedPreferences sp;
    SharedPreferences.Editor editor;


    private static final String TAG = "KakaoLoginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kakao_login);
        Log.i(TAG, "(onCreate() 호출됨)");

        // UI 연결
        btn_kakaoLogin = findViewById(R.id.btn_kakaoLogin);
        tv_otherLogin = findViewById(R.id.tv_otherLogin);

        // sharedPreference 세팅

        // sharedPreference 세팅

        sp = getSharedPreferences("signUp", Activity.MODE_PRIVATE);
        editor = sp.edit();

        spLogin = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editorLogin = spLogin.edit();

        String userSeq = spLogin.getString("userSeq", "");

        Log.i(TAG, "(onCreate()) 1. 최초 로그인할 때, sharedPreference에 userSeq 저장 여부 확인");
        Log.i(TAG, "(onCreate()) 2. 로그인한 계정의 userSeq : " + userSeq);

        if(!userSeq.equals("")){
            Log.i(TAG, "(onCreate()) 2-1. MoimSearchActivity로 이동");
            Intent intent = new Intent(getApplicationContext(), MoimSearchActivity.class);
            startActivity(intent);
            Log.i(TAG, "(onCreate()) 2-2. getJoinedMoimSeqList 메서드 호출");
            getJoinedMoimSeqList(userSeq);

        } else {
        }

        // 다른 로그인 방법 선택 시
        tv_otherLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다른 로그인 액티비티로 이동
                Intent intent = new Intent(getApplicationContext(), OtherLogin.class);
                startActivity(intent);
            }
        });

        // 카카오 로그인 버튼 클릭 시
        btn_kakaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 로그인 버튼 클릭 시, 결과에 대한 처리를 콜백 객체로 만듬
                Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() {
                    @Override
                    public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                        if (oAuthToken != null) {
                            // 토큰이 전달되어, 로그인 성공 시 -> 연결된 창에서 로그인 정보 모두 입력한 경우
                            Log.e("토큰이 전달되어 성공시 :", "yes");
                        }
                        if (throwable != null) {
                            // 토큰이 전달되어, 로그인 실패 시 -> 연결된 창을 닫아버린 경우
                            Log.e("토큰이 전달되어 실패시 :", "no");
                        }

                        updatedKakaoLoginUi();
                        return null;
                    }
                };

                List<Prompt> prompts = Arrays.asList(Prompt.LOGIN);


//                // 기기에 카카오톡이 설치 되어있는지 확인 : true -> 설치된 경우
//                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(KakaoLoginActivity.this)) {
//                    //카카오가 설치된 경우는 loginWithKakaoTalk
//                    UserApiClient.getInstance().loginWithKakaoTalk(KakaoLoginActivity.this, callback);
//                } else {
//                    //카카오가 설치되지 않은 경우는 loginWithKakaoAccount
//                    UserApiClient.getInstance().loginWithKakaoAccount(KakaoLoginActivity.this, prompts, callback);
//
//                }

                // 기기에 카카오톡이 설치 되어있는지 확인 : true -> 설치된 경우

                //카카오가 설치되지 않은 경우는 loginWithKakaoAccount
                UserApiClient.getInstance().loginWithKakaoAccount(KakaoLoginActivity.this, prompts, callback);
            }
        });
    }

    // HTTP통신 매서드 1. 계정이 참여중이 모임 목록 얻기
    // (통신방식)
    // (클라이언트→서버)
    // (서버→클라이언트)
    private void getJoinedMoimSeqList(String userSeq){

        Log.i(TAG, "(getJoinedMoimSeqList) 1. 호출 - 현재 계정이 참여중인 모임 고유값 목록을 수신하고, 서비스를 동작시킨다");
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = serverAddress + "somoim/login/joinedMoimList.php";
        Log.i(TAG, "(getJoinedMoimSeqList) 2. HTTP 통신 URL : "+ url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "(getJoinedMoimSeqList) 4. HTTP 통신 수신하는 값, response : "+ response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String userName = jsonObject.getString("userName");
                    String joinedMoimSeqList = jsonObject.getString("joinedMoimSeqList");
                    editorLogin.putString("userName", userName);
                    editorLogin.putString("joinedMoimSeqList", joinedMoimSeqList);
                    editorLogin.commit();
                    Log.i(TAG, "(getJoinedMoimSeqList) 9. sharedPreference(login)에 저장 여부 재확인, userName : " + spLogin.getString("userName", "") + " / joinedMoimSeqList : " + spLogin.getString("joinedMoimSeqList", ""));

                    // 채팅 소켓 서비스 가동 시키는 타이밍
                    Log.i(TAG, "(getJoinedMoimSeqList) 10. Chat.ChatService 동작(서비스)");
                    Log.i(TAG, "(getJoinedMoimSeqList) 11. intentService.putExtra() key : userSeq / value : " + userSeq);
                    Intent intentService = new Intent(getApplicationContext(), Chat.ChatService.class);
                    intentService.putExtra("userSeq", userSeq);
//            startService(intentService);
                    startService(intentService);


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "(getJoinedMoimSeqList) JSONException 발생하여 데이터 수신 못함");
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // textView.setText("That didn't work!");
                Log.i(TAG, "(getJoinedMoimSeqList) VolleyError 발생하여 데이터 수신 못함");
                Log.e(TAG, "(getJoinedMoimSeqList) error : " + String.valueOf(error));
            }
        }) {
            // 포스트 파라미터 넣기
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<String, String>();
                Log.i(TAG, "(getJoinedMoimSeqList) 3. HTTP 통신 전송하는 값, userSeq : "+ userSeq);
                params.put("userSeq", userSeq);
                return params;
            }

        };
        queue.add(stringRequest);
    }

    private void updatedKakaoLoginUi () {
        // 로그인 여부 확인
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {

            // invoke라는 메서드를 콜백으로 호출
            @Override
            public Unit invoke(User user, Throwable throwable) {
                // 로그인이 되어있다면,
                if (user != null) {

                    Log.i(TAG, "이미 로그인되어있습니다");
                    String kakaoId = String.valueOf(user.getId());
                    String kakaoName = user.getKakaoAccount().getProfile().getNickname();
                    String kakaoProfileImage = user.getKakaoAccount().getProfile().getThumbnailImageUrl();

                    HashMap<String, String> kakaoInfo = new HashMap<String, String>();
                    kakaoInfo.put("kakaoId", kakaoId);
                    kakaoInfo.put("kakaoName", kakaoName);
                    kakaoInfo.put("kakaoProfileImage", kakaoProfileImage);

                    Log.i(TAG, String.valueOf(kakaoInfo));

                    // 카카오 정보를 서버에 전달
                    loginKakao(kakaoInfo);


                } else {
//                    name.setText(null);
//                    profileImage.setImageURI(null);
                    Log.i(TAG, "로그인되어있지 않습니다.");

                }
                return null;
            }
        });
    }



    // HTTP통신 매서드 2. 카카오 회원가입 또는 로그인 프로세스
    // (통신방식)
    // (클라이언트→서버)
    // (서버→클라이언트)
    // 사용자가 카카오톡 로그인이 되었있는지를 확인

    private void loginKakao(HashMap<String, String> kakaoInfo) {

        Log.i(TAG, "loginKakao 동작하는지 확인");
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = serverAddress + "somoim/login/loginKakao.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "response = " + response);


                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        String userSeqFromServer = jsonResponse.getString("userSeq");
                        Log.i(TAG, "userSeqFromServer = " + userSeqFromServer);


                        // case 1. 등록된 카카오 아이디가 없는 경우

                        if(userSeqFromServer.equals("0")){
                            Log.i(TAG, "이미등록된 아이디가 없습니다");
                            editor.putString("kakaoName", kakaoInfo.get("kakaoName"));
                            editor.putString("kakaoProfileImage", kakaoInfo.get("kakaoProfileImage"));
                            editor.putString("kakaoId", kakaoInfo.get("kakaoId"));
                            editor.putString("signUpProcess", "kakao");
                            editor.commit();

                            Log.i(TAG, "sp 저장여부 확인, kakaoName = " + sp.getString("kakaoName",""));
                            Log.i(TAG, "sp 저장여부 확인, kakaoProfileImage = " + sp.getString("kakaoProfileImage",""));
                            Log.i(TAG, "sp 저장여부 확인, kakaoId = " + sp.getString("kakaoId",""));

                            Intent intent = new Intent(getApplicationContext(), SignUpSMS.class);
                            startActivity(intent);


                        }
                        // case 2. 등록된 카카오 아이디가 있는 경우
                        else {

                            Log.i(TAG, "이미등록된 아이디가 있습니다");
                            editorLogin.putString("userSeq", userSeqFromServer);

                            editorLogin.commit();

                            Intent intent = new Intent(getApplicationContext(), MoimSearchActivity.class);
                            startActivity(intent);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // textView.setText("That didn't work!");
                Log.e("error : ", String.valueOf(error));
            }
        }) {
            // 포스트 파라미터 넣기
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<String, String>();

                Log.i(TAG, "서버로 보내는 값 : " + String.valueOf(kakaoInfo));
                params.put("kakaoId", kakaoInfo.get("kakaoId"));
                params.put("kakaoName", kakaoInfo.get("kakaoName"));
                params.put("kakaoProfileImage", kakaoInfo.get("kakaoProfileImage"));
                return params;
            }

        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "(onResume()) 호출");

        //카카오 회원가입 중간에 취소한 경우를 대비해서 초기화
        editor.clear();
        editor.commit();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serviceIntent!=null) {
            stopService(serviceIntent);
            serviceIntent = null;
        }
    }
}