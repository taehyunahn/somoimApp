package Common;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StaticVariable {

    public static String serverAddress = "http://3.39.194.226/";
    public static final String tagClientToServer = "서버로 전송보내는 값 : ";
    public static final String tagServerToClient = "서버에서 전달받은 값 : ";
    public static final String tagSharedPreferenceInput = "sharedPreference에 저장한 값 : ";
    public static final String tagSharedPreferenceOutput = "sharedPreference에서 꺼낸 값 : ";
    public static final String tagIntentInput = "intent로 보내는 값 : ";
    public static final String tagIntentOutput = "intent로 전달 받은 값 : ";


    // 절대경로 파악할 때 사용된 메소드
    @Nullable
    public static String createCopyAndReturnRealPath(@NonNull Context context, @NonNull Uri uri) {
        final ContentResolver contentResolver = context.getContentResolver();

        if (contentResolver == null)
            return null;

        // 파일 경로를 만듬
        String filePath = context.getApplicationInfo().dataDir + File.separator
                + System.currentTimeMillis();

        File file = new File(filePath);
        try {
            // 매개변수로 받은 uri 를 통해  이미지에 필요한 데이터를 불러 들인다.
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null)
                return null;
            // 이미지 데이터를 다시 내보내면서 file 객체에  만들었던 경로를 이용한다.

            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0)
                outputStream.write(buf, 0, len);
            outputStream.close();
            inputStream.close();

        } catch (IOException ignore) {
            return null;
        }

        return file.getAbsolutePath();
    }


}
