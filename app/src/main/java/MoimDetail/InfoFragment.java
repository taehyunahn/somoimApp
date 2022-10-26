package MoimDetail;

import static Common.StaticVariable.serverAddress;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Member;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Chat.ChatData;
import Chat.ChatService;
import Common.DataClass;
import Common.NetworkClient;
import Common.RecyclerViewEmptySupport;
import Common.UploadApis;
import Meetup.MeetupAdapter;
import Meetup.MeetupCreate;
import Meetup.MeetupData;
import Member.MemberAdapter;
import Member.MemberData;
import MoimSearch.MoimAdapter;
import MoimSearch.MoimData;
import MoimSearch.MoimSearchActivity;
import MyProfile.ProfileActivity;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;


public class InfoFragment extends Fragment {


    private static final String TAG = "InfoFragment(모임정보 탭)";

    TextView tv_title, tv_content, tv_memberCount;
    ImageView iv_mainImage;
    Button btn_join, btn_out;
    NestedScrollView nestedScrollView;

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    // 레트로핏 통신 공통
    NetworkClient networkClient;
    UploadApis uploadApis;

    LinearLayout createMeetup;

    String currentRoom_id;
    String joinedMoimSeqList;

    private RecyclerViewEmptySupport recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private RecyclerView member_rv;
    private RecyclerView.Adapter member_adapter;
    private RecyclerView.LayoutManager member_layoutManager;

    private SwipeRefreshLayout swiperefresh;

    List<MeetupData> meetupDataList = new ArrayList<>();
    List<MemberData> memberDataList = new ArrayList<>();

