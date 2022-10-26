package MoimCreate;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import com.example.somoim.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import Chat.ChatData;
import Chat.ChatService;
import Common.DataClass;
import Common.NetworkClient;
import Common.StaticVariable;
import Common.UploadApis;
import MoimDetail.MoimDetailMain;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class MoimCreate extends AppCompatActivity {

    TextView tv_address, tv_interest;
    TextInputLayout til_moimTitle, til_moimContent;
    EditText et_moimTitle, et_moimContent, et_memberCount;
    Button btn_create;
    ImageView iv_mainImage;

    // sharedPreference 세팅
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    // 레트로핏 통신 공통 변수(클래스 연결)
    NetworkClient networkClient;
    UploadApis uploadApis;

    private static final String TAG = "MoimCreate(모임 만들기)";

    private ArrayList<File> fileArrayList1 = new ArrayList<>();

    Uri selectedUri;
    Bitmap bitmap;

    String joinedMoimSeqList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moim_create);
        Log.i(TAG, "(onCreate() 1. 호출)");

        // UI 연결
        tv_address = findViewById(R.id.tv_address); // 주소
        et_memberCount = findViewById(R.id.et_memberCount); // 모임정원수
        til_moimTitle = findViewById(R.id.til_moimTitle); //
        et_moimTitle = findViewById(R.id.et_moimTitle); // 모임 제목
        btn_create = findViewById(R.id.btn_create); // 모임만들기 버튼
        til_moimContent = findViewById(R.id.til_moimContent);
        et_moimContent = findViewById(R.id.et_moimContent); // 모임 내용/목표
        iv_mainImage = findViewById(R.id.iv_mainImage); // 메인 이미지
        tv_interest = findViewById(R.id.tv_interest); // 관심분야

        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();

        
        // 주소 클릭시
        tv_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "'지역구 찾기' 클릭함 → AddressList로 이동");
