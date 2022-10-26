package Chat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import MoimDetail.Board.BoardView;
import MoimDetail.ChatFragment;
import MoimDetail.MoimDetailMain;
import MoimDetail.Photo.AlbumView;


public class ChatService extends Service {
    private static final String TAG = "ChatService";
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    public static final String NOTIFICATION_CHANNEL_ID2 = "10002";

    SharedPreferences already_read_chatroom_seq;
    SharedPreferences.Editor editor;

    public static String NOW_CHAT = ""; // notification 띄울지 여부

    public static Socket member_socket; // 서버와 연결되어있는 소켓 객체
    public static PrintWriter sendWriter;

    public static ChatData chatData = new ChatData(); // 소켓을 통해 서버로 넘길 객체

    int page = 1;


    String chat_room_seq;
    String userName, userSeq, expertSeq, expertName;
    BufferedReader member_bufferedReader;
    PrintWriter member_printWriter;
    Handler handlerHere = new Handler();
    public static String chatRoomSee = "0";

    public ChatService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "(onCreate()) 호출됨 - 별도 동작 없음");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "(onStartCommand()) 호출됨");
        Log.i(TAG, "(onStartCommand()) 1. new Thread() 생성 후 바로 동작");
        // 클라이언트 측 소켓 생성 스레드 만들고, 바로 start
        new Thread() {
            public void run() {
                int port = 5003;

                try {
                    // 로그인과 동시에 '소켓 생성'
                    Socket socket = new Socket("192.168.211.1", port); // 안드로이드를 동작시키는 컴퓨터의 IP 주소
                    Log.i(TAG, "(onStartCommand()) 2. 클라이언트 측 소켓생성, socket : " + socket + " / port : " + port);
                    member_socket = socket;
                    Log.i(TAG, "(onStartCommand()) 3. 생성된 소켓 정보를 전역변수(member_socekt)에 담는다. member_socket : " + member_socket);
                    // 읽어주는 역할
                    member_bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    Log.i(TAG, "(onStartCommand()) 4. 데이터를 읽어주는 buffedReader 객체 생성(소켓 필요). member_bufferedReader : " + member_bufferedReader);
                    // 보내는 역할
                    member_printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                    Log.i(TAG, "(onStartCommand()) 5. 데이터를 보내주는 printWriter 객체 생성(소켓 필요). member_printWriter : " + member_printWriter);
                    // 소켓연결 스레드

                    Log.i(TAG, "(onStartCommand()) 6. ConnectionThread 객체 생성 후 실행 → 처음 소켓 연결되면 자동으로 동작");
                    ConnectionThread thread = new ConnectionThread(member_socket, member_bufferedReader, member_printWriter);
                    thread.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();


        if (intent != null) {
            userSeq = intent.getStringExtra("userSeq");
            Log.i(TAG, "(onStartCommand()) 7. intent가 null이 아니라면, intent로 수신하는 값 intent.getStringExtra(\"userSeq\") : " + userSeq);
            Log.i(TAG, "(onStartCommand()) 8. intent가 null이 아니라면, intent로 수신한 값을 전역변수에 담는다. userSeq : " + userSeq);
            userName = intent.getStringExtra("userName");
//            expertName = intent.getStringExtra("expertName");
//            Log.i(TAG, "userName = " + userName);

        } else {
            return Service.START_STICKY; //자동으로 재시작하라는 명령
        }

        return super.onStartCommand(intent, flags, startId);
//        chat_room_seq = intent.getExtras().getString("chat_room_seq"); // 현재 채팅방의 seq 번호
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "(onDestroy()) 호출됨");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "(onBind()) 호출됨");

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    // 클라이언트 소켓과 서버 소켓을 연결시키는 스레드
    class ConnectionThread extends Thread {

        Socket socket; // 클라이언트 소켓
        BufferedReader bufferedReader; //위 소켓과 연결된 bufferedReader
        PrintWriter printWriter; //위 소켓과 연결된 printWriter

        public ConnectionThread(Socket socket, BufferedReader bufferedReader, PrintWriter printWriter) throws IOException {
            this.socket = socket;
            this.bufferedReader = bufferedReader;
            this.printWriter = printWriter;
        }

        @Override
        public void run() {
            Log.i(TAG, "(ConnectionThread) 1. 시작 (최초로 클라이언트와 소켓을 연결할 때만 동작)");
            SharedPreferences sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
            String userSeq = sp.getString("userSeq", "");
            String userName = sp.getString("userName", "");
            String joinedMoimSeqList = sp.getString("joinedMoimSeqList", "");
            Log.i(TAG, "(ConnectionThread) 2. sharedPreference(login)로 사용할 값을 변수에 담는다. (1)userSeq : " + userSeq + " / (2)userName : " + userName + " / (3)joinedMoimSeqList : " + joinedMoimSeqList);

            try {
                Log.i(TAG, "(ConnectionThread) 3. 채팅서버로 전송할 값을 chatData 객체에 담는다 → String 값으로 변환(Gson 사용)");
                chatData.setCommand("connect"); // 전송 -> 아직 전송이 시작되면 안됨
                chatData.setUserName(userName); // 연결된 계정의 이름
                chatData.setUserSeq(userSeq); // 연결된 계정의 고유값
                chatData.setJoinedMoimSeqList(joinedMoimSeqList); // 계정이 참여중인 채팅방 목록

                Gson gson = new Gson();
                String userJson = gson.toJson(chatData); // 소켓 통신 연결된 즉시 서버로 보내는 값 (유저의 정보를 담는다)
                Log.i(TAG, "(ConnectionThread) 4. 채팅서버로 전송하는 값 : " + userJson);

                // 1. (최초 1회) 서버에 데이터 전송 : 1) userName, 2) userSeq
                printWriter.println(userJson);
                printWriter.flush();
                Log.i(TAG, "(ConnectionThread) 5. printWriter.println을 사용하여 채팅서버로 전송함");

                Log.i(TAG, "(ConnectionThread의 내부 Thread) 0. 생성 - 서버로부터 데이터 수신하는 용도");
                // 서버로부터 데이터 수신
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "(ConnectionThread의 내부 Thread) 0. 동작");
                        String msgFromServer;
//                        while (socket.isConnected()) {
                        while (!socket.isClosed()) {
                            Log.i(TAG, "(ConnectionThread의 내부 Thread) 0. 소켓이 닫히지 않는다면(!socket.isClosed()) 계속 반복(while)하여 실행되는 구간");

                            try {
                                // 채팅메세지를 받으면 동작하는 부분
                                msgFromServer = bufferedReader.readLine();
                                Log.i(TAG, "(ConnectionThread의 내부 Thread) 1. 채팅서버로부터 메세지 수신 : " + msgFromServer);

                                Gson gson = new Gson();
                                ChatData receivedObject = gson.fromJson(msgFromServer, ChatData.class);

                                String userSeqFromS = receivedObject.getUserSeq();
                                String moimSeqFromS = receivedObject.getMoimSeq();
                                String msgFromS = receivedObject.getMsg();
                                String userNameFromS = receivedObject.getUserName();
                                String commendFromS = receivedObject.getCommand();
                                String boardSeqFromS = receivedObject.getBoardSeq();
                                String albumSeqFromS = receivedObject.getAlbumSeq();

                                Log.i(TAG, "(ConnectionThread의 내부 Thread) 1. (발신자) userSeq : " + userSeqFromS + " / moimSeq : " + moimSeqFromS);


                                try {

                                    // 댓글 작성했다는 알림
                                    if(commendFromS.equals("comment")){
                                        notiCommentBoard(userNameFromS, msgFromS, boardSeqFromS);
                                    } else if(commendFromS.equals("commentAlbum")){
                                        notiCommentAlbum(userNameFromS, msgFromS, albumSeqFromS);

                                    } else if(commendFromS.equals("like")){
                                        notiLikeBoard(userNameFromS, boardSeqFromS);

                                    } else if(commendFromS.equals("likeAlbum")){
                                        notiLikeAlbum(userNameFromS, albumSeqFromS);
                                    }

                                    else {

                                        // 일반적인 채팅메세지 수신시
                                        // 여기 코드가 잘 이해가 안된다. 재정의 필요함.
                                        Boolean moimAlreadyJoined = sp.getBoolean("moimAlreadyJoined" + userSeq + moimSeqFromS, false);

                                        // 수신자가 발신자의 채팅방 번호와 동일한 채팅방 화면을 보고 있으면, 채팅방에 메세지 추가하는 스레드 동작
                                        if(chatRoomSee.equals(moimSeqFromS)){
                                            // 클라이언트가 서버에서 채팅 목록 데이터를 받으면, 그 내용으로 현재의 채팅 목록을 갱싱하라는 스레드
                                            ChatFragment.GetMsg chatFragment = new ChatFragment.GetMsg(receivedObject); //ChatFragment 안에 있는 GetMsg extends Thread 객체(static) 생성
                                            if (ChatFragment.mHandler != null) {
                                                ChatFragment.mHandler.post(chatFragment); //ChatFragment 안에 있는 핸들러(static)를 사용하여, GetMsg extends Thread 객체 동작
                                            } else {
                                            }
                                        }

                                        Log.i(TAG, "chatRoomSee : " + chatRoomSee );
                                        Log.i(TAG, "moimSeqFromS : " + moimSeqFromS );

                                        // 수신자가 발신자의 채팅방 번호와 다른 화면을 보고 있으면, 알림 메세지를 보내라
                                        // 채팅서버에서 메세지 대상자에게만 발송했기 때문에.... 이미 수신자는 선별됨.
                                        if (!chatRoomSee.equals(moimSeqFromS)){
                                            notiChatShow(userNameFromS, msgFromS, moimSeqFromS);
                                        } else {
                                        }

                                    }

                                } catch (NullPointerException e) {

                                }


                            } catch (IOException e) {
                                closeEverything(bufferedReader, printWriter, socket);
                            }
                        } stopSelf();
                    }
                }).start();

