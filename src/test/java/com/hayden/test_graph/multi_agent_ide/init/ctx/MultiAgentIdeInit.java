package com.hayden.test_graph.multi_agent_ide.init.ctx;

import com.hayden.test_graph.ctx.ContextValue;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitCtx;
import com.hayden.test_graph.multi_agent_ide.init.nodes.MultiAgentIdeInitNode;
import com.hayden.test_graph.thread.ResettableThread;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Init context for multi-agent-ide test graph.
 * Configures docker-compose startup, event subscription types, and git repositories.
 */
@Component
@ResettableThread
@RequiredArgsConstructor
public class MultiAgentIdeInit implements InitCtx {

    @Builder
    public record EventSubscriptionConfig(
            String subscriptionType,  // websocket, http_polling, kafka, etc.
            String brokerUrl,
            Integer reconnectDelayMs
    ) {
        public EventSubscriptionConfig(String subscriptionType) {
            this(subscriptionType, null, 5000);
        }
    }

    @Builder
    public record GitRepositoryConfig(
            Path repositoryPath,
            String initialBranch,
            boolean initializeWorktrees,
            boolean hasSubmodules,
            List<String> submoduleNames
    ) {
        public GitRepositoryConfig(Path repositoryPath) {
            this(repositoryPath, "main", true, false, List.of());
        }

        public GitRepositoryConfig(Path repositoryPath, List<String> submoduleNames) {
            this(repositoryPath, "main", true, !submoduleNames.isEmpty(), submoduleNames);
        }
    }

    @Builder
    public record SpecFileConfig(
            String specFileName,  // e.g., ".multi-agent-plan.md"
            String specFormat,    // "markdown" or "yaml"
            List<String> requiredSections  // ["Header", "Plan", "Status"]
    ) {
        public SpecFileConfig() {
            this(".multi-agent-plan.md", "markdown", List.of("Header", "Plan", "Status"));
        }
    }

    @Builder
    public record RepositorySpec(
            String name,
            Path sourceDirectory,  // classpath or filesystem path to source files
            String nodeId,
            String goal,
            String parentWorktreeId,
            List<String> submoduleNames,  // null if no submodules
            String branchName
    ) {
        public RepositorySpec(String name, Path sourceDirectory, String nodeId, String goal) {
            this(name, sourceDirectory, nodeId, goal, null, null, null);
        }

        public RepositorySpec(String name, Path sourceDirectory, String nodeId, String goal, List<String> submoduleNames) {
            this(name, sourceDirectory, nodeId, goal, null, submoduleNames, null);
        }
    }

    @Builder
    public record BranchingSpec(
            String parentNodeId,
            String baseCommitHash,
            Integer branchCount,
            String[] strategies,
            String[] branchNames
    ) {}

    @Builder
    public record MergeScenarioSpec(
            String[] parentFiles,
            String[] childFiles,
            String[] conflictingFiles,
            String parentAdvancement,
            String hierarchyDepth,
            boolean autoResolutionEnabled,
            String resolutionStrategy
    ) {}

    @Builder
    public record ReviewWorkflowSpec(
            String reviewableNodeStatus,
            String reviewNodeType,
            String contentType,
            Integer reviewerCount,
            Long timeoutMinutes,
            boolean hasWorkOutput
    ) {}

    @Builder
    public record SubmoduleSpec(
            String[] submoduleNames,
            boolean mainRepoEditable,
            String[] editableSubmodules,
            String[] conflictingSubmodules,
            String pointerStatus  // synchronized, out_of_sync
    ) {}

    @Builder
    public record GoalCompletionSpec(
            String goalDescription,
            Integer nodeCount,
            String nodeStatuses,
            boolean includesFailures,
            boolean includesPruned,
            String expectedSummaryType
    ) {}

    @Builder
    public record PruningSpec(
            String rootNodeId,
            Integer descendantCount,
            boolean hasActiveWorktrees,
            boolean siblingNodesPresent,
            String worktreeStatus
    ) {}

    @Builder
    public record EditingInterruptionSpec(
            String nodeStatus,
            boolean hasOriginalPrompt,
            String editedPrompt,
            String streamingState,  // not_started, partial, complete
            String interruptionReason,  // user_interrupt, edit, none
            String childNodeStatuses
    ) {}

    /**
     * Represents a WorkNode at a specific execution state.
     * Used by scenario-level Given statements to set up node conditions.
     * 
     * Context initialization:
     * - MultiAgentIdeInit: Stores WorkNodeStateSpec definitions
     * - MultiAgentIdeDataDepCtx: Creates runtime WorkNodeState tracking
     * - Edge: WorkNodeStateToDataDepEdge transfers specs to runtime state
     */
    @Builder
    public record NodeStateSpec(
            String nodeType,
            String nodeId,
            String status,  // READY, RUNNING, WAITING_REVIEW, WAITING_INPUT, COMPLETED, FAILED, PRUNED
            String description,
            String goal,
            boolean hasWorktree,
            boolean isStreaming,
            String originalPrompt,
            String editedPrompt,
            String worktreeBranch,
            Integer completionPercentage
    ) {}


