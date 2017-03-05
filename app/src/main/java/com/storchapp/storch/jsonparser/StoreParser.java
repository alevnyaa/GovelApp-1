package com.storchapp.storch.jsonparser;

import android.util.Log;

import com.storchapp.storch.models.Category;
import com.storchapp.storch.models.Store;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by ken on 04.03.2017.
 */

public class StoreParser {
    public static final String TAG = "StoreParser";

    public void parseStoreList(String jsonString){
        List<Category> storeList = new ArrayList<>();
        try {
            JSONArray storesJSON = new JSONArray(jsonString);

            for (int i=0; i< storesJSON.length(); i++){
                JSONObject storeJSON = storesJSON.getJSONObject(i);

                parseStore(storeJSON).addToStores();
            }
        } catch (JSONException e) {
            Log.d(TAG, "error: can't parse json");
        }
    }

    public Store parseStore(JSONObject storeJSON){
        //should this be changed for a builder pattern instead of javabean pattern?
        Store store = new Store();

        try {
            store.setId(storeJSON.getInt("id"));
            store.setStoreCode(storeJSON.getInt("store_code"));
            store.setstoreName(storeJSON.getString("store_name"));
            store.setPhoneNumber(storeJSON.getString("primary_phone_number"));
            store.setAddress(storeJSON.getString("address"));
            store.setWebSite(storeJSON.getString("website_address"));
            store.setPosition(Double.parseDouble(storeJSON.getString("latitude")),
                    Double.parseDouble(storeJSON.getString("longitude")));
            store.setContactName(storeJSON.getString("contact_name"));

            return store;

        }catch (Store.storeBuilderError e){
            Log.d(TAG, "parseCategoryList: " + e);
        }catch (JSONException e){
            Log.d(TAG, "parseCategoryList: " + e);
        }
        return null;
    }



}
