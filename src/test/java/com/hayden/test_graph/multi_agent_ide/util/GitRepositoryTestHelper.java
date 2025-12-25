package com.hayden.test_graph.multi_agent_ide.util;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Utility for creating test git repositories with optional submodules and initial spec files.
 * Handles initialization, committing, and organizing test fixtures.
 */
@Slf4j
public class GitRepositoryTestHelper {

    /**
     * Creates a new git repository at the specified path with initial content.
     *
     * @param repoPath Path where the repository will be created
     * @param initialFiles Map of filename -> content to create in repo
     * @return Git instance for the created repository
     */
    public static Git createTestRepository(Path repoPath, Map<String, String> initialFiles) throws IOException, GitAPIException {
        Files.createDirectories(repoPath);
        
        Git git = Git.init()
                .setDirectory(repoPath.toFile())
                .call();
        
        configureGit(git);
        
        // Add initial files
        if (!initialFiles.isEmpty()) {
            for (Map.Entry<String, String> entry : initialFiles.entrySet()) {
                writeFile(repoPath, entry.getKey(), entry.getValue());
                git.add().addFilepattern(entry.getKey()).call();
            }
            
            git.commit()
                    .setMessage("Initial commit")
                    .call();
        }
        
        log.info("Created test repository at {}", repoPath);
        return git;
    }

    /**
     * Creates a git repository with a single submodule.
     *
     * @param mainRepoPath Path for main repository
     * @param submoduleName Name of the submodule
     * @param submodulePath Path within main repo where submodule will be placed
     * @param mainRepoFiles Initial files for main repo
     * @param submoduleFiles Initial files for submodule
     * @return Git instance for main repository
     */
    public static Git createRepositoryWithSubmodule(
            Path mainRepoPath,
            String submoduleName,
            String submodulePath,
            Map<String, String> mainRepoFiles,
            Map<String, String> submoduleFiles) throws IOException, GitAPIException {
        
        // Create submodule repository first
        Path submoduleRepoPath = mainRepoPath.getParent().resolve(submoduleName + "-repo");
        Git submoduleGit = createTestRepository(submoduleRepoPath, submoduleFiles);
        submoduleGit.close();
        
        // Create main repository
        Git mainGit = createTestRepository(mainRepoPath, mainRepoFiles);
        
        // Add submodule
        mainGit.submoduleAdd()
                .setPath(submodulePath)
                .setURI(submoduleRepoPath.toAbsolutePath().toString())
                .call();
        
        // Commit submodule addition
        mainGit.add().addFilepattern(".gitmodules").call();
        mainGit.add().addFilepattern(submodulePath).call();
        mainGit.commit()
                .setMessage("Add " + submoduleName + " submodule")
                .call();
        
        log.info("Created repository with submodule at {} with submodule {}", mainRepoPath, submodulePath);
        return mainGit;
    }

    /**
     * Creates a git repository with multiple submodules.
     *
     * @param mainRepoPath Path for main repository
     * @param submodules Map of submoduleName -> (submodulePath, files)
     * @param mainRepoFiles Initial files for main repo
     * @return Git instance for main repository
     */
    public static Git createRepositoryWithMultipleSubmodules(
            Path mainRepoPath,
            Map<String, SubmoduleSpec> submodules,
            Map<String, String> mainRepoFiles) throws IOException, GitAPIException {
        
        // Create all submodule repositories first
        Map<String, Path> submodulePaths = new HashMap<>();
        for (Map.Entry<String, SubmoduleSpec> entry : submodules.entrySet()) {
            String name = entry.getKey();
            SubmoduleSpec spec = entry.getValue();
            
            Path submoduleRepoPath = mainRepoPath.getParent().resolve(name + "-repo");
            Git submoduleGit = createTestRepository(submoduleRepoPath, spec.initialFiles);
            submoduleGit.close();
            
            submodulePaths.put(name, submoduleRepoPath);
        }
        
        // Create main repository
        Git mainGit = createTestRepository(mainRepoPath, mainRepoFiles);
        
        // Add all submodules
        for (Map.Entry<String, SubmoduleSpec> entry : submodules.entrySet()) {
            String name = entry.getKey();
            SubmoduleSpec spec = entry.getValue();
            Path submoduleRepoPath = submodulePaths.get(name);
            
            mainGit.submoduleAdd()
                    .setPath(spec.path)
                    .setURI(submoduleRepoPath.toAbsolutePath().toString())
                    .call();
        }
        
        // Commit all submodules
        mainGit.add().addFilepattern(".gitmodules").call();
        for (SubmoduleSpec spec : submodules.values()) {
            mainGit.add().addFilepattern(spec.path).call();
        }
        mainGit.commit()
                .setMessage("Add all submodules")
                .call();
        
        log.info("Created repository with {} submodules at {}", submodules.size(), mainRepoPath);
        return mainGit;
    }

