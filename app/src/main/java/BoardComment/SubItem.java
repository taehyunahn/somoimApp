package BoardComment;

import com.google.gson.annotations.SerializedName;

public class SubItem {

    @SerializedName("commentSeq")
    public String commentSeq;

    @SerializedName("message")
    public String message;

    @SerializedName("success")
    public String success;

    @SerializedName("userStatus")
    public String userStatus;

    @SerializedName("commentInput")
    public String commentInput;

    @SerializedName("userSeq")
    public String userSeq;

    @SerializedName("boardSeq")
    public String boardSeq;

    @SerializedName("albumSeq")
    public String albumSeq;

    //프로필이미지
    @SerializedName("userProfileImage")
    public String userProfileImage;
    //작성자 이름
    @SerializedName("userName")
    public String userName;
    //댓글작성일자
    @SerializedName("commentDate")
    public String commentDate;
    //댓글합계
    @SerializedName("commentCount")
    public String commentCount;
    //좋아요합계
    @SerializedName("likeCount")
    public String likeCount;

    @SerializedName("comment")
    public String comment;



    public SubItem() {

    }

    public String getCommentSeq() {
        return commentSeq;
    }

    public String getMessage() {
        return message;
    }

    public String getSuccess() {
        return success;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public String getCommentInput() {
        return commentInput;
    }

    public String getUserSeq() {
        return userSeq;
    }

    public String getBoardSeq() {
        return boardSeq;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public String getUserName() {
        return userName;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public String getComment() {
        return comment;
    }

    public String getAlbumSeq() {
        return albumSeq;
    }
}
