package Member;

import com.google.gson.annotations.SerializedName;

public class MemberData {

    @SerializedName("meetupTitle")
    public String meetupTitle;

    @SerializedName("address")
    public String address;

    @SerializedName("fee")
    public String fee;

    @SerializedName("memberCount")
    public String memberCount;

    @SerializedName("meetupDate")
    public String meetupDate;

    @SerializedName("meetupTime")
    public String meetupTime;

    @SerializedName("userSeq")
    public String userSeq;

    @SerializedName("moimSeq")
    public String moimSeq;

    @SerializedName("success")
    public String success;

    @SerializedName("meetupSeq")
    public String meetupSeq;

    @SerializedName("userProfileImage")
    public String userProfileImage;

    @SerializedName("userName")
    public String userName;

    @SerializedName("userIntro")
    public String userIntro;

    @SerializedName("userStatus")
    public String userStatus;

    @SerializedName("currentRoom_id")
    public String currentRoom_id;

    @SerializedName("joinedMoimSeqList")
    public String joinedMoimSeqList;


    @SerializedName("leaderSeq")
    public String leaderSeq;


    public MemberData() {

    }

    public String getMeetupTitle() {
        return meetupTitle;
    }

    public String getAddress() {
        return address;
    }

    public String getFee() {
        return fee;
    }

    public String getMemberCount() {
        return memberCount;
    }

    public String getMeetupDate() {
        return meetupDate;
    }

    public String getMeetupTime() {
        return meetupTime;
    }

    public String getUserSeq() {
        return userSeq;
    }

    public String getMoimSeq() {
        return moimSeq;
    }

    public String getSuccess() {
        return success;
    }

    public String getMeetupSeq() {
        return meetupSeq;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserIntro() {
        return userIntro;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setMeetupTitle(String meetupTitle) {
        this.meetupTitle = meetupTitle;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public void setMemberCount(String memberCount) {
        this.memberCount = memberCount;
    }

    public void setMeetupDate(String meetupDate) {
        this.meetupDate = meetupDate;
    }

    public void setMeetupTime(String meetupTime) {
        this.meetupTime = meetupTime;
    }

    public void setUserSeq(String userSeq) {
        this.userSeq = userSeq;
    }

    public void setMoimSeq(String moimSeq) {
        this.moimSeq = moimSeq;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public void setMeetupSeq(String meetupSeq) {
        this.meetupSeq = meetupSeq;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserIntro(String userIntro) {
        this.userIntro = userIntro;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getCurrentRoom_id() {
        return currentRoom_id;
    }

    public void setCurrentRoom_id(String currentRoom_id) {
        this.currentRoom_id = currentRoom_id;
    }

    public String getJoinedMoimSeqList() {
        return joinedMoimSeqList;
    }

    public void setJoinedMoimSeqList(String joinedMoimSeqList) {
        this.joinedMoimSeqList = joinedMoimSeqList;
    }

    public String getLeaderSeq() {
        return leaderSeq;
    }

    public void setLeaderSeq(String leaderSeq) {
        this.leaderSeq = leaderSeq;
    }
}
