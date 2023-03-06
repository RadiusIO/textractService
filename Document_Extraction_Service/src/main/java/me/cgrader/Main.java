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

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
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
