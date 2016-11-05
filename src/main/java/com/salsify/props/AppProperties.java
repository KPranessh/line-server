package com.salsify.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration File to read the Application specific custom properties
 * into this class.
 * 
 * @author Pranessh Kannappan
 *
 */
@ConfigurationProperties(prefix="line-server")
public class AppProperties {
	
	private String filePath;
	private String cacheName;
	private long readTimeoutMs;
	private long fileChunkSize;
	private long byteBufferSize;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public long getReadTimeoutMs() {
		return readTimeoutMs;
	}

	public void setReadTimeoutMs(long readTimeoutMs) {
		this.readTimeoutMs = readTimeoutMs;
	}

	public long getFileChunkSize() {
		return fileChunkSize;
	}

	public void setFileChunkSize(long fileChunkSize) {
		this.fileChunkSize = fileChunkSize;
	}

	public long getByteBufferSize() {
		return byteBufferSize;
	}

	public void setByteBufferSize(long byteBufferSize) {
		this.byteBufferSize = byteBufferSize;
	}
}	//EOF AppProperties.java