    // 서버에서 가져온 로그인 계정 정보를 사용하기 위해서 전역변수 선언
    String currentUserProfileImage;
    String currentUserName;
    String currentUserIntro;
    String myRoom_id;
    private String moimSeq;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "(onCreateView()) 1. 호출");

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_info, container, false);
        tv_title = rootView.findViewById(R.id.tv_title);
        tv_content = rootView.findViewById(R.id.tv_content);
        iv_mainImage = rootView.findViewById(R.id.iv_mainImage);
        createMeetup = rootView.findViewById(R.id.createMeetup);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        member_rv = rootView.findViewById(R.id.member_rv);
        btn_join = rootView.findViewById(R.id.btn_join);
        btn_out = rootView.findViewById(R.id.btn_out);
        tv_memberCount = rootView.findViewById(R.id.tv_memberCount);
        nestedScrollView = rootView.findViewById(R.id.nestedScrollView);
        swiperefresh = rootView.findViewById(R.id.swiperefresh);

        // sharedPreference 세팅
        sp = getContext().getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq","");
        moimSeq = MoimDetailMain.moimSeq;
        Log.i(TAG, "(onCreateView()) 2. sharedPreference(login) 정보 불러옴, userSeq : " + userSeq);
        Log.i(TAG, "(onCreateView()) 3. 프래그먼트가 속한 액티비티의 moimSeq : " + MoimDetailMain.moimSeq);
        Log.i(TAG, "(onCreateView()) 3. 현재 액티비티 전역변수에 저장, moimSeq : " + moimSeq);

        // 정모 목록을 위한 meetup 리사이클러뷰 세팅
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MeetupAdapter(meetupDataList, getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setEmptyView(rootView.findViewById(R.id.list_empty));

        // 모임멤버 목록을 위한 member 리사이클러뷰 세팅
        member_rv.setHasFixedSize(true);
        member_layoutManager = new LinearLayoutManager(getContext());
        member_rv.setLayoutManager(member_layoutManager);
        member_adapter = new MemberAdapter(memberDataList, getContext());
        member_rv.setAdapter(member_adapter);

        // showMeetupList 메서드를 사용하여 서버로 보낼 값 (onCreate 안에 선언)
        HashMap hashmap = new HashMap<String, String>();
        hashmap.put("userSeq", userSeq);
        hashmap.put("moimSeq", moimSeq);
        Log.i(TAG, "(onCreateView()) 4. HTTP통신 서버로 보낼 hashmap 객체 생성, hashmap : " + String.valueOf(hashmap));


        // 모임에 가입했는지에 따라 버튼 상태 변경
//        Boolean clickedOrNot = sp.getBoolean("moimAlreadyJoined" + userSeq + MoimDetailMain.moimSeq, false);
//        if(clickedOrNot){
//            btn_join.setVisibility(View.GONE);
//            btn_out.setVisibility(View.VISIBLE);
//        }

        Log.i(TAG, "(onCreateView()) 5. receiveProfileInfo(userSeq) 매서드 호출 - 로그인 정보를 얻어서 전역변수에 저장");
        receiveProfileInfo(userSeq); // 로그인 계정 정보 요청 -> 전역변수에 저장

        Log.i(TAG, "(onCreateView()) 6. showMoimInfo(moimSeq)");
        showMoimInfo(moimSeq); // 1. 모임 상세정보 요청

        meetupDataList.clear();
        Log.i(TAG, "(onCreateView()) 7. showMeetupList(moimSeq, userSeq)");
        showMeetupList(moimSeq, userSeq); // 2. 모든 정모 목록 요청

        Log.i(TAG, "(onCreateView()) 8. showMemberList(moimSeq)");
        showMemberList(moimSeq); // 3. 멤버 목록 요청

        Log.i(TAG, "(onCreateView()) 9. memberCheck(userSeq, moimSeq)");
        memberCheck(userSeq, moimSeq); // 4. 로그인 계정이 현재 모임의 멤버인지 확인하여, 버튼 변경

        // 스와이프 새로고침
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Log.i(TAG, "(swiperefresh) 1. onRefresh() 호출");
                // onCreateView 생명주기에서 바로 동작하는 세 가지 메서드

                Log.i(TAG, "(swiperefresh) 2. showMoimInfo(moimSeq) 호출");
                showMoimInfo(moimSeq); // 1. 모임의 상세 정보 불러오기

                Log.i(TAG, "(swiperefresh) 3. meetupDataList에 담긴 값 모두 삭제");
                meetupDataList.clear(); // 기존의 리스트 삭제

                Log.i(TAG, "(swiperefresh) 4. showMeetupList(moimSeq, userSeq) 호출");
                showMeetupList(moimSeq, userSeq); // 2. 등록된 정모 목록 보여주기
                Log.i(TAG, "(swiperefresh) 5. memberDataList 담긴 값 모두 삭제");
                memberDataList.clear();

                Log.i(TAG, "(swiperefresh) 6. showMemberList(moimSeq) 호출");
                showMemberList(moimSeq); // 3. 멤버 목록 보여주기

                /* 업데이트가 끝났음을 알림 */
                swiperefresh.setRefreshing(false);
            }
        });

        // 모입 가입하기 -> 멤버등록
        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "(모임가입버튼 클릭) 1. 클릭함");
                Log.i(TAG, "(모임가입버튼 클릭) 2. AlertDialog.Builder 객체 생성");
                AlertDialog.Builder ad = new AlertDialog.Builder(getContext()); // context부분에 getAplicationContext를 쓰면 안된다! 기억할 것
                ad.setMessage("모임에 가입하시겠습니까?");
                ad.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Log.i(TAG, "(모임가입버튼 클릭) 3. '아니오' 선택");
                    }
                });

                ad.setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "(모임가입버튼 → 네) 1. 클릭함");
                        editor.putBoolean("moimAlreadyJoined" + userSeq + moimSeq, true);
                        editor.putBoolean("moimFirstJoined"+userSeq + moimSeq, true); // 처음 채팅방에 들어오면, ㅇㅇㅇ님께서 가입하셨습니다 문구 띄워주는 스레드 동작하기 위해서
                        editor.putBoolean("moimAlreadyJoined" + userSeq + moimSeq, true); // 채팅 목록 리사이클러뷰를 empty로 띄워야겠다. 가입을 하셔야 볼 수 있다고...
                        editor.putString("moimSeqFromNoti" + userSeq, moimSeq);
                        editor.commit();

                        Log.i(TAG, "(모임가입버튼 → 네) 2. s.f에 값 저장, key : moimAlreadyJoined + userSeq + moimSeq, value : " + sp.getBoolean("moimAlreadyJoined" + userSeq + moimSeq, false));
                        Log.i(TAG, "(모임가입버튼 → 네) 2. s.f에 값 저장, key : moimFirstJoined + userSeq + moimSeq, value : " + sp.getBoolean("moimFirstJoined" + userSeq + moimSeq, false));
                        Log.i(TAG, "(모임가입버튼 → 네) 2. s.f에 값 저장, key : moimAlreadyJoined + userSeq + moimSeq, value : " + sp.getBoolean("moimAlreadyJoined" + userSeq + moimSeq, false));
                        Log.i(TAG, "(모임가입버튼 → 네) 2. s.f에 값 저장, key : moimSeqFromNoti + userSeq, value : " + sp.getString("moimAlreadyJoined" + userSeq, ""));

                        Log.i(TAG, "(모임가입버튼 → 네) 3. joinMoim(moimSeq, userSeq) 호출(HTTP통신 매서드)");
                        joinMoim(moimSeq, userSeq);

                        Log.i(TAG, "(모임가입버튼 → 네) 4. 현재 액티비티 닫기(finish)");
                        ((Activity)getContext()).finish();
                        Log.i(TAG, "(모임가입버튼 → 네) 4. MoimDetailMain로 화면 이동");
                        Intent intent = new Intent(getContext(), MoimDetailMain.class);
                        intent.putExtra("intentToChat", true);
                        intent.putExtra("moimSeq", moimSeq);
                        intent.putExtra("fromMoimJoin", true);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Log.i(TAG, "(모임가입버튼 → 네) 5. intent로 보내는 값, key : intentToChat, value : " + true);
                        Log.i(TAG, "(모임가입버튼 → 네) 5. intent로 보내는 값, key : moimSeq, value : " + moimSeq);
                        Log.i(TAG, "(모임가입버튼 → 네) 5. intent로 보내는 값, key : fromMoimJoin, value : " + true);
                        startActivity(intent);

                        // 채팅 화면으로 이동
