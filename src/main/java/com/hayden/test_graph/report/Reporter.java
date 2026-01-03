package com.hayden.test_graph.report;

import com.hayden.utilitymodule.config.EnvConfigProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

@Component
@Slf4j
public class Reporter {

    @Autowired
    EnvConfigProps props;

    public void info(String toLog) {
        log.info(toLog);
//        doWriteToLog("Info: ", "%s ... %s".formatted(toLog.substring(0, 100), "[TRUNCATED]"));
    }

    public void writeTruncated(String prepend, String toLog) {
        doWriteToLog(prepend, "%s ... %s".formatted(toLog.substring(0, Math.min(100, toLog.length())), "[TRUNCATED]%s"
                .formatted(System.lineSeparator())));
    }

    public void logError(String toLog) {
        log.error(toLog);
        doWriteToLog("Error: ", toLog + System.lineSeparator());
    }

    public void doWriteToLog(String prepend, String toLog) {
        Optional.ofNullable(props.getErrorLog())
                .ifPresent(errLog -> {
                    try {
                        if (!Files.exists(errLog.getParent()))
                            errLog.getParent().toFile().mkdirs();

                        if (!Files.exists(errLog))
                            errLog.toFile().createNewFile();
                        Files.write(errLog, "%s: %s".formatted(prepend, toLog).getBytes(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        log.error("Could not write error log", e);
                    }
                });
    }

}
