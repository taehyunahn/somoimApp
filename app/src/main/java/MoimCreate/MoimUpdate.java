package MoimCreate;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.somoim.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import Common.DataClass;
import Common.NetworkClient;
import Common.StaticVariable;
import Common.UploadApis;
import MoimDetail.MoimDetailMain;
import MoimSearch.MoimData;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class MoimUpdate extends AppCompatActivity {

    TextView tv_address, tv_interest;
    TextInputLayout til_moimTitle, til_moimContent;
    EditText et_moimTitle, et_moimContent, et_memberCount;
    Button btn_save;
    ImageView iv_mainImage;

    // sharedPreference 세팅
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    // 레트로핏 통신 공통 변수(클래스 연결)
    NetworkClient networkClient;
    UploadApis uploadApis;

    private static final String TAG = "MoimUpdate(모임수정)";

    private ArrayList<File> fileArrayList1 = new ArrayList<>();

    Uri selectedUri;
    Bitmap bitmap;

    String mainImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moim_update);
        Log.i(TAG, "(onCreate() 1. 호출)");

        // UI 연결
        tv_address = findViewById(R.id.tv_address); // 주소
        et_memberCount = findViewById(R.id.et_memberCount); // 모임정원수
        til_moimTitle = findViewById(R.id.til_moimTitle); //
        et_moimTitle = findViewById(R.id.et_moimTitle); // 모임 제목
        btn_save = findViewById(R.id.btn_save); // 모임만들기 버튼
        til_moimContent = findViewById(R.id.til_moimContent);
        et_moimContent = findViewById(R.id.et_moimContent); // 모임 내용/목표
        iv_mainImage = findViewById(R.id.iv_mainImage); // 메인 이미지
        tv_interest = findViewById(R.id.tv_interest); // 관심분야

        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onCreate()) 2. sharedPreference(login) 값, userSeq : " + userSeq);


        // 현재선택한 모임의 고유값 얻기기
        Intent intent = getIntent();
        String moimSeq = intent.getStringExtra("moimSeq");
        Log.i(TAG, "(onCreate()) 2. Intent로 받은 값, moimSeq : " + moimSeq);

        // 모임정보를 불러오는 메서드 동작
        Log.i(TAG, "(onCreate()) 3. showMoimInfo(moimSeq) 메서드 호출 : " + moimSeq);
        showMoimInfo(moimSeq);


        // 주소 클릭시
        tv_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), SignUpAddress.class);
