package MoimCreate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.somoim.R;

import java.util.ArrayList;
import java.util.List;

public class AddressListUpdate extends AppCompatActivity {
    private List<String> list;          // 데이터를 넣은 리스트변수
    private ListView listView;          // 검색을 보여줄 리스트변수
    private EditText et_search;        // 검색어를 입력할 Input 창
    private SearchAdapterUpdate adapter;      // 리스트뷰에 연결할 아답터
    private ArrayList<String> arraylist;
    private ImageView iv_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list_update);


        et_search = (EditText) findViewById(R.id.et_search); // 검색어 입력창
        listView = (ListView) findViewById(R.id.listView); // 검색결과 나오는 리스트뷰

        // 리스트를 생성한다. (데이터를 넣을 곳)
        list = new ArrayList<String>();

        // 검색에 사용할 데이터을 미리 저장한다.
        settingList();

        // 리스트의 모든 데이터를 arraylist에 복사한다.// list 복사본을 만든다.
        arraylist = new ArrayList<String>();
        arraylist.addAll(list);

        // 리스트에 연동될 아답터를 생성한다.
        adapter = new SearchAdapterUpdate(list, this);

        // 리스트뷰에 아답터를 연결한다.
        listView.setAdapter(adapter);

        // input창에 검색어를 입력시 "addTextChangedListener" 이벤트 리스너를 정의한다.
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // input창에 문자를 입력할때마다 호출된다.
                // search 메소드를 호출한다.
                String text = et_search.getText().toString();
                search(text);
            }
        });



    }
    // 검색을 수행하는 메소드
    public void search(String charText) {

        // 문자 입력시마다 리스트를 지우고 새로 뿌려준다.
        list.clear();

        // 문자 입력이 없을때는 모든 데이터를 보여준다.
        if (charText.length() == 0) {
            list.addAll(arraylist);

        }
        // 문자 입력을 할때..
        else
        {
            // 리스트의 모든 데이터를 검색한다.
            for(int i = 0;i < arraylist.size(); i++)
            {
                // arraylist의 모든 데이터에 입력받은 단어(charText)가 포함되어 있으면 true를 반환한다.
                if (arraylist.get(i).toLowerCase().contains(charText))
                {
                    // 검색된 데이터를 리스트에 추가한다.
                    list.add(arraylist.get(i));
                }
            }
        }
        // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
        adapter.notifyDataSetChanged();
    }

    // 검색에 사용될 데이터를 리스트에 추가한다.
    private void settingList(){
        list.add("서울특별시 종로구");
        list.add("서울특별시 중구");
        list.add("서울특별시 용산구");
        list.add("서울특별시 성동구");
        list.add("서울특별시 광진구");
        list.add("서울특별시 동대문구");
        list.add("서울특별시 중랑구");
        list.add("서울특별시 성북구");
        list.add("서울특별시 강북구");
        list.add("서울특별시 도봉구");
        list.add("서울특별시 노원구");
        list.add("서울특별시 은평구");
        list.add("서울특별시 서대문구");
        list.add("서울특별시 마포구");
        list.add("서울특별시 양천구");
        list.add("서울특별시 강서구");
        list.add("서울특별시 구로구");
        list.add("서울특별시 금천구");
        list.add("서울특별시 영등포구");
        list.add("서울특별시 동작구");
        list.add("서울특별시 관악구");
        list.add("서울특별시 서초구");
        list.add("서울특별시 강남구");
        list.add("서울특별시 송파구");
        list.add("서울특별시 강동구");
        list.add("부산광역시 중구");
        list.add("부산광역시 서구");
        list.add("부산광역시 동구");
        list.add("부산광역시 영도구");
        list.add("부산광역시 부산진구");
        list.add("부산광역시 동래구");
        list.add("부산광역시 남구");
        list.add("부산광역시 북구");
        list.add("부산광역시 강서구");
        list.add("부산광역시 해운대구");
        list.add("부산광역시 사하구");
        list.add("부산광역시 금정구");
        list.add("부산광역시 연제구");
        list.add("부산광역시 수영구");
        list.add("부산광역시 사상구");
        list.add("부산광역시 기장군");



    }

}