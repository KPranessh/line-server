package com.salsify.dal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.salsify.data.FileData;
import com.salsify.props.AppProperties;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Data Abstraction Layer implementation that has the logic to
 * read the file, process it and return the response as {@link FileData}
 * 
 * @author Pranessh Kannappan
 *
 */
@Repository
public class LineDalImpl implements LineDal{

	public static final Logger LOGGER = LoggerFactory.getLogger(LineDalImpl.class);
	
	private final String filePath;
	private final long fileChunkSize;
	private final long byteBufferSize;
	private final Cache cache;
	private final Map<String, Long> EOF_INDICATOR_MAP = new ConcurrentHashMap<>();
	private final Map<Long, Long> FILE_LINE_POINTER_MAP = new ConcurrentHashMap<>();
	
	@Autowired
	public LineDalImpl(AppProperties props, CacheManager cacheManager){
		LOGGER.info("Instantiated LineDalImpl...");
		this.filePath = props.getFilePath();		
		this.fileChunkSize = props.getFileChunkSize();
		this.byteBufferSize = props.getByteBufferSize();
		this.cache = cacheManager.getCache(props.getCacheName());
		
//		initializeLinePointerMap(this.filePath, this.fileChunkSize, this.byteBufferSize);
		initializeLinePointerMap(this.filePath, this.fileChunkSize);
	}

	/**
	 * Method to pre-process the File Data by storing the starting offset bytes
	 * for the required File Lines (determined by the File chunk size).
	 * 
	 * @param filePath
	 * @param fileChunkSize
	 */
	private void initializeLinePointerMap(String filePath, long fileChunkSize){		
		String linedata = null;
		BufferedReader fileReader = null;
		
		String[] lineDataArray = new String[2];
		long startTime = 0L;
		try{
			startTime = System.nanoTime();			
			fileReader = new BufferedReader(new FileReader(this.filePath + ".processed"));

			LOGGER.info("File Chunk Size " + fileChunkSize);
														
			while((linedata = fileReader.readLine()) != null){
				lineDataArray = linedata.split(",");
				
//				file_pointer_array[(int) (Long.parseLong(lineArray[0])/chunkSize)] = Long.parseLong(lineArray[1]);
				FILE_LINE_POINTER_MAP.put(Long.valueOf(lineDataArray[0]), Long.valueOf(lineDataArray[1]));
			}
			fileReader.close();
			
			LOGGER.info("File Line Pointer Map Size " + FILE_LINE_POINTER_MAP.size());
		}
		catch(FileNotFoundException fnfe){
			LOGGER.error("File Not Found!!!");			
		}
		catch(IOException ioe){
			LOGGER.error("IO Exception in reading the File!!!");			
		}
		finally{			
			LOGGER.info("LineDalImpl::initializeLinePointerMap Execution Time [" + (System.nanoTime() - startTime)/1000000 + "] ms");
		}				
	}
	
