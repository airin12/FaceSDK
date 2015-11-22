package mobile.cloud;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.SerializationUtils;

import com.google.gson.Gson;

import Luxand.FSDK;
import Luxand.FSDK.FSDK_FaceTemplate.ByReference;
import pl.mb.faces.requests.FaceFrame;
import pl.mb.faces.requests.FaceRecognitionRequest;

@Path("/face.recognition")
public class FaceRecognitionEndpoint {

	private static String message = null;
	private static String path = null;
	private static boolean loaded = false;

	static {
		try {
			path = FaceRecognitionEndpoint.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			System.load(path + "facesdk.dll");
			int result = FSDK.ActivateLibrary(
					"S38hvBox/Ihlp0op+5+b1Quyv2qlqntMWUlnNdowjQwfuO4vrVMbPfzixTxUI1XaVLWHPBPHPqyHRiimJPoOcCPTHBRXCe99hS6WqIlSbDOXS582/YS8W0b06As7qDTOVjIVVDoJIuxRv+lbRZzmZQZD9ZYuhCCZ8Du59QezvoA=");
			if (result != FSDK.FSDKE_OK)
				message = "Library not initialized";
			else {
				message = "Library activated";
				loaded = true;
			}
			FSDK.Initialize();
		} catch (Exception e) {
			message = "Exception while activating: " + e.getMessage() + "\n " + path;
		}
	}

	@GET
	@Path("/check")
	@Produces(MediaType.TEXT_PLAIN)
	public String checkLibrary() {
		System.out.println("Check request");
		return message;
	}

	@POST
	@Path("/recognize")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public String processVideo(byte[] data) {
		if (!loaded) {
			return message;
		}

		Gson gson = new Gson();
		FaceRecognitionRequest request = SerializationUtils.deserialize(data);

		Map<String, FSDK.FSDK_FaceTemplate.ByReference> templates = new HashMap<String, ByReference>();
		for (Entry<String, byte[]> entry : request.getTemplates().entrySet()) {
			FSDK.FSDK_FaceTemplate.ByReference template = new FSDK.FSDK_FaceTemplate.ByReference();
			template.template = entry.getValue();
			templates.put(entry.getKey(), template);
		}

		Iterator<Long> timeIt = request.getTimes().iterator();
		Iterator<FaceFrame> pictureIt = request.getFrames().iterator();

		LinkedHashMap<Long, Map<String, List<Integer>>> framesWithFaceCoords = new LinkedHashMap<Long, Map<String, List<Integer>>>();

		while (pictureIt.hasNext()) {
			processBitmap(timeIt.next(), pictureIt.next(), templates, framesWithFaceCoords);
		}

		String json = gson.toJson(framesWithFaceCoords);

		System.out.println("Returning " + json);

		return json;
	}

	private String processBitmap(Long time, FaceFrame frame, Map<String, FSDK.FSDK_FaceTemplate.ByReference> templates,
			LinkedHashMap<Long, Map<String, List<Integer>>> framesWithFaceCoords) {
		String log = null;

		FSDK.HImage himage = new FSDK.HImage();
		FSDK.TFaces facesArray = new FSDK.TFaces();
		FSDK.FSDK_Features.ByReference features = new FSDK.FSDK_Features.ByReference();
		int result = 0;

		try {
			BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(frame.getBytes()));
			result = FSDK.LoadImageFromAWTImage(himage, bufferedImage, frame.getMode());
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		
//		result = FSDK.LoadImageFromBuffer(himage, frame.getBytes(), frame.getWidth(), frame.getHeight(), 4 * frame.getWidth(),
//				frame.getMode());

		int matchFaceResult = 0;

		float[] similarity = new float[1];

		if (result == FSDK.FSDKE_OK) {
			// result = FSDK.DetectFace(himage, facePosition);
			result = FSDK.DetectMultipleFaces(himage, facesArray);
			if (result == FSDK.FSDKE_OK) {
				for (FSDK.TFacePosition facePosition : facesArray.faces) {
					result = FSDK.DetectFacialFeaturesInRegion(himage, facePosition, features);
					FSDK.FSDK_FaceTemplate.ByReference currentTemplate = new FSDK.FSDK_FaceTemplate.ByReference();
					matchFaceResult = FSDK.GetFaceTemplate(himage, currentTemplate);
					if (matchFaceResult == FSDK.FSDKE_OK) {
						for (Map.Entry<String, FSDK.FSDK_FaceTemplate.ByReference> entry : templates.entrySet()) {
							String templateName = entry.getKey();
							FSDK.FSDK_FaceTemplate.ByReference faceTemplate = entry.getValue();
							matchFaceResult = FSDK.MatchFaces(currentTemplate, faceTemplate, similarity);
							if (matchFaceResult == FSDK.FSDKE_OK) {

								List<Integer> coordsList = new ArrayList<Integer>();
								coordsList.add(facePosition.xc);
								coordsList.add(facePosition.yc);
								coordsList.add(facePosition.w);
								Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
								map.put(templateName, coordsList);
								framesWithFaceCoords.put(time, map);
								log = "Frames definition updated";
							} else
								log = "Template not matched";
						}
					} else
						log = "Error while getting template from video";
				}
			} else {
				log = "Faces not detected. " + result;
			}
		} else {
			log = "Image not loaded";
		}
		return log;
	}

}
