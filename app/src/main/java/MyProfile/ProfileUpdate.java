package MyProfile;

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

public class ProfileUpdate extends AppCompatActivity {
    // 레트로핏 통신 공통
    NetworkClient networkClient;
    UploadApis uploadApis;

    DatePickerDialog.OnDateSetListener setListener;
    //    EditText et_birthday;
    TextView tv_birthday, tv_address;
    EditText et_name, et_intro;
    RadioGroup radioGroup;
    RadioButton radioButton, radioButton2;

    TextView tv_save;
    SharedPreferences sp;
    SharedPreferences.Editor editor;


    ImageView iv_profileImage, iv_back;
    Uri selectedUri;
    Bitmap bitmap;
    String encodeImage;
    String profileImage;

    String kakaoId, email, password, phoneNumber, ProfileImage, name, gender, birthday, address, intro;

    private ArrayList<File> fileArrayList1 = new ArrayList<>();

    private static final String TAG = "ProfileUpdate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);
        Log.i(TAG, "(onCreate()) 1. 호출");

        // UI연결
        tv_birthday = findViewById(R.id.tv_birthday);
        tv_address = findViewById(R.id.tv_address);
        et_name = findViewById(R.id.et_name);
        et_intro = findViewById(R.id.et_intro);
        radioGroup = findViewById(R.id.radioGroup);
        radioButton = findViewById(R.id.radioButton);
        radioButton2 = findViewById(R.id.radioButton2);
        tv_save = findViewById(R.id.tv_save);
        iv_back = findViewById(R.id.iv_back);


        iv_profileImage = findViewById(R.id.iv_profileImage); // 프로필 사진



        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onCreate()) 2. sharedPreference(login) 값, userSeq : " + userSeq);


        Log.i(TAG, "(onCreate()) 3. receiveProfileInfo(userSeq) 매서드 호출 - 로그인한 계정의 정보 요청");
        receiveProfileInfo(userSeq);


        // 뒤로가기 버튼 클릭시
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "(뒤로가기 버튼 클릭) ProfileActivity로 이동");
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // 저장 버튼 클릭시
        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "(저장 버튼 리스너) 1. 클릭함");

                // (통합) 카카오고유값, 이메일, 비밀번호, 휴대폰번호, 프로필 사진, 이름, 성별, 생년월일, 지역구, 자기소개
                // 최종적으로 서버로 보낼 값을 담은 변수
                String finalProfileImage; // 파일로 서버에 보내서 저장한다. -> 별도 전송
                String finalName = et_name.getText().toString();

                //라디오그룹에서 선택한 버튼의 값을 얻기 위한 프로세스
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonID);
                int idx = radioGroup.indexOfChild(radioButton);
                RadioButton r = (RadioButton) radioGroup.getChildAt(idx);
                String selectedText = r.getText().toString();
                String finalGender = selectedText;

                String finalBirthday = tv_birthday.getText().toString();
                String finalAddress = tv_address.getText().toString();
                String finalIntro = et_intro.getText().toString();
                String userSeq = sp.getString("userSeq","");


                HashMap signUpInfo = new HashMap<String, String>();
                signUpInfo.put("name", finalName);
                signUpInfo.put("gender", finalGender);
                signUpInfo.put("birthday", finalBirthday);
                signUpInfo.put("address", finalAddress);
                signUpInfo.put("intro", finalIntro);
                signUpInfo.put("userSeq", userSeq);

                Log.i(TAG, "(저장 버튼 리스너) 2. 입력된 회원 정보를 변수에 담고 hashMap에 저장, signUpInfo : " + String.valueOf(signUpInfo));

                Log.i(TAG, "(저장 버튼 리스너) 3. 사진 선택한 결과 처리 방식");
                Log.i(TAG, "(저장 버튼 리스너) 3. 선택한 Uri(selectedUri)가 존재하는지 확인");
                if (selectedUri != null) {
                    Log.i(TAG, "(저장 버튼 리스너) 3-1. 선택한 Uri(selectedUri)가 존재함, selectedUri : " + selectedUri);
                    String filePath = StaticVariable.createCopyAndReturnRealPath(getApplicationContext(), selectedUri);
                    Log.i(TAG, "(저장 버튼 리스너) 3-2. Uri(selectedUri)의 파일경로(filePath)를 얻어냄");
                    if (filePath != null) {
                        Log.i(TAG, "(저장 버튼 리스너) 3-2. Uri(selectedUri)의 파일경로(filePath)를 얻어냄");
                        Log.i(TAG, "(저장 버튼 리스너) 3-3. 파일경로, filePath : " + filePath);
                        Log.i(TAG, "(저장 버튼 리스너) 3-4. 파일 객체(file) 객체 생성, 파일경로(filePath)를 매개변수로 사용 : " + filePath);
                        File file = new File(filePath);
                        fileArrayList1.add(file); //서버에 저장하는 fileArrayList1
                        Log.i(TAG, "(저장 버튼 리스너) 3-6. 파일(file)을 ArrayList(fileArrayList1)에 저장, file : " + file);
                    } else {
//                     파일 경로가 null인 경우? 서버에서 가져온 경우!
//                    photoPathList.add(String.valueOf(imageUri));
//                    Log.i(TAG, "서버로 보낼 imageUri = " + imageUri);
                    }
                }

