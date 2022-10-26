package MoimDetail;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.somoim.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Common.NetworkClient;
import Common.RecyclerViewEmptySupport;
import Common.StaticVariable;
import Common.UploadApis;
import MoimDetail.Board.BoardData;
import MoimDetail.Board.BoardImageAdapter;
import MoimDetail.Board.BoardUpdate;
import MoimDetail.Board.BoardView;
import MoimDetail.Photo.AlbumAdapter;
import MoimDetail.Photo.AlbumData;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;


public class AlbumFragment extends Fragment {

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    // 레트로핏 통신 공통
    NetworkClient networkClient;
    UploadApis uploadApis;

    String userSeq;
    String moimSeq;

    List<AlbumData> albumList = new ArrayList<>();
    private RecyclerViewEmptySupport recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout swiperefresh;

    FloatingActionButton fab;

    private static final String TAG = "AlbumFragment(사진첩 탭)";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_photo, container, false);

        Log.i(TAG, "(onCreateView() 1. 호출");

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
        layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setEmptyView(rootView.findViewById(R.id.list_empty));


        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                albumList.clear();
                showAlbumList(moimSeq);

                /* 업데이트가 끝났음을 알림 */
                swiperefresh.setRefreshing(false);
            }
        });

        // 사진추가 버튼 클릭시
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                Toast.makeText(getContext(), "클릭했습니다", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getContext(), PhotoCreate.class);
//                startActivity(intent);
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2222);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "(앨범선택 후) 1. onActivityResult 동작");
        if(data == null){ // 어떤 이미지도 선택하지 않은 경우
            Log.i(TAG, "(앨범선택 후) 2. 선택한 이미지 없음, data == null");
            Toast.makeText(getContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "(앨범선택 후) 2. 이미지를 선택함, data가 null이 아님");
            // 같이 저장할 정보 : 1) 작성자 고유값(userSeq), 2) 선택한 이미지의 파일(fileList), 3) 파일 개수(fileCount), 3) 현재 모임 고유값(moimSeq)
            Log.i(TAG, "(앨범선택 후) 3. hashMap에 HTTP 통신을 위한 계정과 모임 정보를 저장함");
            HashMap hashMap = new HashMap();
            hashMap.put("userSeq", userSeq); // 현재 계정의 고유값
            hashMap.put("moimSeq", moimSeq); // 현재 모임방의 고유값
            Log.i(TAG, "(앨범선택 후) 4. hashMap에 저장된 값 : " + String.valueOf(hashMap) + "(이미지 파일 개수(fileImage)는 다음 step에서 확인하여 추가함");

            Log.i(TAG, "(앨범선택 후) 5. uri, file의 arrayList를 각각 생성함");
            ArrayList<Uri> uriList = new ArrayList<Uri>();
            ArrayList<File> fileList = new ArrayList<File>();

            Log.i(TAG, "(앨범선택 후) 5. data.getClipData() : " + data.getClipData());

            // case 1. 이미지를 하나라도 선택한 경우
            if(data.getClipData() == null){ // 이미지를 하나만 선택한 경우?? 아닌 것 같은데?
                // 같이 저장할 정보 : 1) 작성자 고유값(userSeq), 2) 선택한 이미지의 파일(fileList), 3) 파일 개수(fileCount), 3) 현재 모임 고유값(moimSeq)
            } else { // case 2. 이미지를 여러장 선택한 경우
                Log.i(TAG, "(앨범선택 후) 6. 이미지를 선택한 경우, ClipData 객체 생성");
                ClipData clipData = data.getClipData();
                Log.i(TAG, "(앨범선택 후) 7. 생성된 ClipData 개수(선택한 이미지 파일 수) : " + String.valueOf(clipData.getItemCount()));


                if(clipData.getItemCount() > 10) {
                    // case 2-1. 선택한 이미지가 11장 이상인 경우
                    Log.i(TAG, "(앨범선택 후) 8. 11개 이상이 경우 ");
                    Toast.makeText(getContext(), "사진은 10장까지 선택 가능합니다", Toast.LENGTH_SHORT).show();
                } else{
                    // case 2-2. 선택한 이미지가 1장 이상 10장 이하인 경우
                    Log.i(TAG, "(앨범선택 후) 9. 1개 이상 ~ 10개 이하인 경우 ");
                    Log.i(TAG, "(앨범선택 후) 10. 생성된 ClipData 개수(선택한 이미지 파일 수)만큼 반복문 동작 → uri 객체 생성  → file 객체 생성  → fileList.add ");
                    Log.i(TAG, "(앨범선택 후) 11. 반복문 동작하기 전 fileList.size() : " + fileList.size());
                    for(int i = 0; i < clipData.getItemCount(); i++){
                        Uri imageUri = clipData.getItemAt(i).getUri(); // 선택한 이미지들의 uri를 가져온다
                        try{
                            uriList.add(imageUri); // uri를 list에 담는다
                            // 해당 uriList를 서버에 보내서 저장한다.
                        }catch (Exception e){
                            Log.i(TAG, "File select error", e);
                        }

                        File imageFile = new File(StaticVariable.createCopyAndReturnRealPath(getContext(), imageUri));
                        fileList.add(imageFile);
                        hashMap.put("fileCount", fileList.size()); // 파일 개수 추가

//                        fileList.clear();
//                        uriList.clear();
                    }

                    albumUpload(fileList, hashMap); // HTTP 통신 -> 파일 리스트와 각종 정보(userSeq, moimSeq, fileCount) 전송

                    Log.i(TAG, "(앨범선택 후) 12. 반복문 동작 후 fileList.size() : " + fileList.size());
                }
            }
        }
    }

    private void albumUpload(ArrayList<File> fileArrayList, HashMap<String, String> hashmap) {
        Log.i(TAG, "(albumUpload) 1. 호출 - HTTP 통신, 선택한 이미지 파일을 서버로 전송하여 DB에 저장");
        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);

        Log.i(TAG, "(albumUpload) 2. 서버에 전송할 MultipartBody.Part를 ArrayList 형식으로 생성 → 파일 저장(이름 : imageName + i, 형식 : jpg");
        // 서버에 전송할 파일값 저장
        ArrayList<MultipartBody.Part> filesToServer = new ArrayList<>();
        for (int i = 0; i < fileArrayList.size(); i++) {
            RequestBody fileRequestBody = RequestBody.create(MediaType.parse("image/jpg"), fileArrayList.get(i));
            MultipartBody.Part fileMultiPartBody = MultipartBody.Part.createFormData("imageName"+i, "("+i+")"+".jpg", fileRequestBody);
            filesToServer.add(fileMultiPartBody);
        }

        Log.i(TAG, "(albumUpload) 3. 서버에 전송할 Text를 HashMap<String, RequestBody>으로 저장, userSeq : " + hashmap.get("userSeq") + " / moimSeq : " + hashmap.get("moimSeq") + " / fileCount : " + String.valueOf(fileArrayList.size()));
        // 서버에 전송할 텍스트값 저장
        HashMap<String, RequestBody> textToServer = new HashMap<>();
        textToServer.put("userSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("userSeq")));
        textToServer.put("moimSeq", RequestBody.create(MediaType.parse("text/plain"), hashmap.get("moimSeq")));
        textToServer.put("fileCount", RequestBody.create(MediaType.parse("text/plain"), String.valueOf(fileArrayList.size())));

        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<AlbumData> call = uploadApis.albumUpload(textToServer, filesToServer);
        call.enqueue(new Callback<AlbumData>() {
            @Override
            public void onResponse(Call<AlbumData> call, retrofit2.Response<AlbumData> response) {

                Log.i(TAG, "(albumUpload) 4. response : " + response);
                AlbumData albumData = response.body();
//                String success = albumData.getSuccess();
//                String message = albumData.getMessage();
//                String albumSeq = albumData.albumSeq;

                Gson gson = new Gson();
                String object = gson.toJson(albumData);
                Log.i(TAG, "(albumUpload) 서버에서 전달받은 object : " + object);

                showAlbumList(hashmap.get("moimSeq"));


                // AlbumFragment에 저장된 이미지를 불러와서 리사이클러뷰를 보여주는 메서드(showAlbumList) 동작 시킬 것!
            }

            @Override
            public void onFailure(Call<AlbumData> call, Throwable t) {
                Log.i(TAG, "(albumUpload) 4. onFailure : " + t);
            }
        });
    }

    private void showAlbumList(String moimSeq) {
        Log.i(TAG, "(showAlbumList) 1. 호출 - HTTP 통신, 사진첩 정보 불러오기");

        // 레트로핏 생성하여 인터페이스와 연결
        uploadApis = networkClient.getRetrofit().create(UploadApis.class);
        Log.i(TAG, "(showAlbumList) 2. HTTP통신, 서버로 보내는 값, moimSeq : " + moimSeq);
        // 1. 데이터 형태 지정 (ex. DataClass)
        // 2. uploadApis의 메서드 지정 (ex. uploadApis.signUpInfoUpload)
        Call<List<AlbumData>> call = uploadApis.showAlbumList(moimSeq);
        call.enqueue(new Callback<List<AlbumData>>() {
            @Override
            public void onResponse(Call<List<AlbumData>> call, retrofit2.Response<List<AlbumData>> response) {
                Log.i(TAG, "(showAlbumList) 3. response : " + response);
                Log.i(TAG, "(showAlbumList) 4. response.body() : " + response.body());
                if(response.body() != null){
                    albumList.clear();
                    List<AlbumData> responseBody = response.body();
                    for(int i = 0; i < responseBody.size(); i++) {
                        albumList.add(responseBody.get(i));
                    }
                    // 리사이클러뷰 값 넣기
                    mAdapter = new AlbumAdapter(albumList, getContext());
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<AlbumData>> call, Throwable t) {
                Log.i(TAG, "showImageListOnBoard 에러 = " + t);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        showAlbumList(moimSeq);

    }
}