//                        MoimDetailMain.tabViewPager.setCurrentItem(3);

//                        Intent intent = new Intent(getContext(), MoimDetailMain.class);
//                        // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
//                        intent.putExtra("moimSeq", moimSeq);
//                         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(intent);
                    }
                });
                ad.show();
            }
        });


        // 모입 탈퇴하기 -> 멤버삭제
        btn_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "'모임 탈퇴하기 버튼' 클릭함");
                AlertDialog.Builder ad = new AlertDialog.Builder(getContext()); // context부분에 getAplicationContext를 쓰면 안된다! 기억할 것
                ad.setMessage("모임에서 탈퇴하시겠습니까?");
                ad.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                ad.setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "(모임탈퇴버튼 → 네) 1. 클릭함");

                        editor.remove("moimAlreadyJoined" + userSeq + moimSeq);
                        editor.commit();
                        Log.i(TAG, "(모임탈퇴버튼 → 네) 2. s.f에 값 삭제, key : moimAlreadyJoined + userSeq + moimSeq, value : " + sp.getBoolean("moimAlreadyJoined" + userSeq + moimSeq, false));

//                        btn_join.setVisibility(View.VISIBLE);
//                        btn_out.setVisibility(View.GONE);

                        Log.i(TAG, "(모임탈퇴버튼 → 네) 3. outMoim(moimSeq, userSeq) 호출(HTTP통신 매서드)");
                        outMoim(moimSeq, userSeq); // 모임 탈퇴 메서드
                        Log.i(TAG, "(모임가입버튼 → 네) 4. MoimSearchActivity로 화면 이동");
                        Intent intent = new Intent(getContext(), MoimSearchActivity.class);
                        // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    }
                });
                ad.show();
            }
        });
        return rootView;
    }

    // HTTP통신 매서드 1. 모임 상세정보 요청
    // (통신방식) Retrofit
    // (클라이언트→서버) moimSeq
    // (서버→클라이언트) MoimData 객체의 값(leaderSeq, address, title, mainImage, content, memberCount)
    private void showMoimInfo(String moimSeq) {
        Log.i(TAG, "(showMoimInfo) 1. 호출 - 모임 상세정보 요청");
        Log.i(TAG, "(showMoimInfo) 2. 서버로 보내는 값, moimSeq : " + moimSeq);
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Call<MoimData> call = uploadApis.showMoimInfo(moimSeq);
        call.enqueue(new Callback<MoimData>() {
            @Override
            public void onResponse(Call<MoimData> call, retrofit2.Response<MoimData> response) {
                Log.i(TAG, "(showMoimInfo) 3. 서버에서 받은 응답, reponse.body().toString() : " + response.body().toString());
                if(!response.isSuccessful()){
                    return;
                }
                MoimData responseBody = response.body(); // 모임의 모든 정보를 가진 객체
                String leaderSeq = responseBody.getLeaderSeq(); // 모임장의 고유값
                String address = responseBody.getAddress(); // 모임 주소
                String title = responseBody.getTitle(); // 모임 타이틀
                String mainImage = responseBody.getMainImage(); // 모임 대표사진
                String content = responseBody.getContent(); // 모임 설명
                String memberCount = responseBody.getMemberCount(); // 모임 멤버수

                Gson gson = new Gson();
                String object = gson.toJson(responseBody);
                Log.i(TAG, "(showMoimInfo) 4. 서버에서 받은 객체(MoimData), object : " + object);

                Log.i(TAG, "(showMoimInfo) 5. 모임제목 변경, title : " + title);
                tv_title.setText(title); //모임 제목 설정
                Log.i(TAG, "(showMoimInfo) 5. 모임내용 변경, content : " + content);
                tv_content.setText(content);
                Log.i(TAG, "(showMoimInfo) 5. 멤버수 변경, memberCount : " + memberCount);
                tv_memberCount.setText("모임 멤버 (" + memberCount + "명)");

                Log.i(TAG, "(showMoimInfo) 6. 메인이미지 변경, mainImage : " + mainImage);
                if(mainImage != null){
                    Glide.with(getContext())
                            .load(mainImage)
                            .centerCrop()
                            .into(iv_mainImage);
                }

                String userSeq = sp.getString("userSeq","");
                //모임장의 고유값과 현재 로그인한 계정의 고유값이 같다면, 편집 text 표시하고, 좋아요 아이콘 없앰

                Log.i(TAG, "(showMoimInfo) 7. 현재 계정이 모임장인지 확인, userSeq : " + userSeq + " / leaderSeq : " + leaderSeq);
                if(userSeq.equals(leaderSeq)){
                    Log.i(TAG, "(showMoimInfo) 8. 모임장인 경우");
                    Log.i(TAG, "(showMoimInfo) 8-1. 현재 fragment가 포함된 activity의 매서드 moimLeaderSetting() 호출 ");
                    ((MoimDetailMain)getActivity()).moimLeaderSetting();
                    Log.i(TAG, "(showMoimInfo) 8-2. 정모 만들기 버튼 visible 세팅");
                    createMeetup.setVisibility(View.VISIBLE);
//                    btn_join.setVisibility(View.GONE);
//                    btn_out.setVisibility(View.GONE);
                } else {
                    Log.i(TAG, "(showMoimInfo) 8. 모임장이 아닌 경우");
                }

                // 정모 만들기 버튼 클릭시
                createMeetup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "(정모 만들기 버튼) 1. 클릭함");
                        Log.i(TAG, "(정모 만들기 버튼) 2. MeetupCreate 화면 이동");
                        Intent intent = new Intent(getContext(), MeetupCreate.class);
                        intent.putExtra("moimSeq", moimSeq);
                        Log.i(TAG, "(정모 만들기 버튼) 2. intent로 보내는 값, moimSeq : " + moimSeq);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onFailure(Call<MoimData> call, Throwable t) {
                Log.i(TAG, "onFailure = " + t);
            }
        });
    }

    // HTTP통신 매서드 0. 모임 참여 - member 테이블에서 행 추가
    // (통신방식) Retrofit
    // (클라이언트→서버) moimSeq, userSeq
    // (서버→클라이언트) MemberData 객체의 값(joinedMoimSeqList)
    // (결과 동작) ChatRoomJoinThread 실행하여, 소켓연결된 계정의 참여중인 모임 목록 업데이트
    private void joinMoim(String moimSeq, String userSeq) {

        Log.i(TAG, "(joinMoim) 1. 호출");
        Log.i(TAG, "(joinMoim) 2. HTTP통신 서버로 보내는 값, moimSeq : " + moimSeq + " / userSeq : " + userSeq);

        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Call<MemberData> call = uploadApis.joinMoim(moimSeq, userSeq);
        call.enqueue(new Callback<MemberData>() {
            @Override
            public void onResponse(Call<MemberData> call, retrofit2.Response<MemberData> response) {

                Log.i(TAG, "(joinMoim) 3. HTTP통신 서버에서 받은 값, response.body().toString() : " + response.body().toString());

                if(response.isSuccessful() && response.body() != null){

                    MemberData memberData = response.body(); // 모임의 모든 정보를 가진 객체

                    Gson gson = new Gson();
                    String object = gson.toJson(memberData);
                    Log.i(TAG, "(joinMoim) 3. HTTP통신 서버에서 받은 값 object : " + object);
                    joinedMoimSeqList = memberData.getJoinedMoimSeqList();
                    Log.i(TAG, "(joinMoim) 4. joinedMoimSeqList를 전역변수에 저장, joinedMoimSeqList : " + joinedMoimSeqList);
//                tv_title.setText(title); //모임 제목 설정
                    Toast.makeText(getContext(), "모임에 가입하셨습니다", Toast.LENGTH_SHORT).show();
                    // 모임에 가입한 경우 -> 채팅 화면으로 바로 이동한다
                    // 이때가 처음 채팅방에 들어가는 상황이다.
                    // 여기서 sharedPreference를 사용하면 좋겠다.

                    // 채팅방에 처음 입장할 때 동작하는 스레드
                    Log.i(TAG, "(joinMoim) 5. ChatRoomJoinThread 호출 - 소켓연결된 계정의 참여중인 모임 목록 업데이트");
                    ChatRoomJoinThread chatRoomJoinThread = new ChatRoomJoinThread(ChatService.member_socket);
                    chatRoomJoinThread.start();
                }
            }

            @Override
            public void onFailure(Call<MemberData> call, Throwable t) {
                Log.i(TAG, "onFailure = " + t);
            }
        });
    }

    // HTTP통신 매서드 0. 모임 탈퇴 - member 테이블에서 행 삭제
    // (통신방식) Retrofit
    // (클라이언트→서버) moimSeq, userSeq
    // (서버→클라이언트) MemberData 객체의 값(joinedMoimSeqList)
    // (결과 동작) MoimLeft 실행하여, 소켓연결된 계정의 참여중인 모임 목록 업데이트
    // 모임 탈퇴 메서드
    private void outMoim(String moimSeq, String userSeq) {
        Log.i(TAG, "(outMoim) 1. 호출 - 모임 탈퇴 - member 테이블에서 행 삭제");
        Log.i(TAG, "(outMoim) 2. 서버로 보내는 값, moimSeq : " + moimSeq + " / userSeq : "+ userSeq);

        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Call<MemberData> call = uploadApis.outMoim(moimSeq, userSeq);
        call.enqueue(new Callback<MemberData>() {
            @Override
            public void onResponse(Call<MemberData> call, retrofit2.Response<MemberData> response) {

                Log.i(TAG, "(outMoim) 3. HTTP통신 서버에서 받은 값, response.body().toString() : " + response.body().toString());

                if(response.isSuccessful() && response.body() != null) {
                    MemberData responseBody = response.body(); // 모임의 모든 정보를 가진 객체

                    Gson gson = new Gson();
                    String object = gson.toJson(responseBody);
                    Log.i(TAG, "(outMoim) 4. HTTP통신 서버에서 받는 객체, MemberData : " + object);

                    joinedMoimSeqList = responseBody.getJoinedMoimSeqList();
                    Log.i(TAG, "(outMoim) 5. joinedMoimSeqList를 전역변수에 저장, joinedMoimSeqList : " + joinedMoimSeqList);
                    Toast.makeText(getContext(), "모임에서 탈퇴했습니다", Toast.LENGTH_SHORT).show();

                    Log.i(TAG, "(outMoim) 6. MoimLeft 스레드 동작");
                    MoimLeft moimLeft = new MoimLeft(ChatService.member_socket);
                    moimLeft.start();
                }
            }

            @Override
            public void onFailure(Call<MemberData> call, Throwable t) {
                Log.i(TAG, "onFailure = " + t);
            }
        });
    }



    private void showMeetupList(String moimSeq, String userSeq) {
        Log.i(TAG, "(showMeetupList) 1. 호출 - HTTP 통신, 정모 목록 요청");
        Log.i(TAG, "(showMeetupList) 2. 서버로 보내는 값, moimSeq : " + moimSeq + " / userSeq : " + userSeq);

        // 레트로핏 생성하여 인터페이스와 연결
            uploadApis = networkClient.getRetrofit().create(UploadApis.class);

            // 1. 데이터 형태 지정 (ex. DataClass)
            // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
            Call<List<MeetupData>> call = uploadApis.showMeetupList(moimSeq, userSeq);
            call.enqueue(new Callback<List<MeetupData>>() {
                @Override
                public void onResponse(Call<List<MeetupData>> call, retrofit2.Response<List<MeetupData>> response) {
                    List<MeetupData> responseBody = response.body();
                    for(int i = 0; i < responseBody.size(); i++) {
                        MeetupData meetupData = responseBody.get(i);
                        meetupDataList.add(meetupData);

                        Gson gson = new Gson();
                        String object = gson.toJson(meetupData);
                        Log.i(TAG, "(showMeetupList) 3. 서버에서 받은 객체, meetupData : " + object);
                    }
                    adapter = new MeetupAdapter(meetupDataList, getContext());
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "(showMeetupList) 4. 리사이클러뷰 어댑터에 객체 연결 완료");

                }

                @Override
                public void onFailure(Call<List<MeetupData>> call, Throwable t) {
                    Log.i(TAG, "(showMeetupList) 3. HTTP 통신 실패, t : " + t);
                }
            });
        }

        //모임 멤버 목록 요청 메서드
    private void showMemberList(String moimSeq) {
        Log.i(TAG, "(showMemberList) 1. 호출 - HTTP 통신, 모임 멤버 목록 수신");

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Log.i(TAG, "(showMemberList) 2. 서버로 보내는 값, moimSeq : " + moimSeq);

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<List<MemberData>> call = uploadApis.showMemberList(moimSeq);
        call.enqueue(new Callback<List<MemberData>>() {
            @Override
            public void onResponse(Call<List<MemberData>> call, retrofit2.Response<List<MemberData>> response) {
                List<MemberData> responseBody = response.body();
                for(int i = 0; i < responseBody.size(); i++) {
                    MemberData memberData = responseBody.get(i);
                    memberDataList.add(memberData);
                    Gson gson = new Gson();
                    String object = gson.toJson(memberData);
                    Log.i(TAG, "(showMemberList) 3. HTTP 통신 결과 수신한 객체, 모임 멤버 목록(MemberData) : " + object);
                }
                Log.i(TAG, "(showMemberList) 4. 모임 멤버 객체 list를 리사이클러뷰 어댑터의 매개변수로 사용, 출력!");
                member_adapter = new MemberAdapter(memberDataList, getContext());
                member_rv.setAdapter(member_adapter);
                member_adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<MemberData>> call, Throwable t) {
                Log.i(TAG, "(showMemberList) 서버 통신 실패 : " + t);
            }
        });
    }


