package com.salsify.controller;

import java.io.BufferedReader;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.salsify.data.FileData;
import com.salsify.data.LineId;
import com.salsify.props.AppProperties;
import com.salsify.service.LineService;

import rx.Observable;

/**
 * Rest Controller for the application exposing all the end points
 *  a) /lines/v1/{lineId}
 *  b) /lines/v2/{lineId}
 *  c) /lines/v3/{lineId}
 *  
 * @author Pranessh Kannappan
 *
 */
@RestController
@RequestMapping(value="/lines")
@Validated
public class LineController {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(LineController.class);	
	
	public LineController(){
		LOGGER.info("Instantiated LineController...");
	}
	
	/**
	 * Dependency Injection of the {@link LineService}
	 */
	@Autowired
	LineService lineService;
	
	/**
	 * Dependency Injection of the {@link AppProperties}
	 */
	@Autowired
	AppProperties props;
		
	/**
	 * Version #1
	 * 	This end point is responsible for reading data from the required line# of the file
	 *  synchronously via {@link BufferedReader} 
	 *  	a) If line# is present, return data and status:200
	 *  	b) If line# is not present, return null and status:413
	 *  
	 * @param lineId
	 * @return ResponseEntity<FileData>
	 */
	@GetMapping("/v1/{lineId}")
	public DeferredResult<ResponseEntity<FileData>> getFileLineById_v1(@PathVariable("lineId") @LineId long lineId){
		LOGGER.info("LineController::getFileLineById Started...");
		
		DeferredResult<ResponseEntity<FileData>> deferredResult = new DeferredResult<>(props.getReadTimeoutMs());
		try{
			FileData fileData = lineService.getFileLineById_v1(lineId);
			if(null == fileData.getData()){
				deferredResult.setResult(new ResponseEntity<>(fileData, HttpStatus.PAYLOAD_TOO_LARGE));
			}
			else{
				deferredResult.setResult(new ResponseEntity<>(fileData, HttpStatus.OK));
			}
		}
		catch(Exception exception){
			deferredResult.setErrorResult(exception);
		}
		finally{
			LOGGER.info("LineController::getFileLineById Completed...");
		}		
		
		return deferredResult;
	}
	
	/**
	 * Version #2
	 * 	This end point is responsible for reading data from the required line# of the file
	 *  asynchronously via {@link Observable} and {@link MappedByteBuffer} 
	 *  	a) If line# is present, return data and status:200
	 *  	b) If line# is not present, return null and status:413
	 *  
	 * @param lineId
	 * @return ResponseEntity<FileData>
	 */
	@GetMapping("/v2/{lineId}")
	public DeferredResult<ResponseEntity<FileData>> getFileLineById_v2(@PathVariable("lineId") @LineId long lineId){
		LOGGER.info("LineController::getFileLineById_v2 Started...");
		
		DeferredResult<ResponseEntity<FileData>> deferredResult = new DeferredResult<>(props.getReadTimeoutMs());
		
		lineService.getFileLineById_v2(lineId)
						 .subscribe(fileData -> deferredResult.setResult(processFileDataResponse(fileData)),
								    error -> deferredResult.setErrorResult(error));
								 
		
		LOGGER.info("LineController::getFileLineById_v2 Completed...");
		return deferredResult;
	}
		
	/**
	 * Version #3
	 * 	This end point is responsible for reading data from the required line# of the file
	 *  asynchronously via {@link Observable} and {@link RandomAccessFile} 
	 *  	a) If line# is present, return data and status:200
	 *  	b) If line# is not present, return null and status:413
	 *  
	 * @param lineId
	 * @return ResponseEntity<FileData>
	 */
	@GetMapping("/v3/{lineId}")
	public DeferredResult<ResponseEntity<FileData>> getFileLineById_v3(@PathVariable("lineId") @LineId long lineId){
		LOGGER.info("LineController::getFileLineById_v3 Started...");
		
		DeferredResult<ResponseEntity<FileData>> deferredResult = new DeferredResult<>(props.getReadTimeoutMs());
		
		lineService.getFileLineById_v3(lineId)
						 .subscribe(fileData -> deferredResult.setResult(processFileDataResponse(fileData)),
								    error -> deferredResult.setErrorResult(error));
								 
		
		LOGGER.info("LineController::getFileLineById_v3 Completed...");
		return deferredResult;
	}
	
	/**
	 * Helper method to check the {@link FileData} content and set HTTP Status
	 * 	a) If FileData.data is null, set HTTP 413
	 *  b) ELSE set HTTP 200
	 *  
	 * @param fileData
	 * @return ResponseEntity<FileData>
	 */
	private ResponseEntity<FileData> processFileDataResponse(FileData fileData){
		ResponseEntity<FileData> response = null;
		
		if(fileData != null && 
				fileData.getData() != null){
			response = new ResponseEntity<FileData>(fileData, HttpStatus.OK);
		}
		else{
			response = new ResponseEntity<FileData>(fileData, HttpStatus.PAYLOAD_TOO_LARGE);
		}
		
		return response;
	}
}	//EOF LineController.java