package MyProfile;

import static Common.StaticVariable.serverAddress;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class InterestEdit extends AppCompatActivity {

    private static final String TAG = "InterestEdit";
    ChipGroup chipGroup;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_edit);
        Log.i(TAG, "(onCreate()) 호출");


        // sharedPreference 세팅
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onCreate()) 1. sharedPreference(login) 값, userSeq : " + userSeq);

        //UI 설정
        chipGroup = findViewById(R.id.chipGroup);


        Intent intent = getIntent();
        String interest = intent.getStringExtra("interest");
        Log.i(TAG, "(onCreate()) 2. 인텐트로 전달받는 값, interest : " + interest);

        // onCreate()되면, 이전 화면에서 관심분야로 선택되어있는 항목들을 그대로 선택할 수 있도록 해야 한다.
        // 존재하는 각 chip의 텍스트가  interest 안에 포함된다면, 체크를 누르도록 하면 어떨까?

        Log.i(TAG, "(onCreate()) 3. chipGroup.getChildCount(), chip의 총 개수 : " + chipGroup.getChildCount());
        Log.i(TAG, "(onCreate()) 4. chipGroup.getChildAt(0), 0번째 chip의 정보(객체 생성 시 사용) : " + chipGroup.getChildAt(0));
        Chip chip  = (Chip) chipGroup.getChildAt(0);
        chip.getText();
        Log.i(TAG, "(onCreate()) 5. Chip chip  = (Chip) chipGroup.getChildAt(0) 객체 생성 후, 0번째 chip.getText() : " + chip.getText());

        Log.i(TAG, "(onCreate()) 6. chip의 총 개수만큼 반복문 동작 → chip 객체 생성 → 로그인 계정의 관심사 목록을 포함하고 있으면, check 함수 동작 시킬 것");
        for(int i = 0; i < chipGroup.getChildCount(); i++){
            Chip chipNew = (Chip) chipGroup.getChildAt(i);
            Log.i(TAG, "(onCreate()) 7. 로그인 계정의 관심분야 목록 interest : " + interest + " " + i + "번째 Chip의 텍스트 : " + chipNew.getText());
            if(interest.contains(chipNew.getText())){
            chipNew.setChecked(true);
            }

        }


//        for (int i = 0; i< interestList.length; i++){
//            Chip chip = new Chip(ProfileActivity.this);
//            chip.setText(interestList[i]);
////                    chip.setCloseIcon(getApplicationContext().getDrawable(R.drawable.ic_close));
//            chip.setTextAppearanceResource(R.style.ChipTextStyle);
//            chip.setChipBackgroundColorResource(androidx.cardview.R.color.cardview_dark_background);
//            chipGroup.addView(chip);
//        }
//
//


        // 관심분야 변경사항 저장
        TextView tv_save = (TextView) findViewById(R.id.tv_save);
        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "저장버튼클릭");
                // 현재 클릭되어있는 chip의 모든 텍스트를 ;로 붙여서 하나의 텍스트로 만든다
                Log.i(TAG, "(저장버튼클릭) 1. 전체 Chip의 개수, chipGroup.getChildCount() : " + chipGroup.getChildCount());
                Log.i(TAG, "(저장버튼클릭) 2. 반복문에서 Chip 객체를 생성하고, chip.isChecked된 경우에만 텍스트를 불러와서 StringBuilder에 저장");

                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < chipGroup.getChildCount(); i++){
                    Chip chip = (Chip)chipGroup.getChildAt(i);
                    if(chip.isChecked()){
                        chip.getText();
                        sb.append(chip.getText() + ";");
                        Log.i(TAG, "(저장버튼클릭) 2. " + i + "번째 chip의 text : " + chip.getText() + " / StringBuilder에 담긴 값 : " + sb.toString());
                    }
                }

                String interestAll = sb.toString();
                Log.i(TAG, "(저장버튼클릭) 3. 선택한 관심분야의 text를 종합한 값, interestAll : " + interestAll);

                Log.i(TAG, "(저장버튼클릭) 4. saveInterestEdit 메서드 호출, 최소한 하나 이상 선택할 경우만 동작");
                Log.i(TAG, "(저장버튼클릭) 4. chipGroup.getCheckedChipIds().size() : " + chipGroup.getCheckedChipIds().size());
                if(chipGroup.getCheckedChipIds().size() > 0) {
                    saveInterestEdit(userSeq, interestAll);
                } else {
                    Toast.makeText(InterestEdit.this, "관심분야를 최소 1개 이상 선택해야합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // 현재 계정의 고유값과 관심분야를 종합한 값을 서버로 보내서 저장한다.
    private void saveInterestEdit(String userSeq, String interestAll){
        Log.i(TAG, "(saveInterestEdit) 1. 메서드 호출 - 새롭게 선택한 관심분야 목록 종합값을 서버로 보내서 UPDATE 한다");

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = serverAddress + "somoim/myProfile/saveInterestEdit.php";
        Log.i(TAG, "(saveInterestEdit) 2. HTTP 통신 URL : " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "response = " + response);
                Log.i(TAG, "(saveInterestEdit) 4. HTTP 통신 서버에서 수신한 값, response : " + response);

//                try {
//                    JSONObject jsonResponse = new JSONObject(response);
//                    String userSeqFromServer = jsonResponse.getString("userSeq");
//                    Log.i(TAG, "userSeqFromServer = " + userSeqFromServer);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

                finish();
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                // 백스택 지우고 새로 만들어 전달
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

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
                params.put("userSeq", userSeq);
                params.put("interestAll", interestAll);
                Log.i(TAG, "(saveInterestEdit) 3. HTTP 통신 서버로 보내는 값, userSeq : " + userSeq + " / interestAll : " + interestAll);

                return params;
            }

        };
        queue.add(stringRequest);
    }
}
