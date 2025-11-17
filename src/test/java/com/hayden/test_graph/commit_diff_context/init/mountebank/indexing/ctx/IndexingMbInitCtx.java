package com.hayden.test_graph.commit_diff_context.init.mountebank.indexing.ctx;

import com.hayden.test_graph.commit_diff_context.init.mountebank.indexing.IndexingMbInitNode;
import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.mountebank.ctx.MbInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mbtest.javabank.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@ResettableThread
@RequiredArgsConstructor
public class IndexingMbInitCtx implements MbInitCtx {

    @Builder
    public record MavenRepositoryMock(
            String groupId,
            String artifactId,
            String version,
            Path sourcesJarPath,
            Path pomPath
    ) {}

    @Builder
    public record MockedDependency(
            String groupId,
            String artifactId,
            String version,
            Path sourcesJarLocation
    ) {}

    private Client client;

    private final ContextValue<IndexingMbInitBubbleCtx> bubbleUnderlying;
    private final ContextValue<List<MavenRepositoryMock>> mavenMocks;
    private final ContextValue<Map<String, Path>> artifactPathCache;

    public IndexingMbInitCtx() {
        this(
                ContextValue.empty(),
                ContextValue.ofExisting(new ArrayList<>()),
                ContextValue.ofExisting(new HashMap<>())
        );
    }

    @Autowired
    public void setBubble(IndexingMbInitBubbleCtx bubble) {
        this.bubbleUnderlying.swap(bubble);
        this.bubbleUnderlying.res().one().get().getIndexingMbInit().swap(this);
    }

    public void addMavenRepositoryMock(MavenRepositoryMock mock) {
        var mocks = this.mavenMocks.res().orElse(new ArrayList<>());
        mocks.add(mock);
        this.mavenMocks.swap(mocks);
    }

    public void registerArtifactPath(String groupId, String artifactId, String version, Path path) {
        var cache = this.artifactPathCache.res().orElse(new HashMap<>());
        String key = String.format("%s:%s:%s", groupId, artifactId, version);
        cache.put(key, path);
        this.artifactPathCache.swap(cache);
    }

    public Optional<Path> getArtifactPath(String groupId, String artifactId, String version) {
        return this.artifactPathCache.res()
                .flatMap(cache -> Optional.ofNullable(
                        cache.get(String.format("%s:%s:%s", groupId, artifactId, version))
                ));
    }

    public List<MavenRepositoryMock> getMavenMocks() {
        return this.mavenMocks.res().orElse(new ArrayList<>());
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Optional<Client> getClient() {
        return Optional.ofNullable(client);
    }

    @Override
    public IndexingMbInitBubbleCtx bubble() {
        return this.bubbleUnderlying.res().one().get();
    }

    @Override
    public Class<IndexingMbInitBubbleCtx> bubbleClazz() {
        return IndexingMbInitBubbleCtx.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof IndexingMbInitNode;
    }

    public ContextValue<List<MavenRepositoryMock>> mavenMocks() {
        return mavenMocks;
    }

    public ContextValue<Map<String, Path>> artifactPathCache() {
        return artifactPathCache;
    }
}