//
//                // 이 역할을 시작하는 시점을 변경해야겠다. -> 채팅방에 접속하는 시점에!
//                // 채팅방에 들어갈 때, 이 역할을 주면 어떨까?
//
//
                // sendMessage() 역할  ---> 서버로 데이터를 보내야 한다, 보내고 멈춰야 한다.
                while (true) { // 소켓 서버에서 전달해주는 객체를 계속 받음
                    sendWriter = new PrintWriter(socket.getOutputStream(), true);
                    break;
                }
            } catch (Exception e) {
                closeEverything(bufferedReader, printWriter, socket);
            }
        }
    }

    // 예외처리를 위한 함수
    public void closeEverything(BufferedReader bufferedReader, PrintWriter printWriter, Socket socket) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (printWriter != null) {
                printWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    // 채팅메세지 노티피케이션
    public void notiChatShow(String name, String msg, String moimSeq) {
        Log.i(TAG, "(notiChatShow) 1. sharedPreference(login)에서 꺼낼 값, userSeq : " + userSeq);
        // sharedPreference 세팅
        SharedPreferences sp;
        SharedPreferences.Editor editor;

        sp = getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");

        editor.putBoolean("moveToChat" + userSeq, true);
        editor.putString("moimSeqFromNoti", moimSeq);
        editor.commit();

        Log.i(TAG, "(notiChatShow) 2. sharedPreference(login)에서 넣을 값(1), editor.putBoolean(\"moveToChat\" + userSeq, true) : " + sp.getBoolean("moveToChat" + userSeq, false));
        Log.i(TAG, "(notiChatShow) 3. sharedPreference(login)에서 넣을 값(2), editor.putString(\"moimSeqFromNoti\") : " + sp.getString("moimSeqFromNoti", "") + " (들어가야할 값 : " + moimSeq + ")");

        Log.i(TAG, "(notiChatShow) 4. NotificationManager 객체(manager) 생성");
        // 채널을 생성 및 전달해 줄수 있는 NotificationManager를 생성한다.
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // 이동하려는 액티비티를 작성해준다.
        Log.i(TAG, "(notiChatShow) 5. Intent 객체 생성 후 명세");
        Intent notiIntent = new Intent(this, MoimDetailMain.class);

        notiIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        notiIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notiIntent.putExtra("moimSeq", moimSeq);
        notiIntent.setAction("showmessage");
        Log.i(TAG, "(notiChatShow) 6. PendingIntent 객체 생성 서버로 전송하는 값 (특정 시점에 동작하는 intent)");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notiIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Log.i(TAG, "(notiChatShow) 7. NotificationCompat.Builder 객체(builder) 생성 -> 노티피케이션의 정보 세팅");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle(name)
//                .setContentText("상태바 드래그시 보이는 서브타이틀 " + count)
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true); // 눌러야 꺼지는 설정

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "(notiChatShow) 8. OREO API 26 이상인 경우, 채널 필요");
            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName = "노티페케이션 채널";
            String description = "오레오 이상";
            int importance = NotificationManager.IMPORTANCE_HIGH;// 우선순위 설정

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert manager != null;
            manager.createNotificationChannel(channel);

        } else
            builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert manager != null;
