package MoimDetail.Board;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Common.NetworkClient;
import Common.StaticVariable;
import Common.UploadApis;
import MoimDetail.MoimDetailMain;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class BoardUpdate extends AppCompatActivity {
    TextView tv_save, tv_addImage;
    EditText et_title, et_content;
    ImageView iv_back;

    // 레트로핏 통신 공통 변수(클래스 연결)
    NetworkClient networkClient;
    UploadApis uploadApis;


    private RecyclerView recyclerView;
    private MultiImageAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    List<BoardData> boardDataList = new ArrayList<>();


    private ArrayList<File> fileAll = new ArrayList<>();
    private ArrayList<File> fileAlbumOnly = new ArrayList<>();

    private ArrayList<String> photoPathList = new ArrayList<>();

    private ArrayList<Uri> uriAll = new ArrayList<>();
    private ArrayList<Uri> uriAlbumOnly = new ArrayList<>();

    Uri imageUri;
    Bitmap bitmap;


    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private static final String TAG = "BoardUpdate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_update);

        // sharedPreference 세팅
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();

        String userSeq = sp.getString("userSeq", ""); //로그인한 계정의 고유값
        Log.i(TAG, "userSeq = " + userSeq);
        String moimSeq = MoimDetailMain.moimSeq; // 현재 보고있는 모임의 고유값
        Log.i(TAG, "moimSeq = " + moimSeq);


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

        Intent intent = getIntent();
        String boardSeq = intent.getStringExtra("boardSeq");

        showBoardInfo(boardSeq, userSeq);
        showImageListOnBoard(boardSeq);

        // 게시물 수정 완료 버튼 - 서버에 저장하는 메서드 만들어서 입력
        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "(게시글 수정완료버튼) 1. 클릭함");

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
                hashmap.put("boardSeq", boardSeq); // 게시글 고유값
                Log.i(TAG, "(게시글 수정완료버튼) 2. 입력한 값을 서버로 보낼 hashmap 객체 생성, hashmap : " + String.valueOf(hashmap));

                // 현재 리사이클러뷰(MultiImageAdapter 사용)에 있는 모든 아이템 수만큼 반복문을 돌린다.
                for(int i = 0; i < adapter.getItemCount(); i++) {
                    Log.i(TAG, "(게시글 수정완료버튼) adapter.getItemUri(i) = " + adapter.getItemUri(i));

                    // Uri를 뽑아낸다.
                    Uri imageUri = adapter.getItemUri(i);
                    String filePath = StaticVariable.createCopyAndReturnRealPath(getApplicationContext(), imageUri);
                    // 파일 경로가 null이 아닌 경우? 앨범에서 선택한 경우!
                    if(filePath != null) {
                        Log.i(TAG, "(게시글 수정완료버튼) filePath = " + filePath);
                        File file = new File(filePath);
                        Log.i(TAG, "(게시글 수정완료버튼) file = " + file);
                        fileAll.add(file); //서버에 저장하는 fileArrayList1
                        Log.i(TAG, "(게시글 수정완료버튼) fileAll = " + fileAll);
                    } else {
                        // 파일 경로가 null인 경우? 서버에서 가져온 경우!
                        photoPathList.add(String.valueOf(imageUri));
                        Log.i(TAG, "(게시글 수정완료버튼) 서버로 보낼 imageUri = " + imageUri);
                    }
                }

                //현재 리사이클러뷰에 표시된 모든 이미지를 서버로 저장한다.
                //인자 1. 사진 앨범에서 선택한 파일 형태의 이미지
                //인자 2. 서버에서 가져왔던 이미지 링크(서버)

                Log.i(TAG, "(게시글 수정완료버튼) 클릭시점에 fileAll = " + String.valueOf(fileAll));
                Log.i(TAG, "(게시글 수정완료버튼) 클릭시점에 photoPathList = " + String.valueOf(photoPathList));
                Log.i(TAG, "(게시글 수정완료버튼) 클릭시점에 hashmap = " + String.valueOf(hashmap));

                // 서버에 저장하는 메서드
                boardUpdate(fileAll, photoPathList, hashmap);


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


    // HTTP통신 매서드 1. 게시글 수정
    // (통신방식) Retrofit
    // (클라이언트→서버) fileArrayList, photoPathList, hashmap(title, content, userSeq, moimSeq, boardSeq, fileCount, photoPathCount)
    // (서버→클라이언트) BoardData 객체의 값(boardSeq)

    private void boardUpdate(ArrayList<File> fileArrayList, ArrayList<String> photoPathList, HashMap<String, String> hashmap) {

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
        textToServer.put("content", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("content")));
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        textToServer.put("moimSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("moimSeq")));
        textToServer.put("boardSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("boardSeq")));
        textToServer.put("fileCount", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(fileArrayList.size())));
        textToServer.put("photoPathCount", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(photoPathList.size())));

        // 이미지 파일 경로 수만큼 만든다.
        for (int i = 0; i < photoPathList.size(); i++) {
            textToServer.put("photoPath"+i, RequestBody.create(MediaType.parse("text/plain"), photoPathList.get(i)));
        }

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<BoardData> call = uploadApis.boardUpdate(textToServer, filesToServer);
        call.enqueue(new Callback<BoardData>() {
            @Override
            public void onResponse(Call<BoardData> call, retrofit2.Response<BoardData> response) {
                fileArrayList.clear();
                filesToServer.clear();

                BoardData responseBody = response.body();
                String boardSeq = hashmap.get("boardSeq");
                Log.i(TAG, "boardCreate 메서드 결과 boardSeq = " + boardSeq);

                Intent intent = new Intent(getApplicationContext(), BoardView.class);
                // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("boardSeq", boardSeq);
                startActivity(intent);
                Toast.makeText(BoardUpdate.this, "수정을 완료했습니다", Toast.LENGTH_SHORT).show();

                // 방금 생성된 게시글 상세보기 페이지로 이동하는 코드 구현
            }

            @Override
            public void onFailure(Call<BoardData> call, Throwable t) {
            }
        });
    }

    // 게시글 정보 불러오기 (프로필 사진, 작성자 이름 등의 정보는 제외)
    private void showBoardInfo(String boardSeq, String userSeq) {
        Log.i(TAG, "(showBoardInfo) 1. 호출 - 게시글 정보 불러오기");
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Call<BoardData> call = uploadApis.showBoardInfo(boardSeq, userSeq);
        Log.i(TAG, "(showBoardInfo) 2. 서버로 보내는 값, boardSeq : " + boardSeq + " / userSeq : " + userSeq);
        call.enqueue(new Callback<BoardData>() {
            @Override
            public void onResponse(Call<BoardData> call, retrofit2.Response<BoardData> response) {
                Log.i(TAG, "(showBoardInfo) 3. response.body().toString() : " + response.body().toString());
                if(!response.isSuccessful()){
                    return;
                }

                //받아야할 정보
                //1. 작성자 프로필 사진, 2. 작성자 이름, 3. 작성자의 모임장여부, 4. 게시글 작성일시, 5. 게시글 제목, 6. 게시글 내용,

                BoardData responseBody = response.body(); //

                Gson gson = new Gson();
                String object = gson.toJson(responseBody);
                Log.i(TAG, "(showBoardInfo) 4. 서버로부터 받은 객체, BoardData : " + object);

                String title = responseBody.getTitle(); //
                String content = responseBody.getContent(); //

                Log.i(TAG, "(showBoardInfo) 5. 모임 제목과 내용 설정");
                et_title.setText(title); //모임 제목 설정
                et_content.setText(content);
            }

            @Override
            public void onFailure(Call<BoardData> call, Throwable t) {
                Log.i(TAG, "(showBoardInfo) onFailure = " + t);
            }
        });
    }


    // HTTP통신 매서드 1. 게시글에 들어간 사진 목록 불러오기
    // (통신방식) Retrofit
    // (클라이언트→서버) boardSeq
    // (서버→클라이언트) BoardData 객체의 값(imageUrl)
    private void showImageListOnBoard(String boardSeq) {

        Log.i(TAG, "(showImageListOnBoard) 1. 호출 - 게시글에 들어간 사진 목록 불러오기");

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<List<BoardData>> call = uploadApis.showImageListOnBoard(boardSeq);
        Log.i(TAG, "(showImageListOnBoard) 2. 서버로 보내는 값, boardSeq : " + boardSeq);
        call.enqueue(new Callback<List<BoardData>>() {
            @Override
            public void onResponse(Call<List<BoardData>> call, retrofit2.Response<List<BoardData>> response) {
                Log.i(TAG, "(showImageListOnBoard) 3. 서버에서 전달받은 값, response : " + response);
                List<BoardData> responseBody = response.body();

                Log.i(TAG, "(showImageListOnBoard) 4. List<BoardData> 객체 생성해서 반복문 -> Uri 객체 생성 후 uriAll(ArrayList)에 add");

                for(int i = 0; i < responseBody.size(); i++) {
                    Uri myUri = Uri.parse(responseBody.get(i).getImageUrl());
                    Log.i(TAG, "myUri = " + String.valueOf(myUri));
                    uriAll.add(myUri);
                    Log.i(TAG, "uriAll.size() = " + uriAll.size());
                    Log.i(TAG, "uriAll = " + String.valueOf(uriAll));
                }
                Log.i(TAG, "(showImageListOnBoard) 4. adapter.notifyDataSetChanged()로 리사이클러뷰 갱신");
                // 리사이클러뷰 값 넣기
//                adapter = new BoardImageAdapter(boardDataList, getApplicationContext());
//                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<BoardData>> call, Throwable t) {
                Log.i(TAG, "(showImageListOnBoard) 에러, t : " + t);
            }
        });
    }
}

