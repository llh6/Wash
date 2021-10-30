package com.example.wash.entity;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;


public class Utility {
    public static DataAll handleWashersResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject jsonArray = jsonObject.getJSONObject("data");
            String content = "";
            content = jsonArray.toString();
            return new Gson().fromJson(content, DataAll.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
    @Nullable
    public static String handleLogin(String response)  {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String content = "";
            content = jsonObject.getString("msg");
            return content;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String handleIP(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            String content = "";
            content = jsonObject.getString("msg");
            return content;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
