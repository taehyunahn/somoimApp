package MoimCreate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.somoim.KakaoLoginActivity;
import com.example.somoim.R;

import java.util.List;

import MoimDetail.MoimDetailMain;

public class SearchAdapter extends BaseAdapter {

    private Context context;
    private List<String> list;
    private LayoutInflater inflate;
    private ViewHolder viewHolder;

    // sharedPreference 세팅

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    SharedPreferences spSignUp;
    SharedPreferences.Editor editorSignUp;


    public SearchAdapter(List<String> list, Context context){
        this.list = list;
        this.context = context;
        this.inflate = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if(convertView == null){
            convertView = inflate.inflate(R.layout.row_listview,null);

            viewHolder = new ViewHolder();
            viewHolder.label = (TextView) convertView.findViewById(R.id.label);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }


        sp = context.getSharedPreferences("login", Activity.MODE_PRIVATE);
        editor = sp.edit();

        spSignUp = context.getSharedPreferences("signUp", Activity.MODE_PRIVATE);
        editorSignUp = spSignUp.edit();

        // 리스트에 있는 데이터를 리스트뷰 셀에 뿌린다.
        viewHolder.label.setText(list.get(position));
        viewHolder.label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SearchAdapter", "SearchAdapter에서 아이템을 클릭했습니다");
                editor.putString("selectedAddress", list.get(position)); // 선택한 주소를 s.f 저장
                editor.commit();

                editorSignUp.putString("selectedAddress", list.get(position)); // 선택한 주소를 s.f 저장
                editorSignUp.commit();

                ((Activity)context).finish();






            }
        });

        return convertView;
    }

    class ViewHolder{
        public TextView label;
    }

}
