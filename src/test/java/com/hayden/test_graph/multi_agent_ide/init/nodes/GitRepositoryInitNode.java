package com.hayden.test_graph.multi_agent_ide.init.nodes;

import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.multi_agent_ide.init.ctx.MultiAgentIdeInit;
import com.hayden.test_graph.multi_agent_ide.util.GitRepositoryTestHelper;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * Init node for creating test git repositories.
 * Lazily creates repositories based on configurations gathered from step definitions.
 */
@Slf4j
@Component
@ResettableThread
public class GitRepositoryInitNode implements MultiAgentIdeInitNode {

    @Override
    public MultiAgentIdeInit exec(MultiAgentIdeInit ctx, TestGraphContext h) {
        try {
            // Get all repository specifications from context
            List<MultiAgentIdeInit.RepositorySpec> specs = ctx.getRepositorySpecs();

            // Create repositories for each specification
            for (MultiAgentIdeInit.RepositorySpec spec : specs) {
                createRepositoryFromSpec(spec, ctx);
            }

            if (!specs.isEmpty()) {
                log.info("Git repositories initialized successfully for {} spec(s)", specs.size());
            }
        } catch (Exception e) {
            log.error("Error initializing git repositories", e);
            throw new RuntimeException("Failed to initialize git repositories", e);
        }

        return ctx;
    }

    private void createRepositoryFromSpec(MultiAgentIdeInit.RepositorySpec spec, MultiAgentIdeInit ctx) throws Exception {
        Path repoPath = Paths.get(spec.sourceDirectory().toString());
        Path targetRepoPath = Paths.get("/tmp/" + spec.name() + "-" + System.currentTimeMillis());
        
        log.info("Creating repository {} from source {} to {}", spec.name(), repoPath, targetRepoPath);
        
        // Copy source files to target location
        copyDirectory(repoPath, targetRepoPath);
        
        // Initialize git repository
        Git git = GitRepositoryTestHelper.createTestRepository(targetRepoPath, new HashMap<>());
        
        // Create spec file with submodule information if applicable
        GitRepositoryTestHelper.createSpecFile(
                targetRepoPath,
                spec.nodeId(),
                spec.goal(),
                spec.parentWorktreeId(),
                spec.submoduleNames()
        );
        
        // Commit spec file
        GitRepositoryTestHelper.commitSpecFile(git, ".multi-agent-plan.md", 
                "Initialize spec for " + spec.nodeId());
        
        // Create branch if specified
        if (spec.branchName() != null) {
            GitRepositoryTestHelper.createBranch(git, spec.branchName());
        }
        
        git.close();
        log.info("Repository {} created successfully at {}", spec.name(), targetRepoPath);
    }

    /**
     * Recursively copy directory contents.
     */
    private void copyDirectory(Path source, Path destination) throws IOException {
        Files.createDirectories(destination);
        
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(sourcePath -> {
                try {
                    Path destPath = destination.resolve(source.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(destPath);
                    } else {
                        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error copying directory", e);
                }
            });
        }
    }



    @Override
    public List<Class<? extends MultiAgentIdeInitNode>> dependsOn() {
        // Can depend on other init nodes if needed
        return new ArrayList<>();
    }
}
