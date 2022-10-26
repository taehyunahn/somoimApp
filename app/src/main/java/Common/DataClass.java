package Common;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class DataClass {

    // @SerializedName으로 일치시켜 주지않을 경우엔 클래스 변수명이 일치해야함
//    @SerializedName("userSeq")
//    public String userSeq;
//
//    @SerializedName("photoArrayList")
//    public ArrayList photoArrayList;

    @SerializedName("userSeq")
    public String userSeq;

    @SerializedName("kakaoId")
    public String kakaoId;

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    @SerializedName("phoneNumber")
    public String phoneNumber;

    @SerializedName("name")
    public String name;

    @SerializedName("gender")
    public String gender;

    @SerializedName("birthday")
    public String birthday;

    @SerializedName("address")
    public String address;

    @SerializedName("intro")
    public String intro;

    @SerializedName("profileImage")
    public String profileImage;

    @SerializedName("interest")
    public String interest;

    @SerializedName("title")
    public String title;


    @SerializedName("content")
    public String content;

    @SerializedName("memberCount")
    public String memberCount;

    @SerializedName("category")
    public String category;

    @SerializedName("mainImage")
    public String mainImage;

    @SerializedName("success")
    public String success;

    @SerializedName("moimSeq")
    public String moimSeq;

    @SerializedName("chatRoomSeq")
    public String chatRoomSeq;

    @SerializedName("message")
    public String message;

    @SerializedName("userName")
    public String userName;

    @SerializedName("currentRoom_id")
    private String currentRoom_id;

    @SerializedName("joinedMoimSeqList")
    private String joinedMoimSeqList;

    @SerializedName("joinedCount")
    private String joinedCount;

    @SerializedName("itemCount")
    private String itemCount;

    @SerializedName("recommendItemCount")
    private String recommendItemCount;

    public DataClass() {
    }

    public DataClass(String mainImage, String address, String title, String memberCount) {
        this.mainImage = mainImage;
        this.address = address;
        this.title = title;
        this.memberCount = memberCount;

    }



    public String getUserSeq() {
        return userSeq;
    }

    public void setUserSeq(String userSeq) {
        this.userSeq = userSeq;
    }

    public String getKakaoId() {
        return kakaoId;
    }

    public void setKakaoId(String kakaoId) {
        this.kakaoId = kakaoId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getMoimSeq() {
        return moimSeq;
    }

    public void setMoimSeq(String moimSeq) {
        this.moimSeq = moimSeq;
    }

    public String getChatRoomSeq() {
        return chatRoomSeq;
    }

    public void setChatRoomSeq(String chatRoomSeq) {
        this.chatRoomSeq = chatRoomSeq;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getJoinedCount() {
        return joinedCount;
    }

    public void setJoinedCount(String joinedCount) {
        this.joinedCount = joinedCount;
    }

    public String getItemCount() {
        return itemCount;
    }

    public void setItemCount(String itemCount) {
        this.itemCount = itemCount;
    }

    public String getRecommendItemCount() {
        return recommendItemCount;
    }

    public void setRecommendItemCount(String recommendItemCount) {
        this.recommendItemCount = recommendItemCount;
    }
}


