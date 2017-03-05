package com.storchapp.storch.jsonparser;

/*
Query
{"query" : "$query",
  "places" : [
    {"id" : "$id",
      "name" : "$name",
      "mainCategory" : "$category",
      "long" : "$long",
      "lat" : "$lat"
    },
    {"id" : "$id",
      "name" : "$name",
      "mainCategory" : "$category",
      "long" : "$long",
      "lat" : "$lat"
    },
    ...
  ]
}
Details
{"id" : "$id",
  "name" : "$name",
  "address" : "$address",
  "desc" : "$desc",
  "contact" : "$contact",
  "products" : "$product",
  "categories" : "$categories$",
  "services" : "$services",
  "times" : "$time",
  "image" : {
    "type" : "${jpg,png...}",
    "url" : "$url"
  }
}
 */

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.storchapp.storch.models.Store;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QueryParser {
    public static final String TAG = "QueryParser";

    public static List<Store> parseShopList(String restReply){
        List<Store> storeList = new ArrayList<>();
        try {
            JSONObject jsonRoot = new JSONObject(restReply);
            JSONArray places = jsonRoot.getJSONArray("places");

            for (int i=0; i<places.length(); i++){
                JSONObject place = places.getJSONObject(i);

                //should this be changed for a builder pattern instead of javabean pattern?
                Store store = new Store();

                /*store.

                store.setId(place.getInt("id"));
                store.setName(place.getString("name"));
                store.setMainCategory(place.getString("mainCategory"));
                store.setPosition(new LatLng(
                        place.getDouble("latitude"),
                        place.getDouble("longitude")
                ));
                store.setMarkerOptions();
                //store.setIcon();
                store.setInfo("info");*/

                storeList.add(store);
            }
        } catch (JSONException e) {
            Log.d(TAG, "error: can't parse json");
        }
        return storeList;
    }
}
