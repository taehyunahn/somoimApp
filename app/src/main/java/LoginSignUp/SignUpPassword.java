package LoginSignUp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.somoim.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class SignUpPassword extends AppCompatActivity {
    Button btn_next;

    TextInputLayout til_password, til_passwordCheck;
    EditText et_password, et_passwordCheck;

    Boolean pwValid = false;


    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private static final String TAG = "SignUpPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_password);
        Log.i(TAG, "(onCreate()) 호출");

        // UI 연결
        btn_next = findViewById(R.id.btn_next);
        til_password = findViewById(R.id.til_password);
        til_passwordCheck = findViewById(R.id.til_passwordCheck);
        et_password = findViewById(R.id.et_password);
        et_passwordCheck = findViewById(R.id.et_passwordCheck);

        // sharedPreference 세팅
        sp = getSharedPreferences("signUp", Activity.MODE_PRIVATE);
        editor = sp.edit();

        // 다음 버튼 클릭 시
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "비밀번호 입력 후, 다음 버튼 클릭");
                if(!pwValid){
                    Log.i(TAG, "입력한 비밀번호가 유효하지 않습니다");
                    Toast.makeText(SignUpPassword.this, "비밀번호를 다시 확인해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "입력한 비밀번호가 유효합니다. SingUpSMS(문자인증) 화면으로 이동합니다");
                    Intent intent = new Intent(getApplicationContext(), SignUpSMS.class);
                    String userPassword = et_password.getText().toString();
                    Log.i(TAG, "sharedPreference(signUp)로 저장하는 값, key : password, value : " + userPassword);
                    editor.putString("password", userPassword);
                    editor.commit();
                    Log.i(TAG, "sharedPreference(signUp)로 저장된 값 확인, key : password, value : " + sp.getString("password", ""));
                    startActivity(intent);
                }

            }
        });

        //비밀번호 입력 형식이 잘못되면, 에러 띄움
        et_password = til_password.getEditText();
        et_password.addTextChangedListener(new TextWatcher() {
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
                String pwPattern = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&])[A-Za-z[0-9]$@$!%*#?&]{8,}$";
                Boolean pwPatternValid = Pattern.matches(pwPattern,s);
                if(pwPatternValid == false) {
                    til_password.setError("비밀번호 형식이 잘못되었습니다. 영문+숫자 조합 8자리 이상 입력해주세요");
                    pwValid = false;

                } else {
                    til_password.setHelperText("사용가능한 비밀번호입니다");
                    pwValid = true;
                }
            }
        });



        //위에 입력한 비밀번호와 '비밀번호 확인'에 입력한 값이 다를 경우 에러 메세지 띄움
        et_passwordCheck = til_passwordCheck.getEditText();
        et_passwordCheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().equals(et_password.getText().toString())) {
                    Log.e("'비밀번호'에 입력한 값 : ", et_password.getText().toString());
                    Log.e("'비밀번호 확인'에 입력한 값 : ' ", s.toString());
                    til_passwordCheck.setError("비밀번호가 일치하지 않습니다.");
                    pwValid = false;

                } else {
                    til_passwordCheck.setHelperText("비밀번호가 일치합니다.");
                    pwValid = true;

                }
            }
        });




    }
}