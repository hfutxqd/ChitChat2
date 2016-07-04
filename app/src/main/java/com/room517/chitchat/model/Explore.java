package com.room517.chitchat.model;


import java.io.Serializable;

/**
 * Created by imxqd on 2016/6/8.
 * 朋友圈动态Model
 */
public class Explore implements Serializable {
    String id, nickname, device_id, time;
    Content content;
    int comment_count, like, color;
    boolean isLiked;

    public Explore(String id, String nickname, String device_id, String time, Content content
            , int comment_count, int like, int color, boolean isLiked) {
        this.id = id;
        this.nickname = nickname;
        this.device_id = device_id;
        this.time = time;
        this.content = content;
        this.comment_count = comment_count;
        this.like = like;
        this.color = color;
        this.isLiked = isLiked;
    }

    public Explore() {
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getComment_count() {
        return comment_count;
    }

    public void setComment_count(int comment_count) {
        this.comment_count = comment_count;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public static class Content implements Serializable {
        String text;
        String[] images;
        Location location;

        public Content(String text, String[] images) {
            this.text = text;
            this.images = images;
            location = new Location();
        }

        public Content() {
            location = new Location();
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String[] getImages() {
            return images;
        }

        public void setImages(String[] images) {
            this.images = images;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }

    public static class Location implements Serializable{
        double longitude = 0, latitude = 0;
        String addrName = "";

        public Location(double longitude, double latitude, String addrName) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.addrName = addrName;
        }

        public Location() {
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public String getAddrName() {
            return addrName;
        }

        public void setAddrName(String addrName) {
            this.addrName = addrName;
        }
    }
}