//                Log.i(TAG, "(저장 버튼 리스너) 4. 위에서 생성한 fileArrayList1(ArrayList)와 signUpInfo(HashMap)을 매개변수로 사용하여 signUpInfoWithRetrofit 메서드 호출");
                profileInfoUpdate(fileArrayList1, signUpInfo);

            }
        });

        Calendar calendar = Calendar.getInstance();
//        final int year = calendar.get(Calendar.YEAR);
//        final int month = calendar.get(Calendar.MONTH);
//        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        final int year = calendar.get(Calendar.YEAR) - 20 ;
        final int month = 0;
        final int day = 1;

        tv_birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        ProfileUpdate.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        setListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        setListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
//                String date = day + "/" + month + "/" + year;
                String date = year + "." + month + "." + day;
                tv_birthday.setText(date);
            }
        };

        //캘린더 선택형
//        tv_birthday.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DatePickerDialog datePickerDialog = new DatePickerDialog(
//                        SignUpNameAgeGender.this, new DatePickerDialog.OnDateSetListener(){
//                    @Override
//                    public void onDateSet(DatePicker view, int year, int month, int day) {
//                        month = month + 1;
//                        String date = day+"/"+month + "/" + year;
//                        tv_birthday.setText(date);
//                    }
//                }, year, month, day);
//                datePickerDialog.show();
//            }
//        });

        // 프로필 이미지 선택
        iv_profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //askPermission();  //사진을 갤러리에서 가져오기 버튼을 클릭시 작동 (아래 class 참조)
                //ImagePicker 라이브러리 사용 -> 카메라 촬영 또는 앨범 선택
                ImagePicker.with(ProfileUpdate.this)
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
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
        if (data != null) {
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
    private void profileInfoUpdate(ArrayList<File> fileArrayList, HashMap<String, String> signUpInfo) {

        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // (통합) 카카오고유값, 이메일, 비밀번호, 휴대폰번호, 프로필 사진, 이름, 성별, 생년월일, 지역구, 자기소개
        // 텍스트 전송
        HashMap<String, RequestBody> requestMap = new HashMap<>();
        requestMap.put("name", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("name")));
        requestMap.put("gender", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("gender")));
        requestMap.put("birthday", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("birthday")));
        requestMap.put("address", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("address")));
        requestMap.put("intro", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("intro")));
        requestMap.put("fileCount", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(fileArrayList.size())));
        requestMap.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), signUpInfo.get("userSeq")));


        // 파일 전송
        ArrayList<MultipartBody.Part> files = new ArrayList<>();
        for (int i = 0; i < fileArrayList.size(); i++) {
            RequestBody fileRequestBody = RequestBody.create(MediaType.parse("image/jpg"), fileArrayList.get(i));
            MultipartBody.Part fileMultiPartBody = MultipartBody.Part.createFormData("imageName" + i, "(" + i + ")" + ".jpg", fileRequestBody);
            files.add(fileMultiPartBody);
        }

        Call<DataClass> call = uploadApis.profileInfoUpdate(requestMap, files);
        call.enqueue(new Callback<DataClass>() {
            @Override
            public void onResponse(Call<DataClass> call, retrofit2.Response<DataClass> response) {
                Log.i(TAG, "onResponse = " + response);
                fileArrayList.clear();
                files.clear();

                DataClass responseBody = response.body();

                String userSeq = responseBody.getUserSeq();
                String name = responseBody.getName();

                Log.i(TAG, "userSeq = "+userSeq);
                Log.i(TAG, "name = "+name);



                editor.putString("userName", name);
                editor.commit();



                Toast.makeText(ProfileUpdate.this, "회원정보를 수정했습니다", Toast.LENGTH_SHORT).show();
//                finish();

                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }

            @Override
            public void onFailure(Call<DataClass> call, Throwable t) {
                Log.i(TAG, "t = " + t);
            }
        });
    }


    private void receiveProfileInfo(String userSeq) {

        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        //    @POST("receiveProfileInfoWithRetrofit.php")
        Call<DataClass> call = uploadApis.receiveProfileInfoWithRetrofit(userSeq);
        call.enqueue(new Callback<DataClass>() {
            @Override
            public void onResponse(Call<DataClass> call, retrofit2.Response<DataClass> response) {
                Log.d("onResponse", response.body().toString());

                if(!response.isSuccessful()){
                    return;
                }

                DataClass responseBody = response.body();

                profileImage = responseBody.getProfileImage();
                String name = responseBody.getName();
                String birthday = responseBody.getBirthday();
                String address = responseBody.getAddress();
                String interest = responseBody.getInterest();
                String intro = responseBody.getIntro();
                String gender = responseBody.getGender();

                et_name.setText(name);
                tv_birthday.setText(birthday);
                tv_address.setText(address);
                et_intro.setText(intro);

                Log.i(TAG, "성별확인 = " + gender);

                if(gender.equals("남")){
                    radioButton.setChecked(true);
                } else {
                    radioButton2.setChecked(true);
                }

                editor.putString("selectedInterest", interest);
                editor.putString("selectedAddress", address);
                editor.putString("userName", name);
                editor.commit();

                Glide.with(getApplicationContext()).load(profileImage).circleCrop().into(iv_profileImage);

            }

            @Override
            public void onFailure(Call<DataClass> call, Throwable t) {
                Log.i(TAG, "onFailure = " + t);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.i(TAG, "(onResume()) 호출");
//
//        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
//        editor = sp.edit();
//        String userSeq = sp.getString("userSeq", "");
//        Log.i(TAG, "(onResume()) 1. sharedPreference(login) 값, userSeq : " + userSeq);
//
//        Log.i(TAG, "(onResume()) 2. receiveProfileInfoWithRetrofit 메서드 호출");
//        receiveProfileInfo(userSeq);

        Log.i(TAG, "(onResume()) 1. 호출");
        String selectedAddress = sp.getString("selectedAddress", "");
        String selectedInterest = sp.getString("selectedInterest", "");
        Log.i(TAG, "(onResume()) 2. sharedPreference(login)에 저장된 값, selectedAddress : " + selectedAddress + " (지역입력 창에 넣는다)");
        tv_address.setText(selectedAddress);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "(onDestroy()) 1. 호출");
        Log.i(TAG, "(onDestroy()) 2. sharedPreference(signUp) 값 삭제, selectedAddress : " + sp.getString("selectedAddress", ""));
        editor.remove("selectedAddress");
        editor.remove("selectedInterest");
        editor.commit();
        Log.i(TAG, "(onDestroy()) 2. sharedPreference(signUp) 값 삭제 확인, selectedAddress : " + sp.getString("selectedAddress", ""));
    }
}