//    // 현재 로그인한 계정의 정보를 받아서 전역변수에 담아준다.
//    // 프로필 정보를 가져와야 하는 부분이다.
//    private void receiveProfileInfo(String userSeq) {
//        Log.i(TAG, "(receiveProfileInfo(userSeq) 메서드 호출) 1. 현재 로그인한 계정의 정보를 받아서 전역변수에 저장");
//        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
//        Call<DataClass> call = uploadApis.receiveProfileInfoWithRetrofit(userSeq);
//        Log.i(TAG, "(receiveProfileInfo(userSeq) 메서드 호출) 2. 서버로 보내는 값, userSeq : " + userSeq);
//        call.enqueue(new Callback<DataClass>() {
//            @Override
//            public void onResponse(Call<DataClass> call, retrofit2.Response<DataClass> response) {
//                Log.d("onResponse", response.body().toString());
//
//                if(!response.isSuccessful()){
//                    return;
//                }
//
//                DataClass responseBody = response.body();
//                currentUserProfileImage = responseBody.getProfileImage();
//                currentUserName = responseBody.getName();
//                currentUserIntro = responseBody.getIntro();
//                myRoom_id = responseBody.getIntro();
//                joinedMoimSeqList = responseBody.getJoinedMoimSeqList();
//
//                Gson gson = new Gson();
//                String object = gson.toJson(responseBody);
//                Log.i(TAG, " : " + object);
//                Log.i(TAG, "(receiveProfileInfo(userSeq) 메서드 호출) 3. 서버에서 전달받은 객체(DataClass)" + object);
//            }
//
//            @Override
//            public void onFailure(Call<DataClass> call, Throwable t) {
//                Log.i(TAG, "(receiveProfileInfo(userSeq) 메서드 호출) 서버통신 실패 : " + t);
//
//            }
//        });
//    }


    // HTTP통신 매서드 0. 로그인 계정의 정보 요청
    // (통신방식) volley
    // (클라이언트→서버) userSeq
    // (서버→클라이언트) joinedMoimSeqList, userName

    private void receiveProfileInfo(String userSeq){ // 프로필 사진, 이름, 소개, 참여중이 모임방 목록 필요
        Log.i(TAG, "(receiveProfileInfo) 1. 호출 - 로그인 계정의 정보 요청");
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = serverAddress + "somoim/login/joinedMoimList.php";
        Log.i(TAG, "(receiveProfileInfo) 2. HTTP 통신 URL : "+ url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "(receiveProfileInfo) 4. 서버에서 수신한 값, response : "+ response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Gson gson = new Gson();
                    String object = gson.toJson(jsonObject);
                    Log.i(TAG, "(receiveProfileInfo) 5. 서버에서 수신한 object : " + object);

                    String joinedMoimSeqList = jsonObject.getString("joinedMoimSeqList");
                    String userName = jsonObject.getString("userName");
                    String userProfileImage = jsonObject.getString("userProfileImage");
                    String userIntro = jsonObject.getString("userIntro");

                    currentUserName = userName; // 서버에서 받은 userName을 전역변수에 저장
                    Log.i(TAG, "(receiveProfileInfo) 6. 서버에서 받은 userName을 전연변수에 저장, currentUserName : " + currentUserName);


                    editor.putString("userName", userName); // 어디서 사용되는거지? 추후 확인 필요
                    editor.putString("joinedMoimSeqList", joinedMoimSeqList);
                    editor.commit();


                    Log.i(TAG, "(receiveProfileInfo) 7. sharedPreference(login)에 저장(아래 저장 여부 확인)");
                    Log.i(TAG, "(receiveProfileInfo) 7. s.p key : userName / value : " + sp.getString("userName", ""));
                    Log.i(TAG, "(receiveProfileInfo) 7. s.p key : joinedMoimSeqList / value : " + sp.getString("joinedMoimSeqList", ""));

//                    // 채팅 소켓 서비스 가동 시키는 타이밍
//                    Log.i(TAG, "(receiveProfileInfo) 10. Chat.ChatService 동작(서비스)");
//                    Log.i(TAG, "(receiveProfileInfo) 11. intentService.putExtra() key : userSeq / value : " + userSeq);
//                    Intent intentService = new Intent(getContext(), Chat.ChatService.class);
//                    intentService.putExtra("userSeq", userSeq);
//                    getContext().startService(intentService);


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "(receiveProfileInfo) JSONException 발생하여 데이터 수신 못함");
                }
            }
        }, new Response.ErrorListener() {

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




    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach() 호출");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate() 호출");
    }

    @Override
    public void onStart() {
        super.onStart();

        // sharedPreference 세팅
        sp = getContext().getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq","");
        moimSeq = MoimDetailMain.moimSeq;
        Log.i(TAG, "(onCreateView()) 2. sharedPreference(login) 정보 불러옴, userSeq : " + userSeq);
        Log.i(TAG, "(onCreateView()) 3. 프래그먼트가 속한 액티비티의 moimSeq : " + MoimDetailMain.moimSeq);
        Log.i(TAG, "(onCreateView()) 3. 현재 액티비티 전역변수에 저장, moimSeq : " + moimSeq);

        memberCheck(userSeq, moimSeq);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume() 호출");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause() 호출");
        memberDataList.clear();

        // sharedPreference 세팅

        SharedPreferences sp;
        SharedPreferences.Editor editor;
