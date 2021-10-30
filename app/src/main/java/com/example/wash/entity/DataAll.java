package com.example.wash.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataAll {
    @SerializedName("msg")
    public String message;
    @SerializedName("records")
    public List<Wash> washerList;

}
