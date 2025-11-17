package com.hayden.test_graph.commit_diff_context.init.mountebank.indexing;

import com.hayden.test_graph.commit_diff_context.init.mountebank.indexing.ctx.IndexingMbInitCtx;
import com.hayden.test_graph.meta.ctx.MetaCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mbtest.javabank.Client;
import org.mbtest.javabank.fluent.ImposterBuilder;
import org.mbtest.javabank.http.core.Stub;
import org.mbtest.javabank.http.imposters.Imposter;
import org.mbtest.javabank.http.predicates.Predicate;
import org.mbtest.javabank.http.predicates.PredicateType;
import org.mbtest.javabank.http.responses.Is;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Node that creates mountebank imposters for mocking Maven repository responses.
 * Translates IndexingMbInitCtx Maven mocks into HTTP imposters that simulate
 * Maven Central repository structure and source jar downloads.
 */
@Component
@Slf4j
@ResettableThread
public class MavenRepositoryResponseNode implements IndexingMbInitNode {

    private static final int MAVEN_PORT = 8080;

    public Stream<Imposter> createGetImposters(IndexingMbInitCtx ctx) {
        Imposter stub = new ImposterBuilder()
                .onPort(MAVEN_PORT)
                .build();

        // Add directory listing stubs
        stub = addDirectoryListingStubs(stub, ctx);

        // Add metadata stubs
        stub = addMetadataStubs(stub, ctx);

        // Add sources jar download stubs
        stub = addSourcesJarStubs(stub, ctx);

        // Add POM file stubs
        stub = addPomStubs(stub, ctx);

        return Stream.of(stub);
    }

    @SneakyThrows
    private Imposter addDirectoryListingStubs(Imposter builder, IndexingMbInitCtx ctx) {
        // Root directory listing
        Document rootListing = createDirectoryListing(
                new String[]{"com/", "org/", "net/"},
                false
        );

        Stub rootStub = new Stub();
        Predicate rootPredicate = new Predicate(PredicateType.EQUALS)
                .withMethod("GET")
                .withPath("/maven2/");
        rootStub.addPredicates(java.util.List.of(rootPredicate));
        rootStub.addResponse(new Is()
                .withStatusCode(200)
                .withHeader("Content-Type", "text/html")
                .withBody(rootListing.html()));

        builder = builder.addStub(rootStub);

        // Group ID directory listings
        for (IndexingMbInitCtx.MavenRepositoryMock mock : ctx.getMavenMocks()) {
            String groupPath = mock.groupId().replace('.', '/');
            
            // Group directory (e.g., /maven2/com/example/)
            Document groupListing = createDirectoryListing(
                    new String[]{mock.artifactId() + "/"},
                    false
            );
            
            Stub groupStub = new Stub();
            String groupPredicate = "/maven2/" + groupPath + "/";
            Predicate groupPred = new Predicate(PredicateType.EQUALS)
                    .withMethod("GET")
                    .withPath(groupPredicate);
            groupStub.addPredicates(java.util.List.of(groupPred));
            groupStub.addResponse(new Is()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "text/html")
                    .withBody(groupListing.html()));
            
            builder = builder.addStub(groupStub);
            
            // Artifact directory (e.g., /maven2/com/example/my-project/)
            Document artifactListing = createDirectoryListing(
                    new String[]{mock.version() + "/", "maven-metadata.xml"},
                    true  // Has metadata
            );
            
            Stub artifactStub = new Stub();
            String artifactPath = "/maven2/" + groupPath + "/" + mock.artifactId() + "/";
            Predicate artifactPred = new Predicate(PredicateType.EQUALS)
                    .withMethod("GET")
                    .withPath(artifactPath);
            artifactStub.addPredicates(java.util.List.of(artifactPred));
            artifactStub.addResponse(new Is()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "text/html")
                    .withBody(artifactListing.html()));
            
            builder = builder.addStub(artifactStub);
            
            // Version directory (e.g., /maven2/com/example/my-project/1.0.0/)
            Document versionListing = createDirectoryListing(
                    new String[]{
                            mock.artifactId() + "-" + mock.version() + "-sources.jar",
                            mock.artifactId() + "-" + mock.version() + ".pom"
                    },
                    false
            );
            
