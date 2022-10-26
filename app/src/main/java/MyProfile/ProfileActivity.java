package MyProfile;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.util.ArrayList;

import Chat.ChatData;
import Chat.ChatService;
import Common.BackKeyHandler;
import Common.DataClass;
import Common.NetworkClient;
import Common.UploadApis;
import MoimCreate.MoimCreate;
import MoimSearch.MoimSearchActivity;
import MyMoim.MyMoimActivity;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import retrofit2.Call;
import retrofit2.Callback;

public class ProfileActivity extends AppCompatActivity {
    // 레트로핏 통신 공통
    NetworkClient networkClient;
    UploadApis uploadApis;

    private BackKeyHandler backKeyHandler = new BackKeyHandler(this);


    BottomNavigationView bottomNavigationView;
    TextView tv_logout;
    private static final String TAG = "ProfileActivity";
    final CharSequence[] colors = { "Pink", "Red", "Yellow", "Blue" };
    ArrayList<Integer> slist = new ArrayList();
    boolean icount[] = new boolean[colors.length];
    String msg ="";

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    ImageView iv_profileImage;
    TextView tv_name, tv_birthday, tv_address, tv_editProfile, tv_editInterest, tv_email;
    ChipGroup chipGroup;

    String interest;
    ProgressDialog dialog;
    NestedScrollView nestedScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.i(TAG, "(onCreate()) 1. 호출");

        // UI 연결
        bottomNavigationView = findViewById(R.id.bottom_navigator);
        tv_logout = findViewById(R.id.tv_logout);
        iv_profileImage = findViewById(R.id.iv_profileImage);
        tv_name = findViewById(R.id.tv_name);
        tv_birthday = findViewById(R.id.tv_birthday);
        tv_address = findViewById(R.id.tv_address);
        tv_editProfile = findViewById(R.id.tv_editProfile);
        tv_editInterest = findViewById(R.id.tv_editInterest);
        tv_email = findViewById(R.id.tv_email);
        chipGroup = findViewById(R.id.chipGroup);
        nestedScrollView = findViewById(R.id.nestedScrollView);

        // sharedPreference 세팅
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();

        // ProgressDialog 생성
        dialog = new ProgressDialog(ProfileActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("잠시만 기다려주세요.");
        dialog.show();


        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onCreate()) 2. sharedPreference(login)에 저장된 값을 변수에 담기, userSeq : " + userSeq);

        Log.i(TAG, "(onCreate()) 3. receiveProfileInfo 메서드 호출 - 프로필 정보 블러오기");
        // 프로필 정보 불러오기

        nestedScrollView.setVisibility(View.INVISIBLE);
        receiveProfileInfo(userSeq);

        //로그아웃
        tv_logout.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                android.app.AlertDialog.Builder ad = new AlertDialog.Builder(ProfileActivity.this); // context부분에 getAplicationContext를 쓰면 안된다! 기억할 것
                ad.setMessage("로그아웃하시겠습니까?");
                ad.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                ad.setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "로그아웃 버튼 클릭함");
                        Toast.makeText(ProfileActivity.this, "로그아웃했습니다", Toast.LENGTH_SHORT).show();
                        editor.remove("userSeq");
                        editor.commit();

//                List<Prompt> prompts = Arrays.asList(Prompt.LOGIN);
//                List<Prompt> prompts = Arrays.asList(Prompt.LOGIN);

//                UserApiClient.getInstance().logout(error -> {
//                    return null;
//                });
//
//                UserApiClient.getInstance().unlink(error -> {
//                    return null;
//                });

                        // 로그아웃 시 소켓 해제 스레드 동작작
                       new Thread() {
                            public void run() {
                                try {
                                    ChatService.chatData = new ChatData();
                                    ChatService.chatData.setCommand("EXIT"); // 소켓해제
                                    Gson gson = new Gson();
                                    String _chatMemberJson = gson.toJson(ChatService.chatData);
                                    Log.i(TAG, "_chatMemberJson = " + _chatMemberJson);
                                    ChatService.sendWriter.println(_chatMemberJson);
                                    ChatService.sendWriter.flush();
                                } catch (NullPointerException e) { // 소켓서버에 연결되지 않았을때 앱이 꺼지는 것을 방지 (oos가 null값임)
                                    e.printStackTrace();
                                }
                            }
                        }.start();




                        UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {

                                updateKakaoLoginUI();

                                return null;
                            }
                        });

                        // 동의한 것까지 삭제
                        UserApiClient.getInstance().unlink(new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                updateKakaoLoginUI();



                                return null;
                            }
                        });

                        Intent intent = new Intent(getApplicationContext(), KakaoLoginActivity.class);
                        // 백스택 지우고 새로 만들어 전달
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    }
                });
                ad.show();






            }
        });

        //수정(프로필)
        tv_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "프로필 사진 수정 클릭");
                Intent intent = new Intent(getApplicationContext(), ProfileUpdate.class);
                startActivity(intent);


