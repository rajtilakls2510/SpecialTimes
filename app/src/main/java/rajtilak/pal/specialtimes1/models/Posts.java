package rajtilak.pal.specialtimes1.models;

import java.util.ArrayList;

public class Posts {

    private String userId,postDescription,postImageUrl,postId;
    private long postDate;
    private int privacy;
    private ArrayList<String> liked;


    public Posts()
    {

    }

    public int getPrivacy() {
        return privacy;
    }

    public void setPrivacy(int privacy) {
        this.privacy = privacy;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public long getPostDate() {
        return postDate;
    }

    public void setPostDate(long postDate) {
        this.postDate = postDate;
    }


    public ArrayList<String> getLiked() {
        return liked;
    }

    public void setLiked(ArrayList<String> liked) {
        this.liked = liked;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
