package LoginSignUp;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;


public class KakaoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        KakaoSdk.init(this, "8ad22ffd52d53fbc1685b6533e4d39d2");
    }
}
