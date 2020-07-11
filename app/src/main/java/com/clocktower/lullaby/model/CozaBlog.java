package com.clocktower.lullaby.model;

import java.util.Comparator;

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

    /*Comparator for sorting the list by roll no*/
    public static Comparator<CozaBlog> blogPostComparator = (cozaBlog, cozaBlog1) -> {
        long time1 = cozaBlog.getPost().getTimeStamp().getTime();
        long time2 = cozaBlog1.getPost().getTimeStamp().getTime();
        if (time1>time2) return 1;
        else if (time1<time2)return -1;
        else return 0;
    };
}
