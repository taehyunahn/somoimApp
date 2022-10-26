package MoimSearch;

import com.google.gson.annotations.SerializedName;

public class MoimData {

    @SerializedName("moimSeq")
    public String moimSeq;

    @SerializedName("address")
    public String address;

    @SerializedName("intro")
    public String intro;

    @SerializedName("interest")
    public String interest;

    @SerializedName("title")
    public String title;

    @SerializedName("content")
    public String content;

    @SerializedName("memberCount")
    public String memberCount;

    @SerializedName("memberCountMax")
    public String memberCountMax;

    @SerializedName("mainImage")
    public String mainImage;

    @SerializedName("success")
    public String success;

    @SerializedName("leaderSeq")
    public String leaderSeq;


    public MoimData(){
    }


    public String getMoimSeq() {
        return moimSeq;
    }

    public void setMoimSeq(String moimSeq) {
        this.moimSeq = moimSeq;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(String memberCount) {
        this.memberCount = memberCount;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getLeaderSeq() {
        return leaderSeq;
    }

    public void setLeaderSeq(String leaderSeq) {
        this.leaderSeq = leaderSeq;
    }

    public String getMemberCountMax() {
        return memberCountMax;
    }
}
