package Chat;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ChatInterface {


    @FormUrlEncoded
    @POST("chat/getChattingMsgList.php") //is giving the file name of the web API
    Call<List<ChatData>> get_chatting_msg_list(@Field("userSeq") String userSeq,
                                               @Field("chat_room_seq") String chat_room_seq,
                                               @Field("clientOrExpert") String clientOrExpert

    );

    @FormUrlEncoded
    @POST("chat/getChattingMsgListPaging.php") //is giving the file name of the web API
    Call<List<ChatData>> get_chatting_msg_list_paging(@Field("userSeq") String userSeq,
                                                      @Field("chat_room_seq") String chat_room_seq,
                                                      @Field("page") Integer page
    );
}
