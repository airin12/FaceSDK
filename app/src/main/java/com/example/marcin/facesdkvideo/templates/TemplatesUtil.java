package com.example.marcin.facesdkvideo.templates;

import android.content.Context;
import android.util.Log;

import com.example.marcin.facesdkvideo.config.ConfigurationFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.luxand.FSDK;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Marcin on 2015-10-20.
 */
public class TemplatesUtil {

    private static Map<String,FSDK.FSDK_FaceTemplate> templates = null;

    private TemplatesUtil(){
    }

    public static Map<String,FSDK.FSDK_FaceTemplate> getTemplates(Context context){
        if(templates == null)
            templates = loadTemplatesFromFile(context);
        return templates;
    }

    public static void updateTemplates(FSDK.FSDK_FaceTemplate template, Context context, String name){
        Map<String,FSDK.FSDK_FaceTemplate> templates = getTemplates(context);
        if(templates.containsKey(template))
            return;

        templates.put(name,template);
        saveTemplatesToFile(context);
    }

    private static void saveTemplatesToFile(Context context) {
        Log.e("Templates","Saving template");
        String templatesPath = ConfigurationFactory.getInstance(context).getTemplatesDefinitionLocation();
        OutputStreamWriter outputStreamWriter = null;
        FileOutputStream fileOutputStream = null;
        try {
            File file = new File(templatesPath);
            if(file.exists())
                file.delete();
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            Gson gson = new Gson();
            String json = gson.toJson(getTemplates(context));
            outputStreamWriter.append(json);
        } catch (Exception ex){
            Log.e("Templates","Error while saving templates: "+ex.getMessage());
        } finally {
            IOUtils.closeQuietly(outputStreamWriter);
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    private static Map<String,FSDK.FSDK_FaceTemplate> loadTemplatesFromFile(Context context) {
        String templatesPath = ConfigurationFactory.getInstance(context).getTemplatesDefinitionLocation();
        Log.e("Templates","Getting templates from path "+templatesPath);
        File file = new File(templatesPath);

        if(!file.exists()) {
            Log.e("Templates","File not exists");
            return new HashMap<>();
        }

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            Log.e("Templates","Error while reading file: "+e.getMessage());
            return new HashMap<>();
        } finally {
            if(br!=null)
                IOUtils.closeQuietly(br);
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String,FSDK.FSDK_FaceTemplate>>(){}.getType();
        Map<String,FSDK.FSDK_FaceTemplate> result = gson.fromJson(stringBuilder.toString(),type);
        if(result == null) {
            Log.e("Templates","Gson result is null");
            result = new HashMap<>();
        }
        Log.e("Templates",result==null ? result+"" : result.keySet().toString());
        return result;
    }

}
