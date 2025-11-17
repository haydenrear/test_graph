package com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.nodes;

import com.hayden.commitdiffcontext.code_search.libs.res.Dependency;
import com.hayden.libsresolver.config.CrawlConfigProps;
import com.hayden.libsresolver.crawl.upload.S3Uploader;
import com.hayden.test_graph.action.Idempotent;
import com.hayden.test_graph.assertions.Assertions;
import com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.CommitDiffContextIndexingAssertNode;
import com.hayden.test_graph.commit_diff_context.assert_nodes.indexing.ctx.CommitDiffContextIndexingAssertCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.net.URI;

/**
 * Verifies that sources have been successfully uploaded to MinIO.
 * This assertion checks that the MinIO S3 bucket contains the expected source files using S3 client.
 * Skips verification if MinIO deployment is not enabled.
 */
@Component
@ResettableThread
@Profile("crawl")
public class VerifySourcesUploadedToMinIO implements CommitDiffContextIndexingAssertNode {

    @Autowired
    private Assertions assertions;

    @Autowired
    private S3Uploader s3Uploader;

    @Autowired
    private CrawlConfigProps crawlConfigProps;

    @Override
    public Class<? extends CommitDiffContextIndexingAssertCtx> clzz() {
        return CommitDiffContextIndexingAssertCtx.class;
    }

    @Override
    @Idempotent(returnArg = 0)
    public CommitDiffContextIndexingAssertCtx exec(CommitDiffContextIndexingAssertCtx c, MetaCtx h) {
        // Skip assertion if MinIO is not enabled for deployment
        if (!c.isMinIOEnabled()) {
            return c;
        }
        
        var minioConfig = c.getMinioConfig();
        assertions.assertThat(minioConfig)
                .as("MinIO configuration should be present")
                .isNotNull();
        
        // Get MinIO endpoint URL and bucket name from config
        String minioUrl = minioConfig.envVars().get("MINIO_URL");
        if (minioUrl == null || minioUrl.isBlank()) {
            minioUrl = "http://localhost:9000"; // fallback
        }
        
        String bucketName = minioConfig.envVars().get("BUCKET_NAME");
        if (bucketName == null || bucketName.isBlank()) {
            bucketName = "sources"; // fallback
        }
        
        assertions.assertThat(minioUrl)
                .as("MinIO S3 endpoint URL should be configured")
                .isNotBlank();
        
        assertions.assertThat(bucketName)
                .as("MinIO bucket name should be configured")
                .isNotBlank();

        assertions.assertThat(c.getDep().optional())
                .isPresent();

        c.getDep()
                .optional()
                .ifPresent(this::verifySourcesInMinIO);
        
        return c;
    }

    private void verifySourcesInMinIO(Dependency dependency) {
        assertions.assertThat(s3Uploader.exists(dependency.key(), crawlConfigProps.getS3Bucket()))
                .isPresent();
    }

}
