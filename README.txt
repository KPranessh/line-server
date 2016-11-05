# line-server
git repository for a line server

# Line Server Rest API
A REST API application to service lines out of a file to the network clients. This service looks up the file and returns the content of the requested line # and also the HTTP status along with it (200 - If the data was successfully retrieved, 413 - If the requested line # > max lines in the file). The output format of this API would be in JSON (Javascript Object Notation).

## API ROUTES
This application is a web service that supports the following routes. Just to showcase my thought process, I have included routes for all the 3 variations with which this service was implemented. Please use version #3 for your usage...

	* /lines/v1/{lineId} - Version #1 of this service which uses BufferReader approach to scan the file one line at a time and return the response. This approach makes use of ehCache to cache the already visited line # and its associated line from file in memory. This cache has a max entries limit in the memory and uses LFU for eviction
		a) Open a BufferedReader handle to the input file
		b) Check if we have already encountered an EOF scenario before and if the current input lineIdx >= EOF Idx
			b.1) If yes, then return null and http status 413
			b.2) If no, check if the input lineIdx is already cached
				b.2.1) If yes, return the cached data and http status 200
				b.2.2) If no, continue processing as below
		c) Start reading the file from top one line at a time
		d) When we encounter with the required lineIdx, stop and return the data and http status 200

	* /lines/v2/{lineId} - Version #2 of this service which uses MappedByteBuffer approach to load the file one page at a time into virtual memory, process it and return the response. This approach was taken to speed up the line by line file reading process by preventing the step of transferring the data into buffers and instead utilizing the kernel space. This approach makes use of ehCache to cache the already visited line # and its associated line from file in memory. This cache has a max entries limit in the memory and uses LFU for eviction. This approach makes use of Rx Observables to perform the actual processing asynchronously in a seperate thread and free up the web container thread to accept more http requests. This improves the service to handle increased/ spikes in loads
		a) Open a FileInputStream handle to the input file
		b) Get the File Channel and a handle to the MappedByteBuffer
		b) Check if we have already encountered an EOF scenario before and if the current input lineIdx >= EOF Idx
			b.1) If yes, then return null and http status 413
			b.2) If no, check if the input lineIdx is already cached
				b.2.1) If yes, return the cached data and http status 200
				b.2.2) If no, continue processing as below
		c) Start reading the file from top one line at a time
		d) When we encounter with the required lineIdx, stop and return the data and http status 200

	* /lines/v3/{lineId} - Version #3 of this service which uses RandomAccessFile approach to read just the required lines with an offset (configurable file chunk size) and return the response. This approach speeds up the process further since now we are not scanning all the lines of a file upto the destination line#. This approach supports reading large files as we identify several checkpoints within the file and the read process starts from one of those checkpoints and at the max, reads only the lines upto the next checkpoint. This approach makes use of ehCache to cache the already visited line # and its associated line from file in memory. This cache has a max entries limit in the memory and uses LFU for eviction. This approach makes use of Rx Observables to perform the actual processing asynchronously in a seperate thread and free up the web container thread to accept more http requests. This improves the service to handle increased/ spikes in loads
		a) Before starting the Server, run the InputFilePreprocessor.sh script to pre-process the Input File
		a) When the server starts up, load the pre-processed input file into a ConcurrentHashMap with key=lineIdx and value=starting byte of the file line data. The lineIdx to be pre-processed is determined by the file-chunk-size property
			a.1) file-chunk-size = 25 means every 25th lineIdx is pre-processed and stored in the ConcurrentHashMap
		b) Get a RandomAccessFile handle to the file
		b) Depending on the input lineIdx, determine the closest lineIdx to consider using this formula (lineIdx - ((lineIdx < fileChunkSize) ? (lineIdx - 1) : (lineIdx % fileChunkSize)))
		c) Check if the Map contains the closest lineIdx
			c.1) If no, then return null and http status 413
			b.2) If yes, then start reading file line from the calculated lineIdx till we reach the required lineIdx and return data + http status 200		

## INPUT DATA FILES
Any File can be used as an input as long as the below 3 conditions are met
	a) Each line is terminated with a newline ("\n")
	b) Any given line will fit into memory
	c) The line is valid ASCII (e.g. not Unicode)
 

## SAMPLE OUTPUT
	{
	 "idx" : 100,
	 "data" : "Line read from the file"
	}


## STEPS TO EXECUTE THE PROJECT
	* Copy the project into your system and go to the main folder (line-server)
	* Place an input file (meeting the above criterias) into the line-server/input folder
	* Go to line-server/src/main/resources/application.yml to modify couple of parameters
		* line-server.file-path :: ./input/{File Name}
		* line-server.file-chunk-size :: {This value determines after how many lines in the file, do we need to put a checkpoint. 
		  The checkpoints are stored in a ConcurrentHashMap with key=lineIdx(Long) and value=starting byte of the line(Long).
			* fileSize < 1 GB, fileChunkSize = 10
			* fileSize < 30 GB, fileChunkSize = 50
			* fileSize < 100 GB, fileChunkSize = 150
	* Execute line-server/build.sh to build and compile the project from pom
	* Execute the line-server/InputFilePreprocessor.sh to pre-process the Input File before starting the server. Executing this script will require 2 inputs from user
		* Input File Name:: <Same as above file name>
		* File Chunk Size:: <Same as above file chunk size>
	* Execute the run.sh to start the instance of line-server
	* Test via the URL http://localhost:8080/lines/v3/{lineId}
	* Logs are generated in the line-server/logs folder


## NOTES
 a) How does your system work?
Explained above in the API ROUTES section

 b) How will your system perform with a 1 GB file? a 10 GB file? a 100 GB file?
Considering the File is pre-processed first and then used by the line-server service, predictable performance is achieved for very huge files as well.

REFER TO ATTACHED THE PERFORMANCE METRICS - line-server/load test

 c) How will your system perform with 100 users? 10000 users? 1000000 users?
I have tested with upto 100 Users (Threads) va JMeter. Anything above that and my system was choking. But we can spawn multiple instances of ths line-server to scale to higher # of users.

REFER TO ATTACHED THE PERFORMANCE METRICS - line-server/load test 

 d) What documentation, websites, papers, etc did you consult in doing this assignment?
Spring.io, stackoverflow, ehcache.org, docs.oracle.com, javarevisted.com, programcreek.com

 e) What third-party libraries or other tools does the system use? How did you choose each library or framework you used?
Spring Boot Framework - To build a Rest API from ground up in very short amount of time and it also comes with battle tested architectural patterns built in
ehCache - Needed a memory cache for fast put and get in concurrent environment
RxJava - Reactive asynchronous framework to spawn worker threads to perform io/computational work

 f) How long did you spend on this exercise? If you had unlimited more time to spend on this, how would you spend it and how would you prioritize each item?
I spent couple of nights in setting this up. Having more time
	* I would work more on profiling and tuning the performance of this system with huge files and 100000 and more concurrent users
	* I would also work on polishing the documentation and writing more comprehensive unit tests
	* I would like to work on a pre-process script out side the application that takes huge files and generates the LineIdx-StartingByte Information

 g) If you were to critique your code, what would you have to say about it?
I didn't do much of profiling to this code, heap, GC, thread usage and just went with metrics to see how the system was behaving under different load.
I feel I could do more tuning for the initial pre-processing of the file by dividing them into smaller chunks and processing it OR may be even use MappedByteBuffer (X bytes at a time) to memory map the huge file and populate the ConcurrentHashMap
Currently there is limitation in the system where in huge files (> 100 GB) will require 30 minutes for pre-processing.