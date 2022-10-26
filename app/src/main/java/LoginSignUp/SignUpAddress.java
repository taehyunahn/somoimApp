package LoginSignUp;

import static Common.StaticVariable.serverAddress;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.somoim.R;

public class SignUpAddress extends AppCompatActivity {

    private WebView webView;
    String url = serverAddress + "daum2.html";
    private String result;
    private static final String TAG = "SignUpAddress";

    class MyJavaScriptInterface
    {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processDATA(String data) {

            Bundle bundle = new Bundle();
            bundle.putString("data", data);
//            //java
//
////            String userNameFromServer = jsonResponse.getString("userName");
//
//            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//            SurveyRequestF6 surveyRequestF6 = new SurveyRequestF6();
//            surveyRequestF6.setArguments(bundle);
//            transaction.replace(R.id.scrollView, surveyRequestF6);
//            transaction.commit(); // 저장

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_address);

        webView = findViewById(R.id.webView);
//        webView.addJavascriptInterface(new SignUpAddress.MyJavaScriptInterface(), "Android");
//        webView.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                webView.loadUrl("javascript:sample2_execDaumPostcode();");
//            }
//        });
//
//        webView.loadUrl(url);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new BridgeInterface(), "Android");
        webView.setWebViewClient(new WebViewClient() {
             @Override
             public void onPageFinished(WebView view, String url) {
                 webView.loadUrl("javascript:sample2_execDaumPostcode();"); //안드로이드에서 Javascript 쪽에 작동 요청
             }
         });

        //최초 웹뷰 로드드
       webView.loadUrl(url);
    }

    private class BridgeInterface {
        @JavascriptInterface
        public void processDATA(String data){
            // 다음 주소 검색 결과
            Intent intent = new Intent();
            intent.putExtra("data", data);
            setResult(RESULT_OK, intent);
            finish();

        }
    }
}