package com.wildcat;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.wildcat.ai.assistant.AiAssistant;
import com.wildcat.ai.services.DocumentLoader;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.FTSearchParams;
import redis.clients.jedis.search.SearchResult;

import java.util.List;


@SpringBootApplication
@EnableAsync
@Push
public class Application implements AppShellConfigurator, CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Override
    public void run(String... args) throws Exception {


    }

}
