package com.wildcat.utils;

import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ChatLanguageModelService {

    private final ConfigurableApplicationContext context;

    public void restartContext(String beanName, OpenAiChatModel chatLanguageModel) {
        try {
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
            if(beanFactory.containsSingleton(beanName)) {
                beanFactory.destroySingleton(beanName);
            }
            beanFactory.registerSingleton(beanName, chatLanguageModel);
            System.out.println();
        } catch (Exception ex) {
            System.out.println();
        }
    }


}
