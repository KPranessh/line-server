package com.salsify;

public class Test {
//	
//	public static void main(String[] args) throws Exception{
//		
//		Test obj = new Test();
//		
//		obj.populateFilePointerMap("./input/http_plugin_35.log.processed", 200L);
////		obj.splitRead("./input/http_plugin.log");
////		obj.preProcessInputFileDirect("./input/http_plugin_35.log", "./input/http_plugin_35_processed.txt", 200L);
//		obj.preProcessInputFileMemoryMapped("./input/http_plugin_35.log", "./input/http_plugin_35_processed.txt", 200L);
////		obj.createFileCopies("./input/http_plugin_35.log", "./input/Output/http_plugin_op_35.log", 100L);
////		obj.generateNewFiles("./input/http_plugin_5.log", 10, "./input/http_plugin_10.log");
////		obj.fileReadRandom("./input/http_plugin_35.log", 1440000L, 1000);
////		obj.FileRead01("./input/http_plugin.log", 1000000L, true);
////		obj.FileRead02("./input/http_plugin.log", 1000000L, false);
////		for(long idx=1000000L; idx<1000100L; idx++){
////			obj.FileRead02("./input/http_plugin.log", idx, false);
////		}		
////		obj.FileRead03("./input/http_plugin.log", 1000000L);
//		
////		obj.testObMultiThread();
//	}
//	
//	private void populateFilePointerMap(String ipFile, long chunkSize) throws Exception{
//		long startTime = System.nanoTime();
//		ConcurrentHashMap<Long, Long> file_pointer_map = new ConcurrentHashMap<>();
//		long[] file_pointer_array = new long[10000000];
//		
//		BufferedReader reader = new BufferedReader(new FileReader(ipFile));
//				
//		String line = null;
//		String[] lineArray = new String[2];
//		while((line = reader.readLine()) != null){
//			lineArray = line.split(",");
//			
//			file_pointer_array[(int) (Long.parseLong(lineArray[0])/chunkSize)] = Long.parseLong(lineArray[1]);
////			file_pointer_map.put(Long.valueOf(lineArray[0]), Long.valueOf(lineArray[1]));
//		}
//		reader.close();
//		
////		System.out.println(file_pointer_map.size());
//		System.out.println(file_pointer_array.length);
//		System.out.println("Pre-processed File [" + (System.nanoTime() - startTime)/1000000 + "] ms");
//	}
//	
//	private void preProcessInputFileDirect(String ipFile, String opFile, long chunkSize) throws Exception{
//		long startTime = System.nanoTime();
//		
//		BufferedWriter writer = new BufferedWriter(new FileWriter(opFile));		
//		final FileInputStream fileInputStream = new FileInputStream(ipFile);			
//		final FileChannel fileChannel = fileInputStream.getChannel();				
//
//		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(262144);
//		
//		long lineIdx = 1;
//		long offset = 0L;
//		
//		writer.write(lineIdx + "," + offset + "\n");
//		while(fileChannel.read(byteBuffer) > 0){
//			byteBuffer.flip();
//			
//			for(int idx=0; idx<byteBuffer.limit(); idx++){					
//				offset++;
//				
//				if(byteBuffer.get() == '\n'){
//					lineIdx++;
//					
//					if((lineIdx > 1) && (lineIdx % chunkSize) == 0){
//						writer.write(lineIdx + "," + (offset+1L) + "\n");
//						
//					}
////					if((lineIdx % 1000000) == 0){
////						System.out.println(lineIdx);
////					}	
//				}			
//			}
//			byteBuffer.clear();			
//		}
//		
//		fileInputStream.close();
//		fileChannel.close();		
//		writer.close();
//		
//		System.out.println("Pre-processed File [" + (System.nanoTime() - startTime)/1000000 + "] ms");
//	}
//	
//	private void preProcessInputFileMemoryMapped(String ipFile, String opFile, long chunkSize) throws Exception{
//		long startTime = System.nanoTime();
//		
//		BufferedWriter writer = new BufferedWriter(new FileWriter(opFile));				
////		final FileInputStream fileInputStream = new FileInputStream(ipFile);
//		final RandomAccessFile raf = new RandomAccessFile(ipFile, "r");
//		final FileChannel fileChannel = raf.getChannel();
//		
//		MappedByteBuffer byteBuffer = null;		
//		
//		long lineIdx = 1L;
//		long offset = 0L;		
//		long bufferSize = 131072L;
//		
//		StringBuilder builder = new StringBuilder();
//		
//		writer.write("1,0\n");
//		while((fileChannel.size() - offset) > 0){			
//			
//			bufferSize = ((fileChannel.size() - offset) < (bufferSize)) ? (fileChannel.size() - offset) : bufferSize;		
////			System.out.println("TotalBytesToRead " + (fileChannel.size() - offset) + "   Offset " + offset + "   BufferSize " + bufferSize);
//			
//			byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, offset, bufferSize);							
//						
//			while(byteBuffer.hasRemaining()){				
//				offset++;
//				
//				if(byteBuffer.get() == '\n'){
//					lineIdx++;
//					
//					if((lineIdx > 1) && (lineIdx % chunkSize) == 0){
//						builder.append(lineIdx).append(",").append(offset).append("\n");
//						writer.write(builder.toString());
//						builder.setLength(0);
//					}
////					if((lineIdx % 1000000) == 0){
////						System.out.println(lineIdx);
////					}
//				}			
//			}			
//			byteBuffer.clear();
//			fileChannel.force(true);	
//		}
//		
//		raf.close();
//		fileChannel.close();		
//		writer.close();
//		
//		System.out.println("Pre-processed File [" + (System.nanoTime() - startTime)/1000000 + "] ms");
//	}
//	
//	@SuppressWarnings("unused")
//	private void createFileCopies(String ipFile, String opFile, long pieces) throws Exception{
//		FileInputStream fis = new FileInputStream(ipFile);
//		FileChannel inChannel = fis.getChannel();		
//		
//		long size = inChannel.size();		
//		long position = -1L;
//		long chunkSize = size/pieces;
//		long processedSize = 0L;
//		long transferSize = 0L;
//		
//		int counter = 1;
//		System.out.println("Total Size" + size);
//		while(size > 0){
//			FileOutputStream fos = new FileOutputStream(opFile + ".part" + counter);
//			FileChannel outChannel = fos.getChannel();
//			
//			transferSize = (size > chunkSize) ? chunkSize : size;
//			processedSize = inChannel.transferTo((position + 1L), transferSize, outChannel);
//			
//			position += processedSize;
//			size -= processedSize;
//			
//			fos.close();
//			outChannel.close();
//			counter++;
//		}
//				
//		fis.close();
//		inChannel.close();		
//	}
//	
//	private void generateNewFiles(String filePath, int iterations, String opFile) throws Exception{
//				
//		BufferedWriter writer = new BufferedWriter(new FileWriter(opFile));
//		for (int idx=0; idx<iterations; idx++){
//			BufferedReader reader = new BufferedReader(new FileReader(filePath));			
//			
//			String lineData = null;
//			while((lineData = reader.readLine()) != null){
//				writer.write(lineData + "\n");
//			}
//			
//			reader.close();
//		}
//		
//		writer.close();
//	}
//	private void fileReadRandom(String filePath, long lineId, int chunkSize) throws Exception{
//		long startTime = System.nanoTime();	
//		
////		BufferedReader reader = new BufferedReader(new FileReader(filePath), 8192*2);
////				
////		AtomicInteger atomicInteger = new AtomicInteger(0);
////		
////		reader.lines()
////			  .parallel()
////			  .mapToInt(line -> atomicInteger.getAndIncrement())
////			  .filter(counter -> counter % 1000000 == 0)
////			  .forEach(lineIdx -> System.out.println(lineIdx));
////		
////		reader.close();
//		
//////		RandomAccessFile file = new RandomAccessFile(filePath, "r");		
////		BufferedReader reader = new BufferedReader(new FileReader(filePath), 8192*2);
//		
//////		LineNumberReader reader = new LineNumberReader(new FileReader(filePath), 1024*1024);
////		BufferedWriter writer = new BufferedWriter(new FileWriter("./input/output.csv"));
////		
//////		ConcurrentHashMap<Long, Long> FILE_LINE_POINTER_MAP = new ConcurrentHashMap<>();		
//////		
////		long lineIdx = 1L;
////		long offset = 0L;
////		String linedata = null;
////////		
//////		FILE_LINE_POINTER_MAP.put(lineIdx, offset);		
////		writer.write(lineIdx + "," + offset + "\n");	
////		while((linedata = reader.readLine()) != null){				
//////			lineIdx++;
//////			if((reader.getLineNumber() % 1000000) == 0){
//////				System.out.println(reader.getLineNumber());
//////			}
////			if((lineIdx > 1) && (lineIdx % chunkSize) == 0){				
//////				FILE_LINE_POINTER_MAP.put(lineIdx, (offset + 1L));
////				
////				writer.write(lineIdx + "," + (offset + 1L) + "\n");				
//////				System.out.println(lineIdx + "->" + (offset+1L));								
////			}
//////			
////			offset += linedata.length() + 1L;			
//////			System.out.println("offset " + offset);
////			
//////			System.out.println(lineIdx);
////			lineIdx++;
//////			linedata = file.readLine();
////		}
////////			  
////		reader.close();
////		writer.close();
////		System.out.println(FILE_LINE_POINTER_MAP.size());
////		System.out.println("Load FILE_LINE_POINTER_MAP [" + (System.nanoTime() - startTime)/1000000 + "]");
//		
//		BufferedWriter writer = new BufferedWriter(new FileWriter("./input/output.csv"));		
////		ConcurrentHashMap<Long, Long> FILE_LINE_POINTER_MAP = new ConcurrentHashMap<>();
////		long[] file_pointer_data = new long[1000000];
//////
////		MappedByteBuffer byteBuffer = null;
//		ByteBuffer byteBuffer = null;		
//		int lineIdx = 1;
//		long offset = 0L;		
//		final FileInputStream fileInputStream = new FileInputStream(filePath);			
//		final FileChannel fileChannel = fileInputStream.getChannel();				
////		
////					
//		byteBuffer = ByteBuffer.allocateDirect(16384);
//		
////		file_pointer_data[lineIdx] = offset;
//		while(fileChannel.read(byteBuffer) > 0){
//			byteBuffer.flip();
//			
//			for(int idx=0; idx<byteBuffer.limit(); idx++){					
//				offset++;
//				
//				if(byteBuffer.get() == '\n'){
//					lineIdx++;
//					
//					if((lineIdx > 1) && (lineIdx % chunkSize) == 0){				
////						FILE_LINE_POINTER_MAP.put(lineIdx, (offset+1L));					
//						writer.write(lineIdx + "," + (offset+1L) + "\n");						
////						file_pointer_data[lineIdx/chunkSize] = offset + 1L;
//						
//					}
////					if((lineIdx % 1000000) == 0){
////						System.out.println(lineIdx);
////					}	
//				}			
//			}
//			byteBuffer.clear();			
//		}
//		fileInputStream.close();
//		fileChannel.close();		
//		writer.close();
//		
////		long sizeToReadBytes = fileChannel.size();
////		System.out.println("Total Size " + sizeToReadBytes);						
////		
////		long position = -1L;
////		long bufferSize = 524288L;
////		FILE_LINE_POINTER_MAP.put(lineIdx, offset);			
////		while(sizeToReadBytes > 0){
////			
////			if(sizeToReadBytes < bufferSize){
////				bufferSize = sizeToReadBytes;
////			}
////			
////			byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, ++position, bufferSize);
////			
//////			byteBuffer.load();
////			for(int idx=0; idx<byteBuffer.limit(); idx++){				
////				offset++;
////				
////				if(byteBuffer.get() == '\n'){
////					lineIdx++;
////					
//////					if((lineIdx > 1) && (lineIdx % chunkSize) == 0){
//////						long tempOffset = offset+1L;
//////						FILE_LINE_POINTER_MAP.put(lineIdx, tempOffset);						
////////						writer.write(lineIdx + "," + (offset+1L) + "\n");					
//////					}
////					if((lineIdx % 1000000) == 0){
////						System.out.println(lineIdx);
////					}
////				}			
////			}			
////			byteBuffer.clear();	
////			System.gc();
//////			byteBuffer.reset();
////			
//////			startBufferBytes += countBufferBytes + 1L;
////		
////			sizeToReadBytes -= bufferSize;
////			position += bufferSize + 1L;
////		}
//////		
////		fileInputStream.close();
////		fileChannel.close();
////		writer.close();
////		System.out.println(FILE_LINE_POINTER_MAP.size());
////		System.out.println("Load Time [" + (System.nanoTime() - startTime)/1000000 + "]");
////////		
//////		BufferedReader reader = new BufferedReader(new FileReader(filePath));
//////		ConcurrentHashMap<Long, Long> FILE_LINE_POINTER_MAP = new ConcurrentHashMap<>();
//////		
//////		System.out.println(reader.lines().parallel().count());
//////		
////////		System.out.println(FILE_LINE_POINTER_MAP.size());
//////		System.out.println("Load FILE_LINE_POINTER_MAP [" + (System.nanoTime() - startTime)/1000000 + "]");
////		
////		startTime = System.nanoTime();
////		RandomAccessFile fileRandom = new RandomAccessFile(filePath, "r");
////		long counter = (lineId < chunkSize) ? (lineId - 1L) : (lineId % chunkSize);		
////		int idx = (int) (lineId - counter);
////		System.out.println("Counter " + counter);
////		System.out.println("Nearest Map Index " + idx);
////		
////		String fileData = null;
//////		System.out.println(FILE_LINE_POINTER_MAP.get(idx));
////		System.out.println(file_pointer_data[idx]);
////		if(FILE_LINE_POINTER_MAP.get(idx) != null){
////			fileRandom.seek(FILE_LINE_POINTER_MAP.get(idx));
////			
////			while(counter >= 0){
////				fileData = fileRandom.readLine();
////				System.out.println(counter + "->" + fileData);
////				counter--;				
////			}
////			
////			System.out.println(fileData);
////		}		
////		
////		fileRandom.close();
////		System.out.println("fileReadRandom [" + (System.nanoTime() - startTime)/1000000 + "]");
//		
//	}
//	
//	private void  FileRead01(String filePath, long lineId, boolean memoryMapped) throws Exception{
//		long startTime = System.nanoTime();
//				
////		FileInputStream fileInputStream = new FileInputStream(new File(filePath));
////		FileChannel fileChannel = fileInputStream.getChannel();
//		
//		RandomAccessFile file = new RandomAccessFile(filePath, "rw");		
//		FileChannel fileChannel = file.getChannel();
//		
////		ByteBuffer byteBuffer = null;
////		if(memoryMapped){
////			byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, 8192);
////		}
////		else{
////			byteBuffer = ByteBuffer.allocateDirect(8096);
////		}
//		
////		fileChannel.read(byteBuffer);
////		byteBuffer.flip();
//		
//		MappedByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 8192);
////		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8192*144);
////		ByteBuffer byteBuffer = ByteBuffer.allocate(8192*144);
//				
//		byte file_char = ' ';
//		long counter = 1L;
//		StringBuilder currentLineData = new StringBuilder();
//		int limit = byteBuffer.limit();
////		while(limit>0)
////		{						
////			if(counter < lineId){
////				file_char = (char)byteBuffer.get();
////				if(file_char == '\n'){
////					counter++;
////				}
////			}			
////			else if(counter == lineId){
////				file_char = (char)byteBuffer.get();
////				while(file_char != '\n'){
////					currentLineData.append(file_char);
////				}
////				
////				break;
////			}
////			else{
////				break;
////			}
////			limit--;
////		}
//		
//		while(fileChannel.read(byteBuffer) > 0){
//			byteBuffer.flip();
//			
//			for(int idx=0; idx<byteBuffer.limit(); idx++){
//				file_char = byteBuffer.get();
//				
//				if(file_char == '\n'){
//					counter++;
//				}
//				
//				if(counter == lineId){
////					System.out.println("Reached Counter " + lineId);
//					currentLineData.append((char) file_char);
//				}
//				else if (counter > lineId){
//					break;
//				}				
//			}			
//			byteBuffer.clear();
//		}
//		
//		System.out.println(currentLineData);
//		
//		fileChannel.close();
////		fileInputStream.close();
//		file.close();
//		System.out.println("FileRead01 [" + (System.nanoTime() - startTime)/1000000 + "]");
//	}
//	
//	private String getFileLineData(BufferedReader fileHandle, long lineId){
//		
//		long currentIdx = 1L;
//		String currentLineData = null;
//		
//		try{
//			while(currentIdx <= lineId){
//				currentLineData = fileHandle.readLine();
//				if(null == currentLineData){								
//					break;
//				}
//				currentIdx++;
//			}
//		}
//		catch (IOException ioe){
//			Observable.error(ioe);
//		}
//		
//		
//		return currentLineData;
//	}
//	
//	private void FileRead02(String filePath, long lineId, boolean isStreamReader) throws Exception{
//		long startTime = System.nanoTime();
//		
//		long currentIdx = 1L;
//		String currentLineData = null;
//		BufferedReader fileHandle = null;
//		
//		if(isStreamReader){
//			fileHandle = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)), 8192*144);
//		}
//		else{
//			fileHandle = new BufferedReader(new FileReader(filePath), 8192);
//		}
//		
//		while(currentIdx <= lineId){
//			currentLineData = fileHandle.readLine();
//			if(null == currentLineData){								
//				break;
//			}
//			currentIdx++;
//		}
//		System.out.println(currentLineData);
//		
////		BufferedReader fileHandle = new BufferedReader(new FileReader(filePath));
////		Observable.just(fileHandle)
////				  .subscribeOn(Schedulers.io())
////				  .map(fileReader -> getFileLineData(fileReader, lineId))
////				  .subscribe(data -> System.out.println("File Line Data::" + data + " " + Thread.currentThread().getName()),
////						     error -> System.out.println("Error::" + error),
////						     taskCompleted());
//		
////		Thread.sleep(1000);
//				
//		fileHandle.close();		
////		System.out.println("FileRead02 [" + (System.nanoTime() - startTime)/1000000 + "]");
//		System.out.println((System.nanoTime() - startTime)/1000000);
//	}
//	
//	public static Action0 taskCompleted(){
//		return new Action0() {
//			
//			@Override
//			public void call() {
//				System.out.println("Task Completed");
//				
//			}
//		};		
//	}
//	private void FileRead03(String filePath, long lineId) throws Exception{
//		long startTime = System.nanoTime();
//		
//		long currentIdx = 1L;
//		String currentLineData = null;
//		BufferedReader fileHandle = new BufferedReader(new FileReader(filePath));
//		
//		currentLineData = fileHandle.lines()									
//									.skip(lineId-1)
//									.findFirst()
//									.get();
//		
//		System.out.println(currentLineData);
//		
//		fileHandle.close();		
//		System.out.println("FileRead03 [" + (System.nanoTime() - startTime)/1000000 + "]");
//	}
//	
//	
//	private void testObMultiThread(){		
//		Observable.range(1, 10)
//				  .subscribe(num -> Observable.just(num)
//						  					  .subscribeOn(Schedulers.newThread())
//						  					  .subscribe(data -> System.out.println("Thread=" + Thread.currentThread().getName() + " Data=" + data)));
//		
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//				       
//	}
}
