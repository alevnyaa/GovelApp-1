package com.storchapp.storch.restclient;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.storchapp.storch.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RestClient {
    public static final String TAG = "RestClient";

    private String queryString; //query with + in between words
    private String query; //raw query string
    private String rawUrl; //raw url string
    private URL queryUrl; //built url object
    private JSONObject responseJSON;

    private void clientConnect(String url){
        try{
            URL mUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) mUrl.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            StringBuilder stringBuilder = new StringBuilder();

            String inputStr;

            while((inputStr=bufferedReader.readLine()) != null){
                stringBuilder.append(inputStr);
            }

            try{
                responseJSON = new JSONObject(stringBuilder.toString());
            }catch (JSONException e){
                Log.d(TAG, e.toString());
            }
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }
    }

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
