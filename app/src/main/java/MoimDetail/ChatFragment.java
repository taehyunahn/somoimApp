package MoimDetail;

import static Common.StaticVariable.serverAddress;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.somoim.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import Chat.ChatAdapter;
import Chat.ChatData;
import Chat.ChatInterface;
import Chat.ChatService;
import Chat.ViewType;
import Common.DataClass;
import Common.NetworkClient;
import Common.RecyclerViewEmptySupport;
import Common.UploadApis;
import MoimDetail.Board.BoardData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {


    // 레트로핏 통신 공통
    NetworkClient networkClient;
    UploadApis uploadApis;

    ImageView iv_send;
    EditText et_message;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    String currentUserProfileImage, currentUserName;
    String joinedMoimSeqList;
    ConstraintLayout layout_chatInput;
    TextView list_empty;


    static String staticUserSeq;
    static String staticMoimSeq;


    List<ChatData> chatDataList = new ArrayList<>();
    static List<ChatData> chatDataList2 = new ArrayList<>();

    public static RecyclerViewEmptySupport chat_rv;
    public static RecyclerView.Adapter chat_adapter;
    public static RecyclerView.LayoutManager chat_layoutManager;

    public static Handler mHandler;
    private static final String TAG = "ChatFragment(채팅화면)";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, "(onCreateView()) 1. 호출");
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_chat, container, false);
        iv_send = rootView.findViewById(R.id.iv_send); //채팅 전송 버튼
        et_message = rootView.findViewById(R.id.et_message); // 채팅 입력칸
        chat_rv = rootView.findViewById(R.id.recyclerView); // 채팅 목록 리사이클러뷰
        layout_chatInput = rootView.findViewById(R.id.layout_chatInput);
        list_empty = rootView.findViewById(R.id.list_empty);

        // sharedPreference 세팅 (완료)
        sp = getContext().getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", ""); // 로그인한 계정의 고유값
        staticUserSeq = userSeq;
        String moimSeq = MoimDetailMain.moimSeq; // 현재 속한 모임의 고유값 = 채팅방 고유값과 동일한 역할
        staticMoimSeq = moimSeq;

        // onCreateView 할 때, 0으로 초기화
        ChatService.chatRoomSee = "0";


        Log.i(TAG, "(onCreateView()) 2. 전역 변수에 값 저장, staticUserSeq : " + staticUserSeq + " / staticMoimSeq : " + staticMoimSeq);

        Log.i(TAG, "(onCreateView()) 3. 모임 멤버 여부를 확인하는 memberCheck(userSeq, moimSeq) 메서드 호출");
        memberCheck(userSeq, moimSeq);
        //현재 로그인한 계정의 정보 불러와서 전역변수에 넣기 (userName, userProfileImage)
        Log.i(TAG, "(onCreateView()) 4. 현재 계정의 이름과 프로필 사진을 전역 변수에 담는 receiveProfileInfo(userSeq) 메서드 호출");
        receiveProfileInfo(userSeq);

        // 1. 리사이클러뷰 초기 세팅 (예정)
        chat_rv.setHasFixedSize(true);
        chat_layoutManager = new LinearLayoutManager(getContext());
        chat_rv.setLayoutManager(chat_layoutManager);

        // 2. 핸들러 세팅
        mHandler = new Handler();

        // notifyDataSetChanged할때 리사이클러뷰가 깜빡이는 애니메이션을 없애줌.
        RecyclerView.ItemAnimator animator = chat_rv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        // 3. 서버에서 기존 채팅 목록 요청

        // 4. 채팅방에 처음 입장했을 때,
        // --> 가입인사로 입력한 메세지를 첫번째 메세지로 보낸다. 또는 'ㅇㅇㅇ님께서 모임에 참여하셨습니다'라는 문구를 띄운다 (이 둘다 하는게 좋겠다)
        // --> 모임에 가입하면, 채팅방 Fragment에 들어와서 채팅방 처음 입장 프로세스를 진행한다

        // 채팅방에 처음 접속한 경우에만 동작하도록 한다.
        // ㅇㅇㅇ님께서 모임에 가입하셨습니다.
        Boolean moimJoined = sp.getBoolean("moimFirstJoined" + userSeq + moimSeq, false);
        Log.i(TAG, "(onCreateView()) 5. 모임에 처음 가입했는지 여부 확인 moimJoined : " + moimJoined);

        if (moimJoined) {
            Log.i(TAG, "(onCreateView()) 5. 채팅방에 처음 들어온 경우 → editor.remove(\"moimFirstJoined\" + userSeq + moimSeq)");
            editor.remove("moimFirstJoined" + userSeq + moimSeq);
            editor.commit();
        } else {
            Log.i(TAG, "(onCreateView()) 5. 채팅방에 처음 들어온게 아닌 경우 (별다른 동작 없음).");
        }


        // 5. 채팅방에 다시 입장해서 메세지를 보낼 때,
        // --> 모임 고유값 = 채팅방 고유값, 같은 채팅방에 있는 사람들에게 메세지를 보내라는 명령을 한다.

        // 채팅 메세지 작성 후 보내기 버튼
        iv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "(메세지 전송 클릭)");
                String msg = et_message.getText().toString();
                if (msg.equals("")) {
                    Toast.makeText(getContext().getApplicationContext(), "메세지를 입력하세요", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "(메세지 전송 클릭) 1. MsgToServer 스레드 동작 (서비스를 통해 서버에 메세지 전송)");
                    MsgToServer msgToServer = new MsgToServer(ChatService.member_socket);
                    msgToServer.start();

                    Log.i(TAG, "(메세지 전송 클릭) 2. 나의 화면에 보여줄 채팅메세지 세팅 후 띄워줌");
                    ChatData showMyself = new ChatData();
                    showMyself.setMsg(msg);
                    showMyself.setTime(getTime());
                    showMyself.setViewType(ViewType.RIGHT_CHAT);

                    chatDataList2.add(showMyself);
                    chat_adapter.notifyDataSetChanged();
                    chat_rv.scrollToPosition(chatDataList2.size() - 1);
                }
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }


    //MsgToServer msgToServer = new MsgToServer(ChatService.member_socket);


    // 메세지를 보낼 때 동작하는 스레드
    class MsgToServer extends Thread {
        Socket socket;

        public MsgToServer(Socket socket) {
            try {
                this.socket = socket;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                Log.i(TAG, "(MsgToServer) 1. 스레드 시작 - ChatService.chatData 객체를 채팅 서버로 보내느 스레드");

                sp = getContext().getSharedPreferences("login", Activity.MODE_PRIVATE);
                String userSeq = sp.getString("userSeq", ""); // 로그인한 계정의 고유값
                String msg = et_message.getText().toString(); // 입력한 메세지
                String moimSeq = MoimDetailMain.moimSeq; // 현재 속한 모임의 고유값 = 채팅방 고유값과 동일한 역할

                Log.i(TAG, "(MsgToServer) 2. 채팅서버로 보내는 값, userSeq : " + userSeq + " / currentUserProfileImage : " +  currentUserProfileImage + " / currentUserName : " + currentUserName + " / getTime() : " + getTime() + " / msg : " + msg + " / moimSeq : " + moimSeq + " / joinedMoimSeqList :" + joinedMoimSeqList);

                Log.i(TAG, "(MsgToServer) 3. ChatService.chatData 객체에 위 데이터 저장");
                ChatService.chatData.setCommand("msg"); // 전송
                ChatService.chatData.setUserSeq(userSeq); // 로그인한 계정의 고유값
                ChatService.chatData.setUserProfileImage(currentUserProfileImage); // 로그인한 계정의 프로필 사진
                ChatService.chatData.setUserName(currentUserName); // 로그인한 계정의 이름
                ChatService.chatData.setTime(getTime()); // 현재시각
                ChatService.chatData.setMsg(msg); // 작성한 채팅 메세지
                ChatService.chatData.setMoimSeq(moimSeq); // 현재 속한 모임의 고유값
                ChatService.chatData.setJoinedMoimSeqList(joinedMoimSeqList);
//                ChatService.chatData.setChatRoomSeq(moimSeq); // 임시로 넣어둠. 수정 필요

                Gson gson = new Gson();
                String sendingObject = gson.toJson(ChatService.chatData);
                Log.i(TAG, "(MsgToServer) 4. 서버로 보내는 값(Gson 사용) : " + sendingObject);

                // 서비스 클래스에 있는 PrintWriter 객체를 사용해서, 방금 전에 입력한 값을 담는다.
                ChatService.sendWriter.println(sendingObject);
                ChatService.sendWriter.flush();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        et_message.setText("");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 현재시간을 원하는 형식으로 변경하는 메서드
    private String getTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
        String getTime = dateFormat.format(date);
        String a = getTime.substring(20, 22);
        String b = getTime.substring(11, 16);
        String finalTime = a + " " + b;

        return finalTime;
    }



    // HTTP통신 매서드 1. 현재 계정이 참여중인 모임 고유값 목록 요청
    // (통신방식) volley
    // (클라이언트→서버) userSeq
    // (서버→클라이언트) MoimData 객체의 값(leaderSeq, address, title, mainImage, content, memberCount)

    private void receiveProfileInfo(String userSeq){ // 프로필 사진, 이름, 소개, 참여중이 모임방 목록 필요

        Log.i(TAG, "(receiveProfileInfo) 1. 호출 - 현재 계정이 참여중인 모임 고유값 목록을 수신");
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = serverAddress + "somoim/login/joinedMoimList.php";
        Log.i(TAG, "(receiveProfileInfo) 2. HTTP 통신 URL : "+ url);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "(receiveProfileInfo) 4. HTTP 통신 수신하는 값, response : "+ response);

                StringBuilder sb = new StringBuilder();
                try {

                    JSONObject jsonObject = new JSONObject(response);

                    Gson gson = new Gson();
                    String object = gson.toJson(jsonObject);
                    Log.i(TAG, "(receiveProfileInfo) 5. 서버로부터 받은 값, object : " + object);

                    String joinedMoimSeqListFromServer = jsonObject.getString("joinedMoimSeqList");
                    String userName = jsonObject.getString("userName");
                    String userProfileImage = jsonObject.getString("userProfileImage");
                    String userIntro = jsonObject.getString("userIntro");


                    joinedMoimSeqList = joinedMoimSeqListFromServer;
                    Log.i(TAG, "(receiveProfileInfo) 6. 전역변수에 값 저장, joinedMoimSeqListFromServer : " + joinedMoimSeqListFromServer);
                    currentUserProfileImage = userProfileImage;
                    Log.i(TAG, "(receiveProfileInfo) 6. 전역변수에 값 저장, userProfileImage : " + userProfileImage);
                    currentUserName = userName;
                    Log.i(TAG, "(receiveProfileInfo) 6. 전역변수에 값 저장, userName : " + userName);


                    editor.putString("userName", userName); // 어디서 사용되는거지? 추후 확인 필요
                    editor.putString("joinedMoimSeqList", joinedMoimSeqList);
                    editor.commit();
                    Log.i(TAG, "(receiveProfileInfo) 7. s.p(login)에 값 저장, userName : " + sp.getString("userName", ""));
                    Log.i(TAG, "(receiveProfileInfo) 7. s.p(login)에 값 저장, joinedMoimSeqList : " + sp.getString("joinedMoimSeqList", ""));


//                    // 채팅 소켓 서비스 가동 시키는 타이밍
//                    Log.i(TAG, "-----(getJoinedMoimSeqList) 10. Chat.ChatService 동작(서비스)");
//                    Log.i(TAG, "-----(getJoinedMoimSeqList) 11. intentService.putExtra() key : userSeq / value : " + userSeq);
//                    Intent intentService = new Intent(getContext(), Chat.ChatService.class);
//                    intentService.putExtra("userSeq", userSeq);
//                    getContext().startService(intentService);


                    // moimSeq를 넣어준다
                    ChatService.chatRoomSee = staticMoimSeq;
////                    // 채팅방 화면에 들어왔다는 신호
//                    Log.i(TAG, "(receiveProfileInfo) 8. chatRoomSeeThread 객체 생성 후 start");
//                    ChatRoomSeeThread chatRoomSeeThread = new ChatRoomSeeThread(ChatService.member_socket);
//                    chatRoomSeeThread.start();




                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "(receiveProfileInfo) JSONException 발생하여 데이터 수신 못함");
                }
            }
        }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // textView.setText("That didn't work!");
                Log.i(TAG, "(receiveProfileInfo) VolleyError 발생하여 데이터 수신 못함");
                Log.e("error : ", String.valueOf(error));
            }
        }) {
            // 포스트 파라미터 넣기
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<String, String>();
                Log.i(TAG, "(receiveProfileInfo) 3. HTTP 통신 전송하는 값, userSeq : "+ userSeq);
                params.put("userSeq", userSeq);
                return params;
            }

        };
        queue.add(stringRequest);
    }