    /**
     * Represents git repository state with worktree and submodule configuration.
     * Used to set up repository contexts for worktree operations.
     * 
     * Context initialization:
     * - MultiAgentIdeInit: Stores GitRepositoryStateSpec
     * - MultiAgentIdeDataDepCtx: Initializes git operations and worktree tracking
     * - Edge: GitRepositoryStateEdge prepares repository for operations
     */
    @Builder
    public record GitRepositoryStateSpec(
            String repositoryPath,
            String currentBranch,
            String currentCommitHash,
            List<String> submoduleNames,
            Map<String, String> submoduleCommitHashes,  // submodule name -> commit
            List<String> unmergedBranches,
            Integer worktreeCount
    ) {}

    /**
     * Represents merge conflict state between parent and child worktrees.
     * Used to set up merge conflict scenarios for testing resolution.
     * 
     * Context initialization:
     * - MultiAgentIdeInit: Stores MergeConflictStateSpec
     * - MultiAgentIdeDataDepCtx: Tracks conflict files and resolution state
     * - Edge: MergeConflictEdge simulates conflicts and prepares resolution options
     */
    @Builder
    public record MergeConflictStateSpec(
            String parentWorktreeId,
            String childWorktreeId,
            List<String> conflictingFiles,
            List<String> parentModifiedFiles,
            List<String> childModifiedFiles,
            String conflictState,  // CONFLICTED, RESOLVED, AUTO_RESOLVED
            String resolutionStrategy,  // ours, theirs, union, interactive, manual
            Integer conflictMarkerCount
    ) {}

    /**
     * Represents code generation and streaming state during execution.
     * Used to set up work execution scenarios with streaming output.
     * 
     * Context initialization:
     * - MultiAgentIdeInit: Stores ExecutionStreamingStateSpec
     * - MultiAgentIdeDataDepCtx: Initializes streaming token generation
     * - Edge: ExecutionStreamingEdge simulates token streaming
     */
    @Builder
    public record ExecutionStreamingStateSpec(
            String nodeId,
            String agentType,  // EditorGraphAgent, PlanningGraphAgent, ReviewGraphAgent
            boolean isStreaming,
            Integer streamedTokenCount,
            Integer totalExpectedTokens,
            String streamingPattern,  // uniform, varied, bursty
            Long startTimeMs,
            String generatedContent
    ) {}

    /**
     * Represents review workflow state with reviewer configuration.
     * Used to set up human review nodes and approval workflows.
     * 
     * Context initialization:
     * - MultiAgentIdeInit: Stores ReviewWorkflowStateSpec
     * - MultiAgentIdeDataDepCtx: Tracks review state and decisions
     * - Edge: ReviewWorkflowEdge prepares review nodes
     */
    @Builder
    public record ReviewWorkflowStateSpec(
            String reviewNodeId,
            String worktreeNodeId,  // node being reviewed
            String contentType,  // code, documentation, architecture, test
            Integer requiredReviewerCount,
            List<String> assignedReviewerIds,
            List<String> completedReviewerIds,
            String currentReviewerDecision,  // approved, rejected, revision_requested, pending
            String reviewFeedback,
            Long reviewTimeoutMs
    ) {}

    private final ContextValue<EventSubscriptionConfig> eventSubscriptionConfig = ContextValue.empty();
    private final ContextValue<GitRepositoryConfig> gitRepositoryConfig = ContextValue.empty();
    private final ContextValue<SpecFileConfig> specFileConfig = ContextValue.empty();
    private final List<RepositorySpec> repositorySpecs = new ArrayList<>();
    private final Map<String, Object> mockResponses = new HashMap<>();
    
    // Consolidated spec storage for reduced state
    private final List<BranchingSpec> branchingSpecs = new ArrayList<>();
    private final List<MergeScenarioSpec> mergeScenarioSpecs = new ArrayList<>();
    private final List<ReviewWorkflowSpec> reviewWorkflowSpecs = new ArrayList<>();
    private final List<SubmoduleSpec> submoduleSpecs = new ArrayList<>();
    private final List<GoalCompletionSpec> goalCompletionSpecs = new ArrayList<>();
    private final List<PruningSpec> pruningSpecs = new ArrayList<>();
    private final List<EditingInterruptionSpec> editingInterruptionSpecs = new ArrayList<>();

