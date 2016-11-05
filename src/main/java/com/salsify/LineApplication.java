package com.salsify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import com.salsify.props.AppProperties;

/**
 * Main Execution Class of
 * 
 * line-server REST API
 * 
 * @author Pranessh Kannappan
 *
 */
@SpringBootApplication
@EnableCaching
public class LineApplication {

	/**
	 * Starting method of this Spring Boot application
	 * 
	 * @param args
	 * @return void
	 */
	public static void main(String[] args) {
		SpringApplication.run(LineApplication.class, args);
	}
	
	/**
	 * Spring Bean holding the application properties
	 * {@link AppProperties}
	 * 
	 * @return AppProperties
	 */
	@Bean
	public AppProperties props(){
		return new AppProperties();
	}
	
	/**
	 * Spring Bean holding the ehCacheManager
	 * {@link EhCacheCacheManager}
	 * 
	 * @return CacheManager
	 */
	@Bean
	public CacheManager cacheManager() {
		return new EhCacheCacheManager(ehCacheCacheManager().getObject());
	}

	@Bean
	public EhCacheManagerFactoryBean ehCacheCacheManager() {
		EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
		cmfb.setConfigLocation(new ClassPathResource("ehcache.xml"));
		cmfb.setShared(true);
		return cmfb;
	}
}	//EOF LineApplication.java