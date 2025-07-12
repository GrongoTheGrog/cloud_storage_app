package com.grongo.cloud_storage_app.config;

import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsConfig {

    @Bean
    public S3Client s3 (){
        return S3Client.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    @Bean
    public S3Presigner S3presigner(){
        return S3Presigner.create();
    }

}
