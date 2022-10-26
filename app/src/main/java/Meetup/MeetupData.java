package Meetup;

import com.google.gson.annotations.SerializedName;

public class MeetupData {

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

    public MeetupData() {

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
}
