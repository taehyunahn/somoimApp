package MoimDetail.Photo;

import com.google.gson.annotations.SerializedName;

public class AlbumData {

    @SerializedName("name")
    public String name;

    @SerializedName("status")
    public String status;

    @SerializedName("title")
    public String title;

    @SerializedName("content")
    public String content;

    @SerializedName("date")
    public String date;

    @SerializedName("likeCount")
    public String likeCount;

    @SerializedName("commentCount")
    public String commentCount;

    @SerializedName("boardCategory")
    public String boardCategory;

    @SerializedName("profileImage")
    public String profileImage;

    @SerializedName("mainImage")
    public String mainImage;

    @SerializedName("likeIcon")
    public String likeIcon;

    @SerializedName("comment")
    public String comment;

    @SerializedName("writerSeq")
    public String writerSeq;

    @SerializedName("moimSeq")
    public String moimSeq;

    @SerializedName("boardSeq")
    public String boardSeq;

    @SerializedName("imageUrl")
    public String imageUrl;


    @SerializedName("success")
    public String success;

    @SerializedName("message")
    public String message;

    @SerializedName("albumSeq")
    public String albumSeq;

    @SerializedName("uploaderSeq")
    public String uploaderSeq;

    @SerializedName("likeClicked")
    public String likeClicked;









    public AlbumData(){
    }

    public AlbumData(String name, String title) {
        this.name = name;
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public String getBoardCategory() {
        return boardCategory;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getMainImage() {
        return mainImage;
    }

    public String getLikeIcon() {
        return likeIcon;
    }

    public String getComment() {
        return comment;
    }

    public String getWriterSeq() {
        return writerSeq;
    }

    public String getMoimSeq() {
        return moimSeq;
    }

    public String getBoardSeq() {
        return boardSeq;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getAlbumSeq() {
        return albumSeq;
    }

    public String getUploaderSeq() {
        return uploaderSeq;
    }

    public String getLikeClicked() {
        return likeClicked;
    }
}
