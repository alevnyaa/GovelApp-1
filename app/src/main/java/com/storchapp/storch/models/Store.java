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
    private String storeName, phoneNumber, eMail, address,
            contactName, contactNumber, storeInfo, webSite;
    private LatLng position;
    private int storeCode;

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

    public void addToStores(){
        stores.put(getId(), this);
    }

    public class storeBuilderError extends Exception {
        public storeBuilderError(String msg){
            super(msg);
        }
    }
    //TODO:complete throw thing

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    public void setMarkerOptions() {
        markerOptions = new MarkerOptions()
                .position(position)
                .title(storeName)
                .snippet(storeInfo)
                .icon(icon);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) throws storeBuilderError {
        if(id < 1){
            throw new storeBuilderError("ID can't be less than 1.");
        }else{
            this.id = id;
        }
    }

    public String getstoreName() {
        return storeName;
    }

    public void setstoreName(String storeName)throws storeBuilderError {
        if(storeName.isEmpty()){
            throw new storeBuilderError("Store storeName can't be empty.");
        }else{
            this.storeName = storeName;
        }
    }

    public LatLng getPosition() {
        return position;
    }


    //TODO: can't get latlang from JSON so I combined doubles into position
    public void setPosition(double latitude, double longitude) throws storeBuilderError{
        if(latitude == 0.0 || longitude == 0.0){
            throw new storeBuilderError("Position can't be empty.");
        }else{
            LatLng position = new LatLng(latitude, longitude);
            this.position = position;
        }
    }

    public String getStoreInfo() {
        return storeInfo;
    }

    public void setStoreInfo(String storeInfo)throws storeBuilderError {
        if(storeInfo.isEmpty()){
            throw new storeBuilderError("Store Info can't be empty");
        }else{
            this.storeInfo = storeInfo;
        }
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public int getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(int storeCode) {
        this.storeCode = storeCode;
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
    }

    public String getWebSite() {
        return webSite;
    }

    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }

    public void setIcon(BitmapDescriptor icon) {
        this.icon = icon;
    }
}
