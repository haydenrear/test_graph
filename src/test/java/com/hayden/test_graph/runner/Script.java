package com.hayden.test_graph.runner;

import com.hayden.commitdiffmodel.codegen.types.*;
import org.assertj.core.util.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Script {



//    @SneakyThrows
//    public static void main(String[] args) {
//        var om = new ObjectMapper();
//        var stagedFile = new File("/Users/hayde/IdeaProjects/drools/test_graph/src/test/resources/responses/staged.json");
//        List<PromptDiff> diffs = Lists.newArrayList(PromptDiff.newBuilder()
//                .underlyingDiff(DiffInput.newBuilder()
//                        .newPath("/test")
//                        .diffType(DiffType.MODIFY)
//                        .content(
//                                CommitDiffContentInput.newBuilder()
//                                        .hunks(Lists.newArrayList(
//                                                CommitDiffHunkInput.newBuilder()
//                                                        .hunkLines(HunkLinesInput.newBuilder()
//                                                                .newStartLine(38)
//                                                                .newLineCount(143)
//                                                                .linesDeleted(0)
//                                                                .linesAdded(0)
//                                                                .build())
//                                                        .commitDiffEdits(getEdits())
//                                                        .build()
//                                        ))
//                                        .build()
//                        )
//                        .build())
//                .build());
//        om.writeValue(stagedFile, Staged.newBuilder()
//                .diffs(diffs).build());
//        var commitMessageFile = new File("/Users/hayde/IdeaProjects/drools/test_graph/src/test/resources/responses/commit-errorMessage.json");
//        CommitMessage b = CommitMessage.newBuilder().value("errorMessage!").build();
//        om.writeValue(commitMessageFile, b);
//        var pc = PrevCommit.newBuilder().commitMessage(CommitMessage.newBuilder().value("other!").build())
//                .diffs(Lists.newArrayList())
//                .sessionKey(SessionKey.newBuilder().key("test-key").build()).build();
//        var prevCommitFile = new File("/Users/hayde/IdeaProjects/drools/test_graph/src/test/resources/responses/previous-requests.json");
//        om.writeValue(prevCommitFile, pc);
//        var toolsetRes = new File("/Users/hayde/IdeaProjects/drools/test_graph/src/test/resources/responses/toolset_response.json");
//        var req = new ModelContextProtocolContextRequest(
//                new ModelContextProtocolContextRequest.MpcToolsetRequest.SerMpcToolsetRequest(Map.of(
//                        "name", new ModelContextProtocolContextRequest.MpcParam.NameParam("query"),
//                        "arguments", new ModelContextProtocolContextRequest.MpcParam.MpcArsParam(Map.of("sql", new ModelContextProtocolContextRequest.MpcParam.MpcArg.Sql("SELECT * FROM commit_diff"))))),
//                new ModelContextProtocolContextRequest.MpcServerDescriptor(ServerParameters.builder("docker")
//                        .args("run", "-i", "--rm", "mcp/postgres", "postgresql://postgres:postgres@host.docker.internal:5450/postgres")
//                        .build()));
//
//        om.writeValue(toolsetRes, req);
//
//        var codegenResponsesFile = new File("/Users/hayde/IdeaProjects/drools/test_graph/src/test/resources/responses/codegen_response.json");
//        var cg = new ModelServerResponse.ModelServerCodeResponse(
//                new ModelServerCodingAiClient.CodeResult(new Git.GitDiff[] {new Git.GitDiff.FileCreate(Path.of("hello11111.txt"), "hello")}));
//
//        om.writeValue(codegenResponsesFile, cg);
//
//        var initialCodeResponseFile = new File("/Users/hayde/IdeaProjects/drools/test_graph/src/test/resources/responses/initial_code_response.json");
//        var ic = new ModelServerResponse.ModelServerCodeResponse(
//                new ModelServerCodingAiClient.CodeResult("""
//                                def whatever():
//                                    print("hello world!)
//                                """));
//        om.writeValue(initialCodeResponseFile, ic);
//    }

    static @NotNull ArrayList<CommitDiffEditInput> getEdits() {
        return Lists.newArrayList(
                CommitDiffEditInput.newBuilder()
                        .diffType(EditType.INSERT)
                        .editLocations(
                                EditLocationsInput.newBuilder()
                                        .locationA(EditLocationInput.newBuilder()
                                                .begin(38)
                                                .end(0)
                                                .build())
                                        .locationB(EditLocationInput.newBuilder()
                                                .begin(39)
                                                .end(1)
                                                .build())
                                        .build()
                        )
                        .build(),
                CommitDiffEditInput.newBuilder()
                        .diffType(EditType.REPLACE)
                        .editLocations(
                                EditLocationsInput.newBuilder()
                                        .locationA(EditLocationInput.newBuilder()
                                                .begin(150)
                                                .end(2)
                                                .build())
                                        .locationB(EditLocationInput.newBuilder()
                                                .begin(151)
                                                .end(1)
                                                .build())
                                        .build()
                        )
                        .build(),
                CommitDiffEditInput.newBuilder()
                        .diffType(EditType.REPLACE)
                        .editLocations(
                                EditLocationsInput.newBuilder()
                                        .locationA(EditLocationInput.newBuilder()
                                                .begin(174)
                                                .end(1)
                                                .build())
                                        .locationB(EditLocationInput.newBuilder()
                                                .begin(174)
                                                .end(1)
                                                .build())
                                        .build()
                        )
                        .build(),
                CommitDiffEditInput.newBuilder()
                        .diffType(EditType.INSERT)
                        .editLocations(
                                EditLocationsInput.newBuilder()
                                        .locationA(EditLocationInput.newBuilder()
                                                .begin(178)
                                                .end(0)
                                                .build())
                                        .locationB(EditLocationInput.newBuilder()
                                                .begin(179)
                                                .end(1)
                                                .build())
                                        .build()
                        )
                        .build(),
                CommitDiffEditInput.newBuilder()
                        .diffType(EditType.REPLACE)
                        .editLocations(
                                EditLocationsInput.newBuilder()
                                        .locationA(EditLocationInput.newBuilder()
                                                .begin(180)
                                                .end(1)
                                                .build())
                                        .locationB(EditLocationInput.newBuilder()
                                                .begin(181)
                                                .end(1)
                                                .build())
                                        .build()
                        )
                        .build()
        );
    }
}
