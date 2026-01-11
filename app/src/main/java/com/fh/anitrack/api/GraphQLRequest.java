package com.fh.anitrack.api;

import java.util.Map;

//A helper class to format the queries exactly how anilist api requires it
public class GraphQLRequest {
    //Examples of the queries are located in the class AniListQueries and can be tested here:
    //https://studio.apollographql.com/sandbox/explorer?endpoint=https://graphql.anilist.co
    private String query;
    //Query variables are stored in this map
    private Map<String, Object> variables;

    public GraphQLRequest(String query, Map<String, Object> variables) {
        this.query = query;
        this.variables = variables;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}