//        notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작
        Log.i(TAG, "(notiChatShow) 9. NotificationManager 객체 사용하여 notify하여 알림 띄움");
        manager.notify((int) System.currentTimeMillis(), builder.build()); // 고유숫자로 노티피케이션 동작
    }

    // 게시글에 댓글 알림
    // 알림 클릭시 해당 댓글로 이동
    public void notiCommentBoard(String name, String msg, String boardSeq) {

        // 채널을 생성 및 전달해 줄수 있는 NotificationManager를 생성한다.
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, BoardView.class); // 이동하려는 액티비티를 작성해준다.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        notiIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("boardSeq", boardSeq);
        intent.setAction("showComment");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle(name)
//                .setContentText("상태바 드래그시 보이는 서브타이틀 " + count)
                .setContentText("게시글 댓글 : " + msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true); // 눌러야 꺼지는 설정

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "(notiCommentBoard) 8. OREO API 26 이상인 경우, 채널 필요");
            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName = "노티페케이션 채널";
            String description = "오레오 이상";
            int importance = NotificationManager.IMPORTANCE_HIGH;// 우선순위 설정

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert manager != null;
            manager.createNotificationChannel(channel);

        } else
            builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert manager != null;
//        notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작
        Log.i(TAG, "(notiCommentBoard) 9. NotificationManager 객체 사용하여 notify하여 알림 띄움");
        manager.notify((int) System.currentTimeMillis(), builder.build()); // 고유숫자로 노티피케이션 동작
    }

    // 게시글에 댓글 알림
    // 알림 클릭시 해당 댓글로 이동
    public void notiCommentAlbum(String name, String msg, String albumSeq) {
        // 채널을 생성 및 전달해 줄수 있는 NotificationManager를 생성한다.
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, AlbumView.class); // 이동하려는 액티비티를 작성해준다.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        notiIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("albumSeq", albumSeq);
        intent.setAction("showComment");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle(name)