//혹시 문제 생기면 복원하려고, 보관(2022.09.22. 11:21)
    // HTTP통신 매서드 0. 기록된 채팅 목록을 불러오는 매서드 - Fragment 시작할 때 동작
    // (통신방식) Retrofit
    // (클라이언트→서버) moimSeq, userSeq
    // (서버→클라이언트) ChatData 객체의 값(date, msg, userName, userProfileImage, moimSeq, viewType, unreadCount)
    private void showChatMsgList(String moimSeq, String userSeq) {
        Log.i(TAG, "(showChatMsgList) 1. 메서드 호출 - 지난 채팅 내역 불러오기(HTTP 통신)");
        // 이전 채팅 메세지 기록을 불러오는 통신 -- 시작
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        // @POST("chat/getChattingMsgList.php")
        Call<List<ChatData>> call = uploadApis.showChatMsgList(moimSeq, userSeq);
        Log.i(TAG, "(showChatMsgList) 2. 서버로 보내는 값, moimSeq : " + moimSeq + " / userSeq : " + userSeq);
        call.enqueue(new Callback<List<ChatData>>() {
            @Override
            public void onResponse(@NonNull Call<List<ChatData>> call, @NonNull Response<List<ChatData>> response) {

                Log.i(TAG, "(showChatMsgList) 3. HTTP통신 서버에서 받은 값, response.body().toString() : " + response.body().toString());
                if(response.isSuccessful() && response.body() != null){

                    chatDataList2 = new ArrayList<>(); // 어댑터에 담아줄 메세지 객체 리스트 선언

                    for (int i = 0; i < response.body().size(); i++) {
                        ChatData chatData = new ChatData();
                        chatData = response.body().get(i);

                        Gson gson = new Gson();
                        String object = gson.toJson(chatData);
//                        Log.i(TAG, "(showChatMsgList) 3. HTTP통신 서버에서 받은 객체, object(chatData) : " + object);

                        String now_time = chatData.getDate(); // 현재 메세지 객체의 시간
                        String now_year = now_time.substring(0, 4); // 연도
                        String now_month = now_time.substring(5, 7); // 월
                        String now_date = now_time.substring(8, 10); // 날짜
                        String showDate = now_year + "년 " + now_month + "월 " + now_date + "일 ";
                        String finalTime = "";
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        try {
                            Date date = formatter.parse(now_time);
                            SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
                            String date2 = formatter2.format(date);
                            String a = date2.substring(20, 22);
                            String b = date2.substring(11, 16);
                            finalTime = a + " " + b;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        // 첫 메세지가 아니라면
                        if (i != 0) {
                            String before_time = response.body().get(i - 1).getDate(); // 이전 메세지 객체의 시간
                            String before_date = before_time.substring(8, 10);
                            // 이전 메세지 객체의 시간과 일자가 다르다면
                            if (!now_date.equals(before_date)) {
                                ChatData chatDataForTime = new ChatData(); // 메세지 객체 리스트에 담을 메세지 객체 선언
                                chatDataForTime.setTime(showDate); // 현제 메세지 객체의 년,월,일
                                chatDataList2.add(chatDataForTime); // 메세지 객체 리스트에 담아줌
                            }
                        }
//                        // 첫 메세지 이전에 년,월,일 넣어줌
//                        else if (response.body().size() < 40) { // 페이징할때 같은 날짜가 뜨는것을 방지함
//                            ChatData chatDataForTime = new ChatData(); // 메세지 객체 리스트에 담을 메세지 객체 선언
//                            chatDataForTime.setTime(showDate); // 현제 메세지 객체의 년,월,일
//                            chatDataList2.add(0, chatDataForTime); // 메세지 객체 리스트에 담아줌
//                        }

                        // 첫 메세지 이전에 년,월,일 넣어줌
                        else if (response.body().size() < 40) { // 페이징할때 같은 날짜가 뜨는것을 방지함
                            ChatData chatDataForTime = new ChatData(); // 메세지 객체 리스트에 담을 메세지 객체 선언
                            chatDataForTime.setTime(showDate); // 현제 메세지 객체의 년,월,일
                            chatDataList2.add(0, chatDataForTime); // 메세지 객체 리스트에 담아줌
                        }

                        ChatData chatMessage = new ChatData();
                        chatMessage.setMsg(chatData.getMsg());
                        chatMessage.setUserName(chatData.getUserName());
                        chatMessage.setUserProfileImage(chatData.getUserProfileImage());
                        chatMessage.setTime(finalTime); // 시간 변환해서 넣기
                        chatMessage.setMoimSeq(chatData.getMoimSeq());
                        chatMessage.setViewType(chatData.getViewType());
                        chatMessage.setUnreadCount(chatData.getUnreadCount());


                        if (chatData.getUserSeq().equals(userSeq)) { // 채팅 작성자와 현재 로그인 정보의 고유값이 동일하다면

//                        if(response.body().get(i).getUnread() != null){
//                            chatMessage.setViewType(3); // 메세지를 보낸사람 == 현재 로그인한 사람 → 오른쪽 위치, 안읽음 메세지를 기본으로 함.
//                        } else {

                            if (chatMessage.getViewType() == 4) {
                                chatMessage.setViewType(4);
                            } else {
                                chatMessage.setViewType(2);
                            }
//                        }
                        } else {
                            if (chatMessage.getViewType() == 4) {
                                chatMessage.setViewType(4);
                            } else {
                                chatMessage.setViewType(1); // 메세지를 보낸사람 != 현재 로그인한 사람 → 왼쪽 위치를 기본으로 함.
                            }

                        }
                        chatDataList2.add(chatMessage);
                    }


//                LinearLayoutManager manager = new LinearLayoutManager(getContext().getApplicationContext(), LinearLayoutManager.VERTICAL,false);
//                chat_ryc.setLayoutManager(manager); // LayoutManager 등록
                    chat_adapter = new ChatAdapter(getContext().getApplicationContext(), chatDataList2); // 서버에서 받아온 메세지 목록을 리사이클러뷰에 담는다.
                    chat_rv.setAdapter(chat_adapter);
                    chat_rv.scrollToPosition(chatDataList2.size() - 1);
                }

            }

            // 통신 실패시(예외발생, 인터넷끊김 등의 이유)
            @Override
            public void onFailure(@NonNull Call<List<ChatData>> call, @NonNull Throwable t) {
                Log.i(TAG, "(showChatMsgList)  onFailure = " + t.getLocalizedMessage());
                Log.i(TAG, "(showChatMsgList)  onFailure = " + t);
            }
        });
    }


    // 서버에서 메세지 수신하여, 채팅방에 메세지 표시
    public static class GetMsg implements Runnable {
        private final ChatData chatData;

        public GetMsg(ChatData chatData) {
            this.chatData = chatData;
        }

        @Override
        public void run() {


            Log.i(TAG, "(GetMsg) 1. 동작");
            Log.i(TAG, "(GetMsg) 2. chatData.getViewType() = " + chatData.getViewType());

            // 현재 화면을 보고 있는 상황이라면, DB의 chatMsg 테이블, unreadCount 값을 -1 한다
            // HTTP 통신이 끝난 이후에 아래 코드가 진행되도록 한다.

            if (!chatData.getUserSeq().equals(staticUserSeq)) {
                chatData.setViewType(1);
                if (chatData.getCommand().equals("JOIN") || chatData.getCommand().equals("moimLeft")) {
                    chatData.setViewType(4);
                }
                chatDataList2.add(chatData);
                chat_adapter.notifyDataSetChanged();
                chat_rv.scrollToPosition(chatDataList2.size() - 1);
            } else {
                Log.i(TAG, "(GetMsg) 5. ");
                chatData.setViewType(2);
                chatDataList2.add(chatData);
                chat_adapter.notifyDataSetChanged();
                chat_rv.scrollToPosition(chatDataList2.size() - 1);
            }

            Log.i(TAG, "GetMsg는 동작한다");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "Fragment 생명주기 onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "(onResume) 1. 호출");
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("login", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String userSeq = sp.getString("userSeq", "");
        Log.i(TAG, "(onResume) 2. s.p(login)에서 꺼낸 값, userSeq : " + userSeq);

        editor.putString(staticUserSeq + "currentMoimSeq", staticMoimSeq);
        editor.commit();

        receiveProfileInfo(userSeq);

        Log.i(TAG, "(onResume) 3. staticUserSeq : " + staticUserSeq);
        Log.i(TAG, "(onResume) 4. s.p(login) 저장, staticUserSeq + \"currentMoimSeq\"(채팅의 모임방번호) : " + sp.getString(staticUserSeq + "currentMoimSeq",""));

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "Fragment 생명주기 onPause");
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("login", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(staticUserSeq + "currentMoimSeq");
        editor.commit();
        Log.i(TAG, sharedPreferences.getString(staticUserSeq + "currentMoimSeq", "저장된 값이 없습니다"));

//        // 채팅방 화면 안보기 시작했다는 스레드
//        Log.i(TAG, "chatRoomNoSeeThread 객체 생성 후 start");
//        ChatRoomNoSeeThread chatRoomNoSeeThread = new ChatRoomNoSeeThread(ChatService.member_socket);
//        chatRoomNoSeeThread.start();

        ChatService.chatRoomSee = "0";
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "Fragment 생명주기 onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Fragment 생명주기 onDestroy");


    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "Fragment 생명주기 onDetach");

    }

    // 현재 계정이 현재 방에 가입되어있는지 여부 확인
    private void memberCheck(String userSeq, String moimSeq) {
        Log.i(TAG, "(memberCheck) 1. 메서드 호출 - 현재 계정이 모임의 멤버인지 확인");

        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = serverAddress + "somoim/member/memberCheck.php";
        Log.i(TAG, "(memberCheck) 2. HTTP 통신 URL : " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "(memberCheck) 4. response : " + response);

                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String memberCheck = jsonResponse.getString("memberCheck");
                    Log.i(TAG, "(memberCheck) 5. memberCheck(0이면 멤버 아님) : " + memberCheck);

                    if (memberCheck.equals("0")) {
                        Log.i(TAG, "(memberCheck) 5-1. 멤버가 아닌 경우, 리사이클러뷰 empty 세팅과 채팅 입력창 없앰");
                        // 가입하지 않았다면,
                        // empty 띄워줌
                        chat_rv.setEmptyView(list_empty);
                        layout_chatInput.setVisibility(View.INVISIBLE);

                    } else {
                        Log.i(TAG, "(memberCheck) 5-1. 멤버인 경우, 지난 채팅 데이터 불러오는 showChatMsgList(HTTP 통신) 메서드 호출, 채팅 입력창 보여줌");
                        showChatMsgList(moimSeq, userSeq);
                        list_empty.setVisibility(View.INVISIBLE);
                    }

                    String leaderSeq = jsonResponse.getString("leaderSeq");
                    Log.i(TAG, "(memberCheck) 6. leaderSeq(모임 리더의 고유값) : " + leaderSeq);


                } catch (JSONException e) {
                    Log.i(TAG, "(memberCheck) JSONException : " + e);
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // textView.setText("That didn't work!");
                Log.e("(memberCheck) error : ", String.valueOf(error));
            }
        }) {
            // 포스트 파라미터 넣기
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userSeq", userSeq);
                params.put("moimSeq", moimSeq);
                Log.i(TAG, "(memberCheck) 3. 서버로 전송하는 값, userSeq : " + userSeq + " / moimSeq : " + moimSeq);

                return params;
            }

        };
        queue.add(stringRequest);
    }


    // 채팅방 화면을 보고 있다라는 것을 서버에게 알려주는 스레드
    class ChatRoomSeeThread extends Thread {
        Socket socket;
        public ChatRoomSeeThread(Socket socket) {
            try {
                this.socket = socket;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            Log.i(TAG, "(ChatRoomSeeThread) 1. run - 채팅방 화면을 보고 있다라는 것을 서버에게 알려주는 스레드");
            try {

                ChatService.chatRoomSee = "1";

                Log.i(TAG, "(ChatRoomSeeThread) 1. ChatService.chatRoomSee : " + ChatService.chatRoomSee);

                SharedPreferences sp;
                SharedPreferences.Editor editor;

                sp = getContext().getSharedPreferences("login", Activity.MODE_PRIVATE);
                editor = sp.edit();
                String staticUserSeq = sp.getString("userSeq", "");

                Log.i(TAG, "(ChatRoomSeeThread) 2. 서버로 보낼 객체의 값을 set한다");
                ChatService.chatData = new ChatData();
                ChatService.chatData.setCommand("chatRoomSee"); // 채팅방 접속
                ChatService.chatData.setUserSeq(staticUserSeq); // 로그인한 계정의 고유값
                ChatService.chatData.setMoimSeq(MoimDetailMain.moimSeq);
                ChatService.chatData.setJoinedMoimSeqList(joinedMoimSeqList);
                ChatService.chatData.setChatRoomSee(staticMoimSeq); // 현재 보고있는 모임방 번호를 넣는다.

                Gson gson = new Gson();
                String sendingObject = gson.toJson(ChatService.chatData);
                Log.i(TAG, "(ChatRoomSeeThread) 3. 서버로 보내는 값, " +  sendingObject);
                ChatService.sendWriter.println(sendingObject);
                ChatService.sendWriter.flush();

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }


    // 채팅방 화면을 보고 있다라는 것을 서버에게 알려주는 스레드
    class ChatRoomNoSeeThread extends Thread {
        Socket socket;
        public ChatRoomNoSeeThread(Socket socket) {
            try {
                this.socket = socket;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            Log.i(TAG, "(ChatRoomNoSeeThread) 1. run - 채팅방 화면을 보고 있지 않다는 것을 알려주는 스레드");
            try {
                ChatService.chatRoomSee = "0";
                Log.i(TAG, "(ChatRoomNoSeeThread) 1. ChatService.chatRoomSee : " + ChatService.chatRoomSee);

                SharedPreferences sp;
                SharedPreferences.Editor editor;

                sp = getContext().getSharedPreferences("login", Activity.MODE_PRIVATE);
                editor = sp.edit();
                String staticUserSeq = sp.getString("userSeq", "");

                Log.i(TAG, "(ChatRoomNoSeeThread) 2. 서버로 보낼 객체의 값을 set한다");
                ChatService.chatData = new ChatData();
                ChatService.chatData.setCommand("chatRoomNoSee"); // 채팅방 접속
                ChatService.chatData.setUserSeq(staticUserSeq); // 로그인한 계정의 고유값
                ChatService.chatData.setMoimSeq(MoimDetailMain.moimSeq);
                ChatService.chatData.setJoinedMoimSeqList(joinedMoimSeqList);
                ChatService.chatData.setChatRoomSee("0");

                Gson gson = new Gson();
                String sendingObject = gson.toJson(ChatService.chatData);
                Log.i(TAG, "(ChatRoomNoSeeThread) 3. 서버로 보내는 값, " +  sendingObject);
                ChatService.sendWriter.println(sendingObject);
                ChatService.sendWriter.flush();

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }



}