    /**
     * Creates a spec file (.multi-agent-plan.md) in the repository.
     *
     * @param repoPath Repository path
     * @param nodeId Node ID for the spec
     * @param goal Goal description
     * @param parentWorktreeId Parent worktree ID (null if root)
     * @param submoduleNames List of submodule names if applicable
     * @return Path to created spec file
     */
    public static Path createSpecFile(
            Path repoPath,
            String nodeId,
            String goal,
            String parentWorktreeId,
            List<String> submoduleNames) throws IOException {
        
        StringBuilder specContent = new StringBuilder();
        specContent.append("# Multi-Agent Plan Spec\n\n");
        
        // Header section
        specContent.append("## Header\n\n");
        specContent.append("- **Node ID**: ").append(nodeId).append("\n");
        specContent.append("- **Created**: ").append(new Date()).append("\n");
        if (parentWorktreeId != null) {
            specContent.append("- **Parent Worktree ID**: ").append(parentWorktreeId).append("\n");
        }
        specContent.append("- **Base Branch**: main\n\n");
        
        // Plan section
        specContent.append("## Plan\n\n");
        specContent.append("1. ").append(goal).append("\n");
        specContent.append("   - Analyze requirements\n");
        specContent.append("   - Implement solution\n");
        specContent.append("   - Test implementation\n\n");
        
        // Status section
        specContent.append("## Status\n\n");
        specContent.append("- **Completion**: 0%\n");
        specContent.append("- **Current Step**: Not started\n");
        specContent.append("- **Blocked**: No\n\n");
        
        // Submodules section
        if (submoduleNames != null && !submoduleNames.isEmpty()) {
            specContent.append("## Submodules\n\n");
            for (String submodule : submoduleNames) {
                specContent.append("### ").append(submodule).append("\n\n");
                specContent.append("- **Status**: Pending\n");
                specContent.append("- **Work Items**: None yet\n\n");
            }
        }
        
        Path specPath = repoPath.resolve(".multi-agent-plan.md");
        Files.write(specPath, specContent.toString().getBytes(StandardCharsets.UTF_8));
        
        log.info("Created spec file at {}", specPath);
        return specPath;
    }

    /**
     * Commits the spec file to the repository.
     *
     * @param git Git instance
     * @param specPath Spec file path (relative to repo root)
     * @param message Commit message
     */
    public static void commitSpecFile(Git git, String specPath, String message) throws GitAPIException {
        git.add().addFilepattern(specPath).call();
        git.commit()
                .setMessage(message)
                .call();
        
        log.info("Committed spec file: {}", message);
    }

    /**
     * Creates a working branch in the repository.
     *
     * @param git Git instance
     * @param branchName Name of the branch to create
     */
    public static void createBranch(Git git, String branchName) throws GitAPIException {
        git.checkout()
                .setCreateBranch(true)
                .setName(branchName)
                .call();
        
        log.info("Created branch: {}", branchName);
    }

    /**
     * Writes a file to the repository.
     *
     * @param repoPath Repository path
     * @param filePath Path relative to repo root
     * @param content File content
     */
    public static void writeFile(Path repoPath, String filePath, String content) throws IOException {
        Path fullPath = repoPath.resolve(filePath);
        Files.createDirectories(fullPath.getParent());
        Files.write(fullPath, content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Configures git user for test repository.
     *
     * @param git Git instance
     */
    private static void configureGit(Git git) throws GitAPIException, IOException {
        git.getRepository().getConfig().setString("user", null, "name", "Test User");
        git.getRepository().getConfig().setString("user", null, "email", "test@example.com");
        git.getRepository().getConfig().save();
    }

    /**
     * Specification for a submodule.
     */
    public static class SubmoduleSpec {
        public final String path;
        public final Map<String, String> initialFiles;

        public SubmoduleSpec(String path, Map<String, String> initialFiles) {
            this.path = path;
            this.initialFiles = initialFiles != null ? initialFiles : new HashMap<>();
        }
    }
}
