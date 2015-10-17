/*
 * FaceSDK Library Sample
 * Copyright (C) 2013 Luxand, Inc. 
 */

package com.example.marcin.facesdkvideo;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.MediaController;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.facialfeaturesfromvideo.R;
import com.example.marcin.facesdkvideo.config.Configuration;
import com.example.marcin.facesdkvideo.config.ConfigurationFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.luxand.FSDK;
import com.luxand.FSDK.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class MainActivity extends Activity {
	private HImage oldpicture;
	private static int RESULT_LOAD_IMAGE = 1;
	private boolean processing;
	private FSDK_FaceTemplate faceTemplate = new FSDK_FaceTemplate();
	private LoadType loadType;
	private LinkedHashMap<Long,Map<String,List<Integer>>> framesWithFaceCoords =
			new LinkedHashMap<>();
	private MediaController mediaController;

	private Configuration configuration;

	private class GetFaceTemplateInBackground extends AsyncTask<String,Void,String>{

		@Override
		protected String doInBackground(String... params) {
			String log = null;
			String picturePath = params[0];
			HImage picture = new HImage();
			int result = FSDK.LoadImageFromFile(picture, picturePath);
			if (result == FSDK.FSDKE_OK) {
				int templateResult = FSDK.GetFaceTemplate(picture,faceTemplate);
				if(templateResult == FSDK.FSDKE_OK)
					log = "Template saved";
				else if (templateResult == FSDK.FSDKE_FACE_NOT_FOUND)
					log = "Error while getting template";
			} else {
				log = "Error while loading file for face template";
			}
			processing = false; //long-running code is complete, now user may push the button
			return log;
		}

		@Override
		protected void onPostExecute(String resultstring) {
			TextView tv = (TextView) findViewById(R.id.messageTextView);

			tv.setText(resultstring);
		}
	}

	// Subclass for async processing of FaceSDK functions.
	// If long-run task runs in foreground - Android kills the process.
	private class DetectFaceInBackground extends AsyncTask<String, Void, String> {
		protected FSDK_Features features;
		protected List<TFacePosition> facesCoords;
		protected String picturePath;
		protected HImage picture;
		protected int result;
		protected Bitmap bitmap;
		protected float [] similarity = new float[1];
		protected int matchFaceResult;
		protected TFaces facesArray;

		@Override
		protected String doInBackground(String... params) {
			String log = "";
			picturePath = params[0];
			facesCoords = new ArrayList<>();
//			faceCoords = new TFacePosition();
//			faceCoords.w = 0;
			picture = new HImage();
			facesArray = new TFaces();

			FFmpegMediaMetadataRetriever ffRetriever = new FFmpegMediaMetadataRetriever();
			ffRetriever.setDataSource(picturePath);
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(picturePath);
//			bitmap = retriever.getFrameAtTime(0,MediaMetadataRetriever.OPTION_CLOSEST);

			String durationInMs = ffRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
			int duration = Integer.parseInt(durationInMs) * 1000;

			for(long time = 0; time < duration ; time+=configuration.getSamplingFrequency()) {

				String templateName = "template";
				bitmap = ffRetriever.getFrameAtTime(time,FFmpegMediaMetadataRetriever.OPTION_CLOSEST);

				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				result = FSDK.LoadImageFromJpegBuffer(picture, stream.toByteArray(), stream.size());

				if (result == FSDK.FSDKE_OK) {
//				result = FSDK.DetectFace(picture, faceCoords);
					result = FSDK.DetectMultipleFaces(picture, facesArray);
					features = new FSDK_Features();
					if (result == FSDK.FSDKE_OK) {
						for (TFacePosition facePosition : facesArray.faces) {
							result = FSDK.DetectFacialFeaturesInRegion(picture, facePosition, features);
							FSDK_FaceTemplate currentTemplate = new FSDK_FaceTemplate();
							matchFaceResult = FSDK.GetFaceTemplate(picture, currentTemplate);
							if (matchFaceResult == FSDK.FSDKE_OK) {
								matchFaceResult = FSDK.MatchFaces(currentTemplate, faceTemplate, similarity);
								if (matchFaceResult == FSDK.FSDKE_OK) {
									log = "Template matched with probability " + similarity[0];
									facesCoords.add(facePosition);
									List<Integer> coordsList = new ArrayList<>();
									coordsList.add(facePosition.xc);
									coordsList.add(facePosition.yc);
									coordsList.add(facePosition.w);
									Map<String,List<Integer>> map = new HashMap<>();
									map.put(templateName,coordsList);
									framesWithFaceCoords.put(time, map);
								} else
									log = "Template not matched";
							} else
								log = "Error while getting template from video";
						}
					}
				}
			}

			String frameDefinitionPath = generateJsonFileLoaction(picturePath);

			OutputStreamWriter outputStreamWriter = null;
			FileOutputStream fileOutputStream = null;
			try {
				File file = new File(frameDefinitionPath);
				file.createNewFile();
				fileOutputStream = new FileOutputStream(file);
				outputStreamWriter = new OutputStreamWriter(fileOutputStream);
				Gson gson = new Gson();
				String json = gson.toJson(framesWithFaceCoords);
				outputStreamWriter.append(json);
			} catch (Exception ex){
				Toast.makeText(getBaseContext(), ex.getMessage(),
						Toast.LENGTH_SHORT).show();
			} finally {
				try {
					outputStreamWriter.close();
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			processing = false; //long-running code is complete, now user may push the button
			return log;
		}

		@Override
		protected void onPostExecute(String resultstring) {
			TextView tv = (TextView) findViewById(R.id.messageTextView);

			if (result != FSDK.FSDKE_OK){
				tv.setText("Face not detected");
			} else
			tv.setText(resultstring);


			if(picture == null)
				return;

			FaceImageView imageView = (FaceImageView) findViewById(R.id.imageView1);

			imageView.setImageBitmap(bitmap);

//			tv.setText(resultstring);

			imageView.detectedFaces = facesCoords;

			if(result == FSDK.FSDKE_OK) {
				if (features.features[0] != null) // if detected
					imageView.facial_features = features;
			}

			int [] realWidth = new int[1];
			FSDK.GetImageWidth(picture, realWidth);
			imageView.faceImageWidthOrig = realWidth[0];
			imageView.invalidate(); // redraw, marking up faces and features

			if (oldpicture != null)
				FSDK.FreeImage(oldpicture);
			oldpicture = picture;
		}

		@Override
		protected void onPreExecute() {
		}
		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
	//end of DetectFaceInBackground class



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		configuration = ConfigurationFactory.getInstance(getApplicationContext());
		processing = true; //prevent user from pushing the button while initializing
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); //using res/layout/activity_main.xml

		TextView tv = (TextView) findViewById(R.id.messageTextView);

		try {
			int res = FSDK.ActivateLibrary("mlxAah4CuoSeGQNclX2GnqIfXRy5ALqPdcpIlucjvieu0KKn/nVutHz070FLwSDzichHpBPsfSfVGl4R6J/QEX94do1CDNqQh7pB+ymw4SZ5jCEL1Z1W9o1eH0pjSLdUcidckotH+DHeuYJIqKi7zMLmxqrxiF0/K431ESZDj44=");
			FSDK.Initialize();
			FSDK.SetFaceDetectionParameters(false, false, 100);
			FSDK.SetFaceDetectionThreshold(5);
//			htracker = new HTracker();
//			FSDK.CreateTracker(htracker);

			if (res == FSDK.FSDKE_OK) {
				tv.setText("FaceSDK activated\n");
			} else {
				tv.setText("Error activating FaceSDK: " + res + "\n");
			}
		}
		catch (Exception e) {
			tv.setText("exception " + e.getMessage());
		}

		// Adding button
		Button buttonLoadImage1 = (Button) findViewById(R.id.buttonProcessVideo);
		buttonLoadImage1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg) {
				if (!processing) {
					processing = true;
					loadType = LoadType.VIDEO;
					Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(i, RESULT_LOAD_IMAGE);
				}
			}
		});

		Button buttonLoadImage2 = (Button) findViewById(R.id.buttonLoadTemplate);
		buttonLoadImage2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!processing) {
					processing = true;
					loadType = LoadType.PHOTO;
					Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(i, RESULT_LOAD_IMAGE);
				}
			}
		});

		Button buttonLoadFacesVideo = (Button) findViewById(R.id.buttonLoadVideoWithFaces);
		buttonLoadFacesVideo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg) {
				if (!processing) {
					processing = true;
					loadType = LoadType.FACES_VIDEO;
					Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(i, RESULT_LOAD_IMAGE);
				}
			}
		});

		processing = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			TextView tv = (TextView) findViewById(R.id.messageTextView);
			tv.setText("processing...");
			if(loadType == LoadType.PHOTO)
				new GetFaceTemplateInBackground().execute(picturePath);
			else if(loadType == LoadType.VIDEO)
				new DetectFaceInBackground().execute(picturePath);
			else if(loadType == LoadType.FACES_VIDEO)
				handleVideo(picturePath);
		} else {
			processing = false;
		}
	}

	private void handleVideo(String picturePath) {
		final ShowVideoView videoView = (ShowVideoView) findViewById(R.id.videoView1);
		if (mediaController == null) {
			mediaController = new MediaController(MainActivity.this);
		}

		String jsonPath = generateJsonFileLoaction(picturePath);

		LinkedHashMap<Long,Map<String,List<Integer>>> framesConfig = getJsonFramesConfig(jsonPath);

		FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
		retriever.setDataSource(picturePath);
		final String duration = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
		Bitmap bmp = retriever.getFrameAtTime();
		videoView.setFrameWidth(bmp.getWidth());

		Log.e("TIME","duration "+duration);

		videoView.setFramesWithFaceCoords(framesConfig);
		videoView.setMediaController(mediaController);
		videoView.setVideoPath(picturePath);
		videoView.requestFocus();
		videoView.seekTo(0);
		videoView.setVisibility(VideoView.VISIBLE);
		videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				videoView.setActualDuration(0);
				videoView.setVideoDuration(Long.parseLong(duration));
				videoView.start();
			}
		});

	}

	private String generateJsonFileLoaction(String picturePath){
		String [] splitted = picturePath.split("/");
		String jsonPath = splitted[splitted.length-1].replace("mp4","json");
		jsonPath = configuration.getFacesDefinitionLocation() + jsonPath;
		return jsonPath;
	}

	private LinkedHashMap<Long,Map<String,List<Integer>>> getJsonFramesConfig(String jsonPath) {
		File file = new File(jsonPath);
		StringBuilder stringBuilder = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append('\n');
			}
			br.close();
		}
		catch (IOException e) {
			//You'll need to add proper error handling here
			return null;
		}
		Gson gson = new Gson();
		Type type = new TypeToken<LinkedHashMap<Long,Map<String,List<Integer>>>>(){}.getType();
		LinkedHashMap<Long,Map<String,List<Integer>>> deserialized =
		gson.fromJson(stringBuilder.toString(),type);
		return deserialized;
	}



	private enum LoadType{
		PHOTO,VIDEO, FACES_VIDEO
	}
}