//
        sp = getContext().getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String userSeq = sp.getString("userSeq", "");
        String moimSeqFromNoti = sp.getString("moimSeqFromNoti" + userSeq, "");
        Log.i(TAG, "(onPause()) sharedPreference(login) 값, userSeq : " + userSeq);
        Log.i(TAG, "(onPause()) sharedPreference(login) 값, moimSeqFromNoti : " + moimSeqFromNoti);


///*        editor.remove("moimSeqFromNoti" + userSeq);*/
//        editor.commit();

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop() 호출");
        memberDataList.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy() 호출");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach() 호출");

    }

    // 채팅방 나갈 때 동작하는 스레드
    class MoimLeft extends Thread {
        Socket socket;
        public MoimLeft(Socket socket) {
            try {
                this.socket = socket;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            try {

                // 모임 처음 가입했을 때(채팅방 최초 접속), 서버로 보낼 메세지 객체를 선언한다.
                // 1. 접속한 계정의 고유값(userSeq),
                // 2. 현재 접속한 모임의 고유값(moimSeq),
                // 3. 현재 채팅방의 고유값(chatRoomSeq), = 모임의 고유값과 별도로 구분하지 않아도 될 것 같은데, 추후 고려 예정
                // 4. 서버 쪽에서 처리할 명령 구분 (command), = 1) 채팅방 최초 참여, 2) 메세지 발송, 3) 채팅방에서 나가기
                // 5. 접속한 계정의 프로필 이미지 (userProfileImage)
                // 6. 접속한 계정이 작성한 메세지 (msg)
                // 7. 뷰타입을 보내야 하나? 이건 잘 모르겠다. 확인 필요. 1) 날짜 가운데 표시, 2) 내가 보낸 건 오른 쪽, 3) 다른 사람이 보낸 건 왼쪽
                // 8. 오더타입? 이건 무슨 역할인지 모르겠다.
                // 9. 받을 사람의 고유값을 지정해야 하나? 이것도 잘 모르겠다.
                // sharedPreference 세팅

                SharedPreferences sp;
                SharedPreferences.Editor editor;

                sp = getContext().getSharedPreferences("login", Activity.MODE_PRIVATE);
                editor = sp.edit();
                String userSeq = sp.getString("userSeq", "");

                Log.i(TAG, "(MoimLeft) 1. 모임탈퇴 시, 동작하는 스레드");
                Log.i(TAG, "(MoimLeft) 2. 서버로 보낼 값을 ChatService.chatData 객체에 담는다");

                ChatService.chatData = new ChatData();
                ChatService.chatData.setCommand("moimLeft"); // 채팅방 접속
                ChatService.chatData.setUserSeq(userSeq); // 로그인한 계정의 고유값
                ChatService.chatData.setUserName(currentUserName);
                ChatService.chatData.setUserProfileImage(currentUserProfileImage);
                ChatService.chatData.setTime(getTime()); // 현재시각
                ChatService.chatData.setMoimSeq(moimSeq);
                ChatService.chatData.setJoinedMoimSeqList(joinedMoimSeqList);
                ChatService.chatData.setMsg(currentUserName + "님께서 모임을 탈퇴했습니다.");

                Gson gson = new Gson();


                Log.i(TAG, "(MoimLeft) 3. ChatService.chatData 객체를 String 값으로 변환(Gson 사용)");
                String sendingObject = gson.toJson(ChatService.chatData);
                Log.i(TAG, "(MoimLeft) 4. 서버로 보내는 값, ChatService.chatData : " + sendingObject);

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

    // 채팅방 입장할 때 동작하는 스레드
    class ChatRoomJoinThread extends Thread {
        Socket socket;
        public ChatRoomJoinThread(Socket socket) {
            try {
                this.socket = socket;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            Log.i(TAG, "(ChatRoomJoinThread) 채팅방 입장할 때 동작하는 스레드 run");
            try {

                // 모임 처음 가입했을 때(채팅방 최초 접속), 서버로 보낼 메세지 객체를 선언한다.
                // 1. 접속한 계정의 고유값(userSeq),
                // 2. 현재 접속한 모임의 고유값(moimSeq),
                // 3. 현재 채팅방의 고유값(chatRoomSeq), = 모임의 고유값과 별도로 구분하지 않아도 될 것 같은데, 추후 고려 예정
                // 4. 서버 쪽에서 처리할 명령 구분 (command), = 1) 채팅방 최초 참여, 2) 메세지 발송, 3) 채팅방에서 나가기
                // 5. 접속한 계정의 프로필 이미지 (userProfileImage)
                // 6. 접속한 계정이 작성한 메세지 (msg)
                // 7. 뷰타입을 보내야 하나? 이건 잘 모르겠다. 확인 필요. 1) 날짜 가운데 표시, 2) 내가 보낸 건 오른 쪽, 3) 다른 사람이 보낸 건 왼쪽
                // 8. 오더타입? 이건 무슨 역할인지 모르겠다.
                // 9. 받을 사람의 고유값을 지정해야 하나? 이것도 잘 모르겠다.

                // sharedPreference 세팅

                SharedPreferences sp;
                SharedPreferences.Editor editor;

                sp = getContext().getSharedPreferences("login", Activity.MODE_PRIVATE);
                editor = sp.edit();
                String staticUserSeq = sp.getString("userSeq", "");
                String userName = sp.getString("userName", "");
                String myRoom_id = sp.getString("myRoom_id", "");


                Log.i(TAG, "(ChatRoomJoinThread) 서버로 보낼 객체의 값을 set한다");
                ChatService.chatData = new ChatData();
                ChatService.chatData.setCommand("JOIN"); // 채팅방 접속
                ChatService.chatData.setUserSeq(staticUserSeq); // 로그인한 계정의 고유값
                ChatService.chatData.setUserName(userName);
                ChatService.chatData.setUserProfileImage(currentUserProfileImage);
                ChatService.chatData.setTime(getTime()); // 현재시각
                ChatService.chatData.setMoimSeq(moimSeq);
                ChatService.chatData.setJoinedMoimSeqList(joinedMoimSeqList);
                ChatService.chatData.setMsg(currentUserName+"님께서 입장하셨습니다.");


                Gson gson = new Gson();
                String sendingObject = gson.toJson(ChatService.chatData);

                Log.i(TAG, "_chatMemberJson = " +  sendingObject);
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

    // HTTP통신 매서드 1. 현재 계정이 현재 방에 가입되어있는지 여부 확인
    // (통신방식) Volley
    // (클라이언트→서버) userSeq, moimSeq
    // (서버→클라이언트) memberCheck, leaderSeq

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
                    String leaderSeq = jsonResponse.getString("leaderSeq");

                    Log.i(TAG, "(memberCheck) 5. memberCheck(0이면 멤버 아님) : " + memberCheck);

                    if (memberCheck.equals("0")) {
                        Log.i(TAG, "(memberCheck) 5-1. 멤버가 아닌 경우, 리사이클러뷰 empty 세팅과 채팅 입력창 없앰");
                        // 가입하지 않았다면,
                        btn_join.setVisibility(View.VISIBLE);
                        btn_out.setVisibility(View.GONE);

                    } else {
                        // 모임에 이미 가입했다면,
                        // 지난 채팅 메세지 목록 불러오기
                        Log.i(TAG, "(memberCheck) 5-2. 멤버인 경우, 지난 채팅 데이터 불러오는 showChatMsgList(HTTP 통신) 메서드 호출, 채팅 입력창 보여줌");
                        btn_join.setVisibility(View.GONE);
                        btn_out.setVisibility(View.VISIBLE);
                    }

                    Log.i(TAG, "(memberCheck) 6. leaderSeq(모임 리더의 고유값) : " + leaderSeq + " (userSeq : " + userSeq + "와 일치하는지 비교)");
                    if(leaderSeq.equals(userSeq)){
                        Log.i(TAG, "(memberCheck) 7. 현재 계정이 모임장인 경우, 가입 또는 탈퇴 버튼을 없앤다");
                        btn_join.setVisibility(View.GONE);
                        btn_out.setVisibility(View.GONE);
                    }

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

}
