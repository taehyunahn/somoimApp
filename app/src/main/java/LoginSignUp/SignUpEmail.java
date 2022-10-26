package LoginSignUp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.somoim.R;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpEmail extends AppCompatActivity {

    Button btn_next;
    TextInputLayout til_email;
    EditText et_email;
    Boolean emailValid = false;

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    CheckBox cb_agree1;

    private static final String TAG = "SignUpEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_email);
        Log.i(TAG, "(onCreate()) 호출");

        // UI 연결
        btn_next = findViewById(R.id.btn_next);
        til_email = findViewById(R.id.til_email);
        et_email = findViewById(R.id.et_email);
        cb_agree1 = findViewById(R.id.cb_agree1);


        // sharedPreference 세팅

        sp = getSharedPreferences("signUp", Activity.MODE_PRIVATE);
        editor = sp.edit();

        //이용약관 동의 체크박스
        cb_agree1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(cb_agree1.isChecked()) {
                    btn_next.setEnabled(true);
                    Log.i(TAG, "(onCreate()) 약관 동의 체크박스를 선택했습니다.");


                } else if(!cb_agree1.isChecked()){
                    btn_next.setEnabled(false);
                    Log.i(TAG, "(onCreate()) 약관 동의 체크박스를 해제했습니다.");
                }
            }
        });



        // 다음 버튼 클릭 시
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "다음 버튼을 클릭했습니다");

                if(!emailValid){
                    Toast.makeText(SignUpEmail.this, "이메일을 다시 확인해주세요!", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "이메일이 유효하지 않습니다.");

                } else {
                    Log.i(TAG, "이메일을 정상 입력하여, SingUpPassword(비빌번호 입력) 화면으로 이동합니다");
                    Intent intent = new Intent(getApplicationContext(), SignUpPassword.class);
                    String inputEmail = et_email.getText().toString();
                    Log.i(TAG, "sharedPreference(signUp)로 저장하는 값, key : email, value : " + inputEmail);
                    editor.putString("email", inputEmail);
                    editor.commit();
                    startActivity(intent);
                    Log.i(TAG, "sharedPreference(signUp)에 저장된 값 확인, key : email, value : " + sp.getString("email", ""));

                }

            }
        });


        et_email = til_email.getEditText(); // textInputLayout과 EditText 연결
        et_email.addTextChangedListener(new TextWatcher() { //textInputLayout 안에 있는 EditText 감지
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Pattern emailPattern = Patterns.EMAIL_ADDRESS; //이메일 정규식
                Matcher emailMatcher = emailPattern.matcher(s.toString());
                if(emailMatcher.find()){
                    til_email.setError(null);
                    String inputEmail = et_email.getText().toString(); //입력한 이메일주소
                    //이메일 존재 여부 확인
                    validateEmail(inputEmail);
                } else {
                    til_email.setError("올바른 이메일 주소를 입력해주세요");
                    emailValid = false;
                }

            }
        });
    }

    private void validateEmail(String inputEmail){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse=new JSONObject(response);
                    boolean success=jsonResponse.getBoolean("success");
                    if(success){
//                        til_email.setError("사용할 수 있는 이메일입니다");
                        til_email.setHelperText("사용할 수 있는 이메일입니다");
                        emailValid = true;
                    }
                    else{
                        til_email.setError("중복된 이메일입니다");
                        emailValid = false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        ValidateRequest validateRequest = new ValidateRequest(inputEmail,responseListener);
        RequestQueue queue= Volley.newRequestQueue(SignUpEmail.this);
        queue.add(validateRequest);
    }
}