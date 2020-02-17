package com.clocktower.lullaby.model;

public class CozaBlog {

    private int likeCount = 0;
    private int commentCount = 0;
    private boolean isLiked = false;
    private Post post;

    public CozaBlog(Post post) {
        this.post = post;
    }

    public Post getPost() {
        return post;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}
