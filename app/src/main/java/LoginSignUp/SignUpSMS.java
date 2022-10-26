package LoginSignUp;

import static Common.StaticVariable.serverAddress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.somoim.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUpSMS extends AppCompatActivity {

    Button btn_certify, btn_resend, btn_done, btn_next;
    ConstraintLayout certifyContainer;
    TextInputLayout til_phoneNumber, til_certifyNumber;
    EditText et_phoneNumber, et_certifyNumber;
    Boolean phoneNumberValid = false;

    Handler handler = new Handler();
    int i;
    TextView tv_timer;
    private static final String TAG = "SignUpSMS";
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_sms);
        Log.i(TAG, "(onCreate()) 호출");

        // UI 연결
        btn_next = findViewById(R.id.btn_next);

        btn_certify = findViewById(R.id.btn_certify); // SMS 인증 버튼
        tv_timer = findViewById(R.id.tv_timer);
        btn_resend = findViewById(R.id.btn_resend);
        btn_done = findViewById(R.id.btn_done);
        certifyContainer = findViewById(R.id.certifyContainer);
        til_phoneNumber = findViewById(R.id.til_phoneNumber);
        til_certifyNumber = findViewById(R.id.til_certifyNumber);
        et_phoneNumber = findViewById(R.id.et_phoneNumber);
        et_certifyNumber = findViewById(R.id.et_certifyNumber);

        // sharedPreference 세팅
        sp = getSharedPreferences("signUp", Activity.MODE_PRIVATE);
        editor = sp.edit();



        //비밀번호 입력 형식이 잘못되면, 에러 띄움
        et_phoneNumber = til_phoneNumber.getEditText();
        et_phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
