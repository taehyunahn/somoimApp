package MoimDetail.Board;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import BoardComment.CommentAdapter;
import BoardComment.CommentData;
import Chat.ChatData;
import Chat.ChatService;
import Common.NetworkClient;
import Common.UploadApis;
import MoimDetail.MoimDetailMain;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class BoardView extends AppCompatActivity implements CommentAdapter.EventHandler{

    ImageView iv_back, iv_more, iv_profileImage;
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

    String moimSeq;

    private static final String TAG = "BoardView(게시글 상세화면)";

    Boolean commentFocus = false;

    String writerSeq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_view);
        Log.i(TAG, "(onCreate) 1. 호출");

        // UI연결
        iv_back = findViewById(R.id.iv_back);
        iv_more = findViewById(R.id.iv_more);
        iv_profileImage = findViewById(R.id.iv_profileImage);
        tv_name = findViewById(R.id.tv_name);
        tv_status = findViewById(R.id.tv_status);
        tv_title = findViewById(R.id.tv_title);
        tv_date = findViewById(R.id.tv_date);
        tv_content = findViewById(R.id.tv_content);
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


        // sharedPreference 세팅
        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq","");
        Log.i(TAG, "(onCreate) 2. s.p(login)으로 받은 값, userSeq : " + userSeq);


        Intent intent = getIntent();
        String boardSeq = intent.getStringExtra("boardSeq");
        Log.i(TAG, "(onCreate) 3. intent로 받은 값, boardSeq : " + boardSeq);

