package io.github.cleydyr.biblivre.backup.uniquelizer;

import io.github.cleydyr.biblivre.backup.uniquelizer.processor.ZipOperations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BiblivreBackupUniquelizerApplication {

    @Bean
    public ZipOperations zipOperations() {
        return new ZipOperations();
    }

    public static void main(String[] args) {
        System.exit(
                SpringApplication.exit(
                        SpringApplication.run(BiblivreBackupUniquelizerApplication.class, args)));
    }
}
