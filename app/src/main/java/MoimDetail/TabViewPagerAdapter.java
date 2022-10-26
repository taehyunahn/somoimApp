package MoimDetail;

import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabViewPagerAdapter extends FragmentPagerAdapter {
    private int page_count;

    // sharedPreference 세팅

    SharedPreferences sp;
    SharedPreferences.Editor editor;


    public TabViewPagerAdapter(FragmentManager fm, int pageCount, int behavior) {
        super(fm,behavior);
        this.page_count = pageCount;

    }
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                InfoFragment infoFragment = new InfoFragment();
                return infoFragment;

            case 1:
                BoardFragment boardFragment = new BoardFragment();
                return boardFragment;
            case 2:
                AlbumFragment albumFragment = new AlbumFragment();
                return albumFragment;

            case 3:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;

            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return page_count;
    }
    // 로그인한 계정이 현재 조회 중인 모임에 가입한 경우인지 확인해서
    // if문 넣을 것. 속하지 않았으면, 넘어가지 않고, Toast 메세지로 "가입하셔야 방문할 수 있습니다"

}
