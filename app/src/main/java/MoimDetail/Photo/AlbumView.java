package MoimDetail.Photo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import BoardComment.CommentAdapter;
import BoardComment.CommentData;
import Chat.ChatData;
import Chat.ChatService;
import Common.NetworkClient;
import Common.UploadApis;
import MoimDetail.Board.BoardData;
import MoimDetail.Board.BoardImageAdapter;
import MoimDetail.Board.BoardUpdate;
import MoimDetail.Board.BoardView;
import MoimDetail.MoimDetailMain;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Url;

public class AlbumView extends AppCompatActivity implements CommentAdapter.EventHandler{

    ImageView iv_back, iv_more, iv_profileImage, iv_albumImage;
    TextView tv_name, tv_status, tv_title, tv_date, tv_content, tv_commentCount, tv_likeCount;
    EditText et_commentInput;
    Button btn_commentSend, btn_like, btn_likeCancel, btn_commentFocus;

    CheckBox cb_like;
    NestedScrollView nestedScrollView;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private RecyclerView comment_rv;
    private RecyclerView.Adapter comment_adapter;
    private RecyclerView.LayoutManager comment_layoutManager;

    private SwipeRefreshLayout swiperefresh;

    List<BoardData> boardDataList = new ArrayList<>();
    List<CommentData> commentDataList = new ArrayList<>();

    // 레트로핏 통신 공통
    NetworkClient networkClient;
    UploadApis uploadApis;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private static final String TAG = "AlbumView";

