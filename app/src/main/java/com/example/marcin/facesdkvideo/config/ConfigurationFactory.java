package com.example.marcin.facesdkvideo.config;

import android.content.Context;

/**
 * Created by Marcin on 2015-10-17.
 */
public class ConfigurationFactory {
    private static Configuration instance;

    public static Configuration getInstance(Context context){
        if(instance == null){
            instance = new Configuration(context);
        }
        return instance;
    }
}
