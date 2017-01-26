package com.govelapp.govelapp.jsonparser;

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

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.govelapp.govelapp.shopclasses.Shop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QueryParser {
    public static final String TAG = "QueryParser";

    public static List<Shop> parseShopList(String restReply){
        List<Shop> shopList = new ArrayList<>();
        try {
            JSONObject jsonRoot = new JSONObject(restReply);
            JSONArray places = jsonRoot.getJSONArray("places");

            for (int i=0; i<places.length(); i++){
                JSONObject place = places.getJSONObject(i);

                //should this be changed for a builder pattern instead of javabean pattern?
                Shop shop = new Shop();

                shop.setId(place.getInt("id"));
                shop.setName(place.getString("name"));
                shop.setMainCategory(place.getString("mainCategory"));
                shop.setPosition(new LatLng(
                        place.getDouble("latitude"),
                        place.getDouble("longitude")
                ));
                shop.setMarkerOptions();
                //shop.setIcon();
                shop.setInfo("info");

                shopList.add(shop);
            }
        } catch (JSONException e) {
            Log.d(TAG, "error: can't parse json");
        }
        return shopList;
    }
}