	/**
	 * Method to pre-process the File Data by storing the starting offset bytes
	 * for the required File Lines (determined by the File chunk size).
	 * 
	 * @param filePath
	 * @param fileChunkSize
	 */
	private void initializeLinePointerMap(String filePath, long fileChunkSize, long byteBufferSize){		
		long lineIdx = 1L;
		long offset = 0L;				
		FileInputStream inputStream = null;
		FileChannel fileChannel = null;
		MappedByteBuffer byteBuffer = null;
		
		long startTime = 0L;
		try{
			startTime = System.nanoTime();			
			LOGGER.info("File Chunk Size " + fileChunkSize);
			
			inputStream = new FileInputStream(filePath);
			fileChannel = inputStream.getChannel();
			
			long sizeToReadBytes = fileChannel.size();			
			
			FILE_LINE_POINTER_MAP.put(lineIdx, offset);			
			while(sizeToReadBytes > 0){			
				byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0L, byteBufferSize);
				
				byteBuffer.load();
				for(int idx=0; idx<byteBuffer.limit(); idx++){				
					offset++;
					
					if(byteBuffer.get() == '\n'){
						lineIdx++;
						
						if((lineIdx > 1) && (lineIdx % fileChunkSize) == 0){
							long tempOffset = offset + 1L;
							FILE_LINE_POINTER_MAP.put(lineIdx, tempOffset);											
						}
					}			
				}			
				byteBuffer.clear();			
				sizeToReadBytes -= byteBufferSize;
			}

			LOGGER.info("File Line Pointer Map Size " + FILE_LINE_POINTER_MAP.size());
		}
		catch(FileNotFoundException fnfe){
			LOGGER.error("File Not Found!!!");			
		}
		catch(IOException ioe){
			LOGGER.error("IO Exception in reading the File!!!");			
		}
		finally{			
			LOGGER.info("LineDalImpl::initializeLinePointerMap Execution Time [" + (System.nanoTime() - startTime)/1000000 + "] ms");
		}				
	}
	
	/**
	 * Version #1 - Synchronous retrieval of File line via {@link BufferedReader}
	 * ehCache is used to cache the input/ output of this method in memory under the
	 * bucket 'fileDataFindCache'.
	 * 	
	 * @param lineId
	 * @return FileData
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Override
	@Cacheable(value="fileDataFindCache", key="#lineId")
	public FileData getLineById_v1(long lineId) throws FileNotFoundException, IOException{	
		BufferedReader fileReader = null;
		long currentIdx = 1L;
		Long eofIdx = 0L;
		String currentLineData = null;
		FileData fileData = null;
		
		long startTime = 0L;
		try{
			LOGGER.info("LineDalImpl::getLineById Started..." + lineId);
			
			startTime = System.nanoTime();
			fileReader = new BufferedReader(new FileReader(this.filePath));
			
			//Check if the requested lineID is already past EOF...
			//If yes, then finish processing and return null
			eofIdx = EOF_INDICATOR_MAP.get("EOF");
			if(eofIdx!= null && lineId > eofIdx){
				LOGGER.info("Crossed EOF!!! [" + (eofIdx) + "]");
			}
			else{				
				while(currentIdx <= lineId){
					currentLineData = fileReader.readLine();
					if(null == currentLineData){
						LOGGER.info("Crossed EOF!!! [" + (currentIdx-1) + "]");
						EOF_INDICATOR_MAP.put("EOF", currentIdx-1);
						break;
					}
					currentIdx++;
				}								
			}
			fileReader.close();
			
			fileData = new FileData(lineId, currentLineData);			
		}
		catch(FileNotFoundException fnfe){
			LOGGER.error("File Not Found!!!");
			throw fnfe;
		}
		catch(IOException ioe){
			LOGGER.error("IO Exception in reading the File!!!");
			throw ioe;
		}
		finally{
			LOGGER.info("LineDalImpl::getLineById Execution Time [" + (System.nanoTime() - startTime)/1000000 + "] ms");
			LOGGER.info("LineDalImpl::getLineById Completed...");
		}
		
		return fileData;
	}
	
	/**
	 * Version #2 - Asynchronous retrieval of File line via {@link MappedByteBuffer}
	 * ehCache is used to cache the already retrieved lines of the file.
	 *  
	 * @param lineId
	 * @return Observable<FileData>
	 */
	@Override	
	public Observable<FileData> getLineById_v2(long lineId){
		long startTime = System.nanoTime();		
		
		String currentLineData = null;
		
		Long eofIdx = (Long) getCacheValue("EOF");
		String cacheValue = (String) getCacheValue(lineId);				
		
		Observable<FileData> fileData = null;
		if(eofIdx != null && lineId > eofIdx){
			LOGGER.info("Crossed EOF!!! [" + (eofIdx) + "]");
			
			fileData = Observable.just(new FileData(lineId, currentLineData));			
		}
		else if(cacheValue != null){
			LOGGER.info("Reading value from cache...");
			
			currentLineData = cacheValue;
			fileData = Observable.just(new FileData(lineId, currentLineData));
		}
		else{
			LOGGER.info("Reading value from file...");
			
			Observable.OnSubscribe<FileData> getFileDataFunc = (subscriber) -> getFileData_v2(subscriber, lineId);			
			fileData =  Observable.create(getFileDataFunc)
								  .subscribeOn(Schedulers.io());							  	 
		}
		
		LOGGER.info("LineDalImpl::getLineById_v2 Execution Time [" + (System.nanoTime() - startTime)/1000000 + "] ms");
		return fileData;
	}
		
	/**
	 * Retrieve the File data using {@link MappedByteBuffer}.
	 * The retrieved data is set via the Subscriber.onNext().
	 * Once the data is retrieved Subscriber.onCompleted() is called.
	 * 
	 * @param subscriber
	 * @param lineId
	 */
	private void getFileData_v2(Subscriber<? super FileData> subscriber, long lineId){		
		long currentIdx = 1L;
		byte file_char = ' ';			
		StringBuilder fileLineData = new StringBuilder();
		
		FileInputStream fileInputStream = null;
		FileChannel fileChannel = null; 					
		MappedByteBuffer byteBuffer = null;
		
		long startTime = 0L;
		try{
			startTime = System.nanoTime();
			LOGGER.info("LineDalImpl::getFileData_v2 Started..." + lineId);
			
			fileInputStream = new FileInputStream(new File(this.filePath));
			fileChannel = fileInputStream.getChannel();
			byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
			
			for(int idx=0; idx<byteBuffer.limit(); idx++){
				file_char = byteBuffer.get();
				
				if(file_char == '\n'){
					currentIdx++;
				}				
				else if(currentIdx == lineId){									
					fileLineData.append((char) file_char);
				}
				else if (currentIdx > lineId){					
					break;
				}				
			}			
			byteBuffer.clear();
			fileChannel.close();
			fileInputStream.close();
			
			//Setting the data into cache...
			if(fileLineData.length() > 0){
				putCacheValue(lineId, fileLineData.toString());
			}
			else{
				LOGGER.info("Crossed EOF!!! [" + (currentIdx-1) + "]");
				putCacheValue("EOF", currentIdx-1);
			}
			
			if(!subscriber.isUnsubscribed()){
				subscriber.onNext(new FileData(lineId, fileLineData.toString()));
				subscriber.onCompleted();
			}						
		}
		catch(FileNotFoundException fnfe){
			LOGGER.error("File Not Found! [" + this.filePath +"]");
			subscriber.onError(fnfe);
		}
		catch(IOException ioe){
			LOGGER.error("Error in reading File! [" + this.filePath +"]");
			subscriber.onError(ioe);
		}
		catch(Exception e){
			LOGGER.error("Error in getting File Data! [" + this.filePath +"]");
			subscriber.onError(e);
		}
		finally{
			LOGGER.info("LineDalImpl::getFileData_v2 Execution Time [" + (System.nanoTime() - startTime)/1000000 + "] ms");
			LOGGER.info("LineDalImpl::getFileData_v2 Completed...");		
		}
	}
	
	/**
	 * Version #3 - Asynchronous retrieval of File line via {@link RandomAccessFile}
	 * ehCache is used to cache the already retrieved lines of the file.
	 *  
	 * @param lineId
	 * @return Observable<FileData>
	 */
	@Override	
	public Observable<FileData> getLineById_v3(long lineId){
		long startTime = System.nanoTime();		
		
		String currentLineData = null;
		Observable<FileData> fileData = null;
		
		String cacheValue = (String) getCacheValue(lineId);		
		if(cacheValue != null){
			LOGGER.info("Reading value from cache...");
			
			currentLineData = cacheValue;
			fileData = Observable.just(new FileData(lineId, currentLineData));
		}	
		else{
			LOGGER.info("Reading value from file...");
			
			Observable.OnSubscribe<FileData> getFileDataFunc = (subscriber) -> getFileData_v3(subscriber, lineId);			
			fileData =  Observable.create(getFileDataFunc)
								  .subscribeOn(Schedulers.io());							  	 
		}
		
		LOGGER.info("LineDalImpl::getLineById_v3 Execution Time [" + (System.nanoTime() - startTime)/1000000 + "] ms");
		return fileData;
	}
	
	/**
	 * Retrieve the File data using {@link RandomAccessFile}.
	 * The retrieved data is set via the Subscriber.onNext().
	 * Once the data is retrieved Subscriber.onCompleted() is called.
	 * 
	 * @param subscriber
	 * @param lineId
	 */
	private void getFileData_v3(Subscriber<? super FileData> subscriber, long lineId){		
		String fileLineData = null;
		
		RandomAccessFile randomFile = null;
		FileChannel fileChannel = null;
		ByteBuffer byteBuffer = null;
		
		byte lineDataByte;
		StringBuilder reqLineData = new StringBuilder();
		
		long startTime = 0L;
		try{
			startTime = System.nanoTime();
			LOGGER.info("LineDalImpl::getFileData_v3 Started..." + lineId);
			
			randomFile = new RandomAccessFile(this.filePath, "r");
			long counter = (lineId < this.fileChunkSize) ? (lineId - 1L) : (lineId % this.fileChunkSize);		
			long idx = lineId - counter;			
									
			if(FILE_LINE_POINTER_MAP.get(idx) != null){
				randomFile.seek(FILE_LINE_POINTER_MAP.get(idx));
				fileChannel = randomFile.getChannel();				
				byteBuffer = ByteBuffer.allocateDirect(8192);
				
				while((counter >= 0) && (fileChannel.read(byteBuffer) > 0)){
					byteBuffer.flip();
					
					while(byteBuffer.hasRemaining()){
						lineDataByte = byteBuffer.get();												
												
						if(lineDataByte == '\n'){
							counter--;
						}
						else if(counter == 0){
							reqLineData.append((char) lineDataByte);
						}
					}
					byteBuffer.clear();
				}								
				fileChannel.close();
			}
			randomFile.close();
			
			
			if(reqLineData.length() > 0){
				fileLineData = reqLineData.toString();
			}
			
			//Setting the data into cache...
			putCacheValue(lineId, fileLineData);
			
			if(!subscriber.isUnsubscribed()){
				subscriber.onNext(new FileData(lineId, fileLineData));
				subscriber.onCompleted();
			}						
		}
		catch(FileNotFoundException fnfe){
			LOGGER.error("File Not Found! [" + this.filePath +"]");
			subscriber.onError(fnfe);
		}
		catch(Exception e){
			LOGGER.error("Error in getting File Data! [" + this.filePath +"]");
			subscriber.onError(e);
		}
		finally{
			LOGGER.info("LineDalImpl::getFileData_v3 Execution Time [" + (System.nanoTime() - startTime)/1000000 + "] ms");
			LOGGER.info("LineDalImpl::getFileData_v3 Completed...");		
		}
	}		
	
	/**
	 * Helper method to retrieve a cached value.
	 * 
	 * @param cacheKey
	 * @return returnValue
	 */
	private Object getCacheValue(Object cacheKey){
		Object returnValue = null;		
		
		ValueWrapper cacheWrapper = this.cache.get(cacheKey);
		if(cacheWrapper != null){
			returnValue = cacheWrapper.get();
		}
		
		return returnValue;
	}
	
	/**
	 * Helper method to set data into the cache.
	 * 
	 * @param cacheKey
	 * @param cacheValue
	 */
	private void putCacheValue(Object cacheKey, Object cacheValue){
		this.cache.put(cacheKey, cacheValue);
	}		
}	//EOF LineDalImpl.java