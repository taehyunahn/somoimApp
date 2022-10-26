package MoimDetail.Board;

import com.google.gson.annotations.SerializedName;

public class MultiImageData {
    private String photoId;

    @SerializedName("imageUrl")
    public String imageUrl;

    public MultiImageData() {
    }

    public MultiImageData(String photoId) {
        this.photoId = photoId;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
