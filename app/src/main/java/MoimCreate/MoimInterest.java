package MoimCreate;

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
import MoimSearch.MoimSearchActivity;

public class MoimInterest extends AppCompatActivity {
    Button btn_save;
    private BackKeyHandler backKeyHandler = new BackKeyHandler(this);
    ChipGroup chipGroup;
    Chip chip1, chip2, chip3, chip4, chip5, chip6, chip7, chip8;
    private static final String TAG = "MoimInterest";

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moim_interest);
        Log.i(TAG, "생명주기 onCreate");

        // UI연결
        btn_save = findViewById(R.id.btn_save);
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
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();

        // 저장 버튼 클릭시
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // chipGroup에서 선택된 chip의 text 값 얻는 방법
                List<Integer> a = chipGroup.getCheckedChipIds();
                Log.i(TAG, "chipGroup.getCheckedChipIds() = " + chipGroup.getCheckedChipIds());
                Log.i(TAG, "a.get(0) = " + a.get(0));
                Chip r = (Chip) chipGroup.getChildAt(chipGroup.indexOfChild(chipGroup.findViewById(a.get(0))));
                String selectedInterest = r.getText().toString();
                Log.i(TAG, "selectedInterest = " + selectedInterest);
                editor.putString("selectedInterest", selectedInterest);
                editor.commit();
                finish();



            }
        });
    }

    private void updateInterestWithVolley(String userSeq, String interestAll) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = serverAddress + "somoim/signUp/updateInterest.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("response : ", response);
                Intent intent = new Intent(getApplicationContext(), MoimSearchActivity.class);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "생명주기 onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "생명주기 onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "생명주기 onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "생명주기 onPause");
    }
}