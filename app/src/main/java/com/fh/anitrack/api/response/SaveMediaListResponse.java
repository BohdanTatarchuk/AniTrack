package com.fh.anitrack.api.response;

public class SaveMediaListResponse {
    public Data data;
    
    public static class Data {
        public SaveMediaListEntry SaveMediaListEntry;
    }
    
    public static class SaveMediaListEntry {
        public int id;
        public int mediaId;
        public String status;
        public float score;
        public int progress;
        public int progressVolumes;
        public int repeat;
        public boolean _private;
        public String notes;
        public FuzzyDate startedAt;
        public FuzzyDate completedAt;
    }
    
    public static class FuzzyDate {
        public Integer year;
        public Integer month;
        public Integer day;
    }
}