//        Boolean scrollBottom = intent.getBooleanExtra("scrollBottom", false);
//        Log.i(TAG, "댓글 작성 후 다실 열면 scrollBottom = " + String.valueOf(scrollBottom));
//        if(scrollBottom){
//            nestedScrollView.post(new Runnable() {
//                @Override
//                public void run() {
//                    nestedScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
//                }
//            });
//        }

        // 서버에 게시글 정보 요청해서 보여주는 메서드

        showBoardInfo(boardSeq, userSeq);
        showImageListOnBoard(boardSeq);

        // 댓글 정보 보여주는 메서드
        showCommentList(boardSeq, userSeq);

        // member 리사이클러뷰 세팅
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new BoardImageAdapter(boardDataList, getApplicationContext());
        recyclerView.setAdapter(adapter);


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

                Log.i(TAG, "댓글 전송 버튼을 클릭했습니다");
                String commentInput  = et_commentInput.getText().toString();

                if(commentInput.equals("")){
                    Toast.makeText(BoardView.this, "댓글을 입력해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    // 서버로 보낼 값 (onCreate 안에 선언)
                    HashMap hashmap = new HashMap<String, String>();
                    hashmap.put("commentInput", commentInput);
                    hashmap.put("userSeq", userSeq);
                    hashmap.put("boardSeq", boardSeq);
                    Log.i(TAG, "서버로 보내는 값 hashmap = " + String.valueOf(hashmap));

                    commentCreate(hashmap);
                }


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
                inflater.inflate(R.menu.menu_board, popup.getMenu());
                // + Handling click events
                // To perform an action when the user selects a menu item,
                // you must implement the PopupMenu.OnMenuItemClickListener interface and register it with your PopupMenu by calling setOnMenuItemclickListener().
                // When the user selects an item, the system calls the onMenuItemClick() callback in your interface.
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.editBoard){
                            Log.i(TAG, "게시글 수정 클릭");
                            Intent intent = new Intent(getApplicationContext(), BoardUpdate.class);
                            intent.putExtra("boardSeq", boardSeq);
                            startActivity(intent);
                        } else if(item.getItemId() == R.id.deleteBoard){
                            Log.i(TAG, "게시글 삭제 클릭");

                            // 서버로 보낼 값 (onCreate 안에 선언)
                            HashMap hashmap = new HashMap<String, String>();
                            hashmap.put("userSeq", userSeq);
                            hashmap.put("boardSeq", boardSeq);
                            Log.i(TAG, "게시물 삭제하기 클릭 후 서버로 보내는 값 hashmap = " + String.valueOf(hashmap));
                            boardDelete(hashmap);
                            // 게시글의 고유값을 서버에 보내서 삭제한다. 그리고 모임 상세정보의 게시판 목록으로 간다 (모임의 고유값 필요)

                        }
                        return false;
                    }
                });

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
                editor.putBoolean(userSeq + boardSeq + "likeClicked", true);
                editor.commit();


                // 게시물의 고유값(boardSeq), 누른사람의 계정 고유값(userSeq)을 서버에 보낸다
                // likeBoard 테이블에 boardSeq를 값을 사용해서 insert한다
                // likeBoard 테이블에 클라이언트가 보낸 boardSeq와 일치하는 seq 개수(해당 게시글의 좋아요 합계)를 클라이언트에 보낸다

                Log.i(TAG, "좋아요 버튼을 클릭했습니다");

                // 서버로 보낼 값 (onCreate 안에 선언)
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("userSeq", userSeq);
                hashmap.put("boardSeq", boardSeq);
                Log.i(TAG, "서버로 보내는 값 hashmap = " + String.valueOf(hashmap));

                boardLikeAdd(hashmap);

                NotiLikeMsgToServer1 notiLikeMsgToServer1 = new NotiLikeMsgToServer1(ChatService.member_socket, boardSeq, writerSeq);
                notiLikeMsgToServer1.start();


            }
        });

        // 좋아요 취소 버튼 클릭 시
        btn_likeCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeCanceled();
                editor.remove(userSeq + boardSeq + "likeClicked");
                editor.commit();

                // 서버로 보낼 값 (onCreate 안에 선언)
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("userSeq", userSeq);
                hashmap.put("boardSeq", boardSeq);
                Log.i(TAG, "서버로 보내는 값 hashmap = " + String.valueOf(hashmap));
                boardLikeCancel(hashmap);
            }
        });

        // 스와이프 새로고침
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 서버에 게시글 정보 요청해서 보여주는 메서드
                showBoardInfo(boardSeq, userSeq);

                commentDataList.clear();
                boardDataList.clear();

                showImageListOnBoard(boardSeq);
                // 댓글 정보 보여주는 메서드
                showCommentList(boardSeq, userSeq);

                swiperefresh.setRefreshing(false);


            }
        });
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MoimDetailMain.class);
        // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("moimSeq", moimSeq);
        intent.putExtra("fromBoardView", true);
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

        Boolean likeClicked = sp.getBoolean(userSeq + boardSeq + "likeClicked", false);
        if(likeClicked){
            likeClicked();
        }
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


    // HTTP통신 매서드 1. 게시글 정보 불러오기
    // (통신방식) Retrofit
    // (클라이언트→서버) boardSeq, userSeq
    // (서버→클라이언트) MoimData 객체의 값(profileImage, name, date, title, content, commentCount, likeCount, writerSeq)

    private void showBoardInfo(String boardSeq, String userSeq) {

        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Call<BoardData> call = uploadApis.showBoardInfo(boardSeq, userSeq);
        call.enqueue(new Callback<BoardData>() {
            @Override
            public void onResponse(Call<BoardData> call, retrofit2.Response<BoardData> response) {
                Log.d("onResponse", response.body().toString());

                if(!response.isSuccessful()){
                    return;
                }

                //받아야할 정보
                //1. 작성자 프로필 사진, 2. 작성자 이름, 3. 작성자의 모임장여부, 4. 게시글 작성일시, 5. 게시글 제목, 6. 게시글 내용,

                BoardData responseBody = response.body(); //
                String profileImage = responseBody.getProfileImage(); //
                String name = responseBody.getName(); //

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
                writerSeq = responseBody.getWriterSeq(); //
                moimSeq = responseBody.getMoimSeq();


                Log.i(TAG, "profileImage = " + profileImage);
                Log.i(TAG, "name = " + name);
                Log.i(TAG, "date = " + date);
                Log.i(TAG, "revisedDate = " + revisedDate);

                Log.i(TAG, "title = " + title);
                Log.i(TAG, "content = " + content);
                Log.i(TAG, "boardSeq = " + boardSeq);
                Log.i(TAG, "showBoardInfo에서 commentCount = " + commentCount);
                Log.i(TAG, "showBoardInfo에서 likeCount = " + likeCount);
                Log.i(TAG, "writerSeq = " + writerSeq);

                if(!writerSeq.equals(userSeq)){
                    iv_more.setVisibility(View.INVISIBLE);
                }


                if(responseBody != null){
                    Glide.with(getApplicationContext())
                            .load(profileImage)
                            .centerCrop()
                            .into(iv_profileImage);
                }
                tv_name.setText(name);
                tv_date.setText(revisedDate);
                tv_title.setText(title); //모임 제목 설정
                tv_content.setText(content);
                tv_commentCount.setText("댓글 " + commentCount + "개");
                if(likeCount.equals("0")){
                    tv_likeCount.setText("가장 먼저 좋아요를 눌러주세요!");
                } else {
                    tv_likeCount.setText(likeCount + "명이 좋아하셨습니다");
                }

            }

            @Override
            public void onFailure(Call<BoardData> call, Throwable t) {
                Log.i(TAG, "onFailure = " + t);
            }
        });
    }

    // 게시글에 들어간 사진 목록 불러오기
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
                for(int i = 0; i < responseBody.size(); i++) {

                    Gson gson = new Gson();
                    String object = gson.toJson(responseBody.get(i));
                    Log.i(TAG, "(showImageListOnBoard) 3. 서버에서 전달받은 객체, object(BoardData) : " + object);
                    boardDataList.add(responseBody.get(i));
                }
                Log.i(TAG, "(showImageListOnBoard) 3. boardDataList.add(전달받은 객체)하여 adapter와 연결 후 갱신");
                // 리사이클러뷰 값 넣기
                adapter = new BoardImageAdapter(boardDataList, getApplicationContext());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<BoardData>> call, Throwable t) {
                Log.i(TAG, "(showImageListOnBoard) 에러, t : " + t);
            }
        });
    }

    // HTTP통신 매서드 1. 댓글 정보를 불러오는 메서드
    // (통신방식) Retrofit
    // (클라이언트→서버) boardSeq, userSeq
    // (서버→클라이언트) CommentData 객체의 값(userSeq(writerSeq), boardSeq, commentSeq, albumSeq, userProfileImage, userName, commentInput, commentDate)
    private void showCommentList(String boardSeq, String userSeq) {
        Log.i(TAG, "(showCommentList) 1. 호출 - 댓글 정보를 불러오는 메서드");

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<List<CommentData>> call = uploadApis.showCommentList(boardSeq, userSeq);
        Log.i(TAG, "(showCommentList) 2. 서버로 보내는 값, boardSeq : " + boardSeq + " / userSeq : " + userSeq);

        call.enqueue(new Callback<List<CommentData>>() {
            @Override
            public void onResponse(Call<List<CommentData>> call, retrofit2.Response<List<CommentData>> response) {
                Log.i(TAG, "(showCommentList) 3. 서버에서 받은 response = " + response);
                List<CommentData> responseBody = response.body();
                for(int i = 0; i < responseBody.size(); i++) {

                    Gson gson = new Gson();
                    String object = gson.toJson(responseBody.get(i));
                    Log.i(TAG, "(showCommentList) 4. 서버에서 받은 객체, object(CommentData) : " + object);
                    commentDataList.add(responseBody.get(i));

                }
                // 리사이클러뷰 값 넣기
//                comment_adapter = new CommentAdapter(commentDataList, getApplicationContext(), this);
//                comment_rv.setAdapter(comment_adapter);
                comment_adapter.notifyDataSetChanged();

                et_commentInput.setText("");

                if(commentFocus){
                    et_commentInput.post(new Runnable() {
                        @Override
                        public void run() {
                            et_commentInput.setFocusableInTouchMode(true);
                            et_commentInput.requestFocus();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(et_commentInput,0);
                        }
                    });
                    commentFocus = false;
                }
            }

            @Override
            public void onFailure(Call<List<CommentData>> call, Throwable t) {
                Log.i(TAG, "showImageListOnBoard 에러 = " + t);
            }
        });
    }

    // HTTP통신 매서드 1. 댓글 내용을 입력 후 저장하는 메서드 (댓글을 입력한 계정의 고유값을 보낸다. 입력한 내용을 보낸다.
    // (통신방식) Retrofit
    // (클라이언트→서버) commentInput, userSeq, boardSeq
    // (서버→클라이언트) CommentData 객체의 값(userProfileImage, userName, userSeq, commentInput, boardSeq, commentDate, commentSeq, success)

    private void commentCreate(HashMap<String, String> hashmap) {
        Log.i(TAG, "(commentCreate) 1. 호출 - 댓글 내용을 입력 후 저장하는 메서드 (댓글을 입력한 계정의 고유값을 보낸다. 입력한 내용을 보낸다.");

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("commentInput", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("commentInput")));
        Log.i(TAG, "(commentCreate) 2. 서버로 보내는 값, commentInput :  " + hashmap.get("commentInput"));
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        Log.i(TAG, "(commentCreate) 2. 서버로 보내는 값, userSeq :  " + hashmap.get("userSeq"));
        textToServer.put("boardSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("boardSeq")));
        Log.i(TAG, "(commentCreate) 2. 서버로 보내는 값, boardSeq :  " + hashmap.get("boardSeq"));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<CommentData> call = uploadApis.createCommentBoard(textToServer);
        call.enqueue(new Callback<CommentData>() {
            @Override
            public void onResponse(Call<CommentData> call, retrofit2.Response<CommentData> response) {
                Log.i(TAG, "(commentCreate) 3. 서버로부터 받는 값, response :  " + response);

                CommentData responseBody = response.body();

                Gson gson = new Gson();
                String object = gson.toJson(responseBody);
                Log.i(TAG, "(commentCreate) 3. 서버로부터 받는 값, object(CommentData) : " + object);

                String userProfileImage = responseBody.getUserProfileImage();
                String userName = responseBody.getUserName();
                String userSeq = responseBody.getUserSeq();
                String commentInput = responseBody.getCommentInput();
                String boardSeq = responseBody.getBoardSeq();
                String commentDate = responseBody.getCommentDate();
                String commentSeq = responseBody.getCommentSeq();
                String success = responseBody.getSuccess();
                String writerSeq = responseBody.getWriterSeq();

                if(success.equals("1")){
                    Toast.makeText(BoardView.this, "댓글 작성을 완료했습니다", Toast.LENGTH_SHORT).show();



                    commentDataList.clear();
                    commentFocus = true;
                    showCommentList(boardSeq, userSeq);



//                    commentDataList.add(responseBody);
//                    comment_adapter.notifyDataSetChanged();

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
                    NotiCommentMsgToServer1 notiCommentMsgToServer1 = new NotiCommentMsgToServer1(ChatService.member_socket, boardSeq, writerSeq);
                    notiCommentMsgToServer1.start();






                    // 댓글 작성 완료 후에 현재 화면의 최 하단 스크롤로 이동
                    nestedScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            nestedScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
                        }
                    });

                }


            }
            @Override
            public void onFailure(Call<CommentData> call, Throwable t) {
            }
        });
    }



    // HTTP통신 매서드 1. 게시글에 좋아요 클릭시, likeBoard 테이블에 행 추가(boardSeq, userSeq 넣기)
    // (통신방식) Retrofit
    // (클라이언트→서버) hashmap(userSeq, boardSeq)
    // (서버→클라이언트) CommentData 객체의 값(success, likeCount)

    private void boardLikeAdd(HashMap<String, String> hashmap) {
        Log.i(TAG, "(boardLikeAdd) 1. 호출 - 게시글에 좋아요 클릭시, likeBoard 테이블에 행 추가(boardSeq, userSeq 넣기)");
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        Log.i(TAG, "(boardLikeAdd) 2. 서버로 보내는 값, userSeq : " + hashmap.get("userSeq"));
        textToServer.put("boardSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("boardSeq")));
        Log.i(TAG, "(boardLikeAdd) 2. 서버로 보내는 값, boardSeq : " + hashmap.get("boardSeq"));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<CommentData> call = uploadApis.boardLikeAdd(textToServer);
        call.enqueue(new Callback<CommentData>() {
            @Override
            public void onResponse(Call<CommentData> call, retrofit2.Response<CommentData> response) {
                Log.i(TAG, "(boardLikeAdd) 3. 서버로부터 받은 값, response : " + response);

                CommentData responseBody = response.body();
                Gson gson = new Gson();
                String object = gson.toJson(responseBody);
                Log.i(TAG, "(boardLikeAdd) 3. 서버로부터 받은 값, object(CommentData) : " + object);

                String success = responseBody.getSuccess();
                String likeCount = responseBody.getLikeCount();

                if(success.equals("1")){
                    Toast.makeText(BoardView.this, "좋아요를 클릭했습니다", Toast.LENGTH_SHORT).show();

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

    // HTTP통신 매서드 2. 게시글에 좋아요 클릭시, likeBoard 테이블에 행 추가(boardSeq, userSeq 넣기)
    // (통신방식) Retrofit
    // (클라이언트→서버) hashmap(userSeq, boardSeq)
    // (서버→클라이언트) CommentData 객체의 값(success, likeCount)
    private void boardLikeCancel(HashMap<String, String> hashmap) {
        Log.i(TAG, "(boardLikeCancel) 1. 호출 - 게시글에 좋아요 클릭시, likeBoard 테이블에 행 추가(boardSeq, userSeq 넣기)");
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        Log.i(TAG, "(boardLikeCancel) 2. 서버로 보내는 값, userSeq : " + hashmap.get("userSeq"));
        textToServer.put("boardSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("boardSeq")));
        Log.i(TAG, "(boardLikeCancel) 2. 서버로 보내는 값, boardSeq : " + hashmap.get("boardSeq"));


        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<CommentData> call = uploadApis.boardLikeCancel(textToServer);
        call.enqueue(new Callback<CommentData>() {
            @Override
            public void onResponse(Call<CommentData> call, retrofit2.Response<CommentData> response) {
                Log.i(TAG, "(boardLikeCancel) 3. 서버로부터 받은 값, response : " + response);
                CommentData responseBody = response.body();
                Gson gson = new Gson();
                String object = gson.toJson(responseBody);
                Log.i(TAG, "(boardLikeCancel) 3. 서버로부터 받은 값, object(CommentData) : " + object);

                String success = responseBody.getSuccess();
                String likeCount = responseBody.getLikeCount();

                if(success.equals("1")){
                    Toast.makeText(BoardView.this, "좋아요를 취소했습니다", Toast.LENGTH_SHORT).show();
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


    // HTTP통신 매서드 1. 게시글 삭제
    // (통신방식) Retrofit
    // (클라이언트→서버) userSeq, boardSeq
    // (서버→클라이언트) CommentData 객체의 값()
    private void boardDelete(HashMap<String, String> hashmap) {
        Log.i(TAG, "(boardDelete) 1. 호출");
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        Log.i(TAG, "(boardDelete) 2. 서버로 보내는 값, userSeq : " + hashmap.get("userSeq"));
        textToServer.put("boardSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("boardSeq")));
        Log.i(TAG, "(boardDelete) 2. 서버로 보내는 값, boardSeq : " + hashmap.get("boardSeq"));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<CommentData> call = uploadApis.boardDelete(textToServer);
        call.enqueue(new Callback<CommentData>() {
            @Override
            public void onResponse(Call<CommentData> call, retrofit2.Response<CommentData> response) {


                Log.i(TAG, "(boardDelete) 3. 서버에서 받은 응답, response : " + response);
                CommentData responseBody = response.body();
                Gson gson = new Gson();
                String object = gson.toJson(responseBody);
                Log.i(TAG, "(boardDelete) 3. 서버에서 받은 응답, object(CommentData) : " + object);

                String success = responseBody.getSuccess();
                String boardSeq = responseBody.getBoardSeq();
                String likeCount = responseBody.getLikeCount();


                if(success.equals("1")){
                    Toast.makeText(BoardView.this, "게시글을 삭제했습니다", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(getApplicationContext(), BoardView.class);
//                    // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
//                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                     intent.putExtra("boardSeq", boardSeq);
//                    startActivity(intent);
                    finish();

                    Intent intent = new Intent(getApplicationContext(), MoimDetailMain.class);
                    // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("moimSeq", moimSeq);
                    startActivity(intent);

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
        Log.i(TAG, "commentCount : " + commentCount);
        Log.i(TAG, "commentCount.length() : " + commentCount.length());

        String commentCountNum = "";
        if(commentCount.length() == 5){
            commentCountNum = commentCount.substring(3,4);
            Log.i(TAG, "commentCountNum : " + commentCountNum);
        } else if(commentCount.length() == 6){
            commentCountNum = commentCount.substring(3,5);
            Log.i(TAG, "commentCountNum : " + commentCountNum);
        }

        int currentCommentCountInt = Integer.parseInt(commentCountNum) - 1;
        Log.i(TAG, "currentCommentCountInt : " + String.valueOf(currentCommentCountInt));
        String finalCommentCount = String.valueOf(currentCommentCountInt);
        Log.i(TAG, "finalCommentCount : " + finalCommentCount);
        tv_commentCount.setText("댓글 " + finalCommentCount + "개");

    }



//    // 댓글 남겼다는 노티피케이션을 서버로 보냄
//    NotiCommentMsgToServer notiCommentMsgToServer = new NotiCommentMsgToServer(ChatService.member_socket, boardSeq, writerSeq);
//    notiCommentMsgToServer.start();

    // 메세지를 보낼 때 동작하는 스레드
    class NotiCommentMsgToServer1 extends Thread {
        Socket socket;
        String boardSeq;
        String writerSeq;

        public NotiCommentMsgToServer1(Socket socket, String boardSeq, String writerSeq) {
            try {
                this.socket = socket;
                this.boardSeq = boardSeq;
                this.writerSeq = writerSeq;
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
                ChatService.chatData = new ChatData();

                ChatService.chatData.setCommand("comment"); // 명령
                ChatService.chatData.setUserSeq(userSeq); // 댓글 작성자의 고유값
                ChatService.chatData.setWriterSeq(writerSeq); //게시글 작성자의 고유값
                ChatService.chatData.setUserName(userName); // 댓글을 작성자의 이름
                ChatService.chatData.setMsg(msg); // 작성한 댓글 메세지
                ChatService.chatData.setBoardSeq(boardSeq); //
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
//    NotiLikeMsgToServer1 notiLikeMsgToServer = new NotiLikeMsgToServer1(ChatService.member_socket, boardSeq, writerSeq);
//    notiCommentMsgToServer.start();

    // 메세지를 보낼 때 동작하는 스레드
    class NotiLikeMsgToServer1 extends Thread {
        Socket socket;
        String boardSeq;
        String writerSeq;

        public NotiLikeMsgToServer1(Socket socket, String boardSeq, String writerSeq) {
            try {
                this.socket = socket;
                this.boardSeq = boardSeq;
                this.writerSeq = writerSeq;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                Log.i(TAG, "(NotiLikeMsgToServer1) 1. 스레드 시작 - ChatService.chatData 객체를 채팅 서버로 보내는 스레드");

                sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
                String userSeq = sp.getString("userSeq", ""); // 로그인한 계정의 고유값
                String userName = sp.getString("userName", "");

                Log.i(TAG, "(NotiLikeMsgToServer1) 2. ChatService.chatData 객체에 위 데이터 저장");
                ChatService.chatData = new ChatData();

                ChatService.chatData.setCommand("like"); // 명령
                ChatService.chatData.setUserSeq(userSeq); // 댓글 작성자의 고유값
                ChatService.chatData.setWriterSeq(writerSeq); //게시글 작성자의 고유값
                ChatService.chatData.setUserName(userName); // 댓글을 작성자의 이름
                ChatService.chatData.setBoardSeq(boardSeq); //
                ChatService.chatData.setMoimSeq(moimSeq);

                Gson gson = new Gson();
                String sendingObject = gson.toJson(ChatService.chatData);
                Log.i(TAG, "(NotiLikeMsgToServer1) 3. 서버로 보내는 값(Gson 사용) : " + sendingObject);

                // 서비스 클래스에 있는 PrintWriter 객체를 사용해서, 방금 전에 입력한 값을 담는다.
                ChatService.sendWriter.println(sendingObject);
                Log.i(TAG, "(NotiLikeMsgToServer1) 4. ");

                ChatService.sendWriter.flush();
                Log.i(TAG, "(NotiLikeMsgToServer1) 5. ");

                et_commentInput.setText("");


            } catch (NetworkOnMainThreadException e) {
                e.printStackTrace();
            }
        }
    }


}
