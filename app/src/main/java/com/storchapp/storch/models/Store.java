package com.storchapp.storch.models;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Store {
    final static String TAG = "Store";
    private int id;
    private String name;
    private LatLng position;
    private String info;
    private BitmapDescriptor icon;
    private MarkerOptions markerOptions;
    private List<Integer> categoryIDList = new ArrayList<>();

    protected static HashMap<Integer, Store> stores = new HashMap<>();

    public List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        for (int id : categoryIDList) {
            categories.add(Category.categories.get(id));
        }
        return categories;
    }

    private class storeBuilderError extends Exception {

    }
    //TODO:complete throw thing

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    public void setMarkerOptions() {
        markerOptions = new MarkerOptions()
                .position(position)
                .title(name)
                .snippet(info)
                .icon(icon);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }

    public void setIcon(BitmapDescriptor icon) {
        this.icon = icon;
    }
}
