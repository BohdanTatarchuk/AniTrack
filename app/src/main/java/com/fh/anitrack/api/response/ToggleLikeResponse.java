package com.fh.anitrack.api.response;

public class ToggleLikeResponse {
    public Data data;
    public static class Data {
        public ToggleLikeV2 ToggleLikeV2;
    }
    public static class ToggleLikeV2 {
        public int id;
        public boolean isLiked;
        public int likeCount;
    }
}