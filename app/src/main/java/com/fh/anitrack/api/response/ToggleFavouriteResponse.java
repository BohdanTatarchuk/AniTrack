package com.fh.anitrack.api.response;

import java.util.List;

public class ToggleFavouriteResponse {
    public Data data;

    public static class Data {
        public ToggleFavourite ToggleFavourite;
    }

    public static class ToggleFavourite {
        public MediaConnection anime;
        public MediaConnection manga;
    }

    public static class MediaConnection {
        public List<MediaNode> nodes;
    }

    public static class MediaNode {
        public int id;
    }
}
