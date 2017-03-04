package com.storchapp.storch.jsonparser;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.storchapp.storch.models.Category;
import com.storchapp.storch.models.Store;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;


public class CategoryParser {
    public static final String TAG = "CategoryParser";

    public static void parseCategoryList(String jsonString){
        List<Category> categoryList = new ArrayList<>();
        try {
            JSONArray categoriesJSON = new JSONArray(jsonString);

            for (int i=0; i< categoriesJSON.length(); i++){
                JSONObject categoryJSON = categoriesJSON.getJSONObject(i);

                //should this be changed for a builder pattern instead of javabean pattern?
                Category cat = new Category();

                try {
                    cat.setId(categoryJSON.getInt("id"));
                    cat.setName_en(categoryJSON.getString("name_en"));
                    cat.setName_tr(categoryJSON.getString("name_tr"));
                    cat.setParent(categoryJSON.getInt("parent"));

                    String storeString = categoryJSON.getString("stores");

                    List<Integer> storeIDsInt = new ArrayList<>();

                    String[] storeIDString = storeString.split(Pattern.quote(","));

                    for(String s: storeIDString){
                        storeIDsInt.add(Integer.parseInt(s));
                    }

                    cat.setStoreIDList(storeIDsInt);

                    cat.addToCategories();

                }catch (Category.CategoryBuilderError e){
                    Log.d(TAG, "parseCategoryList: " + e);
                }

            }
        } catch (JSONException e) {
            Log.d(TAG, "error: can't parse json");
        }
    }
}
