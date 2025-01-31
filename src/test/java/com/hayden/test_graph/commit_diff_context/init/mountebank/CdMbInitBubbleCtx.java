package com.hayden.test_graph.commit_diff_context.init.mountebank;

import com.hayden.commitdiffmodel.codegen.types.*;
import com.hayden.test_graph.commit_diff_context.init.commit_diff_init.ctx.CommitDiffInitBubble;
import com.hayden.test_graph.commit_diff_context.init.repo_op.ctx.RepoOpBubble;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.exec.single.GraphExec;
import com.hayden.test_graph.init.ctx.InitBubble;
import com.hayden.test_graph.init.docker.ctx.DockerInitBubbleCtx;
import com.hayden.test_graph.init.mountebank.ctx.MbInitBubbleCtx;
import com.hayden.test_graph.init.mountebank.ctx.MbInitCtx;
import com.hayden.test_graph.thread.ResettableThread;
import com.hayden.utilitymodule.sort.GraphSort;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@ResettableThread
public class CdMbInitBubbleCtx implements MbInitBubbleCtx {

    @Getter
    private CommitDiffContextGraphQlModel commitDiffContextValue;

    @Builder
    public record CommitDiffContextGraphQlModel(GitRepoPromptingRequest addRepo,
                                                GitRepositoryRequest repositoryRequest,
                                                RagOptions ragOptions,
                                                SessionKey sessionKey) {

        public List<PrevDiff> prevDiffs() {
            return Optional.ofNullable(this.addRepo())
                    .flatMap(gpr -> Optional.ofNullable(gpr.getPrev()))
                    .map(PrevCommit::getDiffs)
                    .orElse(new ArrayList<>());
        }

        public Optional<String> commitMessage() {
            return Optional.ofNullable(addRepo)
                    .flatMap(gr -> Optional.ofNullable(gr.getCommitMessage()))
                    .flatMap(cm -> Optional.ofNullable(cm.getValue()));
        }

        public List<ContextData> getContextData() {
            return Optional.ofNullable(addRepo)
                    .flatMap(gr -> Optional.ofNullable(gr.getContextData()))
                    .orElse(new ArrayList<>());
        }

        public List<PrevRequests> getPrevRequests() {
            return Optional.ofNullable(addRepo)
                    .flatMap(gr -> Optional.ofNullable(gr.getPrevRequests()))
                    .orElse(new ArrayList<>());
        }

        public List<PromptDiff> stagedDiffs() {
            return Optional.ofNullable(addRepo())
                    .flatMap(g -> Optional.ofNullable(g.getStaged()))
                    .map(Staged::getDiffs)
                    .orElse(new ArrayList<>());
        }


    }

    public CdMbInitBubbleCtx() {
        this.initialize();
    }

    public void initialize() {
        this.commitDiffContextValue = new CommitDiffContextGraphQlModel(
                GitRepoPromptingRequest.newBuilder()
                        .gitRepo(GitRepo.newBuilder().build())
                        .build(),
                GitRepositoryRequest.newBuilder()
                        .gitRepo(GitRepo.newBuilder().build())
                        .gitBranch(GitBranch.newBuilder().build())
                        .build(),
                RagOptions.newBuilder()
                        .commitsPerK(3)
                        .topK(3)
                        .maxDepth(3)
                        .build(),
                SessionKey.newBuilder().key(UUID.randomUUID().toString()).build());
    }

    @Override
    public boolean executableFor(GraphExec.GraphExecNode n) {
        return n instanceof CdMbInitBubbleNode;
    }

    @Override
    public List<Class<? extends TestGraphContext>> bubblers() {
        return List.of(CdMbInitCtx.class);
    }

    @Override
    public List<Class<? extends InitBubble>> dependsOn() {
        return List.of(DockerInitBubbleCtx.class, RepoOpBubble.class);
    }
}
