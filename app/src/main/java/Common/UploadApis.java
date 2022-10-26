package Common;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import BoardComment.CommentData;
import Chat.ChatData;
import Meetup.MeetupData;
import Member.MemberData;
import MoimDetail.Board.BoardData;
import MoimDetail.Photo.AlbumData;
import MoimSearch.MoimData;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface UploadApis {

    @FormUrlEncoded
    @POST("expertPhotoTest.php")
    Call<DataClass> getUserLogin(
            @Field("name") String name,
            @Field("image") String image
    );

    @FormUrlEncoded
    @POST("expertPhotoTest.php")
    Call<DataClass> sendPhoto(
            @Field("imageNameList") ArrayList<String> imageNameList
    );

    @Multipart
    @POST("uploadmultipleimages")
    Call<ResponseBody> uploadMultiImage(@Part MultipartBody.Part file1, @Part MultipartBody.Part file2);

    @Multipart // is specifying to retrofit to use multipart
    @POST("expertPhotoTest.php") //is giving the file name of the web API
    Call<String> uploadImage( // this method has two parameters. MultipartBody.Part will contain the image
            @Part MultipartBody.Part file,
            @Part("filename") RequestBody name
    );





//    @Multipart // is specifying to retrofit to use multipart
//    @POST("httpPractice/photoUpload.php") //is giving the file name of the web API
//    Call<DataClass> uploadImage(@Part("userSeq") RequestBody userSeq,
//                                @Part("expertSeq") RequestBody expertSeq,
//                                @PartMap Map<String, RequestBody> params,
//                                @Part List<MultipartBody.Part> files
//    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/myProfile/profileInfoUpdate.php") //is giving the file name of the web API
    Call<DataClass> profileInfoUpdate(@PartMap Map<String, RequestBody> params,
                                     @Part List<MultipartBody.Part> files
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/signUp/signUpInfoUpload.php") //is giving the file name of the web API
    Call<DataClass> signUpInfoUpload(@PartMap Map<String, RequestBody> params,
                                     @Part List<MultipartBody.Part> files
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/board/boardCreate.php") //is giving the file name of the web API
    Call<BoardData> boardCreate(@PartMap Map<String, RequestBody> params,
                                @Part List<MultipartBody.Part> files
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/board/boardUpdate.php") //is giving the file name of the web API
    Call<BoardData> boardUpdate(@PartMap Map<String, RequestBody> params,
                                @Part List<MultipartBody.Part> files
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/album/albumUpload.php") //is giving the file name of the web API
    Call<AlbumData> albumUpload(@PartMap Map<String, RequestBody> params,
                                @Part List<MultipartBody.Part> files
    );


    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/moimManage/showMoimInfo.php") //is giving the file name of the web API
    Call<DataClass> showMoimInfo(@PartMap Map<String, RequestBody> params,
                                   @Part List<MultipartBody.Part> files
    );


    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/moimManage/updateMoimInfo.php") //is giving the file name of the web API
    Call<DataClass> updateMoimInfo(@PartMap Map<String, RequestBody> params,
                                     @Part List<MultipartBody.Part> files
    );


    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/moimManage/requestMoimList.php") //is giving the file name of the web API
    Call<List<DataClass>> requestMoimList(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/moimManage/requestMoimListSearch.php") //is giving the file name of the web API
    Call<List<DataClass>> requestMoimListSearch(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/moimManage/joinedMoimList.php") //is giving the file name of the web API
    Call<List<DataClass>> joinedMoimList(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/moimManage/joinedMoimListPaging.php") //is giving the file name of the web API
    Call<List<DataClass>> joinedMoimListPaging(@PartMap Map<String, RequestBody> params
    );


    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/moimManage/recommendedMoimList.php") //is giving the file name of the web API
    Call<List<DataClass>> recommendedMoimList(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/moimManage/recommendedMoimListPaging.php") //is giving the file name of the web API
    Call<List<DataClass>> recommendedMoimListPaging(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/moimManage/createdMoimList.php") //is giving the file name of the web API
    Call<List<DataClass>> createdMoimList(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/moimManage/createdMoimListPaging.php") //is giving the file name of the web API
    Call<List<DataClass>> createdMoimListPaging(@PartMap Map<String, RequestBody> params
    );



    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/moimManage/createMoim.php") //is giving the file name of the web API
    Call<DataClass> createMoim(@PartMap Map<String, RequestBody> params,
                                     @Part List<MultipartBody.Part> files
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/meetup/createMeetup.php") //is giving the file name of the web API
    Call<MeetupData> createMeetup(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/meetup/updateMeetup.php") //is giving the file name of the web API
    Call<MeetupData> updateMeetup(@PartMap Map<String, RequestBody> params
    );


    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/album/createCommentAlbum.php") //is giving the file name of the web API
    Call<CommentData> createCommentAlbum(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/board/createCommentBoard.php") //is giving the file name of the web API
    Call<CommentData> createCommentBoard(@PartMap Map<String, RequestBody> params
    );


    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/board/boardLikeAdd.php") //is giving the file name of the web API
    Call<CommentData> boardLikeAdd(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/album/likeAlbumAdd.php") //is giving the file name of the web API
    Call<CommentData> likeAlbumAdd(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/comment/likeCommentAdd.php") //is giving the file name of the web API
    Call<CommentData> likeCommentAdd(@PartMap Map<String, RequestBody> params
    );


    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/comment/updateComment.php") //is giving the file name of the web API
    Call<CommentData> updateComment(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/comment/deleteComment.php") //is giving the file name of the web API
    Call<CommentData> deleteComment(@PartMap Map<String, RequestBody> params
    );





    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/board/boardLikeCancel.php") //is giving the file name of the web API
    Call<CommentData> boardLikeCancel(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/album/likeAlbumCancel.php") //is giving the file name of the web API
    Call<CommentData> likeAlbumCancel(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/board/boardDelete.php") //is giving the file name of the web API
    Call<CommentData> boardDelete(@PartMap Map<String, RequestBody> params
    );

    @Multipart // is specifying to retrofit to use multipart
    @POST("somoim/album/deleteAlbum.php") //is giving the file name of the web API
    Call<CommentData> deleteAlbum(@PartMap Map<String, RequestBody> params
    );



    @FormUrlEncoded
    @POST("httpPractice/photoListShow.php") //is giving the file name of the web API
    Call<List<DataClass>> photoListShow(@Field("userSeq") String userSeq,
                                        @Field("expertSeq") String expertSeq
    );


    @FormUrlEncoded
    @POST("httpPractice/removePhoto.php") //is giving the file name of the web API
    Call<DataClass> removePhoto(@Field("photoPath") String photoPath
    );


    @FormUrlEncoded
    @POST("somoim/myProfile/receiveProfileInfoWithRetrofit.php")
    Call<DataClass> receiveProfileInfoWithRetrofit(@Field("userSeq") String userSeq);

    @FormUrlEncoded
    @POST("somoim/moimManage/showMoimInfo.php")
    Call<MoimData> showMoimInfo(@Field("moimSeq") String moimSeq);


    @FormUrlEncoded
    @POST("somoim/board/showBoardInfo.php")
    Call<BoardData> showBoardInfo(@Field("boardSeq") String boardSeq,
                                  @Field("userSeq")   String userSeq);


    @FormUrlEncoded
    @POST("somoim/album/showAlbumInfo.php")
    Call<AlbumData> showAlbumInfo(@Field("albumSeq") String albumSeq,
                                  @Field("userSeq") String userSeq);


    @FormUrlEncoded
    @POST("somoim/member/joinMoim.php")
    Call<MemberData> joinMoim(@Field("moimSeq") String moimSeq,
                                @Field("userSeq") String userSeq);


    @FormUrlEncoded
    @POST("somoim/member/outMoim.php")
    Call<MemberData> outMoim(@Field("moimSeq") String moimSeq,
                             @Field("userSeq") String userSeq);



    @FormUrlEncoded
    @POST("somoim/meetup/joinMeetup.php")
    Call<MemberData> joinMeetup(@Field("moimSeq") String moimSeq,
                                @Field("userSeq") String userSeq,
                                @Field("meetupSeq") String meetupSeq);


    @FormUrlEncoded
    @POST("somoim/meetup/outMeetup.php")
    Call<MemberData> outMeetup(@Field("moimSeq") String moimSeq,
                               @Field("userSeq") String userSeq,
                               @Field("meetupSeq") String meetupSeq);

    @FormUrlEncoded
    @POST("somoim/meetup/memberCheck.php")
    Call<MemberData> memberCheck(@Field("moimSeq") String moimSeq,
                               @Field("userSeq") String userSeq,
                               @Field("meetupSeq") String meetupSeq);


    @FormUrlEncoded
    @POST("somoim/meetup/showMeetupList.php")
    Call<List<MeetupData>> showMeetupList(@Field("moimSeq") String moimSeq,
                                          @Field("userSeq") String userSeq);

    @FormUrlEncoded
    @POST("somoim/chat/showChatMsgListPaging.php")
    Call<List<ChatData>> showChatMsgListPaging(@Field("moimSeq") String moimSeq,
                                             @Field("userSeq") String userSeq,
                                             @Field("page") int page,
                                             @Field("int") int limit);

    @FormUrlEncoded
    @POST("somoim/chat/showChatMsgList.php")
    Call<List<ChatData>> showChatMsgList(@Field("moimSeq") String moimSeq,
                                         @Field("userSeq") String userSeq);


    @FormUrlEncoded
    @POST("somoim/board/showBoardList.php")
    Call<List<BoardData>> showBoardList(@Field("moimSeq") String moimSeq,
                                        @Field("userSeq") String userSeq);

    @FormUrlEncoded
    @POST("somoim/board/showPhotoList.php")
    Call<List<AlbumData>> showPhotoList(@Field("moimSeq") String moimSeq,
                                        @Field("userSeq") String userSeq);



    @FormUrlEncoded
    @POST("somoim/board/showImageListOnBoard.php")
    Call<List<BoardData>> showImageListOnBoard(@Field("boardSeq") String boardSeq);

    @FormUrlEncoded
    @POST("somoim/album/showAlbumList.php")
    Call<List<AlbumData>> showAlbumList(@Field("moimSeq") String moimSeq);



    @FormUrlEncoded
    @POST("somoim/comment/showCommentList.php")
    Call<List<CommentData>> showCommentList(@Field("boardSeq") String boardSeq,
                                            @Field("userSeq") String userSeq);

    @FormUrlEncoded
    @POST("somoim/album/showCommentListAlbum.php")
    Call<List<CommentData>> showCommentListAlbum(@Field("albumSeq") String albumSeq);

//    @FormUrlEncoded
//    @POST("somoim/album/showCommentListAlbum.php")
//    Call<List<Object>> showCommentListAlbum(@Field("albumSeq") String albumSeq);


    @FormUrlEncoded
    @POST("somoim/member/showMemberList.php")
    Call<List<MemberData>> showMemberList(@Field("moimSeq") String moimSeq);


    @FormUrlEncoded
    @POST("somoim/meetup/meetupMemberList.php")
    Call<List<MemberData>> meetupMemberList(@Field("moimSeq") String moimSeq,
                                            @Field("meetupSeq") String meetupSeq);


//    @FormUrlEncoded
//    @POST("expertUpdatePhotoList.php")
//    Call<List<Photo>> getAllPhoto(@Field("userSeq") String userSeq);




    @Multipart // is specifying to retrofit to use multipart
    @POST("fragment24/switchSearch.php") //is giving the file name of the web API
    Call<DataClass> switchSearchCheck(
                                @PartMap Map<String, RequestBody> params
    );



//
//    // '고수찾기'에서 고수 리사이클러뷰 만들기 위한 요청
//    @FormUrlEncoded
//    @POST("expertListUpdated.php") //is giving the file name of the web API
//    Call<List<ExpertData>> showExpertList(@Field("userSeq") String userSeq,
//                                          @Field("page") String page,
//                                          @Field("limit") String limit,
//                                          @Field("service") String service,
//                                          @Field("address") String address
//
//    );



}
