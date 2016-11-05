package com.salsify.dal;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;

import com.salsify.data.FileData;

import rx.Observable;

/**
 * Interface to the Data Abstraction Layer for the Application
 * 
 * @author Pranessh Kannappan
 *
 */
public interface LineDal {
	
	/**
	 * Version #1 - Synchronous retrieval of File line via {@link BufferedReader}
	 * 	
	 * @param lineId
	 * @return FileData
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	FileData getLineById_v1(long lineId) throws FileNotFoundException, IOException;
	
	/**
	 * Version #2 - Asynchronous retrieval of File line via {@link MappedByteBuffer}
	 * 
	 * @param lineId
	 * @return Observable<FileData>
	 */
	Observable<FileData> getLineById_v2(long lineId);
	
	/**
	 * Version #3 - Asynchronous retrieval of File line via {@link RandomAccessFile}
	 * 
	 * @param lineId
	 * @return Observable<FileData>
	 */
	Observable<FileData> getLineById_v3(long lineId);
}