    String time = get_now_time();
    // 다운받은 파일이 저장될 위치 설정
    private final String outputFilePath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS + "/ImageTemp") + "/" + time;
    private DownloadManager mDownloadManager;
    private Long mDownloadQueueId;
    private File file, dir;
    private String saveFolderName = "ImageTemp";

    private String savePath= "ImageTemp";
    private String FileName = null;


    String imageUrl;
    String uploaderSeq;
    String moimSeq;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);
        Log.i(TAG, "(onCreate()) 1. 호출");

        // UI 연결
        iv_back = findViewById(R.id.iv_back); // 뒤로가기
        iv_more = findViewById(R.id.iv_more); // 더보기
        iv_profileImage = findViewById(R.id.iv_profileImage); // 업로더 프로필 사진
        tv_name = findViewById(R.id.tv_name); // 업로더 이름
        tv_date = findViewById(R.id.tv_date); // 업로드된 날짜
        recyclerView = findViewById(R.id.recyclerView);
        comment_rv = findViewById(R.id.comment_rv); // 댓글 리사이클러뷰
        btn_commentSend = findViewById(R.id.btn_commentSend); // 댓글 전송 버튼
        et_commentInput = findViewById(R.id.et_commentInput); // 댓글 입력 내용
        tv_commentCount = findViewById(R.id.tv_commentCount); // 댓글 합계
        btn_likeCancel = findViewById(R.id.btn_likeCancel); // 좋아요 취소버튼
        btn_like = findViewById(R.id.btn_like); // 좋아요 버튼
        tv_likeCount = findViewById(R.id.tv_likeCount); // 좋아요 숫자
        btn_commentFocus = findViewById(R.id.btn_commentFocus); //댓글 달기 버튼 클릭 시, 댓글 인풋으로 포커스 이동
        nestedScrollView = findViewById(R.id.nestedScrollView);
        cb_like = findViewById(R.id.cb_like); // 좋아요 체크박스
        swiperefresh = findViewById(R.id.swiperefresh);
        iv_albumImage = findViewById(R.id.iv_albumImage); // 앨범 사진


        // sharedPreference 세팅
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq","");
        Log.i(TAG, "(onCreate()) 2. sharedPreference(login) 값, userSeq : " + userSeq);

        Intent intent = getIntent(); // 앨범 사진 목록 리사이클러뷰 아이템 클릭 후 도착한 상황
        String albumSeq = intent.getStringExtra("albumSeq");
        Log.i(TAG, "(onCreate()) 3. Intent로 전달받은 값, albumSeq : " + albumSeq);

        // 서버에 게시글 정보 요청해서 보여주는 메서드
        Log.i(TAG, "(onCreate()) 4. 선택한 사진의 정보를 불러오는 매서드 호출");
        showAlbumInfo(albumSeq, userSeq);

        // 댓글 정보 보여주는 메서드
        Log.i(TAG, "(onCreate()) 5. 선택한 사진의 댓글 목록을 불러오는 매서드 호출");
        showCommentListAlbum(albumSeq);

        // comment(댓글) 리사이클러뷰 세팅
        comment_rv.setHasFixedSize(true);
        comment_layoutManager = new LinearLayoutManager(getApplicationContext());
        comment_rv.setLayoutManager(comment_layoutManager);
        comment_adapter = new CommentAdapter(commentDataList, getApplicationContext(), this);
        comment_rv.setAdapter(comment_adapter);


        // 댓글 달기 버튼 클릭 시, 댓글 입력창으로 포커스 이동
        btn_commentFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "댓글 달기 버튼 클릭함 → 댓글 입력창으로 포커스 이동");
                et_commentInput.post(new Runnable() {
                    @Override
                    public void run() {
                        et_commentInput.setFocusableInTouchMode(true);
                        et_commentInput.requestFocus();
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(et_commentInput,0);
                    }
                });
            }
        });

        // 댓글 전송 버튼
        btn_commentSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "댓글 전송 버튼 클릭함 → 입력한 댓글을 DB에 저장함");
                String commentInput  = et_commentInput.getText().toString();
                Log.i(TAG, "(댓글 전송 버튼 클릭함) 1. 입력한 댓글 : " + commentInput);
                // 서버로 보낼 값 (onCreate 안에 선언)
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("commentInput", commentInput);
                hashmap.put("userSeq", userSeq);
                hashmap.put("albumSeq", albumSeq);
                Log.i(TAG, "(댓글 전송 버튼 클릭함) 2. 서버로 보낼 hashmap = " + String.valueOf(hashmap));
                Log.i(TAG, "(댓글 전송 버튼 클릭함) 3. commentCreate(hashmap) 호출");
                commentCreate(hashmap);

            }
        });


        // 더보기 버튼 -> 게시글 수정 또는 삭제 메뉴
        iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "더보기 버튼 클릭");

                // PopupMenu 사용방법 (a Menu in XML를 defining해야 한다)
                // 1. Instantiate a PopupMenu with its constructor, which takes the current application Context and the View to which the menu should be anchored.
                PopupMenu popup = new PopupMenu(getApplicationContext(), v);
                // 2. Use MenuInflater to inflate your menu resource into the Menu object returned by PopupMenu.getMenu().
                MenuInflater inflater = popup.getMenuInflater();
                if(uploaderSeq.equals(userSeq)){
                    inflater.inflate(R.menu.menu_album, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getItemId() == R.id.save){
                                Log.i(TAG, "저장하기 버튼 클릭함");
                                saveImage();

                            } else if(item.getItemId() == R.id.delete){
                                Log.i(TAG, "삭제하기");
                                // 서버로 보낼 값 (onCreate 안에 선언)
                                HashMap hashmap = new HashMap<String, String>();
                                hashmap.put("userSeq", userSeq);
                                hashmap.put("albumSeq", albumSeq);
                                Log.i(TAG, "게시물 삭제하기 클릭 후 서버로 보내는 값 hashmap = " + String.valueOf(hashmap));
                                deleteAlbum(hashmap);
                                // 게시글의 고유값을 서버에 보내서 삭제한다. 그리고 모임 상세정보의 게시판 목록으로 간다 (모임의 고유값 필요)

                            }
                            return false;
                        }
                    });
                } else {
                    inflater.inflate(R.menu.menu_album_save, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getItemId() == R.id.save){
                                Log.i(TAG, "저장하기 버튼 클릭함");
                                Uri uri = Uri.parse(imageUrl);

                                saveImage();
                            }
                            return false;
                        }
                    });
                }

                // 3. Call PopupMenu.show().
                popup.show();

            }
        });

        // 좋아요 버튼 클릭 시 - 버튼 invisible하고, 서버에 즐가시킬 것
        btn_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 좋아요 버튼을 invisible하고, 취소버튼을 visible한다
                // 좋아요 버튼을 클릭했으면 invisible 상태를 유지할 수 있도록 sp에 저장한다
                // 저장할 때 key 값으로는 userSeq와 boardSeq 두 가지를 조합해서 사용한다
                // sp에 저장된 값으로 버튼을 보이거나 안보이게 하는 동작은 onResume에서 한다
                likeClicked();
                editor.putBoolean(userSeq + albumSeq + "likeClicked", true);
                editor.commit();


                // 게시물의 고유값(boardSeq), 누른사람의 계정 고유값(userSeq)을 서버에 보낸다
                // likeBoard 테이블에 boardSeq를 값을 사용해서 insert한다
                // likeBoard 테이블에 클라이언트가 보낸 boardSeq와 일치하는 seq 개수(해당 게시글의 좋아요 합계)를 클라이언트에 보낸다

                Log.i(TAG, "좋아요 버튼을 클릭했습니다");

                // 서버로 보낼 값 (onCreate 안에 선언)
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("userSeq", userSeq);
                hashmap.put("albumSeq", albumSeq);
                Log.i(TAG, "서버로 보내는 값 hashmap = " + String.valueOf(hashmap));

                likeAlbumAdd(hashmap);

                NotiLikeMsgToServer2 notiLikeMsgToServer2 = new NotiLikeMsgToServer2(ChatService.member_socket, albumSeq, uploaderSeq);
                notiLikeMsgToServer2.start();
            }
        });

        // 좋아요 취소 버튼 클릭 시
        btn_likeCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeCanceled();
                editor.remove(userSeq + albumSeq + "likeClicked");
                editor.commit();

                // 서버로 보낼 값 (onCreate 안에 선언)
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("userSeq", userSeq);
                hashmap.put("albumSeq", albumSeq);
                Log.i(TAG, "서버로 보내는 값 hashmap = " + String.valueOf(hashmap));
                likeAlbumCancel(hashmap);
            }
        });

        // 스와이프 새로고침
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 서버에 게시글 정보 요청해서 보여주는 메서드
                showAlbumInfo(albumSeq, userSeq);

                commentDataList.clear();
                boardDataList.clear();
