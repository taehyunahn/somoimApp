package BoardComment;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CommentData {

    private List<SubItem> subItemList;

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


    @SerializedName("uploaderSeq")
    public String uploaderSeq;

    @SerializedName("moimSeq")
    public String moimSeq;

    @SerializedName("writerSeq")
    public String writerSeq;


    public CommentData() {

    }

    public List<SubItem> getSubItemList() {
        return subItemList;
    }

    public void setSubItemList(List<SubItem> subItemList) {
        this.subItemList = subItemList;
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

    public String getUploaderSeq() {
        return uploaderSeq;
    }

    public String getMoimSeq() {
        return moimSeq;
    }

    public String getWriterSeq() {
        return writerSeq;
    }
}
