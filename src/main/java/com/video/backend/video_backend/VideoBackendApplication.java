package com.video.backend.video_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
public class VideoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoBackendApplication.class, args);
	}

	@Bean(name = "transcodingExecutor")
	public Executor transcodingExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(20);
		executor.setThreadNamePrefix("transcoding-");
		executor.initialize();
		return executor;
	}

}
