package com.example.marcin.facesdkvideo.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

public class Configuration {

    private static final String SHARED_PREFS_PROPERTY = "SharedPreferences";
    private static final String SHARED_PREFS_LOCATION_PROPERTY = "Location";
    private static final String SHARED_PREFS_FREQUENCY_PROPERTY = "Frequency";
    private static final String SHARED_PREFS_TEMPLATES_LOCATION_PROPERTY = "Templates";

    SharedPreferences sharedPreferences;

    private String facesDefinitionLocation;
    private String templatesDefinitionLocation;
    private int samplingFrequency;

    Configuration(Context context){
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_PROPERTY, Context.MODE_PRIVATE);
        facesDefinitionLocation = sharedPreferences.getString(SHARED_PREFS_LOCATION_PROPERTY, Environment.getExternalStorageDirectory().getAbsolutePath() + "/FaceSDK/");
        templatesDefinitionLocation = sharedPreferences.getString(SHARED_PREFS_TEMPLATES_LOCATION_PROPERTY, Environment.getExternalStorageDirectory().getAbsolutePath()+"/FaceSDK/templates/") + "template.json";
        samplingFrequency = sharedPreferences.getInt(SHARED_PREFS_FREQUENCY_PROPERTY,1000000);
    }

    public String getFacesDefinitionLocation() {
        return facesDefinitionLocation;
    }

    public int getSamplingFrequency() {
        return samplingFrequency;
    }

    public String getTemplatesDefinitionLocation() {
        return templatesDefinitionLocation;
    }

    public void setSamplingFrequency(int samplingFrequency) {
        sharedPreferences.edit().putInt(SHARED_PREFS_FREQUENCY_PROPERTY,samplingFrequency);
        this.samplingFrequency = samplingFrequency;
    }

    public void setFacesDefinitionLocation(String facesDefinitionLocation) {
        sharedPreferences.edit().putString(SHARED_PREFS_LOCATION_PROPERTY,facesDefinitionLocation);
        this.facesDefinitionLocation = facesDefinitionLocation;
    }

    public void setTemplatesDefinitionLocation(String templatesDefinitionLocation) {
        sharedPreferences.edit().putString(SHARED_PREFS_TEMPLATES_LOCATION_PROPERTY,facesDefinitionLocation);
        this.templatesDefinitionLocation = templatesDefinitionLocation;
    }
}