            Stub versionStub = new Stub();
            String versionPath = "/maven2/" + groupPath + "/" + mock.artifactId() + "/" + mock.version() + "/";
            Predicate versionPred = new Predicate(PredicateType.EQUALS)
                    .withMethod("GET")
                    .withPath(versionPath);
            versionStub.addPredicates(java.util.List.of(versionPred));
            versionStub.addResponse(new Is()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "text/html")
                    .withBody(versionListing.html()));
            
            builder = builder.addStub(versionStub);
        }

        return builder;
    }

    @SneakyThrows
    private Imposter addMetadataStubs(Imposter builder, IndexingMbInitCtx ctx) {
        for (IndexingMbInitCtx.MavenRepositoryMock mock : ctx.getMavenMocks()) {
            String groupPath = mock.groupId().replace('.', '/');
            String metadataPath = "/maven2/" + groupPath + "/" + mock.artifactId() + "/maven-metadata.xml";
            
            String metadata = String.format("""
                    <?xml version="1.0" encoding="UTF-8"?>
                    <metadata modelVersion="1.1.0">
                        <groupId>%s</groupId>
                        <artifactId>%s</artifactId>
                        <versioning>
                            <latest>%s</latest>
                            <release>%s</release>
                            <versions>
                                <version>%s</version>
                            </versions>
                            <lastUpdated>20240101000000</lastUpdated>
                        </versioning>
                    </metadata>
                    """, mock.groupId(), mock.artifactId(), mock.version(), mock.version(), mock.version());
            
            Stub metadataStub = new Stub();
            Predicate metadataPred = new Predicate(PredicateType.EQUALS)
                    .withMethod("GET")
                    .withPath(metadataPath);
            metadataStub.addPredicates(java.util.List.of(metadataPred));
            metadataStub.addResponse(new Is()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "application/xml")
                    .withBody(metadata));
            
            builder = builder.addStub(metadataStub);
        }

        return builder;
    }

    @SneakyThrows
    private Imposter addSourcesJarStubs(Imposter builder, IndexingMbInitCtx ctx) {
        for (IndexingMbInitCtx.MavenRepositoryMock mock : ctx.getMavenMocks()) {
            String groupPath = mock.groupId().replace('.', '/');
            String jarPath = "/maven2/" + groupPath + "/" + mock.artifactId() + "/" + mock.version() + "/" +
                    mock.artifactId() + "-" + mock.version() + "-sources.jar";
            
            // Read the actual jar file if available
            byte[] jarContent = new byte[0];
            if (mock.sourcesJarPath() != null && Files.exists(mock.sourcesJarPath())) {
                jarContent = Files.readAllBytes(mock.sourcesJarPath());
            } else {
                // Create minimal jar stub (just a basic binary content)
                jarContent = "PK\u0003\u0004".getBytes(); // ZIP file header
            }
            
            Stub jarStub = new Stub();
            Predicate jarPred = new Predicate(PredicateType.EQUALS)
                    .withMethod("GET")
                    .withPath(jarPath);
            jarStub.addPredicates(java.util.List.of(jarPred));
            jarStub.addResponse(new Is()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "application/java-archive")
                    .withHeader("Content-Length", String.valueOf(jarContent.length))
                    .withBody(new String(jarContent)));
            
            builder = builder.addStub(jarStub);
            
            log.debug("Added sources jar stub for {}", mock.artifactId());
        }

        return builder;
    }

    @SneakyThrows
    private Imposter addPomStubs(Imposter builder, IndexingMbInitCtx ctx) {
        for (IndexingMbInitCtx.MavenRepositoryMock mock : ctx.getMavenMocks()) {
            String groupPath = mock.groupId().replace('.', '/');
            String pomPath = "/maven2/" + groupPath + "/" + mock.artifactId() + "/" + mock.version() + "/" +
                    mock.artifactId() + "-" + mock.version() + ".pom";
            
            String pom = String.format("""
                    <?xml version="1.0" encoding="UTF-8"?>
                    <project xmlns="http://maven.apache.org/POM/4.0.0"
                             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                                                 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                        <modelVersion>4.0.0</modelVersion>
                        <groupId>%s</groupId>
                        <artifactId>%s</artifactId>
                        <version>%s</version>
                    </project>
                    """, mock.groupId(), mock.artifactId(), mock.version());
            
            Stub pomStub = new Stub();
            Predicate pomPred = new Predicate(PredicateType.EQUALS)
                    .withMethod("GET")
                    .withPath(pomPath);
            pomStub.addPredicates(java.util.List.of(pomPred));
            pomStub.addResponse(new Is()
                    .withStatusCode(200)
                    .withHeader("Content-Type", "application/xml")
                    .withBody(pom));
            
            builder = builder.addStub(pomStub);
        }

        return builder;
    }

    private Document createDirectoryListing(String[] items, boolean includeMetadata) {
        Document doc = new Document("");
        
        if (includeMetadata) {
            Element metaLink = new Element("a");
            metaLink.attr("href", "maven-metadata.xml");
            doc.body().appendChild(metaLink);
        }

        for (String item : items) {
            Element link = new Element("a");
            link.attr("href", item);
            link.text(item);
            doc.body().appendChild(link);
        }

        return doc;
    }
}
