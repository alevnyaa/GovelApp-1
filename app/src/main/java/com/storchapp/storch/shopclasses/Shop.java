package com.storchapp.storch.shopclasses;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Shop {
    final static String TAG = "Shop";

    private enum category{
        CAFE,RESTAURANT,SHOP,MARKET,DEFAULT
    }

    private int id;
    private String name;
    private String info;
    private category mainCategory;
    private LatLng position;
    private MarkerOptions markerOptions;
    private BitmapDescriptor icon;

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

    public String getInfo() {return info;}

    public void setInfo(String info) {this.info = info;}

    public category getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(String mainCategory) {
        this.mainCategory = interpretCategory(mainCategory);
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    public void setMarkerOptions(MarkerOptions markerOptions){
        this.markerOptions = markerOptions;
    }

    public void setMarkerOptions() {
        markerOptions = new MarkerOptions()
                .position(position)
                .title(name)
                .snippet(info)
                .icon(icon);
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }

    public void setIcon(BitmapDescriptor icon) {
        this.icon = icon;
    }

    /*public void setIcon(){
        switch(mainCategory){
            case category.CAFE:
                ic_launcher = BitmapDescriptorFactory.fromResource(R.drawable.marker_cafe);
               break;
            case category.MARKET:
                ic_launcher = BitmapDescriptorFactory.fromResource(R.drawable.marker_market);
                break;
            default:
                ic_launcher = BitmapDescriptorFactory.fromResource(R.drawable.marker_default);
        }
    }*/

    private category interpretCategory(String categoryString){
        switch(categoryString) {
            case "CAFE": return category.CAFE;
            case "RESTAURANT": return category.RESTAURANT;
            case "SHOP": return category.SHOP;
            case "MARKET": return category.MARKET;
            default: return category.DEFAULT;
        }
    }
}
