package org.springframework.boot.docker.compose.core;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class ExposeCompose {

    @Delegate
    DefaultDockerCompose defaultDockerCompose;

}
