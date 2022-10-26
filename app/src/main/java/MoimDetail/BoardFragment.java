package MoimDetail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import Common.DataClass;
import Common.NetworkClient;
import Common.RecyclerViewEmptySupport;
import Common.UploadApis;
import Meetup.MeetupAdapter;
import Meetup.MeetupData;
import MoimDetail.Board.BoardAdapter;
import MoimDetail.Board.BoardCreate;
import MoimDetail.Board.BoardData;
import MoimSearch.MoimAdapter;
import MoimSearch.MoimData;
import retrofit2.Call;
import retrofit2.Callback;


public class BoardFragment extends Fragment {

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    // 레트로핏 통신 공통
    NetworkClient networkClient;
    UploadApis uploadApis;

    String userSeq;
    String moimSeq;

    List<BoardData> boardDataList = new ArrayList<>();
    private RecyclerViewEmptySupport recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout swiperefresh;

    FloatingActionButton fab;

    private static final String TAG = "BoardFragment(게시판 탭)";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_board, container, false);

        Log.i(TAG, "(onCreateView) 1. 호출");

        // UI 연결
        recyclerView = rootView.findViewById(R.id.recyclerView);
        fab = rootView.findViewById(R.id.fab); // 플로팅액션버튼
        swiperefresh = rootView.findViewById(R.id.swiperefresh);

        // sharedPreference 세팅
        sp = getContext().getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();

        userSeq = sp.getString("userSeq","" ); // 로그인 정보의 고유값
        moimSeq = MoimDetailMain.moimSeq; // 모임의 고유값



        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setEmptyView(rootView.findViewById(R.id.list_empty));

        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                boardDataList.clear();
                showBoardList(moimSeq, userSeq);

                /* 업데이트가 끝났음을 알림 */
                swiperefresh.setRefreshing(false);
            }
        });


        // 게시판 글쓰기 버튼 클릭시
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

//                Toast.makeText(getContext(), "클릭했습니다", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getContext(), BoardCreate.class);
                startActivity(intent);
            }
        });

        return rootView;
    }


    // HTTP통신 매서드 1. 모임 게시글 목록 요청
    // (통신방식) Retrofit
    // (클라이언트→서버) moimSeq, userSeq
    // (서버→클라이언트) BoardData 객체의 값(name, title, content, date, profileImage, boardSeq, likeCount, commentCount)
    private void showBoardList(String moimSeq, String userSeq) {
        Log.i(TAG, "(showBoardList) 1. 호출 - 모임 게시글 목록 요청");
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Log.i(TAG, "(showBoardList) 2. HTTP통신 서버로 보내는 값, moimSeq : " + moimSeq + " / userSeq : " + userSeq);

        Call<List<BoardData>> call = uploadApis.showBoardList(moimSeq, userSeq);
        call.enqueue(new Callback<List<BoardData>>() {
            @Override
            public void onResponse(Call<List<BoardData>> call, retrofit2.Response<List<BoardData>> response) {

                Log.i(TAG, "(showBoardList) 3. HTTP통신 서버에서 받은 값, response.body().toString() : " + response.body().toString());
                if(response.isSuccessful() && response.body() != null){

                    List<BoardData> responseBody = response.body();
                    for(int i = 0; i < responseBody.size(); i++) {
                        boardDataList.add(responseBody.get(i));
                        Gson gson = new Gson();
                        String object = gson.toJson(responseBody.get(i));
                        Log.i(TAG, "(showBoardList) 4. HTTP통신 서버에서 받은 객체, BoardData : " + object);
                    }
                    mAdapter = new BoardAdapter(boardDataList, getContext());
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                    Log.i(TAG, "(showBoardList) 5. 리사이클러뷰 어댑터 사용하여 출력");
                }
            }

            @Override
            public void onFailure(Call<List<BoardData>> call, Throwable t) {
                Log.i(TAG, "(showBoardList) 에러 = " + t);
            }
        });
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i(TAG, "Fragment 생명주기 onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Fragment 생명주기 onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "Fragment 생명주기 onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "Fragment 생명주기 onResume");
        showBoardList(moimSeq, userSeq);

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "Fragment 생명주기 onPause");
        boardDataList.clear();
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

}