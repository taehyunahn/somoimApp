package MoimDetail.Photo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.somoim.R;

import java.util.List;

import MoimDetail.Board.BoardView;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    List<AlbumData> albumList;
    Context context;
    private static final String TAG = "PhotoAdapter";

    public AlbumAdapter(List<AlbumData> albumList, Context context) {
        this.albumList = albumList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_big_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        // 업로드된 사진
        if(albumList.get(position).getImageUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(albumList.get(position).getImageUrl())
                    .centerCrop()
                    .into(holder.iv_photo);
        }

        //아이템 전체 레이아웃 클릭시
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AlbumView.class);
                intent.putExtra("albumSeq", albumList.get(position).getAlbumSeq());
                Log.i(TAG, "albumSeq = " + albumList.get(position).getAlbumSeq());
                context.startActivity(intent);
                Log.i(TAG, "사진 상세히 보기로 이동합니다");
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //viewHolder의 view 안에 글자가 표시되도록 textView 2개를 추가되도록 할 것이다
        CardView parentLayout;
        ImageView iv_photo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parentLayout = itemView.findViewById(R.id.parentLayout); //
            iv_photo = itemView.findViewById(R.id.iv_photo); //
        }
    }
}


