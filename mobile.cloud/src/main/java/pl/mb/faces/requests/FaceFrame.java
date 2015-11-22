package pl.mb.faces.requests;

import java.io.Serializable;

public class FaceFrame implements Serializable{
	private byte[] bytes;
	private int mode;
	
	public byte[] getBytes() {
		return bytes;
	}
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}	
}