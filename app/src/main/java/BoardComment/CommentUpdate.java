package BoardComment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.somoim.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import Common.DataClass;
import Common.NetworkClient;
import Common.UploadApis;
import MoimDetail.Board.BoardView;
import MoimDetail.Photo.AlbumView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class CommentUpdate extends AppCompatActivity {

    ImageView iv_back;
    TextView tv_save;
    TextInputLayout til_commentInput;
    TextInputEditText et_commentInput;

    // 레트로핏 통신 공통 - 상단에 선언
    NetworkClient networkClient; // 별도로 클래스 만듬
    UploadApis uploadApis; // 변돌로 클래스 만듬, interface

    private static final String TAG = "CommentUpdate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_update);

        iv_back = findViewById(R.id.iv_back);
        tv_save = findViewById(R.id.tv_save);
        til_commentInput = findViewById(R.id.til_commentInput);
        et_commentInput = findViewById(R.id.et_commentInput);

        Intent intent = getIntent();
        String commentInput = intent.getStringExtra("commentInput");
        String boardSeq = intent.getStringExtra("boardSeq");
        String commentSeq = intent.getStringExtra("commentSeq");
        String albumSeq = intent.getStringExtra("albumSeq");

        // 댓글 내용 세팅
        et_commentInput.setText(commentInput);

        // 완료 버튼 클릭시
        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "완료 버튼 클릭함");

                String comment = et_commentInput.getText().toString();

                // 서버로 보낼 값 (onCreate 안에 선언)
                HashMap hashmap = new HashMap<String, String>();
                hashmap.put("comment", comment);
                hashmap.put("boardSeq", boardSeq);
                hashmap.put("commentSeq", commentSeq);
                hashmap.put("albumSeq", albumSeq);

                Log.i(TAG, "hashmap 서버로 보내는 값 = " + String.valueOf(hashmap));

                updateComment(hashmap);
                // 필요한 값  = 게시글 고유번호,
            }
        });



    }


    // 게시글에 좋아요 클릭시, likeBoard 테이블에 행 추가(boardSeq, userSeq 넣기)
    private void updateComment(HashMap<String, String> hashmap) {
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("comment", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("comment")));
        textToServer.put("commentSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("commentSeq")));

        if(hashmap.get("boardSeq")!=null){
            textToServer.put("boardSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("boardSeq")));
        }
        if(hashmap.get("albumSeq")!=null){
            textToServer.put("albumSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("albumSeq")));
        }


        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<CommentData> call = uploadApis.updateComment(textToServer);
        call.enqueue(new Callback<CommentData>() {
            @Override
            public void onResponse(Call<CommentData> call, retrofit2.Response<CommentData> response) {

                Log.i(TAG, "response = " + response);
                CommentData responseBody = response.body();
                Log.i(TAG, "responseBody = " + responseBody);
                String success = responseBody.getSuccess();

                if(success.equals("2")){
                    Toast.makeText(CommentUpdate.this, "댓글 수정을 완료했습니다", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), BoardView.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("boardSeq", hashmap.get("boardSeq"));
                    startActivity(intent);
                } else if(success.equals("1")) {
                    Toast.makeText(CommentUpdate.this, "댓글 수정을 완료했습니다", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), AlbumView.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("albumSeq", hashmap.get("albumSeq"));
                    startActivity(intent);
                }
            }
            @Override
            public void onFailure(Call<CommentData> call, Throwable t) {
            }
        });
    }


}