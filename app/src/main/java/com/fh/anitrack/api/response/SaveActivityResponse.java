package com.fh.anitrack.api.response;

public class SaveActivityResponse {
    public Data data;
    public static class Data {
        public SaveTextActivity SaveTextActivity;
    }
    public static class SaveTextActivity {
        public int id;
        public String text;
    }
}