//
//                showImageListOnBoard(albumSeq);
//                // 댓글 정보 보여주는 메서드
                showCommentListAlbum(albumSeq);

                swiperefresh.setRefreshing(false);


            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MoimDetailMain.class);
        // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.i(TAG, "onBackPressed() 시점, moimSeq : " + moimSeq);
        intent.putExtra("moimSeq", moimSeq);
        intent.putExtra("fromAlbumView", true);


        startActivity(intent);
    }



    @Override
    protected void onResume() {
        super.onResume();
        // sharedPreference 세팅
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq","");
        Intent intent = getIntent();
        String boardSeq = intent.getStringExtra("boardSeq");
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.i(TAG, "(onPostResume) 1. 호출");

//        // 브로드캐스트 리시버 등록
//        // ACTION_DOWNLOAD_COMPLETE : 다운로드가 완료되었을 때 전달
//        IntentFilter completeFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
//        Log.i(TAG, "(onPostResume) 2. IntentFilter 객체(completeFilter) 생성");
//        Log.i(TAG, "(onPostResume) 2. registerReceiver(downloadCompleteReceiver, completeFilter)");
//        registerReceiver(downloadCompleteReceiver, completeFilter);

    }

    @Override
    public void onPause(){
        super.onPause();
//        unregisterReceiver(downloadCompleteReceiver);
    }



    private void saveImage() {
        Log.i(TAG, "(saveImage) 다운로드할 이미지 선택");
        ActivityCompat.requestPermissions(AlbumView.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        FileOutputStream fileOutputStream = null;
        File file = getDisc();

        if(!file.exists() && !file.mkdirs()){
            file.mkdirs();
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmsshhmmss");
        String date = simpleDateFormat.format(new Date());
        String name = "IMG" + date + ".jpg";
        String file_name = file.getAbsolutePath() + "/" + name;
        File new_file = new File(file_name);


        try{
            BitmapDrawable draw = (BitmapDrawable) iv_albumImage.getDrawable();
            Bitmap bitmap = draw.getBitmap();
            fileOutputStream = new FileOutputStream(new_file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            Toast.makeText(getApplicationContext(), "이미지를 저장했습니다.", Toast.LENGTH_SHORT).show();
            fileOutputStream.flush();
            fileOutputStream.close();

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        refreshGallery(new_file);
    }

    private File getDisc(){
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(file, "somoimApp");
    }

    private void refreshGallery(File file){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        getApplicationContext().sendBroadcast(intent);
    }


    // 좋아요 버튼 클릭시 동작하는 함수
    private void likeClicked() {
        btn_like.setVisibility(View.GONE); //안보이게
        btn_likeCancel.setVisibility(View.VISIBLE); //보이게
        cb_like.setChecked(true);

    }

    // 좋아요 취소 버튼 클릭시 동작하는 함수
    private void likeCanceled() {
        btn_likeCancel.setVisibility(View.GONE); //안보이게
        btn_like.setVisibility(View.VISIBLE); //보이게
        cb_like.setChecked(false);

    }


    // 앨범 정보 불러오기
    private void showAlbumInfo(String albumSeq, String userSeq) {
        Log.i(TAG, "(showAlbumInfo) 1. 호출 - HTTP 통신, 앨범 정보 불러오기");
        Log.i(TAG, "(showAlbumInfo) 2. HTTP 통신, 서버로 전송하는 값, albumSeq : " + albumSeq + " / userSeq : " + userSeq);
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Call<AlbumData> call = uploadApis.showAlbumInfo(albumSeq, userSeq);
        call.enqueue(new Callback<AlbumData>() {
            @Override
            public void onResponse(Call<AlbumData> call, retrofit2.Response<AlbumData> response) {
                Log.i(TAG, "(showAlbumInfo) 3. HTTP 통신, 서버에서 전달받은 값,  response.body().toString() : " + response.body().toString());

                if(!response.isSuccessful()){
                    return;
                }
                //받아야할 정보
                //1. 작성자 프로필 사진, 2. 작성자 이름, 3. 작성자의 모임장여부, 4. 게시글 작성일시, 5. 게시글 제목, 6. 게시글 내용,

                AlbumData responseBody = response.body(); //

                Gson gson = new Gson();
                String object = gson.toJson(responseBody);
                Log.i(TAG, "(showAlbumInfo) 4. HTTP 통신, 서버에서 전달받은 객체, object : " + object);

                String profileImage= responseBody.getProfileImage(); //
                String name = responseBody.getName(); //
                imageUrl = responseBody.getImageUrl();

                String dateValue = responseBody.getDate(); //
                Log.i(TAG, "dateValue = " + dateValue);
                String monthOnly = dateValue.substring(5, 7);
                String dateOnly = dateValue.substring(8, 10);
                String showDate = monthOnly + "월 " + dateOnly + "일 ";
                String timeOnly = "";
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date date = null;
                try {
                    date = formatter.parse(dateValue);
                    SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
                    String date2 = formatter2.format(date);
                    String amOrPm = date2.substring(20, 22);
                    String minAndSecond = date2.substring(11, 16);
                    timeOnly = amOrPm + " " + minAndSecond;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String revisedDate = showDate + timeOnly;

                String title = responseBody.getTitle(); //
                String content = responseBody.getContent(); //
                String commentCount = responseBody.getCommentCount();
                String likeCount = responseBody.getLikeCount(); //
                uploaderSeq = responseBody.getUploaderSeq(); //
                String status = responseBody.getStatus();
                String likeClicked = responseBody.getLikeClicked();
                moimSeq = responseBody.getMoimSeq();

                Log.i(TAG, "(showAlbumInfo) 4. moimSeq : " + moimSeq);


//                if(!uploaderSeq.equals(userSeq)){
//                    iv_more.setVisibility(View.INVISIBLE);
//                }

                if(responseBody != null){
                    Glide.with(getApplicationContext())
                            .load(profileImage)
                            .centerCrop()
                            .into(iv_profileImage);

                    Glide.with(getApplicationContext())
                            .load(imageUrl)
                            .centerCrop()
                            .into(iv_albumImage);
                }
                tv_name.setText(name);
                tv_date.setText(revisedDate);
                tv_commentCount.setText("댓글 " + commentCount + "개");
                if(likeCount.equals("0")){
                    tv_likeCount.setText("가장 먼저 좋아요를 눌러주세요!");
                } else {
                    tv_likeCount.setText(likeCount + "명이 좋아하셨습니다");
                }
//                tv_status.setText(status);

                if(!likeClicked.equals("0")){
                    likeClicked();
                }
            }


            @Override
            public void onFailure(Call<AlbumData> call, Throwable t) {
                Log.i(TAG, "onFailure = " + t);
            }
        });
    }
//
//    // 게시글에 들어간 사진 목록 불러오기
//    private void showImageListOnBoard(String boardSeq) {
//
//        Log.i(TAG, "showImageListOnBoard 동작");
//
//        // 레트로핏 생성하여 인터페이스와 연결
//        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
//
//        // 1. 데이터 형태 지정 (ex. DataClass)
//        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
//        Call<List<BoardData>> call = uploadApis.showImageListOnBoard(boardSeq);
//        call.enqueue(new Callback<List<BoardData>>() {
//            @Override
//            public void onResponse(Call<List<BoardData>> call, retrofit2.Response<List<BoardData>> response) {
//                Log.i(TAG, "showImageListOnBoard response = " + response);
//                List<BoardData> responseBody = response.body();
//                for(int i = 0; i < responseBody.size(); i++) {
//                    boardDataList.add(responseBody.get(i));
//                }
//                // 리사이클러뷰 값 넣기
//                adapter = new BoardImageAdapter(boardDataList, getApplicationContext());
//                recyclerView.setAdapter(adapter);
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onFailure(Call<List<BoardData>> call, Throwable t) {
//                Log.i(TAG, "showImageListOnBoard 에러 = " + t);
//            }
//        });
//    }

    // 댓글 정보를 불러오는 메서드 (객체로 받아서, 리사이클러뷰에 넣는다)
    private void showCommentListAlbum(String albumSeq) {
        Log.i(TAG, "(showCommentListAlbum) 1. 호출 - 댓글 정보를 불러오는 메서드 (객체로 받아서, 리사이클러뷰에 넣는다)");
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<List<CommentData>> call = uploadApis.showCommentListAlbum(albumSeq);
        call.enqueue(new Callback<List<CommentData>>() {
            @Override
            public void onResponse(Call<List<CommentData>> call, retrofit2.Response<List<CommentData>> response) {

                Log.i(TAG, "(showCommentListAlbum) response = " + response);
                List<CommentData> responseBody = response.body();
                for(int i = 0; i < responseBody.size(); i++) {
                    commentDataList.add(responseBody.get(i));
                }
                // 리사이클러뷰 값 넣기
//                comment_adapter = new CommentAdapter(commentDataList, getApplicationContext(), this);
//                comment_rv.setAdapter(comment_adapter);
                comment_adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<CommentData>> call, Throwable t) {
                Log.i(TAG, "(showCommentListAlbum) 에러 = " + t);
            }
        });
    }


    // 댓글 내용을 입력 후 저장하는 메서드 (댓글을 입력한 계정의 고유값을 보낸다. 입력한 내용을 보낸다.
    private void commentCreate(HashMap<String, String> hashmap) {
        Log.i(TAG, "(commentCreate) 1. 메서드 호출");
        Log.i(TAG, "(commentCreate) 2. 서버로 보내는 값 = " + String.valueOf(hashmap));

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("commentInput", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("commentInput")));
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        textToServer.put("albumSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("albumSeq")));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<CommentData> call = uploadApis.createCommentAlbum(textToServer);
        call.enqueue(new Callback<CommentData>() {
            @Override
            public void onResponse(Call<CommentData> call, retrofit2.Response<CommentData> response) {

                Log.i(TAG, "(commentCreate) 3. 서버에서 전달받은 값, response : " + response);

                if(response.body() != null){
                    CommentData commentData = response.body();

                    Gson gson = new Gson();
                    String object = gson.toJson(commentData);
                    Log.i(TAG, "(commentCreate) 4. 서버에서 전달받은 객체 object : " + object);

                    String userProfileImage = commentData.getUserProfileImage();
                    String userName = commentData.getUserName();
                    String userSeq = commentData.getUserSeq();
                    String commentInput = commentData.getCommentInput();
                    String albumSeq = commentData.getAlbumSeq();
                    String commentDate = commentData.getCommentDate();
                    String commentSeq = commentData.getCommentSeq();
                    String success = commentData.getSuccess();
                    String uploaderSeq = commentData.getUploaderSeq();
                    moimSeq = commentData.getMoimSeq();

                    if(success.equals("1")){
                        Toast.makeText(AlbumView.this, "댓글 작성을 완료했습니다", Toast.LENGTH_SHORT).show();
;

//                    finish();
//                    Intent intent = new Intent(getApplicationContext(), BoardView.class);
//                    // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
//                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                     intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                     intent.putExtra("boardSeq", boardSeq);
//                     intent.putExtra("scrollBottom", true);
//                     Log.i(TAG, "commentCreate 동작 후 boardSeq = " + boardSeq);
//                    Log.i(TAG, "commentCreate 동작 후 scrollBottom = " + true);
//                    startActivity(intent);
                        commentDataList.add(commentData);
                        comment_adapter.notifyDataSetChanged();

                        // '댓글 n개'라는 text에서 n을 추출하고, 1을 더해서 String 값으로 댓글 합계를 변경한다
                        String commentCount = tv_commentCount.getText().toString();
                        String commentCountNum = commentCount.substring(3,4);
                        int currentCommentCountInt = Integer.parseInt(commentCountNum) + 1;
                        String finalCommentCount = String.valueOf(currentCommentCountInt);
                        tv_commentCount.setText("댓글 " + finalCommentCount + "개");
//
//                    tv_commentCount.post(new Runnable() {
//                        @Override
//                        public void run() {
//                        }
//                    });

                            // 댓글 남겼다는 노티피케이션을 서버로 보냄
                        NotiCommentMsgToServer notiCommentMsgToServer = new NotiCommentMsgToServer(ChatService.member_socket, albumSeq, uploaderSeq);
                        notiCommentMsgToServer.start();



                        // 댓글 작성 완료 후에 현재 화면의 최 하단 스크롤로 이동
                        nestedScrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                nestedScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
                            }
                        });

                    }

                }



            }
            @Override
            public void onFailure(Call<CommentData> call, Throwable t) {
            }
        });
    }

    // 게시글에 좋아요 클릭시, likeBoard 테이블에 행 추가(boardSeq, userSeq 넣기)
    private void likeAlbumAdd(HashMap<String, String> hashmap) {
        Log.i(TAG, "(likeAlbumAdd) 1. 호출 - 게시글에 좋아요 클릭 시, likeBoard 테이블에 행 추가");
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Log.i(TAG, "(likeAlbumAdd) 2. HTTP 통신, 서버로 보내는 값, hashmap : " + String.valueOf(hashmap));

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        textToServer.put("albumSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("albumSeq")));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<CommentData> call = uploadApis.likeAlbumAdd(textToServer);
        call.enqueue(new Callback<CommentData>() {
            @Override
            public void onResponse(Call<CommentData> call, retrofit2.Response<CommentData> response) {

                Log.i(TAG, "(likeAlbumAdd) 3. 서버로부터 수신한 값, response = " + response);
                CommentData responseBody = response.body();
                Log.i(TAG, "responseBody = " + responseBody);
                String success = responseBody.getSuccess();
                String boardSeq = responseBody.getBoardSeq();
                String likeCount = responseBody.getLikeCount();

                Gson gson = new Gson();
                String object = gson.toJson(responseBody);
                Log.i(TAG, "(likeAlbumAdd) 4. 서버로부터 수신한 값, object : " + object);

                if(success.equals("1")){
                    Toast.makeText(AlbumView.this, "좋아요를 클릭했습니다", Toast.LENGTH_SHORT).show();

                    if(likeCount.equals("0")){
                        tv_likeCount.setText("가장 먼저 좋아요를 눌러주세요!");
                    } else {
                        tv_likeCount.setText(likeCount + "명이 좋아하셨습니다");
                    }


//                    Intent intent = new Intent(getApplicationContext(), BoardView.class);
//                    // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
//                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                     intent.putExtra("boardSeq", boardSeq);
//                    startActivity(intent);
                }
            }
            @Override
            public void onFailure(Call<CommentData> call, Throwable t) {
            }
        });
    }

    // 게시글에 좋아요 클릭시, likeBoard 테이블에 행 추가(boardSeq, userSeq 넣기)
    private void likeAlbumCancel(HashMap<String, String> hashmap) {
        Log.i(TAG, "(likeAlbumCancel) 1. 호출 - 게시글에 좋아요 클릭시, likeBoard 테이블에 행 추가");
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Log.i(TAG, "(likeAlbumCancel) 2. HTTP 통신, 서버로 보내는 값, hashmap : " + String.valueOf(hashmap));
        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        textToServer.put("albumSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("albumSeq")));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<CommentData> call = uploadApis.likeAlbumCancel(textToServer);
        call.enqueue(new Callback<CommentData>() {
            @Override
            public void onResponse(Call<CommentData> call, retrofit2.Response<CommentData> response) {
                Log.i(TAG, "(likeAlbumCancel) 3. 서버로부터 수신한 값, response = " + response);
                Log.i(TAG, "response = " + response);
                CommentData responseBody = response.body();
                Log.i(TAG, "responseBody = " + responseBody);
                String success = responseBody.getSuccess();
                String boardSeq = responseBody.getBoardSeq();
                String likeCount = responseBody.getLikeCount();

                if(success.equals("1")){
                    Toast.makeText(AlbumView.this, "좋아요를 취소했습니다", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(getApplicationContext(), BoardView.class);
//                    // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
//                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                     intent.putExtra("boardSeq", boardSeq);
//                    startActivity(intent);
                    if(likeCount.equals("0")){
                        tv_likeCount.setText("가장 먼저 좋아요를 눌러주세요!");
                    } else {
                        tv_likeCount.setText(likeCount + "명이 좋아하셨습니다");
                    }
                }
            }
            @Override
            public void onFailure(Call<CommentData> call, Throwable t) {
            }
        });
    }


    // 게시글 삭제
    private void deleteAlbum(HashMap<String, String> hashmap) {
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        textToServer.put("albumSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("albumSeq")));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<CommentData> call = uploadApis.deleteAlbum(textToServer);
        call.enqueue(new Callback<CommentData>() {
            @Override
            public void onResponse(Call<CommentData> call, retrofit2.Response<CommentData> response) {

                Log.i(TAG, "response = " + response);
                CommentData responseBody = response.body();
                Log.i(TAG, "responseBody = " + responseBody);
                String success = responseBody.getSuccess();
                String albumSeq = responseBody.getAlbumSeq();
                String likeCount = responseBody.getLikeCount();

                if(success.equals("1")){
                    Toast.makeText(AlbumView.this, "게시사진을 삭제했습니다", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(getApplicationContext(), BoardView.class);
//                    // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
//                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                     intent.putExtra("boardSeq", boardSeq);
//                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<CommentData> call, Throwable t) {
            }
        });
    }


    @Override
    public void handle(int position) {
        // '댓글 n개'라는 text에서 n을 추출하고, 1을 더해서 String 값으로 댓글 합계를 변경한다
        String commentCount = tv_commentCount.getText().toString();
        String commentCountNum = commentCount.substring(3,4);
        int currentCommentCountInt = Integer.parseInt(commentCountNum) - 1;
        String finalCommentCount = String.valueOf(currentCommentCountInt);
        tv_commentCount.setText("댓글 " + finalCommentCount + "개");

    }


    // 갤러리 갱신
    private void galleryAddPic(String Image_Path) {
        // 이전 사용 방식
        /*Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(Image_Path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.context.sendBroadcast(mediaScanIntent);*/

        File file = new File(Image_Path);
        MediaScannerConnection.scanFile(
                this,
                new String[]{file.toString()},
                null, null);
    }

    private String get_now_time() {
        long now = System.currentTimeMillis(); // 현재 시간을 가져온다.
        Date mDate = new Date(now); // Date형식으로 고친다.
        // 날짜, 시간을 가져오고 싶은 형태로 가져올 수 있다.
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss", Locale.KOREA);
        String now_time = simpleDate.format(mDate);
        Log.d(TAG, "태그 현재시간 now_time : " + now_time);
        return now_time;
    }


//    // 댓글 남겼다는 노티피케이션을 서버로 보냄
//    NotiCommentMsgToServer notiCommentMsgToServer = new NotiCommentMsgToServer(ChatService.member_socket, boardSeq, writerSeq);
//    notiCommentMsgToServer.start();

    // 메세지를 보낼 때 동작하는 스레드
    class NotiCommentMsgToServer extends Thread {
        Socket socket;
        String albumSeq;
        String uploaderSeq;

        public NotiCommentMsgToServer(Socket socket, String albumSeq, String uploaderSeq) {
            try {
                this.socket = socket;
                this.albumSeq = albumSeq;
                this.uploaderSeq = uploaderSeq;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                Log.i(TAG, "(notiCommentMsgToServer) 1. 스레드 시작 - ChatService.chatData 객체를 채팅 서버로 보내는 스레드");

                sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
                String userSeq = sp.getString("userSeq", ""); // 로그인한 계정의 고유값
                String userName = sp.getString("userName", "");
                String msg = et_commentInput.getText().toString(); // 입력한 메세지
                Log.i(TAG, "(notiCommentMsgToServer) 2. msg : " + msg);

                Log.i(TAG, "(notiCommentMsgToServer) 2. ChatService.chatData 객체에 위 데이터 저장");
                ChatService.chatData.setCommand("commentAlbum"); // 명령
                ChatService.chatData.setUserSeq(userSeq); // 댓글 작성자의 고유값
                ChatService.chatData.setUploaderSeq(uploaderSeq); //게시글 작성자의 고유값
                ChatService.chatData.setUserName(userName); // 댓글을 작성자의 이름
                ChatService.chatData.setMsg(msg); // 작성한 댓글 메세지
                ChatService.chatData.setAlbumSeq(albumSeq); //
                ChatService.chatData.setMoimSeq(moimSeq);

                Gson gson = new Gson();
                String sendingObject = gson.toJson(ChatService.chatData);
                Log.i(TAG, "(notiCommentMsgToServer) 3. 서버로 보내는 값(Gson 사용) : " + sendingObject);

                // 서비스 클래스에 있는 PrintWriter 객체를 사용해서, 방금 전에 입력한 값을 담는다.
                ChatService.sendWriter.println(sendingObject);
                Log.i(TAG, "(notiCommentMsgToServer) 4. ");

                ChatService.sendWriter.flush();
                Log.i(TAG, "(notiCommentMsgToServer) 5. ");

                et_commentInput.setText("");


            } catch (NetworkOnMainThreadException e) {
                e.printStackTrace();
            }
        }
    }


    //    // 댓글 남겼다는 노티피케이션을 서버로 보냄
//    NotiLikeMsgToServer2 notiLikeMsgToServer = new NotiLikeMsgToServer2(ChatService.member_socket, boardSeq, writerSeq);
//    notiLikeMsgToServer.start();

    // 메세지를 보낼 때 동작하는 스레드
    class NotiLikeMsgToServer2 extends Thread {
        Socket socket;
        String albumSeq;
        String uploaderSeq;

        public NotiLikeMsgToServer2(Socket socket, String albumSeq, String uploaderSeq) {
            try {
                this.socket = socket;
                this.albumSeq = albumSeq;
                this.uploaderSeq = uploaderSeq;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            try {
                Log.i(TAG, "(NotiLikeMsgToServer2) 1. 스레드 시작 - ChatService.chatData 객체를 채팅 서버로 보내는 스레드");

                sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
                String userSeq = sp.getString("userSeq", ""); // 로그인한 계정의 고유값
                String userName = sp.getString("userName", "");
                String msg = et_commentInput.getText().toString(); // 입력한 메세지
                Log.i(TAG, "(NotiLikeMsgToServer2) 2. msg : " + msg);

                Log.i(TAG, "(NotiLikeMsgToServer2) 2. ChatService.chatData 객체에 위 데이터 저장");
                ChatService.chatData.setCommand("likeAlbum"); // 명령
                ChatService.chatData.setUserSeq(userSeq); // 댓글 작성자의 고유값
                ChatService.chatData.setUploaderSeq(uploaderSeq); //게시글 작성자의 고유값
                ChatService.chatData.setUserName(userName); // 댓글을 작성자의 이름
                ChatService.chatData.setAlbumSeq(albumSeq); //
                ChatService.chatData.setMoimSeq(moimSeq);

                Gson gson = new Gson();
                String sendingObject = gson.toJson(ChatService.chatData);
                Log.i(TAG, "(NotiLikeMsgToServer2) 3. 서버로 보내는 값(Gson 사용) : " + sendingObject);

                // 서비스 클래스에 있는 PrintWriter 객체를 사용해서, 방금 전에 입력한 값을 담는다.
                ChatService.sendWriter.println(sendingObject);
                Log.i(TAG, "(NotiLikeMsgToServer2) 4. ");

                ChatService.sendWriter.flush();
                Log.i(TAG, "(NotiLikeMsgToServer2) 5. ");

                et_commentInput.setText("");


            } catch (NetworkOnMainThreadException e) {
                e.printStackTrace();
            }
        }
    }


}