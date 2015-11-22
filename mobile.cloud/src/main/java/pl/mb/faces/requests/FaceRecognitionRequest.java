package pl.mb.faces.requests;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public class FaceRecognitionRequest implements Serializable{

	private List<FaceFrame> frames;
	private Map<String,byte[]> templates;
	private List<Long> times;
	
	public List<FaceFrame> getFrames() {
		return frames;
	}
	public void setFrames(List<FaceFrame> frames) {
		this.frames = frames;
	}
	public Map<String,byte[]> getTemplates() {
		return templates;
	}
	public void setTemplates(Map<String,byte[]> templates) {
		this.templates = templates;
	}
	public List<Long> getTimes() {
		return times;
	}
	public void setTimes(List<Long> times) {
		this.times = times;
	}
	
}
