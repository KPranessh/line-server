package com.salsify.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salsify.data.FileData;
import com.salsify.props.AppProperties;
import com.salsify.service.LineService;

import rx.Observable;

@RunWith(MockitoJUnitRunner.class)
public class CodePuzzleControllerTest {	
	
	@InjectMocks
	private LineController controller;
	
	@Mock
	private LineService service;
	
	@Mock
	private AppProperties props;
	
	private MockMvc mockMvc;
	
	ObjectMapper mapper = new ObjectMapper();
	
	@Before
	public void setUp() throws Exception {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	private ResultActions doAsync(String url) throws Exception {
	    MvcResult mvcResult = mockMvc.perform(get(url))
	        .andExpect(request().asyncStarted())
	        .andReturn();
	       
	       return mockMvc.perform(asyncDispatch(mvcResult));
	}
	
	@Test
	public void test_getFileLineById_v1_ok() throws Exception{
		Mockito.when(service.getFileLineById_v1(100L)).thenReturn(new FileData(100L, "Synchronous read via BufferedReader."));
		
		String output = doAsync("/lines/v1/100")
							.andExpect(MockMvcResultMatchers.status().isOk())
							.andReturn()
							.getResponse()
							.getContentAsString();
		
		Map<Object, Object> outputMap = mapper.readValue(output, Map.class);
		
		assertEquals(100, outputMap.get("idx"));
		assertEquals("Synchronous read via BufferedReader.", outputMap.get("data"));
	}
	
	@Test
	public void test_getFileLineById_v1_payload_large() throws Exception{
		Mockito.when(service.getFileLineById_v1(1000000L)).thenReturn(new FileData(100L, null));
		
		String output = doAsync("/lines/v1/1000000")
							.andExpect(MockMvcResultMatchers.status().isPayloadTooLarge())
							.andReturn()
							.getResponse()
							.getContentAsString();
		
		Map outputMap = mapper.readValue(output, Map.class);
		
		assertEquals(1000000, outputMap.get("idx"));
		assertEquals(null, outputMap.get("data"));
	}

	@Test
	public void test_getFileLineById_v2_ok() throws Exception{
		Mockito.when(service.getFileLineById_v2(1000L)).thenReturn(Observable.just(new FileData(1000L, "Asynchronous read via MappedByteBuffer.")));
		
		String output = doAsync("/lines/v2/1000")
							.andExpect(MockMvcResultMatchers.status().isOk())
							.andReturn()
							.getResponse()
							.getContentAsString();
		
		Map outputMap = mapper.readValue(output, Map.class);
		
		assertEquals(1000, outputMap.get("idx"));
		assertEquals("Asynchronous read via MappedByteBuffer.", outputMap.get("data"));
	}
	
	@Test
	public void test_getFileLineById_v2_payload_large() throws Exception{
		Mockito.when(service.getFileLineById_v2(1000000L)).thenReturn(Observable.just(new FileData(1000000L, null)));
		
		String output = doAsync("/lines/v2/1000000")
							.andExpect(MockMvcResultMatchers.status().isPayloadTooLarge())
							.andReturn()
							.getResponse()
							.getContentAsString();
		
		Map outputMap = mapper.readValue(output, Map.class);
		
		assertEquals(1000000, outputMap.get("idx"));
		assertEquals(null, outputMap.get("data"));
	}
	
	@Test
	public void test_getFileLineById_v3_ok() throws Exception{
		Mockito.when(service.getFileLineById_v3(1000L)).thenReturn(Observable.just(new FileData(1000L, "Asynchronous read via RandomAccessFile in chunks.")));
		
		String output = doAsync("/lines/v3/10000")
							.andExpect(MockMvcResultMatchers.status().isOk())
							.andReturn()
							.getResponse()
							.getContentAsString();
				
		Map outputMap = mapper.readValue(output, Map.class);
		
		assertEquals(10000, outputMap.get("idx"));
		assertEquals("Asynchronous read via RandomAccessFile in chunks.", outputMap.get("data"));
	}
	
	@Test
	public void test_getFileLineById_v3_payload_large() throws Exception{
		Mockito.when(service.getFileLineById_v3(1000000L)).thenReturn(Observable.just(new FileData(1000000L, null)));
		
		String output = doAsync("/lines/v3/1000000")
							.andExpect(MockMvcResultMatchers.status().isPayloadTooLarge())
							.andReturn()
							.getResponse()
							.getContentAsString();
		
		Map outputMap = mapper.readValue(output, Map.class);
		
		assertEquals(1000000, outputMap.get("idx"));
		assertEquals(null, outputMap.get("data"));
	}
}

