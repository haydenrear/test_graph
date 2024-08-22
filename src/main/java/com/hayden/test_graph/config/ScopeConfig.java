package com.hayden.test_graph.config;

import com.hayden.test_graph.ctx.HierarchicalContext;
import com.hayden.test_graph.ctx.TestGraphContext;
import com.hayden.test_graph.graph.SubGraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.SimpleThreadScope;

import java.util.List;

@Slf4j
@Configuration
public class ScopeConfig {

    @Bean
    public CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope("thread", simpleThreadScope());
        return configurer;
    }

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
                                builder.setScope("thread");
                                registry.registerBeanDefinition("initCtxSubgraph%s".formatted(i.getClass().getSimpleName()), builder.getBeanDefinition());
                            });
                }
            } else {
                throw new RuntimeException("Could not register beans.");
            }
        };
    }

    SimpleThreadScope simpleThreadScope() {
        return new SimpleThreadScope();
    }

}
