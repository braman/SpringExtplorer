package kz.ramanqul.upwork.mark_joachim.SpringExtplorer;

import javax.annotation.Resource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import kz.ramanqul.upwork.mark_joachim.SpringExtplorer.service.StorageService;

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
