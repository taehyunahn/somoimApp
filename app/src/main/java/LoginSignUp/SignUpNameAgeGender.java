package LoginSignUp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.somoim.R;
import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import Common.DataClass;
import Common.NetworkClient;
import Common.StaticVariable;
import Common.UploadApis;
import MoimCreate.AddressList;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class SignUpNameAgeGender extends AppCompatActivity {

    // 레트로핏 통신 공통
    NetworkClient networkClient;
    UploadApis uploadApis;

    Button btn_next;
    DatePickerDialog.OnDateSetListener setListener;
//    EditText et_birthday;
    TextView tv_birthday, tv_address;
    EditText et_name, et_intro;
    RadioGroup radioGroup;
    RadioButton radioButton, radioButton2;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    SharedPreferences spLogin;
    SharedPreferences.Editor editorLogin;

    ImageView iv_profileImage;
    Uri selectedUri;
    Bitmap bitmap;
    String encodeImage;

    String kakaoId, email, password, phoneNumber, ProfileImage, name, gender, birthday, address, intro;

    private ArrayList<File> fileArrayList1 = new ArrayList<>();

    private static final String TAG = "SignUpNameAgeGender";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_name_age_gender);
        Log.i(TAG, "(onCreate()) 1. 호출");

        // UI연결
        btn_next = findViewById(R.id.btn_next);
        tv_birthday = findViewById(R.id.tv_birthday);
        tv_address = findViewById(R.id.tv_address);
        et_name = findViewById(R.id.et_name);
        et_intro = findViewById(R.id.et_intro);
        radioGroup = findViewById(R.id.radioGroup);
        radioButton = findViewById(R.id.radioButton);
        radioButton2 = findViewById(R.id.radioButton2);

        iv_profileImage = findViewById(R.id.iv_profileImage); // 프로필 사진

        // sharedPreference 세팅
        sp = getSharedPreferences("signUp", Activity.MODE_PRIVATE);
        editor = sp.edit();

        // sharedPreference 세팅
        spLogin = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editorLogin = spLogin.edit();


        // 카카오로 회원가입하는 경우
        Log.i(TAG, "(onCreate()) 2. 카카오톡으로 회원가입하는 경우를 위해, sharedPreference(SignUp)으로 값을 꺼낸다");

        String signUpProcess = sp.getString("signUpProcess", "");
        String kakaoName = sp.getString("kakaoName", "");
        String kakaoProfileImage = sp.getString("kakaoProfileImage", "");
        String kakaoId = sp.getString("kakaoId", "");
        Log.i(TAG, "(onCreate()) 3. sharedPreference(SignUp)에 저장된 값, (1)signUpProcess : " + signUpProcess + " / (2)kakaoName : " + kakaoName
                                                                        + " / (3)kakaoProfileImage : " + kakaoProfileImage + " / (4)kakaoId : " + kakaoId);
        if(signUpProcess.equals("kakao")){
            Log.i(TAG, "(onCreate()) 4. 카카오로 회원가입한 경우, 이름과 프로필 사진을 자동입력한다. kakaoName : " + et_name + "kakaoProfileImage : " + kakaoProfileImage);
            et_name.setText(kakaoName);
            Glide.with(getApplicationContext()).load(kakaoProfileImage).circleCrop().into(iv_profileImage);
        }


        Log.i(TAG, "(onCreate()) 5. Calendar 객체 생성");

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR) ;
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        Log.i(TAG, "(onCreate()) 6. Calendar 관련 final 변수 선언, year : " + year + " / month : " + month + " / day : " + day);

//        final int year = calendar.get(Calendar.YEAR) - 20 ;
//        final int month = 0;
//        final int day = 1;

        // 생년월일 입력창 클릭
        tv_birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "생년월일 입력창 클릭 - DatePickerDialog");
                Log.i(TAG, "(DatePickerDialog) 1. DatePickerDialog 객체 생성");
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        SignUpNameAgeGender.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        setListener, year, month, day);
                Log.i(TAG, "(DatePickerDialog) 2. 다이얼로그의 백그라운드 색상 지정");
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Log.i(TAG, "(DatePickerDialog) 3. 다이얼로그 show 메서드 호출");
                datePickerDialog.show();
            }
        });

        setListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.i(TAG, "(DatePickerDialog.OnDateSetListener) 1. onDateSet 메서드 호출");

                month = month + 1;
