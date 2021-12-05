package com.example.quizzadmin;

import java.util.List;

/*
category model class to carry Name,number of sets,category key and image url of different category
 */
public class CategoryModel {

    /*
    Variables will have same name as key name saved in firebase database
    to retrieve all the data at once with the help of for loop in
            CatagoriesActivity.java 146:17
    */
    private String name;
    private List<String> sets;
    private String url;
    String key;

    public CategoryModel(){
        //for firebase
    }

    public CategoryModel(String name, List<String> sets, String url, String key) {
        this.name = name;
        this.sets = sets;
        this.url = url;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSets() {
        return sets;
    }

    public void setSets(List<String> sets) {
        this.sets = sets;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