//                String a = et_password.getText().toString();
                // 대소문자 구분 숫자 특수문자  조합 9 ~ 12 자리
                String phoneNumberPattern = "^01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$";
                Boolean phoneNumberPatternValid = Pattern.matches(phoneNumberPattern,s);
                if(phoneNumberPatternValid == false) {
                    til_phoneNumber.setError("휴대폰번호 형식이 잘못되었습니다.");
                    phoneNumberValid = false;

                } else {
                    til_phoneNumber.setHelperText("사용가능한 휴대폰번호입니다");
                    phoneNumberValid = true;
                }
            }
        });



        // SMS 인증 버튼 클릭
        // 1. 난수생성해서 DB에 저장
        // 2. 핸들러 동작 3분 시간제한
        btn_certify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "인증하기 버튼 클릭 → sendCertifySMS 메서드 호출");
                sendCertifySMS(et_phoneNumber.getText().toString());

            }
        });

        // 재요청 버튼 클릭
        btn_resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "인증번호 재요청 클릭");
                btn_resend.setVisibility(View.INVISIBLE);
                sendCertifySMS("0"); // 아무도 받을 수 없는 곳에 인증 요청 -> DB에 새로운 인증번호를 저장하기 위한 목적으로 사용됨.
                et_certifyNumber.setEnabled(true);
                et_certifyNumber.setText("");
            }
        });

        // 인증완료 버튼 클릭
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "인증완료 버튼 클릭");
                String inputCode = et_certifyNumber.getText().toString();
                String phoneNumber = et_phoneNumber.getText().toString();
                Log.i(TAG, "checkCertifyCode 메서드 호출");
                checkCertifyCode(phoneNumber, inputCode);
            }
        });


    }

    // SMS 전송 요청
    private void sendCertifySMS(String phoneNumber){
        Log.i(TAG, "(sendCertifySMS) 1. 메서드 호출");
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = serverAddress + "somoim/signUp/sendSms.php";
        Log.i(TAG, "(sendCertifySMS) 2. HTTP 통신 서버 URL : " + url);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "(sendCertifySMS) 4. HTTP 통신 결과 response : " + response + "(서버 측에서 생성한 인증번호)");

                if(response.equals("0")){ // 인증서 발송 실패
                    Log.i(TAG, "(sendCertifySMS) 5. HTTP 통신 결과 response가 0인 경우 : 통신 실패 ");
                    Toast.makeText(getApplicationContext(), "인증코드 발송에 실패했습니다", Toast.LENGTH_SHORT).show();

                } else { // 인증서 발송 성공
                    Log.i(TAG, "(sendCertifySMS) 5. HTTP 통신 결과 response가 0이 아닌 경우 : 통신 성공 ");
                    Log.i(TAG, "(sendCertifySMS) 6. 서버 측에서 해당 번호로 인증번호를 생성하여 발송한다.");
                    Toast.makeText(getApplicationContext(), "인증코드를 발송했습니다.", Toast.LENGTH_SHORT).show();

                    // 인증번호 입력 레이아웃 표시
                    certifyContainer.setVisibility(View.VISIBLE);
                    // 전화번호 입력 및 인증하기 비활성화
                    et_phoneNumber.setEnabled(false);
                    btn_certify.setEnabled(false);
                    btn_done.setVisibility(View.VISIBLE);

                    Log.i(TAG, "(sendCertifySMS) 7. 인증번호 타이머 스레드 동작 - 60초 후에 서버와 HTTP 통신 다시 요청(expireCertifyCode 메서드 호출)");
                    // 인증번호 타이머 가동
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (i = 60; i >= 0; i--){
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_timer.setText("남은 시간 : "+i + "초");
                                        if(tv_timer.getText().toString().equals("남은 시간 : 0초")){
                                            et_certifyNumber.setEnabled(false);
                                            et_certifyNumber.setText("인증번호를 다시 요청해주세요");
                                            btn_resend.setVisibility(View.VISIBLE); // 재전송 버튼 보이도록
//                                            btn_next.setVisibility(View.INVISIBLE);
//                                            btn_resend.setVisibility(View.VISIBLE);
//                                            // 새로운 인증코드를 서버에 추가하는 로직을 넣는다.
//                                            // 버튼을 인증번호 재전송으로 변경한다.
//                                            et_passwordCode.setText("인증번호를 다시 요청해주세요");
//                                            et_passwordCode.setEnabled(false);
                                            tv_timer.setText("인증번호 만료");
                                            et_phoneNumber.setEnabled(true);
                                            Log.i(TAG, "(sendCertifySMS) 8. expireCertifyCode 메서드 호출");
                                            expireCertifyCode(phoneNumber); // 서버에 새로운 코드로 업데이트, 중복사용 못하도록
                                            btn_done.setVisibility(View.GONE);
                                            btn_resend.setVisibility(View.VISIBLE);
                                            et_phoneNumber.setText("");
                                        }
                                    }
                                });



                                try{
                                    Thread.sleep(1000);
                                }catch (InterruptedException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();


                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // textView.setText("That didn't work!");
                Log.i(TAG, "error = " + error);

            }
        }) {
            // 포스트 파라미터 넣기
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("phoneNumber", phoneNumber);
                Log.i(TAG, "(sendCertifySMS) 3. HTTP 통신 서버로 전송하는 값, phoneNumber : " + phoneNumber + "(사용자가 입력한 휴대폰 번호)");
                return params;
            }

        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);


    }

    private void expireCertifyCode(String phoneNumber){
        Log.i(TAG, "(expireCertifyCode) 1. 메서드 호출 - 인증번호를 다시 생성해서 DB에 저장한다. 그러면 인증번호를 임의로 갱신하는 효과를 얻을 수 있따");
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//        String url = "http://54.180.115.147/somoim/signUp/expireCertifyCode.php";
        String url = serverAddress + "somoim/signUp/expireCertifyCode.php";
        Log.i(TAG, "(expireCertifyCode) 2. HTTP 통신 url : " + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "response = " + response);
                Log.i(TAG, "(expireCertifyCode) 4. HTTP 통신 결과 response(받을 정보 없음) : " + response);
//                try {
//                    JSONObject jsonResponse = new JSONObject(response);
//                    //jsonResponse.getString("userName"));
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // textView.setText("That didn't work!");
                Log.i(TAG, "error = " + error);

            }
        }) {
            // 포스트 파라미터 넣기
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("phoneNumber", phoneNumber);
                Log.i(TAG, "(expireCertifyCode) 3. HTTP 통신 서버로 보내느 값, phoneNumber : " + phoneNumber + ("아무데도 보내지 않으려고 0을 넣음)"));

                return params;
            }

        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }


    private void checkCertifyCode (String phoneNumber, String inputCode) {
        Log.i(TAG, "(checkCertifyCode) 1. 메서드 호출 - 입력한 인증번호와 서버에 저장된 인증번호가 맞는지 확인하는 목적");
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = serverAddress + "somoim/signUp/checkCertifyCode.php";
        Log.i(TAG, "(checkCertifyCode) 2. HTTP 서버 통신 url : " + url);

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i(TAG, "(checkCertifyCode) 3. HTTP 서버 통신 결과 response : " + response);

                    String codeFromServer = response;
                    Log.i(TAG, "(checkCertifyCode) 4. 서버에서 전달받은 인증번호 : " + codeFromServer);
                    Log.i(TAG, "(checkCertifyCode) 5. UI화면에 입력한 인증번호 : " + inputCode);

                    if(inputCode.equals(codeFromServer)){
                        Log.i(TAG, "(checkCertifyCode) 6. 입력한 인증번호가 일치하는 경우, SingUpNameAgeGender(회원정보 입력) 화면으로 이동");
                        Toast.makeText(SignUpSMS.this, "인증을 완료했습니다", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), SignUpNameAgeGender.class);
                        editor.putString("phoneNumber", et_phoneNumber.getText().toString()); //
                        editor.commit();
                        Log.i(TAG, "(checkCertifyCode) 7. 인증을 완료하면, sharedPreference(SignUp)에 값 저장, phoneNubmer : " + phoneNumber);
                        Log.i(TAG, "(checkCertifyCode) 8. sharedPreference(SignUp)에 저장된 값 확인, phoneNubmer : " + sp.getString("phoneNumber", ""));
                        startActivity(intent);
                    } else {
                        Log.i(TAG, "(checkCertifyCode) 6. 입력한 인증번호가 일치하지 않는 경우입니다");
                        Toast.makeText(SignUpSMS.this, "인증번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                    }


//                try {
//                    JSONObject jsonResponse = new JSONObject(response);
//                    //jsonResponse.getString("userName"));
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // textView.setText("That didn't work!");
                    Log.i(TAG, "(checkCertifyCode) error = " + error);

                }
            }) {
                // 포스트 파라미터 넣기
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("inputCode", inputCode);
                    params.put("phoneNumber", phoneNumber);

                    Log.i(TAG, "(checkCertifyCode) 3. HTTP 서버로 보내는 값, inputCode(입력한 인증코드) " + inputCode + " / phoneNumber : " + phoneNumber);

                    return params;
                }

            };

            // Add the request to the RequestQueue.
            queue.add(stringRequest);

    }
}