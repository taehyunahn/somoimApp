package MoimDetail.Board;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.somoim.R;

import java.util.List;

public class BoardImageAdapter extends RecyclerView.Adapter<BoardImageAdapter.ViewHolder> {

    List<BoardData> boardDataList;
    Context context;
    private static final String TAG = "BoardAdapter";

    public BoardImageAdapter(List<BoardData> boardDataList, Context context) {
        this.boardDataList = boardDataList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_item_image, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        String imageUrl = boardDataList.get(position).getImageUrl();
        Log.i(TAG, "imageUrl = " + imageUrl);
        if(imageUrl != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .into(holder.imageView);
        }



    }

    @Override
    public int getItemCount() {
        return boardDataList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView); //

        }

    }
}