//                Intent intent = new Intent(getApplicationContext(), ProfileUpdate.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
            }
        });

        // 관심분야 수정
        tv_editInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "관심분야 편집 버튼 클릭 → InterestEdit 화면으로 이동");
                Log.i(TAG, "InterestEdit 화면으로 전달하는 값, interest(현재계정의 관심분야 목록) : " + interest);
                Intent intent = new Intent(getApplicationContext(), InterestEdit.class);
                // 관심분야 목록을 다음 화면으로 전달한다
                intent.putExtra("interest", interest);
                startActivity(intent);
            }
        });


        // 프래그먼트처럼 하단 바
        bottomNavigationView.setSelectedItemId(R.id.profile);
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
//                        startActivity(new Intent(getApplicationContext(), MyMoimActivity.class));

                        Intent intent2 = new Intent(getApplicationContext(), MyMoimActivity.class);
//                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent2);

                        overridePendingTransition(0,0);


                        return true;
                    case R.id.profile:
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


    private void receiveProfileInfo(String userSeq) {
        Log.i(TAG, "(receiveProfileInfo) 1. 메서드 호출");

        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        //    @POST("receiveProfileInfoWithRetrofit.php")
        Call<DataClass> call = uploadApis.receiveProfileInfoWithRetrofit(userSeq);
        Log.i(TAG, "(receiveProfileInfo) 2. HTTP 통신 서버로 보내는 값, userSeq : " +userSeq);
        call.enqueue(new Callback<DataClass>() {
            @Override
            public void onResponse(Call<DataClass> call, retrofit2.Response<DataClass> response) {

                Log.i(TAG, "(receiveProfileInfo) 3. HTTP 통신 결과 response.body().toString() : " + response.body().toString());

                if(!response.isSuccessful()){
                    return;
                }

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


                DataClass responseBody = response.body();
                String profileImage = responseBody.getProfileImage();
                String name = responseBody.getName();
                String birthday = responseBody.getBirthday();
                String address = responseBody.getAddress();
                String email = responseBody.getEmail();
                interest = responseBody.getInterest();

                Gson gson = new Gson();
                String object = gson.toJson(responseBody);
                Log.i(TAG, "(receiveProfileInfo) 4. HTTP 통신을 통해 전달받은 객체 정보 : " + object);

                tv_name.setText(name);
                Log.i(TAG, "(receiveProfileInfo) 5. 이름 설정, name : " + name);
                tv_email.setText(email);
                Log.i(TAG, "(receiveProfileInfo) 6. 이메일 설정, email: " + email);
                tv_birthday.setText(birthday);
                Log.i(TAG, "(receiveProfileInfo) 7. 생년월일 설정, birthday : " + birthday);
                tv_address.setText(address);
                Log.i(TAG, "(receiveProfileInfo) 8. 이름 설정, address : " + address);
                Glide.with(getApplicationContext()).load(profileImage).circleCrop().into(iv_profileImage);
                Log.i(TAG, "(receiveProfileInfo) 9. 프로필사진 설정, profileImage : " + profileImage);

                // 관심분야 설정 → Chip 생성
                Log.i(TAG, "(receiveProfileInfo) 10. 관심분야 설정(묶음을 받아서 ;를 기준으로 split 하고 Chip 생성), interest : " + interest);
                String[] interestList = interest.split(";");
                Log.i(TAG, "(receiveProfileInfo) 10-1. ';'를 기준으로 분리해서 String[] interestList에 저장");
                Log.i(TAG, "(receiveProfileInfo) 10-2. String[] interest 사이즈만큼 반복문 동작시키며, Chip 생성 (interestList.length : " + interestList.length + ")");
                for (int i = 0; i< interestList.length; i++){
                    Chip chip = new Chip(ProfileActivity.this);
                    chip.setText(interestList[i]);
                    Log.i(TAG, "(receiveProfileInfo) 10-3. interestList[i] : " + interestList[i]);
//                    chip.setCloseIcon(getApplicationContext().getDrawable(R.drawable.ic_close));
                    chip.setTextAppearanceResource(R.style.ChipTextStyle);
                    chip.setChipBackgroundColorResource(androidx.cardview.R.color.cardview_dark_background);
                    chipGroup.addView(chip);
                }


            }

            @Override
            public void onFailure(Call<DataClass> call, Throwable t) {
                Log.i(TAG, "(receiveProfileInfo) 서버통신 실패 onFailure 호출 : " + t);
            }
        });


    }

    private void updateKakaoLoginUI() {
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null) {
                    Log.i("1카카오1", "2");


                } else {

                    Log.i("1카카오1", "로그아웃");

//                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);//액티비티 스택제거
//                    startActivity(intent);


                }
                return null;
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "(onResume()) 호출");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "(onStop()) 호출");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "(onPause()) 호출");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "(onDestroy()) 호출");

    }



}