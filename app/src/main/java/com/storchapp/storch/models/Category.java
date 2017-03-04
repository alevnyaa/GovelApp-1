package com.storchapp.storch.models;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Category {
    final static String TAG = "Category";
    private int id;
    private String name_en;
    private String name_tr;
    private int parent;
    private List<Integer> storeIDList = new ArrayList<>();
    static protected HashMap<Integer, Category> categories = new HashMap<>();

    public List<Store> getStores(){
        List<Store> stores = new ArrayList<>();
        for (int id: storeIDList){
            stores.add(Store.stores.get(id));
        }
        return stores;
    }


    public Category getParentCat(){
      return categories.get(Integer.valueOf(id));
    }

    public void saveCategory(){
        categories.put(Integer.valueOf(id), this);
    }
    //TODO:complete throw thing

    public class CategoryBuilderError extends Exception{
        public CategoryBuilderError(String msg){
            super(msg);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) throws CategoryBuilderError {
        if(id < 1){
            throw new CategoryBuilderError("ID can't be smaller than one.");
        }else{
            this.id = id;
        }
    }

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) throws CategoryBuilderError {
        if(name_en.isEmpty()){
            throw new CategoryBuilderError("Name can't be empty.");
        }else{
            this.name_en = name_en;
        }
    }

    public String getName_tr() {
        return name_tr;
    }

    public void setName_tr(String name_tr) throws CategoryBuilderError {
        if(name_tr.isEmpty()){
            throw new CategoryBuilderError("Name can't be empty.");
        }else{
            this.name_tr = name_tr;
        }
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) throws CategoryBuilderError {
        if(parent < 1){
            throw new CategoryBuilderError("Parent can't be smaller than one.");
        }
        this.parent = parent;
    }

    public List<Integer> getStoreIDList() {
        return storeIDList;
    }

    public void setStoreIDList(List<Integer> storeIDList) {
        this.storeIDList = storeIDList;
    }

}
