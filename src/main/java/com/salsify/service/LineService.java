package com.salsify.service;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.salsify.data.FileData;

import rx.Observable;

/**
 * Interface to the Service component of the Application
 * 
 * @author Pranessh Kannappan
 *
 */
public interface LineService {

	/**
	 * Version #1 - Synchronous retrieval of File data
	 * 	
	 * @param lineId
	 * @return FileData
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	FileData getFileLineById_v1(long lineId) throws FileNotFoundException, IOException;;
	
	/**
	 * Version #2 - Asynchronous retrieval of File data via {@link Observable}
	 * 
	 * @param lineId
	 * @return Observable<FileData>
	 */
	Observable<FileData> getFileLineById_v2(long lineId);
	
	/**
	 * Version #3 - Asynchronous retrieval of File line via {@link Observable}
	 * 
	 * @param lineId
	 * @return Observable<FileData>
	 */
	Observable<FileData> getFileLineById_v3(long lineId);
}