//                String date = day + "/" + month + "/" + year;
                String date = year + "." + month + "." + day;
                tv_birthday.setText(date);

                Log.i(TAG, "(DatePickerDialog.OnDateSetListener) 2. month = month + 1 : " + month);
                Log.i(TAG, "(DatePickerDialog.OnDateSetListener) 2. String date = year + \".\" + month + \".\" + day : " + date);

                // 최정 선택한 값을 shared에 저장한다.
            }
        };



        // 회원가입완료 버튼 클릭시
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "회원가입완료 버튼 클릭 - HTTP 통신을 위한 작업 진행");

                //라디오그룹에서 선택한 버튼의 값을 얻기 위한 프로세스
                Log.i(TAG, "(회원가입 버튼 클릭) 1. 선택한 radioButton의 텍스는 구하는 절차");
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                Log.i(TAG, "(회원가입 버튼 클릭) 1. 선택한 radioButton의 텍스는 구하는 절차");
                View radioButton = radioGroup.findViewById(radioButtonID);
                int idx = radioGroup.indexOfChild(radioButton);
                RadioButton r = (RadioButton) radioGroup.getChildAt(idx);
                String finalGender = "";
                if(r != null){
                    String selectedText = r.getText().toString();
                    finalGender = selectedText;
                }

                // (통합) 카카오고유값, 이메일, 비밀번호, 휴대폰번호, 프로필 사진, 이름, 성별, 생년월일, 지역구, 자기소개
                // 최종적으로 서버로 보낼 값을 담은 변수
                String finalKakaoId = sp.getString("kakaoId", ""); // 카카오 고유값
                String finalEmail = sp.getString("email", ""); // 이메일
                String finalPassword = sp.getString("password", ""); // 비밀번호
                String finalPhoneNumber = sp.getString("phoneNumber", ""); // 휴대폰번호
                String finalName = et_name.getText().toString(); // 이름

                //카카오로 로그인한 경우에는 이미지 링크 서버에 전송
                String finalProfileImage = sp.getString("kakaoProfileImage", "");



                String finalBirthday = tv_birthday.getText().toString();
                String finalAddress = tv_address.getText().toString();
                String finalIntro = et_intro.getText().toString();

                HashMap signUpInfo = new HashMap<String, String>();
                signUpInfo.put("kakaoId", finalKakaoId);
                signUpInfo.put("email", finalEmail);
                signUpInfo.put("password", finalPassword);
                signUpInfo.put("phoneNumber", finalPhoneNumber);
                signUpInfo.put("name", finalName);
                signUpInfo.put("gender", finalGender);
                signUpInfo.put("birthday", finalBirthday);
                signUpInfo.put("address", finalAddress);
                signUpInfo.put("intro", finalIntro);
                signUpInfo.put("kakaoProfileImage", finalProfileImage);


                Log.i(TAG, "signUpInfo = " + String.valueOf(signUpInfo));

                if(selectedUri != null) {
                    String filePath = StaticVariable.createCopyAndReturnRealPath(getApplicationContext(), selectedUri);
                    if(filePath != null) {
                        Log.i(TAG, "filePath = " + filePath);
                        File file = new File(filePath);
                        Log.i(TAG, "file = " + file);
                        fileArrayList1.add(file); //서버에 저장하는 fileArrayList1
                        Log.i(TAG, "fileArrayList1 = " + fileArrayList1);
                    } else {
//                    // 파일 경로가 null인 경우? 서버에서 가져온 경우!
//                    photoPathList.add(String.valueOf(imageUri));
//                    Log.i(TAG, "서버로 보낼 imageUri = " + imageUri);
                    }
                }

                if(!finalName.equals("") && !finalGender.equals("") &&  !finalBirthday.equals("") && !finalAddress.equals("") && !finalIntro.equals("")) {
                    //서버에 회원정보 저장하고, 완료하면 다음 페이지로 이동
                    signUpInfoWithRetrofit(fileArrayList1, signUpInfo);
                    Intent intent = new Intent(getApplicationContext(), SignUpInterest.class);
                    startActivity(intent);
                 } else {
                    Toast.makeText(SignUpNameAgeGender.this, "모든 정보를 누락없이 입력해주세요", Toast.LENGTH_SHORT).show();
                }

            }
        });


        // 프로필 이미지 선택
        iv_profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //askPermission();  //사진을 갤러리에서 가져오기 버튼을 클릭시 작동 (아래 class 참조)
                //ImagePicker 라이브러리 사용 -> 카메라 촬영 또는 앨범 선택
                ImagePicker.with(SignUpNameAgeGender.this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

        // 지역 선택
        tv_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent = new Intent(getApplicationContext(), SignUpAddress.class);
//                getSearchResult.launch(intent);

                Intent intent = new Intent(getApplicationContext(), AddressList.class);
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);


            }
        });

        // 서버에 저장할 값
        // (이메일 로그인인 경우) 이메일, 비밀번호, 휴대폰번호, 프로필 사진, 이름, 성별, 생년월일, 지역구, 자기소개
        // (카카오 로그인인 경우) 카카오고유값, 이메일, 휴대폰번호, 프로필 사진, 이름, 성별, 생년월일, 지역구, 자기소개
        // (통합) 카카오고유값, 이메일, 비밀번호, 휴대폰번호, 프로필 사진, 이름, 성별, 생년월일, 지역구, 자기소개





    }

    // 프로필 이미지 선택 후 작업 Override
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( data != null ) {
            if (requestCode == 1 || requestCode == RESULT_OK || data.getData() != null) {
                selectedUri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    iv_profileImage.setImageBitmap(bitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private final ActivityResultLauncher<Intent> getSearchResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    String data = result.getData().getStringExtra("data");
                    tv_address.setText(data);

                }
            });


    // (설명) 레트로핏을 사용하여, 현재 리사이클러뷰에 띄워진 아이템 목록을 서버로 전송한다.
    // (동작) 고수고유값, 회원고유값을 보낸다. 파일을 생성한다. DB의 photo 테이블에 이미지주소를 insert하고, 고수고유값도 함께 저장한다.
    private void signUpInfoWithRetrofit(ArrayList<File> fileArrayList, HashMap<String, String> signUpInfo) {

        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // (통합) 카카오고유값, 이메일, 비밀번호, 휴대폰번호, 프로필 사진, 이름, 성별, 생년월일, 지역구, 자기소개
        // 텍스트 전송
        HashMap<String, RequestBody> requestMap = new HashMap<>();
        requestMap.put("kakaoId", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("kakaoId")));
        requestMap.put("email", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("email")));
        requestMap.put("password", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("password")));
        requestMap.put("phoneNumber", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("phoneNumber")));
        requestMap.put("name", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("name")));
        requestMap.put("gender", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("gender")));
        requestMap.put("birthday", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("birthday")));
        requestMap.put("address", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("address")));
        requestMap.put("intro", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("intro")));
        requestMap.put("kakaoProfileImage", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("kakaoProfileImage")));
        requestMap.put("fileCount", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(fileArrayList.size())));

        // 파일 전송
        ArrayList<MultipartBody.Part> files = new ArrayList<>();
        for (int i = 0; i < fileArrayList.size(); i++) {
            RequestBody fileRequestBody = RequestBody.create(MediaType.parse("image/jpg"), fileArrayList.get(i));
            MultipartBody.Part fileMultiPartBody = MultipartBody.Part.createFormData("imageName"+i, "("+i+")"+".jpg", fileRequestBody);
            files.add(fileMultiPartBody);
        }

        Call<DataClass> call = uploadApis.signUpInfoUpload(requestMap, files);
        call.enqueue(new Callback<DataClass>() {
            @Override
            public void onResponse(Call<DataClass> call, retrofit2.Response<DataClass> response) {
                Log.i(TAG, "onResponse = " + response);
                fileArrayList.clear();
                files.clear();
                Toast.makeText(SignUpNameAgeGender.this, "회원가입을 완료했습니다", Toast.LENGTH_SHORT).show();

                DataClass responseBody = response.body();

                String userSeq = responseBody.getUserSeq();
                String userName = responseBody.getUserName();
                editor.putString("userSeq", userSeq);
                editor.putString("userName", userName);
                editor.commit();

                editorLogin.putString("userSeq", userSeq);
                editorLogin.putString("userName", userName);
                editorLogin.commit();

//                Intent intent = new Intent(getApplicationContext(), SignUpInterest.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
            }

            @Override
            public void onFailure(Call<DataClass> call, Throwable t) {
                Log.i(TAG, "t = " + t);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "(onResume()) 1. 호출");
        String selectedAddress = sp.getString("selectedAddress", "");
        Log.i(TAG, "(onResume()) 2. sharedPreference(login)에 저장된 값, selectedAddress : " + selectedAddress + " (지역입력 창에 넣는다)");
        tv_address.setText(selectedAddress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "(onDestroy()) 1. 호출");
        Log.i(TAG, "(onDestroy()) 2. sharedPreference(signUp) 값 삭제, selectedAddress : " + sp.getString("selectedAddress", ""));
//        editor.remove("selectedInterest");
        editor.remove("selectedAddress");
        editor.commit();
        Log.i(TAG, "(onDestroy()) 2. sharedPreference(signUp) 값 삭제 확인, selectedAddress : " + sp.getString("selectedAddress", ""));
    }
}