    // Scenario-level given spec storage (Phase 1+)
    private final List<NodeStateSpec> workNodeStateSpecs = new ArrayList<>();
    private final List<GitRepositoryStateSpec> gitRepositoryStateSpecs = new ArrayList<>();
    private final List<MergeConflictStateSpec> mergeConflictStateSpecs = new ArrayList<>();
    private final List<ExecutionStreamingStateSpec> executionStreamingStateSpecs = new ArrayList<>();
    private final List<ReviewWorkflowStateSpec> reviewWorkflowStateSpecs = new ArrayList<>();
    
    @Getter
    private final ContextValue<String> testListenerId = ContextValue.empty();

    MultiAgentIdeBubble bubble;

    @Autowired
    @ResettableThread
    public void setBubble(MultiAgentIdeBubble bubble) {
        this.bubble = bubble;
    }

    public void setEventSubscriptionConfig(EventSubscriptionConfig config) {
        eventSubscriptionConfig.set(config);
    }

    public EventSubscriptionConfig getEventSubscriptionConfig() {
        return eventSubscriptionConfig.get();
    }

    public void setGitRepositoryConfig(GitRepositoryConfig config) {
        gitRepositoryConfig.set(config);
    }

    public GitRepositoryConfig getGitRepositoryConfig() {
        return gitRepositoryConfig.get();
    }

    public void setSpecFileConfig(SpecFileConfig config) {
        specFileConfig.set(config);
    }

    public SpecFileConfig getSpecFileConfig() {
        return specFileConfig.get();
    }

    public void addRepositorySpec(RepositorySpec spec) {
        repositorySpecs.add(spec);
    }

    public void registerMockResponse(String key, Object response) {
        mockResponses.put(key, response);
    }

    public Object getMockResponse(String key) {
        return mockResponses.get(key);
    }

    public void addBranchingSpec(BranchingSpec spec) {
        branchingSpecs.add(spec);
    }

    public void addMergeScenarioSpec(MergeScenarioSpec spec) {
        mergeScenarioSpecs.add(spec);
    }

    public void addReviewWorkflowSpec(ReviewWorkflowSpec spec) {
        reviewWorkflowSpecs.add(spec);
    }

    public void addSubmoduleSpec(SubmoduleSpec spec) {
        submoduleSpecs.add(spec);
    }

     public void addGoalCompletionSpec(GoalCompletionSpec spec) {
        goalCompletionSpecs.add(spec);
    }

    public void addPruningSpec(PruningSpec spec) {
        pruningSpecs.add(spec);
    }

    public void addEditingInterruptionSpec(EditingInterruptionSpec spec) {
        editingInterruptionSpecs.add(spec);
    }

    public void addWorkNodeStateSpec(NodeStateSpec spec) {
        workNodeStateSpecs.add(spec);
    }

    public NodeStateSpec getWorkNodeStateSpecByNodeId(String nodeId) {
        return workNodeStateSpecs.stream()
                .filter(spec -> spec.nodeId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    public void addGitRepositoryStateSpec(GitRepositoryStateSpec spec) {
        gitRepositoryStateSpecs.add(spec);
    }


    public GitRepositoryStateSpec getGitRepositoryStateByPath(String repoPath) {
        return gitRepositoryStateSpecs.stream()
                .filter(spec -> spec.repositoryPath().equals(repoPath))
                .findFirst()
                .orElse(null);
    }

    public void addMergeConflictStateSpec(MergeConflictStateSpec spec) {
        mergeConflictStateSpecs.add(spec);
    }


    public MergeConflictStateSpec getMergeConflictStateBetween(String parentId, String childId) {
        return mergeConflictStateSpecs.stream()
                .filter(spec -> spec.parentWorktreeId().equals(parentId) && spec.childWorktreeId().equals(childId))
                .findFirst()
                .orElse(null);
    }

    public void addExecutionStreamingStateSpec(ExecutionStreamingStateSpec spec) {
        executionStreamingStateSpecs.add(spec);
    }

    public ExecutionStreamingStateSpec getExecutionStreamingStateByNodeId(String nodeId) {
        return executionStreamingStateSpecs.stream()
                .filter(spec -> spec.nodeId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    public void addReviewWorkflowStateSpec(ReviewWorkflowStateSpec spec) {
        reviewWorkflowStateSpecs.add(spec);
    }

    public ReviewWorkflowStateSpec getReviewWorkflowStateByReviewNodeId(String reviewNodeId) {
        return reviewWorkflowStateSpecs.stream()
                .filter(spec -> spec.reviewNodeId().equals(reviewNodeId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Class<? extends TestGraphContext>> dependsOn() {
        return List.of();
    }

    @Override
    public Class<MultiAgentIdeBubble> bubbleClazz() {
        return MultiAgentIdeBubble.class;
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof MultiAgentIdeInitNode;
    }

    @Override
    public MultiAgentIdeBubble bubble() {
        return bubble;
    }
}
