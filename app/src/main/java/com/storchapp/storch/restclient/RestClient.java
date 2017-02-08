package com.storchapp.storch.restclient;

import android.net.Uri;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class RestClient {
    public static final String TAG = "RestClient";

    private String queryString; //query with + in between words
    private String query; //raw query string
    private String rawUrl; //raw url string
    private URL queryUrl; //built url object

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQueryString() {
        return queryString;
    }

    //set after cleansing raw query
    public void setQueryString() {
        this.queryString = query.replaceAll("([^a-zA-Z0-9])", "").replaceAll(" ", "+");
    }

    public String getRawUrl() {
        return rawUrl;
    }

    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    private URL getQueryUrl(){
        return queryUrl;
    }

    //compose URL from rawUrl and queryString
    public void setQueryUrl(){
        Uri.Builder queryBuilder = new Uri.Builder();
        setQueryString();

        queryBuilder.scheme("http")
                .authority(rawUrl)
                .appendPath("query")
                .appendQueryParameter("q", queryString);
        String urlString = queryBuilder.build().toString();
        try{
            queryUrl = new URL(urlString);
        }catch(MalformedURLException e){
            Log.d(TAG, "error: MalformedURLException");
        }
    }

    //Implement URL connection and get here
    public String getWebRequest(){
        return null;
    }

    public String getStandardQueryJson(String url, String query){
        setQuery(query);
        setQueryString();
        setRawUrl(url);
        setQueryUrl();
        return getWebRequest();
    }

}
