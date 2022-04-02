package com.example.social.model;

public class CommentModel {

    String cid, comment,timestamp,uname,school;

    public CommentModel(){

    }

    public CommentModel(String cid, String comment, String timestamp, String uname, String school) {
        this.cid = cid;
        this.comment = comment;
        this.timestamp = timestamp;
        this.uname = uname;
        this.school = school;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }
}
