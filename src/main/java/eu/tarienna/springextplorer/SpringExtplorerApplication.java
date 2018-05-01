package eu.tarienna.springextplorer;

import javax.annotation.Resource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import eu.tarienna.springextplorer.service.StorageService;

@SpringBootApplication
public class SpringExtplorerApplication implements CommandLineRunner {

    @Resource
    StorageService storageService;
    
	public static void main(String[] args) {
		SpringApplication.run(SpringExtplorerApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
	    storageService.init();
	}
}
