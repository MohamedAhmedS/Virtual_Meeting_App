package com.example.virtualmeetingapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

public class SystemPrefs {

    String TAG = SystemPrefs.class.getSimpleName();
    Context context;
    SharedPreferences sharedPreferences;
    private String PREF_NAME="virtualMeeting";



    public SystemPrefs(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setObjectData(String objectKey, Object object){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson json = new Gson();
        String jsonObject = json.toJson(object);
        Log.v(TAG+" OBJECT_TO_JSON","");
        editor.putString(objectKey,jsonObject);
        editor.commit();
    }
    public Object getOjectData(String objectKey, Class objectClass){
        String objectString  = sharedPreferences.getString(objectKey,"");
        Gson json = new Gson();
//        Log.v(TAG+" OBJECT_TO_JSON",objectString);
        Object object = json.fromJson(objectString,objectClass);
        return object;
    }
}
