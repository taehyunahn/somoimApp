package LoginSignUp;

import static Common.StaticVariable.serverAddress;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ValidateRequest extends StringRequest {
    //서버 url 설정(php파일 연동)
    final static  private String URL= serverAddress + "somoim/signUp/validateEmail.php";
//    String url = serverAddress + "somoim/signUp/sendSms.php";

    private Map<String,String> map;

    public ValidateRequest(String userEmail, Response.Listener<String>listener){
        super(Method.POST,URL,listener,null);

        map=new HashMap<>();
        map.put("userEmail",userEmail);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
