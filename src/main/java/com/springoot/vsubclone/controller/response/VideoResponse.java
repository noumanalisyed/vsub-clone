package com.springoot.vsubclone.controller.response;

public class VideoResponse {
    private String videoPath;

    public VideoResponse(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }
}