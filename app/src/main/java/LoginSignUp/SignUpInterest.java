package LoginSignUp;

import static Common.StaticVariable.serverAddress;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Common.BackKeyHandler;

public class SignUpInterest extends AppCompatActivity {
    Button btn_next;
    private BackKeyHandler backKeyHandler = new BackKeyHandler(this);
    ChipGroup chipGroup;
    Chip chip1, chip2, chip3, chip4, chip5, chip6, chip7, chip8;
    private static final String TAG = "SignUpInterest";

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_interest);
        Log.i(TAG, "(onCreate() 호출)");

        // UI연결
        btn_next = findViewById(R.id.btn_next);
        chipGroup = findViewById(R.id.chipGroup);
        chip1 = findViewById(R.id.chip1);
        chip2 = findViewById(R.id.chip2);
        chip3 = findViewById(R.id.chip3);
        chip4 = findViewById(R.id.chip4);
        chip5 = findViewById(R.id.chip5);
        chip6 = findViewById(R.id.chip6);
        chip7 = findViewById(R.id.chip7);
        chip8 = findViewById(R.id.chip8);

        // sharedPreference 세팅
        sp = getSharedPreferences("signUp", Activity.MODE_PRIVATE);
        editor = sp.edit();

        // 다음 버튼 클릭시
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "다음 버튼 클릭");

                // chipGroup에서 선택된 chip의 text 값 얻는 방법
                List<Integer> a = chipGroup.getCheckedChipIds();
                Log.i(TAG, "1. chipeGroup 중에서 선택된 chipe의 id를 List 저장. List<Integer> a : " + a);
                StringBuilder sb = new StringBuilder();
                Log.i(TAG, "2. StringBuilder 객체 생성");
                Log.i(TAG, "3. 선택한 chip의 갯수만큼 반복문 동작(chip 객체 생성 → getText() → StringBuilder 객체에 append(chip 이름 + ;), size : " + a.size());
                for (int i = 0; i < a.size(); i++){
                    Chip r = (Chip) chipGroup.getChildAt(chipGroup.indexOfChild(chipGroup.findViewById(a.get(i))));
                    String selectedText = r.getText().toString();
                    sb.append(selectedText+";");
                    Log.i(TAG,  i + "번째 chip의 Text : " + selectedText + " / StringBuilder에 담긴 값 : " + sb.toString());
                }

                String interestAll = sb.toString();
                Log.i(TAG, "4. 선택한 chip의 text를 종합한 값 : " + interestAll);
//                result = result.substring(0, result.length() -1);

                String userSeq = sp.getString("userSeq","");
                Log.i(TAG, "5. 회원가입 후 sharedPreference(signUp)에 저장된 userSeq : " + interestAll);

                //관심분야 저장 HTTP 통신
                Log.i(TAG, "4. 선택한 chip의 text를 종합한 값 : " + interestAll);
                updateInterest(userSeq, interestAll);


//                Intent intent = new Intent(getApplicationContext(), MoimSearchActivity.class);
//                startActivity(intent);
            }
        });

    }
    @Override
    public void onBackPressed() {
        backKeyHandler.onBackPressed();
    }


    private void updateInterest(String userSeq, String interestAll) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = serverAddress + "somoim/signUp/updateInterest.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("response : ", response);
                Intent intent = new Intent(getApplicationContext(), KakaoLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                // 회원가입 절차가 끝나는 시점에는 sharedPreference(signUp) 초기화
                editor.clear();
                editor.commit();
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
                params.put("interest", interestAll);
                params.put("userSeq", userSeq);

                return params;
            }

        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

}