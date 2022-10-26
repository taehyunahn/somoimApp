package Chat;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class ChatData implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("userSeq")
    private String userSeq;

    @SerializedName("userName")
    private String userName;

    @SerializedName("moimSeq")
    private String moimSeq;

    @SerializedName("command")
    private String command;

    @SerializedName("userProfileImage")
    private String userProfileImage;

    @SerializedName("msg")
    private String msg;

    @SerializedName("time")
    private String time;

    @SerializedName("orderType")
    private String orderType;

    @SerializedName("senderSeq")
    private String senderSeq;

    @SerializedName("senderName")
    private String senderName;

    @SerializedName("senderProfileImage")
    private String senderProfileImage;

    @SerializedName("viewType")
    private int viewType;

    @SerializedName("date")
    private String date;

    @SerializedName("chatRoomSeq")
    private String chatRoomSeq;

    @SerializedName("myRoom_id")
    private String myRoom_id;

    @SerializedName("currentRoom_id")
    private String currentRoom_id;

    @SerializedName("joinedMoimSeqList")
    private String joinedMoimSeqList;

    @SerializedName("unreadCount")
    private String unreadCount;

    @SerializedName("albumSeq")
    private String albumSeq;

    @SerializedName("uploaderSeq")
    private String uploaderSeq;

    @SerializedName("boardSeq")
    private String boardSeq;

    @SerializedName("writerSeq")
    private String writerSeq;


    
    private String chatRoomSee;




    public ChatData() {
    }

    public String getUserSeq() {
        return userSeq;
    }

    public void setUserSeq(String userSeq) {
        this.userSeq = userSeq;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMoimSeq() {
        return moimSeq;
    }

    public void setMoimSeq(String moimSeq) {
        this.moimSeq = moimSeq;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }


    public String getSenderSeq() {
        return senderSeq;
    }

    public void setSenderSeq(String senderSeq) {
        this.senderSeq = senderSeq;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderProfileImage() {
        return senderProfileImage;
    }

    public void setSenderProfileImage(String senderProfileImage) {
        this.senderProfileImage = senderProfileImage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getChatRoomSeq() {
        return chatRoomSeq;
    }

    public void setChatRoomSeq(String chatRoomSeq) {
        this.chatRoomSeq = chatRoomSeq;
    }

    public String getMyRoom_id() {
        return myRoom_id;
    }

    public void setMyRoom_id(String myRoom_id) {
        this.myRoom_id = myRoom_id;
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

    public String getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(String unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getChatRoomSee() {
        return chatRoomSee;
    }

    public void setChatRoomSee(String chatRoomSee) {
        this.chatRoomSee = chatRoomSee;
    }

    public String getAlbumSeq() {
        return albumSeq;
    }

    public void setAlbumSeq(String albumSeq) {
        this.albumSeq = albumSeq;
    }

    public String getUploaderSeq() {
        return uploaderSeq;
    }

    public void setUploaderSeq(String uploaderSeq) {
        this.uploaderSeq = uploaderSeq;
    }

    public String getBoardSeq() {
        return boardSeq;
    }

    public void setBoardSeq(String boardSeq) {
        this.boardSeq = boardSeq;
    }

    public String getWriterSeq() {
        return writerSeq;
    }

    public void setWriterSeq(String writerSeq) {
        this.writerSeq = writerSeq;
    }
}