//                .setContentText("상태바 드래그시 보이는 서브타이틀 " + count)
                .setContentText("사진 댓글 : " + msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true); // 눌러야 꺼지는 설정

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "(notiCommentBoard) 8. OREO API 26 이상인 경우, 채널 필요");
            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName = "노티페케이션 채널";
            String description = "오레오 이상";
            int importance = NotificationManager.IMPORTANCE_HIGH;// 우선순위 설정

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert manager != null;
            manager.createNotificationChannel(channel);

        } else
            builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert manager != null;
//        notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작
        Log.i(TAG, "(notiCommentBoard) 9. NotificationManager 객체 사용하여 notify하여 알림 띄움");
        manager.notify((int) System.currentTimeMillis(), builder.build()); // 고유숫자로 노티피케이션 동작
    }


    // 게시글에 좋아요 알림
    public void notiLikeBoard(String name,  String boardSeq) {
        // 채널을 생성 및 전달해 줄수 있는 NotificationManager를 생성한다.
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, BoardView.class); // 이동하려는 액티비티를 작성해준다.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        notiIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("boardSeq", boardSeq);
        intent.setAction("showComment");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle(name)
//                .setContentText("상태바 드래그시 보이는 서브타이틀 " + count)
                .setContentText("회원님의 게시글을 좋아합니다")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true); // 눌러야 꺼지는 설정

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "(notiCommentBoard) 8. OREO API 26 이상인 경우, 채널 필요");
            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName = "노티페케이션 채널";
            String description = "오레오 이상";
            int importance = NotificationManager.IMPORTANCE_HIGH;// 우선순위 설정

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert manager != null;
            manager.createNotificationChannel(channel);

        } else
            builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert manager != null;
//        notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작
        Log.i(TAG, "(notiCommentBoard) 9. NotificationManager 객체 사용하여 notify하여 알림 띄움");
        manager.notify((int) System.currentTimeMillis(), builder.build()); // 고유숫자로 노티피케이션 동작
    }

    // 게시글에 댓글 알림
    // 알림 클릭시 해당 댓글로 이동
    public void notiLikeAlbum(String name, String albumSeq) {
        // 채널을 생성 및 전달해 줄수 있는 NotificationManager를 생성한다.
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, AlbumView.class); // 이동하려는 액티비티를 작성해준다.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        notiIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("albumSeq", albumSeq);
        intent.setAction("showComment");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle(name)
//                .setContentText("상태바 드래그시 보이는 서브타이틀 " + count)
                .setContentText("회원님의 사진을 좋아합니다")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true); // 눌러야 꺼지는 설정

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "(notiCommentBoard) 8. OREO API 26 이상인 경우, 채널 필요");
            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName = "노티페케이션 채널";
            String description = "오레오 이상";
            int importance = NotificationManager.IMPORTANCE_HIGH;// 우선순위 설정

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert manager != null;
            manager.createNotificationChannel(channel);

        } else
            builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert manager != null;
//        notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작
        Log.i(TAG, "(notiCommentBoard) 9. NotificationManager 객체 사용하여 notify하여 알림 띄움");
        manager.notify((int) System.currentTimeMillis(), builder.build()); // 고유숫자로 노티피케이션 동작
    }


}