package com.salsify.util;

public class LinePreProcessor {
//	
//	public static void main(String[] args) throws IOException{		
//		Scanner scanner = new Scanner(System.in);
//		
//		System.out.print("Input File Name:: ");
//		String ipFile = scanner.next();
//		
//		System.out.print("File Chunk Size:: ");
//		long fileChunkSize = scanner.nextLong();
//		
//		scanner.close();
//		
//		new LinePreProcessor().preProcessInputFileMemoryMapped(ipFile, fileChunkSize);
//	}
//
//	private void preProcessInputFileMemoryMapped(String ipFile, long chunkSize) throws IOException{
//		long startTime = System.nanoTime();
//		
//		BufferedWriter writer = null;
//		RandomAccessFile raf = null;
//		FileChannel fileChannel = null;
//		MappedByteBuffer byteBuffer = null;
//		
//		try{
//			writer = new BufferedWriter(new FileWriter(ipFile + ".processed"));		
//			raf = new RandomAccessFile(ipFile, "r");
//			fileChannel = raf.getChannel();
//			
//			long lineIdx = 1L;
//			long offset = 0L;		
//			long bufferSize = 131072L;
//			
//			StringBuilder builder = new StringBuilder();
//			
//			writer.write("1,0\n");
//			while((fileChannel.size() - offset) > 0){				
//				bufferSize = ((fileChannel.size() - offset) < (bufferSize)) ? (fileChannel.size() - offset) : bufferSize;		
//				
//				byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, offset, bufferSize);							
//				while(byteBuffer.hasRemaining()){				
//					offset++;
//					
//					if(byteBuffer.get() == '\n'){
//						lineIdx++;
//						
//						if((lineIdx > 1) && (lineIdx % chunkSize) == 0){
//							builder.append(lineIdx).append(",").append(offset).append("\n");
//							writer.write(builder.toString());
//							builder.setLength(0);
//						}
//					}			
//				}			
//				byteBuffer.clear();
//				fileChannel.force(true);	
//			}
//			
//			raf.close();
//			fileChannel.close();		
//			writer.close();
//		}
//		catch(IOException ioe){
//			throw ioe;
//		}
//		finally{
//			System.out.println("Pre-processed File [" + (System.nanoTime() - startTime)/1000000 + "] ms");
//		}
//	}
}