//                Intent intent = new Intent(getApplicationContext(), SignUpAddress.class);
//                getSearchResult.launch(intent);
                Intent intent = new Intent(getApplicationContext(), AddressList.class);
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });

        // 관심분야 클릭시
        tv_interest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "'관심분야 선택' 클릭함 → MoimInterest로 이동");
                Intent intent = new Intent(getApplicationContext(), MoimInterest.class);
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });

        // 프로필 이미지 선택
            iv_mainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "프로필 이미지 클릭함 → 앨범 선택");
                //askPermission();  //사진을 갤러리에서 가져오기 버튼을 클릭시 작동 (아래 class 참조)
                //ImagePicker 라이브러리 사용 -> 카메라 촬영 또는 앨범 선택
                ImagePicker.with(MoimCreate.this)
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });



        //모임만들기 버튼
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "모임만들기 버튼 클릭 → 입력한 값을 HTTP 통신으로 서버에 저장");

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
                Log.i(TAG, "(모임만들기 버튼 클릭 시) 1. HTTP 통신 서버로 보내는 값, hashmap : " + String.valueOf(hashmap));

                if(!address.equals("") && !title.equals("") && !content.equals("") && !memberCount.equals("")) {
                    // 모임 개설 정보를 서버에 전송하여 저장한다
                    Log.i(TAG, "(모임만들기 버튼 클릭 시) 2. createMoim(fileArrayList1, hashmap) 호출");
                    createMoim(fileArrayList1, hashmap);

                } else {
                    Toast.makeText(MoimCreate.this, "모임에 대한 정보를 누락없이 입력해주세요", Toast.LENGTH_SHORT).show();
                }
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


    // 주소 API 사용 후 결과값 처리하는 메서드
    private final ActivityResultLauncher<Intent> getSearchResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    String data = result.getData().getStringExtra("data");
                    tv_address.setText(data);
                }
            });


    private void createMoim(ArrayList<File> fileArrayList, HashMap<String, String> hashmap) {
        Log.i(TAG, "(createMoim) 1. 호출 - 모임 생성하는 HTTP 통신");
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
        textToServer.put("fileCount", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(fileArrayList.size())));

        // 서버에 전송할 파일값 저장
        ArrayList<MultipartBody.Part> filesToServer = new ArrayList<>();
        for (int i = 0; i < fileArrayList.size(); i++) {
            RequestBody fileRequestBody = RequestBody.create(MediaType.parse("image/jpg"), fileArrayList.get(i)); // fileArraylist에 파일을 담아서 넣는다
            MultipartBody.Part fileMultiPartBody = MultipartBody.Part.createFormData("imageName"+i, "("+i+")"+".jpg", fileRequestBody);
            filesToServer.add(fileMultiPartBody);
        }

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<DataClass> call = uploadApis.createMoim(textToServer, filesToServer);
        call.enqueue(new Callback<DataClass>() {
            @Override
            public void onResponse(Call<DataClass> call, retrofit2.Response<DataClass> response) {

                Log.i(TAG, "response = " + response);

                fileArrayList.clear();
                filesToServer.clear();

                DataClass responseBody = response.body();
                String chatRoomSeq = responseBody.getChatRoomSeq();
                Log.i(TAG, "chatRoomSeq = " + chatRoomSeq);
                String moimSeq = responseBody.getMoimSeq();
                Log.i(TAG, "moimSeq = " + moimSeq);
                String message = responseBody.getMessage();
                Log.i(TAG, "message = " + message);
                String success = responseBody.getSuccess();
                Log.i(TAG, "success = " + success);


                if(success.equals("1")){

                    Toast.makeText(MoimCreate.this, "모임을 생성했습니다", Toast.LENGTH_SHORT).show();
                    finish();

                    String userSeq = sp.getString("userSeq", "");
                    editor.putBoolean("moimAlreadyJoined" + userSeq  + moimSeq, true);
                    editor.commit();

                    Intent intent = new Intent(getApplicationContext(), MoimDetailMain.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("moimSeq", moimSeq); // 모임의 고유값을 전달 (모임 상세보기)
                    intent.putExtra("fromMoimCreate", true);
                    startActivity(intent);


                    // 채팅방을 개설했음을 알리는 스레드
                    // new_room이라는 명령을 보내서, 서버측 JDBC를 사용해서 DB insert와 update 작업 함.
                    ChatRoomCreateThread chatRoomCreateThread = new ChatRoomCreateThread(ChatService.member_socket, userSeq, moimSeq);
                    chatRoomCreateThread.start();


                }

            }

            @Override
            public void onFailure(Call<DataClass> call, Throwable t) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "생명주기 onDestroy");

        editor.remove("selectedInterest");
        editor.remove("selectedAddress");
        editor.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "생명주기 onResume");

        String selectedInterest = sp.getString("selectedInterest", "");
        String selectedAddress = sp.getString("selectedAddress", "");

        tv_interest.setText(selectedInterest);
        tv_address.setText(selectedAddress);

        Log.i(TAG, "selectedInterest = " + selectedInterest);
        Log.i(TAG, "selectedAddress = " + selectedAddress);


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

    // 채팅방 입장할 때 동작하는 스레드
    class ChatRoomCreateThread extends Thread {
        Socket socket;
        String userSeq;
        String moimSeq;

        public ChatRoomCreateThread(Socket socket, String userSeq, String moimSeq) {
            try {
                this.socket = socket;
                this.userSeq = userSeq;
                this.moimSeq = moimSeq;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            Log.i(TAG, "(ChatRoomEnterThread) 채팅방 입장할 때 동작하는 스레드 run");
            try {

                Log.i(TAG, "(ChatRoomEnterThread) 서버로 보낼 객체의 값을 set한다");
                ChatService.chatData = new ChatData();
                ChatService.chatData.setCommand("new_room"); // 채팅방 새로 개설 (모임 만들면 동시에 만들어짐)
                ChatService.chatData.setUserSeq(userSeq);
                ChatService.chatData.setMoimSeq(moimSeq);
                ChatService.chatData.setJoinedMoimSeqList(joinedMoimSeqList);

                Gson gson = new Gson();
                String sendingObject = gson.toJson(ChatService.chatData);

                ChatService.sendWriter.println(sendingObject);
                ChatService.sendWriter.flush();

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        et_message.setText("");
//                    }
//                });
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}