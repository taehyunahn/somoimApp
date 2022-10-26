package MoimDetail.Board;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;
import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import Common.DataClass;
import Common.NetworkClient;
import Common.RecyclerViewEmptySupport;
import Common.StaticVariable;
import Common.UploadApis;
import MoimDetail.MoimDetailMain;
import MoimSearch.MoimAdapter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class BoardCreate extends AppCompatActivity {

    TextView tv_save, tv_addImage;
    EditText et_title, et_content;
    ImageView iv_back;

    // 레트로핏 통신 공통 변수(클래스 연결)
    NetworkClient networkClient;
    UploadApis uploadApis;


    private RecyclerView recyclerView;
    private MultiImageAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<File> fileAll = new ArrayList<>();
    private ArrayList<File> fileAlbumOnly = new ArrayList<>();

    private ArrayList<String> photoPathList = new ArrayList<>();

    private ArrayList<Uri> uriAll = new ArrayList<>();
    private ArrayList<Uri> uriAlbumOnly = new ArrayList<>();

    Uri imageUri;
    Bitmap bitmap;


    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private static final String TAG = "BoardCreate(게시글 작성화면)";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_create);
        Log.i(TAG, "(onCreate()) 1. 호출");

        // sharedPreference 세팅
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();

        String userSeq = sp.getString("userSeq", ""); //로그인한 계정의 고유값
        Log.i(TAG, "userSeq = " + userSeq);

        Log.i(TAG, "(onCreate()) 2. s.p(login)에서 꺼내는 값, userSeq : " + userSeq);
        String moimSeq = MoimDetailMain.moimSeq; // 현재 보고있는 모임의 고유값
        Log.i(TAG, "(onCreate()) 2. 관련 activity의 static 변수값을 꺼냄, MoimDetailMain.moimSeq : " + moimSeq);

        //ui 연결
        tv_save = findViewById(R.id.tv_save); // 게시물 작성 완료 버튼
        et_title = findViewById(R.id.et_title); // 게시물 타이틀
        et_content = findViewById(R.id.et_content); // 게시물 내용
        iv_back = findViewById(R.id.iv_back); // 뒤로가기 버튼
        tv_addImage = findViewById(R.id.tv_addImage); // 사진 추가 텍스트
        recyclerView = findViewById(R.id.recyclerView); // 리사이클러뷰

        // 리사이클러뷰 세팅 ※ 의미 공부 필요
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MultiImageAdapter(uriAll, getApplicationContext());
        recyclerView.setAdapter(adapter);


        // 게시물 작성 완료 버튼 - 서버에 저장하는 메서드 만들어서 입력
        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "(게시물 작성완료 버튼) 1. 클릭함");

                //게시물 타이틀에 입력한 값 불러오기
                String title = et_title.getText().toString();
                String content = et_content.getText().toString();
                //게시물 내용에 입력한 값 불러오기

                // 서버로 보낼 값 (onCreate 안에 선언) : 현재 로그인한 계정(작성자 고유값), 현재 모임의 고유값, 제목, 내용
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("title", title);
                hashmap.put("content", content);
                hashmap.put("userSeq", userSeq); // 로그인한 계정의 고유값
                hashmap.put("moimSeq", moimSeq); // 현재 모임의 고유값

                Log.i(TAG, "(게시물 작성완료 버튼) 2. 입력한 값을 서버로 보내기 위해 hashmap 객체 생성, hashmap : " + String.valueOf(hashmap));

                Log.i(TAG, "(게시물 작성완료 버튼) 3. 게시글 안에 들어가는 사진 목록의 uri와 file의 arrayList를 생성");
                // 현재 리사이클러뷰(MultiImageAdapter 사용)에 있는 모든 아이템 수만큼 반복문을 돌린다.
                for(int i = 0; i < adapter.getItemCount(); i++) {
                    // Uri를 뽑아낸다.
                    Uri imageUri = adapter.getItemUri(i);
                    Log.i(TAG, "(게시물 작성완료 버튼) 3-1. Uri 객체 생성, imageUri : " + String.valueOf(imageUri));

                    String filePath = StaticVariable.createCopyAndReturnRealPath(getApplicationContext(), imageUri);
                    Log.i(TAG, "(게시물 작성완료 버튼) 3-2. 파일경로 String값 얻음");
                    // 파일 경로가 null이 아닌 경우? 앨범에서 선택한 경우!
                    if(filePath != null) {
                        Log.i(TAG, "(게시물 작성완료 버튼) 3-2. 파일경로 String값이 null이 아닌 경우 -> 앨범에서 새로 선택한 사진이 있음");
                        File file = new File(filePath);
                        Log.i(TAG, "(게시물 작성완료 버튼) 3-3. 파일 객체 생성, file : " + String.valueOf(file));
                        fileAll.add(file); //서버에 저장하는 fileArrayList1
                        Log.i(TAG, "(게시물 작성완료 버튼) 3-4. 파일ArrayList(fileAll)에 추가, fileAll : " + String.valueOf(fileAll));

                    } else {
                        Log.i(TAG, "(게시물 작성완료 버튼) 3-2. 파일경로 String값이 null인 경우 -> 앨범에서 새로 선택한 사진이 없음. 이미지 경로를 photoPathList에 추가");
                        // 파일 경로가 null인 경우? 서버에서 가져온 경우!
                        photoPathList.add(String.valueOf(imageUri));
                        Log.i(TAG, "(게시물 작성완료 버튼) 3-3. 이미지 경로를 photoPathList에 추, photoPathList : " + String.valueOf(photoPathList));
                    }
                }

                //현재 리사이클러뷰에 표시된 모든 이미지를 서버로 저장한다.
                //인자 1. 사진 앨범에서 선택한 파일 형태의 이미지
                //인자 2. 서버에서 가져왔던 이미지 링크(서버)

                Log.i(TAG, "(게시물 작성완료 버튼) 4. boardCreate(fileAll, photoPathList, hashmap) HTTP 통신 매서드 호출");
                // 서버에 저장하는 메서드
                boardCreate(fileAll, photoPathList, hashmap);


            }
        });

        // 뒤로가기 버튼
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 사진등록하기
        tv_addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2222);
            }
        });

    }
    // 앨범에서 액티비티로 돌아온 후 실행되는 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2222){
            if(data == null){   // 어떤 이미지도 선택하지 않은 경우
                Toast.makeText(getApplicationContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
            }
            else{// 이미지를 하나라도 선택한 경우
                ClipData clipData = data.getClipData();
                // 선택한 이미지가 11장 이상인 경우
                if(clipData.getItemCount() > 10){
                    Toast.makeText(getApplicationContext(), "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
                }
                else{
                    // 선택한 이미지가 1장 이상 10장 이하인 경우
                    Log.i(TAG, "선택한 이미지가 1장 이상 10장 이하인 경우");
                    for (int i = 0; i < clipData.getItemCount(); i++){
                        imageUri = clipData.getItemAt(i).getUri();  // 선택한 이미지들의 uri를 가져온다.
                        uriAll.add(imageUri); //서버에서 가져온 이미지 + 앨범에서 선택한 이미지
                        uriAlbumOnly.add(imageUri); //앨범에서 선택한 이미지만
                        File file = new File(StaticVariable.createCopyAndReturnRealPath(getApplicationContext(), imageUri));
                        fileAlbumOnly.add(file);
                    }

                    Log.i(TAG, "사진 파일을 선택하고 난 직후의 uriArrayList와 fileArrayList");
                    Log.i(TAG, "uriAll" + String.valueOf(uriAll));
                    Log.i(TAG, "uriAlbumOnly" + String.valueOf(uriAlbumOnly));
                    Log.i(TAG, "fileAlbumOnly" + String.valueOf(fileAlbumOnly));
//
//                    adapter = new MultiImageAdapter(uriAll, getApplicationContext());
//                    recyclerView.setAdapter(adapter);   // 리사이클러뷰에 어댑터 세팅
//                    recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));     // 리사이클러뷰 수평 스크롤 적용
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    // HTTP통신 매서드 1. 게시글 작성 메서드
    // (통신방식) Retrofit
    // (클라이언트→서버) fileArrayList, photoPathList, hashmap(title, content, userSeq, moimSeq)
    // (서버→클라이언트) MoimData 객체의 값(leaderSeq, address, title, mainImage, content, memberCount)
    //
    private void boardCreate(ArrayList<File> fileArrayList, ArrayList<String> photoPathList, HashMap<String, String> hashmap) {
        Log.i(TAG, "(boardCreate) 1. 호출 - 게시글 작성 메서드");
        Log.i(TAG, "(boardCreate) 2. 서버로 보내는 값, ①fileArrayList, ②photoPathList, ③hashmap");
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 파일값 저장
        ArrayList<MultipartBody.Part> filesToServer = new ArrayList<>();
        for (int i = 0; i < fileArrayList.size(); i++) {
            RequestBody fileRequestBody = RequestBody.create(MediaType.parse("image/jpg"), fileArrayList.get(i));
            MultipartBody.Part fileMultiPartBody = MultipartBody.Part.createFormData("imageName"+i, "("+i+")"+".jpg", fileRequestBody);
            filesToServer.add(fileMultiPartBody);
        }

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("title", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("title")));
        Log.i(TAG, "(boardCreate) 2. 서버로 보내는 값, title : " + hashmap.get("title"));
        textToServer.put("content", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("content")));
        Log.i(TAG, "(boardCreate) 2. 서버로 보내는 값, content : " + hashmap.get("content"));
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        Log.i(TAG, "(boardCreate) 2. 서버로 보내는 값, userSeq : " + hashmap.get("userSeq"));
        textToServer.put("moimSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("moimSeq")));
        Log.i(TAG, "(boardCreate) 2. 서버로 보내는 값, moimSeq : " + hashmap.get("moimSeq"));
        textToServer.put("fileCount", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(fileArrayList.size())));
        Log.i(TAG, "(boardCreate) 2. 서버로 보내는 값, fileCount : " + String.valueOf(fileArrayList.size()));
        textToServer.put("photoPathCount", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(photoPathList.size())));
        Log.i(TAG, "(boardCreate) 2. 서버로 보내는 값, photoPathCount : " + String.valueOf(photoPathList.size()));

        // 이미지 파일 경로 수만큼 만든다.
        for (int i = 0; i < photoPathList.size(); i++) {
            textToServer.put("photoPath"+i, RequestBody.create(MediaType.parse("text/plain"), photoPathList.get(i)));
        }

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<BoardData> call = uploadApis.boardCreate(textToServer, filesToServer);
        call.enqueue(new Callback<BoardData>() {
            @Override
            public void onResponse(Call<BoardData> call, retrofit2.Response<BoardData> response) {


                fileArrayList.clear();
                filesToServer.clear();

                BoardData responseBody = response.body();
                String boardSeq = responseBody.getBoardSeq();
                Log.i(TAG, "boardCreate 메서드 결과 boardSeq = " + boardSeq);

                finish();
                // 방금 생성된 게시글 상세보기 페이지로 이동하는 코드 구현
                Intent intent = new Intent(getApplicationContext(), BoardView.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("boardSeq", boardSeq);
                startActivity(intent);
                Toast.makeText(BoardCreate.this, "게시글 작성을 완료했습니다", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<BoardData> call, Throwable t) {
            }
        });
    }

}





