package com.example.social.model;

public class PostModel extends PostID {
    String postId,user,title,description,image,postedTime, postLike,postComment;

    public PostModel(){

    }

    public PostModel(String postId, String user, String title, String description, String image, String postedTime, String postLike, String postComment) {
        this.postId = postId;
        this.user = user;
        this.title = title;
        this.description = description;
        this.image = image;
        this.postedTime = postedTime;
        this.postLike = postLike;
        this.postComment = postComment;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPostedTime() {
        return postedTime;
    }

    public void setPostedTime(String postedTime) {
        this.postedTime = postedTime;
    }

    public String getPostLike() {
        return postLike;
    }

    public void setPostLike(String postLike) {
        this.postLike = postLike;
    }

    public String getPostComment() {
        return postComment;
    }

    public void setPostComment(String postComment) {
        this.postComment = postComment;
    }
}
