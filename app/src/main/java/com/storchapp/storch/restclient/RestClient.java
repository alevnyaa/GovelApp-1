package com.storchapp.storch.restclient;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.storchapp.storch.MainActivity;
import com.storchapp.storch.jsonparser.CategoryParser;
import com.storchapp.storch.models.Category;

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

    CategoryParser cp = new CategoryParser();

    public void updateCategories(){
        try{
            cp.parseCategoryList(httpsGetRawString("https://95.85.27.32/categories/"));
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }
    }

    public Category getCategoryByID(int categoryID) {
        String mURL = "https://95.85.27.32/categories/" + categoryID + "/";
        try{
            return cp.parseCategory(new JSONObject(httpsGetRawString(mURL)));
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }
        return null;
    }

    public void updateStores(){
        return;
    }

    private String httpsGetRawString(String url){
        try{
            URL mUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) mUrl.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            StringBuilder stringBuilder = new StringBuilder();

            String inputStr;

            while((inputStr=bufferedReader.readLine()) != null) {
                stringBuilder.append(inputStr);
            }

            return inputStr;
        }catch (Exception e){
            Log.d(TAG, e.toString());

            //TODO: if string null smt is wrong
            return null;
        }
    }


}
