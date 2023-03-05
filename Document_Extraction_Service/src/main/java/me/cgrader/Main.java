package me.cgrader;

import me.cgrader.storage.IStorageService;
import me.cgrader.storage.StorageProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;

import static me.cgrader.textract.AnalyzeDocument.analyzeDoc;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);

//		runtest();
	}

	private static void runtest() {
		String sourceDoc;
//		sourceDoc = args[0];
//		sourceDoc = "./testfiles/bermuda.png";
//		sourceDoc = "./testfiles/sciencepaper.png";
		sourceDoc = "./testfiles/diagram.jpeg";
		Region region = Region.US_EAST_2;
		TextractClient textractClient = TextractClient.builder()
				.region(region)
				.credentialsProvider(ProfileCredentialsProvider.create())
				.build();

		analyzeDoc(textractClient, sourceDoc);
		textractClient.close();
	}

	@Bean
	CommandLineRunner initStorage(IStorageService storageService) {
		return (args) -> {
			storageService.init();
		};
	}
	@Bean
	TextractClient initTextractClient() {
		return
			TextractClient.builder()
					.region(Region.US_EAST_2)
					.credentialsProvider(ProfileCredentialsProvider.create())
					.build();
	}
}
