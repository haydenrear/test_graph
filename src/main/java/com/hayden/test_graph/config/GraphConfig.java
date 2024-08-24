package com.hayden.test_graph.config;

import com.hayden.test_graph.ctx.HierarchicalContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.SubGraph;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.hayden.test_graph.config.ScopeConfig.THREAD_SCOPE;

@Configuration
@Slf4j
public class GraphConfig {

    @Bean
    BeanFactoryPostProcessor buildInitSubGraphs(
            List<? extends TestGraphContext> initContexts
    ) {
        return beanFactory -> {
            if (beanFactory instanceof BeanDefinitionRegistry registry) {
                if (initContexts.isEmpty()) {
                    log.error("Could not register subgraphs.");
                } else {
                    initContexts.stream()
                            // we can only do for leaf as can retrieve parent
                            .filter(HierarchicalContext::isExecutable)
                            .forEach(i -> {
                                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SubGraph.class);
                                builder.addConstructorArgValue(i);
                                builder.setScope(THREAD_SCOPE);
                                registry.registerBeanDefinition(subGraphBeanName(i), builder.getBeanDefinition());
                            });
                }
            } else {
                throw new RuntimeException("Could not register beans because bean factory was not a BeanDefinitionRegistry.");
            }
        };
    }

    private static @NotNull String subGraphBeanName(TestGraphContext i) {
        return "%sSubGraph".formatted(StringUtils.uncapitalize(i.getClass().getSimpleName()));
    }
}
