package com.salsify.data;

/**
 * Model to hold File Data
 * 
 * @author Pranessh Kannappan
 *
 */
public class FileData {
	
	private long idx;
	private String data;

	public FileData(long idx, String data){
		this.idx = idx;
		this.data = data;
	}
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public long getIdx() {
		return idx;
	}

	public void setIdx(long idx) {
		this.idx = idx;
	}
}	//EOF FileData.java