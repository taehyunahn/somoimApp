package LoginSignUp;

import static Common.StaticVariable.serverAddress;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OtherLogin extends AppCompatActivity {

    TextView tv_findPassword, tv_signUp;
    ImageButton ib_back; // 뒤로가기 버튼
    Button btn_login;
    private static final String TAG = "OtherLogin";
    EditText et_password, et_email;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_login);

        Log.i(TAG, "(onCreate() 1. 호출)");

        // UI 연결
        tv_findPassword = findViewById(R.id.tv_findPassword); // 비밀번호 찾기
        tv_signUp = findViewById(R.id.tv_signUp); // 회원가입
        ib_back = findViewById(R.id.ib_back); // 뒤로가기 버튼
        btn_login = findViewById(R.id.btn_login); // 로그인 버튼
        et_password = findViewById(R.id.et_password); // 비밀번호 입력값
        et_email = findViewById(R.id.et_email); // 이메일 입력값

        // sharedPreference 세팅
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();

        SpannableString spannableString = new SpannableString(tv_findPassword.getText());
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
        tv_findPassword.setText(spannableString);

        // 뒤로가기 버튼 클릭시
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 액티비티 닫기
            }
        });

        // 회원가입 버튼 클릭시
        tv_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "회원가입 버튼 클릭");
                Intent intent = new Intent(getApplicationContext(), SignUpEmail.class);
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "로그인 버튼 클릭");
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();
                Log.i(TAG, "로그인을 위한 HTTP 통신 메서드, loginProcessWithVolley(email, password) 호출");
                loginProcess(email, password);
            }
        });

    }

//    String url = serverAddress + "somoim/signUp/test.php";

    private void loginProcess(String email, String password) {
        Log.i(TAG, "(loginProcess) 1. 호출 - 로그인을 위한 HTTP 통신");
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = serverAddress + "somoim/login/loginProcess.php";
        Log.i(TAG, "(loginProcess) 2. HTTP 통신 URL : " + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.i(TAG, "(loginProcess) 4. 서버에서 전달받은 값, response : " + response);

                if (response.equals("0")) {
                    Toast.makeText(getApplicationContext(), "로그인 정보를 확인하세요", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        String userSeq = jsonResponse.getString("userSeq");
                        String userName = jsonResponse.getString("userName");
                        String resign = jsonResponse.getString("resign"); // 회원탈퇴 여부 확인
//                        String myRoom_id = jsonResponse.getString("myRoom_id"); // 해당 계정이 들어간 채팅방 모음

                        Log.i(TAG, "userSeq = " + userSeq);
                        Log.i(TAG, "userName = " + userName);
                        Log.i(TAG, "resign = " + resign);
//                        Log.i(TAG, "myRoom_id = " + myRoom_id);

                        if(resign.equals("resign")){
                            Toast.makeText(getApplicationContext(), "회원탈퇴한 계정입니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            editor.putString("userSeq", userSeq);
                            editor.putString("userName", userName);
//                            editor.putString("myRoom_id", myRoom_id);
                            editor.commit();
                            Intent intent = new Intent(getApplicationContext(), KakaoLoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
//                        params.put("userSeq", userSeq);
                params.put("email", email);
                params.put("password", password);
                Log.i(TAG, "(loginProcess) 3. 서버로 보내는 값, params : " + String.valueOf(params));

                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

}