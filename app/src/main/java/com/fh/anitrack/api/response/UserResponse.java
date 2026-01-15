package com.fh.anitrack.api.response;

import com.google.gson.annotations.SerializedName;

//this class describes how does the response to getCurrentUser() api call looks. Is used in the navigation sidebar
public class UserResponse {
    public Data data;

    public static class Data {
        @SerializedName("Viewer")
        public Viewer viewer;
    }

    public static class Viewer {
        public int id;
        public String name;
        public Avatar avatar;
    }

    public static class Avatar {
        public String large;
    }
}