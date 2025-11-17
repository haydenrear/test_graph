package com.hayden.test_graph.commit_diff_context.config;

import com.hayden.libsresolver.config.CrawlConfigProps;
import com.hayden.libsresolver.crawl.upload.S3Uploader;
import org.springframework.context.annotation.*;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Configuration
@Profile({"indexing-pipeline", "crawl"})
@Import({S3Uploader.class, CrawlConfigProps.class})
public class CommitDiffContextIndexingConfig {

    @Bean
    public AwsCredentialsProvider awsCredentials(CrawlConfigProps crawlConfigProps) {
        String accessKey = crawlConfigProps.getS3User();
        String secretKey = crawlConfigProps.getS3Password();
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

}
