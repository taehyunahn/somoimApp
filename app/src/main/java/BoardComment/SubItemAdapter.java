package BoardComment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.somoim.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import Common.NetworkClient;
import Common.UploadApis;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class SubItemAdapter extends RecyclerView.Adapter<SubItemAdapter.ViewHolder> {

    private List<SubItem> subItemList;

    private EventHandler handler;

    Context context;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private static final String TAG = "MemberAdapter";

    // 레트로핏 통신 공통 - 상단에 선언
    NetworkClient networkClient; // 별도로 클래스 만듬
    UploadApis uploadApis; // 변돌로 클래스 만듬, interface


    public SubItemAdapter(List<SubItem> subItemList, Context context, EventHandler handler) {
        this.subItemList = subItemList;
        this.context = context;
        this.handler = handler;
    }

    public SubItemAdapter(List<SubItem> subItemList) {
        this.subItemList = subItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_sub_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        sp = context.getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();

        String userSeq = sp.getString("userSeq","");

        SubItem subItem = subItemList.get(position);
        // 작성자의 고유값
        String writerSeq = subItem.getUserSeq();
        Log.i(TAG, "게시글 상세보기 화면에서 확인할 것");
        Log.i(TAG, "writerSeq = " + writerSeq);
        Log.i(TAG, "userSeq = " + userSeq);
        if(!writerSeq.equals(userSeq)){
            holder.iv_more.setVisibility(View.INVISIBLE);
        }

        String boardSeq = subItem.getBoardSeq();
        String commentSeq = subItem.getCommentSeq();
        String albumSeq = subItem.getAlbumSeq();
        Log.i(TAG, "댓글의 고유값 commentSeq = " +  commentSeq);

        // 프로필이미지
        if(subItem.getUserProfileImage() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(subItem.getUserProfileImage())
                    .centerCrop()
                    .into(holder.iv_profileImage);
        }
        // 작성자 이름
        holder.tv_name.setText(subItem.getUserName());
        // 댓글 내용
        holder.tv_commentContent.setText(subItem.getCommentInput());
        // 댓글 작성일자
        String dateValue = subItem.getCommentDate();
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
        holder.tv_date.setText(revisedDate);


        // 좋아요 체크박스 클릭
        holder.cb_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // commentSeq를 서버에 보내고, 이에 해당하는 좋아요 개수를 카운팅하도록 한다
                // 서버로 보낼 값 (onCreate 안에 선언)
//                HashMap hashmap = new HashMap<String, String>();
//                hashmap.put("commentSeq", commentSeq);
//                hashmap.put("userSeq", userSeq);
//                hashmap.put("boardSeq", boardSeq);
//
//                Log.i(TAG, String.valueOf(hashmap));
//                likeCommentAdd(hashmap);

                Log.i(TAG, "좋아요 체크박스를 클릭했습니다");
            }
        });

        // 답글 달기 클릭 시 (댓글의 댓글)
        holder.tv_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "답글 달기를 클릭했습니다");
            }
        });

        // 좋아요 합계 (작업 예정)


        // 댓글합계 (작업 예정)

        // 더보기 기능
        holder.iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "더보기 버튼 클릭");

                // PopupMenu 사용방법 (a Menu in XML를 defining해야 한다)
                // 1. Instantiate a PopupMenu with its constructor, which takes the current application Context and the View to which the menu should be anchored.
                PopupMenu popup = new PopupMenu(context.getApplicationContext(), v);
                // 2. Use MenuInflater to inflate your menu resource into the Menu object returned by PopupMenu.getMenu().
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_comment, popup.getMenu());
                // + Handling click events
                // To perform an action when the user selects a menu item,
                // you must implement the PopupMenu.OnMenuItemClickListener interface and register it with your PopupMenu by calling setOnMenuItemclickListener().
                // When the user selects an item, the system calls the onMenuItemClick() callback in your interface.
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.editBoard){
                            Log.i(TAG, "댓글 수정 클릭");
                            Intent intent = new Intent(context, CommentUpdate.class);
                            intent.putExtra("commentInput", subItem.getCommentInput());
                            intent.putExtra("boardSeq", boardSeq);
                            intent.putExtra("commentSeq", commentSeq);
                            intent.putExtra("albumSeq", albumSeq);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);



                        } else if(item.getItemId() == R.id.deleteBoard){
                            Log.i(TAG, "댓글 삭제 클릭");

                            // 서버로 보낼 값 (onCreate 안에 선언)
                            HashMap hashmap = new HashMap<String, String>();
                            hashmap.put("commentSeq", commentSeq);
                            hashmap.put("userSeq", userSeq);
                            hashmap.put("boardSeq", boardSeq);
                            hashmap.put("albumSeq", albumSeq);

                            deleteComment(hashmap);
                            subItemList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, subItemList.size());
                            handler.handle(position);
                            //comment 고유값을 서버로 보내서, 해당 comment 행 삭제 요청


                        }
                        return false;
                    }
                });

                // 3. Call PopupMenu.show().
                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return subItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //viewHolder의 view 안에 글자가 표시되도록 textView 2개를 추가되도록 할 것이다

        private CheckBox cb_like;
        private ImageView iv_profileImage, iv_more;
        private TextView tv_name, tv_status, tv_date, tv_commentContent, tv_like, tv_comment, tv_likeCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_profileImage = itemView.findViewById(R.id.iv_profileImage); //
            iv_more = itemView.findViewById(R.id.iv_more); //
            cb_like = itemView.findViewById(R.id.cb_like); //
            tv_name = itemView.findViewById(R.id.tv_name); //
            tv_status = itemView.findViewById(R.id.tv_status); //
            tv_date = itemView.findViewById(R.id.tv_date); //
            tv_commentContent = itemView.findViewById(R.id.tv_commentContent); //
            tv_like = itemView.findViewById(R.id.tv_like); //
            tv_comment = itemView.findViewById(R.id.tv_comment); //
            tv_likeCount = itemView.findViewById(R.id.tv_likeCount); //
        }
    }


    // 게시글에 좋아요 클릭시, likeBoard 테이블에 행 추가(boardSeq, userSeq 넣기)
    private void deleteComment(HashMap<String, String> hashmap) {
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("commentSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("commentSeq")));
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        if(hashmap.get("boardSeq")!=null){
            textToServer.put("boardSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("boardSeq")));
        }
        if(hashmap.get("albumSeq")!=null){
            textToServer.put("albumSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("albumSeq")));
        }



        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<CommentData> call = uploadApis.deleteComment(textToServer);
        call.enqueue(new Callback<CommentData>() {
            @Override
            public void onResponse(Call<CommentData> call, retrofit2.Response<CommentData> response) {

                Log.i(TAG, "response = " + response);
                CommentData responseBody = response.body();
                Log.i(TAG, "responseBody = " + responseBody);
                String success = responseBody.getSuccess();
                String boardSeq = responseBody.getBoardSeq();

                if(success.equals("1")){
                    Log.i(TAG, "댓글 삭제에 성공했습니다");
                    Toast.makeText(context, "댓글을 삭제했습니다", Toast.LENGTH_SHORT).show();


//                    Intent intent = new Intent(context, BoardView.class);
//                    // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    intent.putExtra("boardSeq", boardSeq);
//                    intent.putExtra("scrollBottom", true);
//                    Log.i(TAG, "commentCreate 동작 후 boardSeq = " + boardSeq);
//                    Log.i(TAG, "commentCreate 동작 후 scrollBottom = " + true);
//                    context.startActivity(intent);
                }
            }
            @Override
            public void onFailure(Call<CommentData> call, Throwable t) {
            }
        });
    }



    // 게시글에 좋아요 클릭시, likeBoard 테이블에 행 추가(boardSeq, userSeq 넣기)
    private void likeCommentAdd(HashMap<String, String> hashmap) {
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        textToServer.put("boardSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("boardSeq")));
        textToServer.put("commentSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("commentSeq")));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<CommentData> call = uploadApis.likeCommentAdd(textToServer);
        call.enqueue(new Callback<CommentData>() {
            @Override
            public void onResponse(Call<CommentData> call, retrofit2.Response<CommentData> response) {

                Log.i(TAG, "response = " + response);
                CommentData responseBody = response.body();
                Log.i(TAG, "responseBody = " + responseBody);
                String success = responseBody.getSuccess();
                String boardSeq = responseBody.getBoardSeq();
                String likeCount = responseBody.getLikeCount();


                if(success.equals("1")){
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

    public interface EventHandler {
        void handle(int position);
    }

}


