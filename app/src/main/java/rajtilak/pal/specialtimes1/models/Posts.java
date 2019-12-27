package rajtilak.pal.specialtimes1.models;

public class Posts {

    private String userId,postDescription,postImageUrl;
    private long postDate;
    private int privacy;


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
}
