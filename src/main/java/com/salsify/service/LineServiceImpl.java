package com.salsify.service;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.salsify.dal.LineDal;
import com.salsify.data.FileData;

import rx.Observable;

/**
 * Service Component of the Application. This component
 * acts as the bridge between the Controller (handling http requests)
 * and Data Abstraction Layer (crud operation on the data source. File
 * in this case).
 * 
 * @author Pranessh Kannappan
 *
 */
@Service
public class LineServiceImpl implements LineService{
	
	public static final Logger LOGGER = LoggerFactory.getLogger(LineServiceImpl.class);
	
	public LineServiceImpl(){
		LOGGER.info("Instantiated LineServiceImpl...");
	}
	
	@Autowired
	LineDal lineDal;
	
	/**
	 * Version #1 - Synchronous retrieval of File data
	 * 	
	 * @param lineId
	 * @return FileData
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Override
	public FileData getFileLineById_v1(long lineId) throws FileNotFoundException, IOException{
		LOGGER.info("LineServiceImpl::getFileLineById_v1 Started...");
		
		FileData fileData = lineDal.getLineById_v1(lineId);
		
		LOGGER.info("LineServiceImpl::getFileLineById_v1 Completed...");
		return fileData;
	}

	/**
	 * Version #2 - Asynchronous retrieval of File data via {@link Observable}
	 * 
	 * @param lineId
	 * @return Observable<FileData>
	 */
	@Override
	public Observable<FileData> getFileLineById_v2(long lineId) {
		LOGGER.info("LineServiceImpl::getFileLineById_v2 Started...");
	
		Observable<FileData> fileData = lineDal.getLineById_v2(lineId);
	
		LOGGER.info("LineServiceImpl::getFileLineById_v2 Completed...");
		return fileData;
	}
	
	/**
	 * Version #2 - Asynchronous retrieval of File data via {@link Observable}
	 * 
	 * @param lineId
	 * @return Observable<FileData>
	 */
	@Override
	public Observable<FileData> getFileLineById_v3(long lineId) {
		LOGGER.info("LineServiceImpl::getFileLineById_v3 Started...");
	
		Observable<FileData> fileData = lineDal.getLineById_v3(lineId);
	
		LOGGER.info("LineServiceImpl::getFileLineById_v3 Completed...");
		return fileData;
	}
}	//EOF LineServiceImpl.java