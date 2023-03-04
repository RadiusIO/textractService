package me.cgrader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;

import static me.cgrader.textract.AnalyzeDocument.analyzeDoc;

@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);

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
}