//                getSearchResult.launch(intent);
                Log.i(TAG, "(주소 클릭 리스너) 1. 호출 - AddressListUpdate.class로 이동");
                Intent intent = new Intent(getApplicationContext(), AddressListUpdate.class);
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });

        // 관심분야 클릭시
        tv_interest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "(관심분야 클릭 리스너) 1. 호출 - MoimInterest.class로 이동");
                Intent intent = new Intent(getApplicationContext(), MoimInterest.class);
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });

        // 메인 이미지 선택
        iv_mainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "(메인사진 클릭 리스너) 1. 호출 - 라이브러리 사용(앨범 또는 카메라 촬영");
                //askPermission();  //사진을 갤러리에서 가져오기 버튼을 클릭시 작동 (아래 class 참조)
                //ImagePicker 라이브러리 사용 -> 카메라 촬영 또는 앨범 선택
                ImagePicker.with(MoimUpdate.this)
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });




        // 수정완료 버튼
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 서버에 저장하는 메서드 동작
                Log.i(TAG, "(수정 완료 리스너) 1. 호출");

                String address = tv_address.getText().toString();
                String title = et_moimTitle.getText().toString();
                String content = et_moimContent.getText().toString();
                String memberCount = et_memberCount.getText().toString();
                String interest = tv_interest.getText().toString();
                String userSeq = sp.getString("userSeq","");

                // 서버로 보낼 값 (onCreate 안에 선언)
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("address", address);
                hashmap.put("title", title);
                hashmap.put("content", content);
                hashmap.put("memberCount", memberCount);
                hashmap.put("interest", interest);
                hashmap.put("userSeq", userSeq);
                hashmap.put("moimSeq", moimSeq);
                hashmap.put("mainImage", mainImage);
                hashmap.put("fileCount", String.valueOf(fileArrayList1.size()));


                Log.i(TAG, "(수정 완료 리스너) 2. 입력값 전체 hashmap에 저장, hashmap : " + String.valueOf(hashmap));
                Log.i(TAG, "(수정 완료 리스너) 3. updateMoimInfo(fileArrayList1, hashmap) 메서드 호출 - HTTP 통신");
                updateMoimInfo(fileArrayList1, hashmap);

            }
        });


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
                    iv_mainImage.setImageBitmap(bitmap);

                    if (selectedUri != null) {
                        String filePath = StaticVariable.createCopyAndReturnRealPath(getApplicationContext(), selectedUri);
                        if (filePath != null) {
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
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "(onResume()) 1. 호출");

        String selectedInterest = sp.getString("selectedInterest", "");
        String selectedAddress = sp.getString("selectedAddress", "");

        tv_interest.setText(selectedInterest);
        tv_address.setText(selectedAddress);

        Log.i(TAG, "(onResume()) selectedInterest = " + selectedInterest);
        Log.i(TAG, "(onResume()) selectedAddress = " + selectedAddress);

    }

    private void updateMoimInfo(ArrayList<File> fileArrayList, HashMap<String, String> hashmap) {
        Log.i(TAG, "(updateMoimInfo) 1. 호출 - 모임 관련 변경사항을 서버에 저장");

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("address", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("address")));
        textToServer.put("title", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("title")));
        textToServer.put("content", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("content")));
        textToServer.put("memberCount", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("memberCount")));
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        textToServer.put("interest", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("interest")));
        textToServer.put("moimSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("moimSeq")));
        textToServer.put("mainImage", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("mainImage")));
        textToServer.put("fileCount", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(fileArrayList.size())));

        Log.i(TAG, "(updateMoimInfo) 2. 서버로 보내는 String 값, hashmap : " + String.valueOf(hashmap));

        // 서버에 전송할 파일값 저장
        ArrayList<MultipartBody.Part> filesToServer = new ArrayList<>();
        for (int i = 0; i < fileArrayList.size(); i++) {
            RequestBody fileRequestBody = RequestBody.create(MediaType.parse("image/jpg"), fileArrayList.get(i)); // fileArraylist에 파일을 담아서 넣는다
            MultipartBody.Part fileMultiPartBody = MultipartBody.Part.createFormData("imageName"+i, "("+i+")"+".jpg", fileRequestBody);
            filesToServer.add(fileMultiPartBody);
        }
        if(fileArrayList.size() > 0){
            Log.i(TAG, "(updateMoimInfo) 3. 서버로 보내는 파일, file : " + fileArrayList.get(0));
        }


        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<DataClass> call = uploadApis.updateMoimInfo(textToServer, filesToServer);
        call.enqueue(new Callback<DataClass>() {
            @Override
            public void onResponse(Call<DataClass> call, retrofit2.Response<DataClass> response) {

                Log.i(TAG, "(updateMoimInfo) 4. 서버로부터 HTTP 통신 응답, response : " + response);

                fileArrayList.clear();
                Log.i(TAG, "(updateMoimInfo) 5. fileArrayList.clear()");
                filesToServer.clear();
                Log.i(TAG, "(updateMoimInfo) 6. filesToServer.clear()");


                DataClass responseBody = response.body();
                String success = responseBody.getSuccess();
                String moimSeq = responseBody.getMoimSeq();

                Gson gson = new Gson();
                String object = gson.toJson(responseBody);
                Log.i(TAG, "object : " + object);

                Log.i(TAG, "(updateMoimInfo) 7. 서버로부터 수신한 객체(DataClass) 정보, object : " + object);

                Log.i(TAG, "(updateMoimInfo) 8. success가 1인 경우는 DB 저장 성공, success : " + success);
                if(success.equals("1")){
                    Toast.makeText(MoimUpdate.this, "모임정보를 수정했습니다", Toast.LENGTH_SHORT).show();

                    Log.i(TAG, "(updateMoimInfo) 9. finish()하고, MoimDetailMain.class로 이동");
                    finish();

                    Intent intent = new Intent(getApplicationContext(), MoimDetailMain.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("moimSeq", moimSeq); // 모임의 고유값을 전달 (모임 상세보기)
                    intent.putExtra("fromMoimUpdate", true);
                    startActivity(intent);
                }
            }
            @Override
            public void onFailure(Call<DataClass> call, Throwable t) {
                Log.i(TAG, "(updateMoimInfo) onFailure : " + t);

            }
        });
    }


    private void showMoimInfo(String moimSeq) {

        Log.i(TAG, "(showMoimInfo) 1. 호출 - 모임의 정보를 불러오는 메서드");
        Log.i(TAG, "(showMoimInfo) 2. 서보로 HTTP 통신 보내는 값, moimSeq : " + moimSeq);
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Call<MoimData> call = uploadApis.showMoimInfo(moimSeq);
        call.enqueue(new Callback<MoimData>() {
            @Override
            public void onResponse(Call<MoimData> call, retrofit2.Response<MoimData> response) {
                Log.d("onResponse", response.body().toString());

                if(!response.isSuccessful()){
                    return;
                }

                MoimData responseBody = response.body(); // 모임의 모든 정보를 가진 객체
                String leaderSeq = responseBody.getLeaderSeq(); // 모임장의 고유값
                String address = responseBody.getAddress(); // 모임 주소
                String title = responseBody.getTitle(); // 모임 타이틀
                mainImage = responseBody.getMainImage(); // 모임 대표사진
                String content = responseBody.getContent(); // 모임 설명
                String interest = responseBody.getInterest(); //
                String memberCountMax = responseBody.getMemberCountMax();

                Gson gson = new Gson();
                String object = gson.toJson(responseBody);
                Log.i(TAG, "(showMoimInfo) 3. HTTP 통신 결과 응답 객체(MoimData) object : " + object);

                Log.i(TAG, "(showMoimInfo)leaderSeq = " + leaderSeq);
                Log.i(TAG, "address = " + address);
                Log.i(TAG, "title = " + title);
                Log.i(TAG, "mainImage = " + mainImage);
                Log.i(TAG, "content = " + content);
                Log.i(TAG, "memberCountMax = " + memberCountMax);

                et_moimTitle.setText(title); //모임 제목 설정
                et_moimContent.setText(content); // 모임 설명 설정
                tv_address.setText(address);
                tv_interest.setText(interest);
                et_memberCount.setText(memberCountMax);

                if(mainImage != null){
                    Glide.with(getApplicationContext())
                            .load(mainImage)
                            .centerCrop()
                            .into(iv_mainImage);
                }

                editor.putString("selectedAddress", address); // 선택한 주소를 s.f 저장
                editor.putString("selectedInterest", interest);
                editor.commit();

            }

            @Override
            public void onFailure(Call<MoimData> call, Throwable t) {
                Log.i(TAG, "onFailure = " + t);
            }
        });
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

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "(onPause()) 1. 호출");
    }
}