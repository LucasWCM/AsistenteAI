package com.wildcat.ai.configuration;

import com.wildcat.ai.assistant.AiAssistant;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.UnifiedJedis;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O;

@Configuration
@RequiredArgsConstructor
public class LangChainServicesConfiguration {

    @Value("${assistant-ia-integration.open-ai.apiKey}")
    private String openAiApìKey;

    @Value("${assistant-ia-integration.redis.client.host}")
    private String redisClientHost;

    @Value("${assistant-ia-integration.redis.client.user}")
    private String redisClientUser;

    @Value("${assistant-ia-integration.redis.client.password}")
    private String redisClientPassword;

    @Value("${assistant-ia-integration.redis.client.port}")
    private Integer redisClientPort;

    @Value("${assistant-ia-integration.redis.client.index}")
    private String redisIndexName;

    @Value("${assistant-ia-integration.redis.client.dimensions}")
    private Integer embeddingDimensions;

    @Bean
    public OpenAiChatModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(openAiApìKey)
                .modelName(GPT_4_O)
                .temperature(0.4)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public AiAssistant assistant(ChatLanguageModel chatLanguageModel,
                                 ContentRetriever contentRetriever) {
        return AiServices.builder(AiAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .build();
    }

    @Primary
    @Bean
    public EmbeddingModel embeddingModel(){
        return new AllMiniLmL6V2EmbeddingModel();
    }

   @Bean
   public ContentRetriever contentRetriever(EmbeddingStore embeddingStore, EmbeddingModel embeddingModel) {
       return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .minScore(0.6)
                .maxResults(45)
                .build();
    }

    @Bean
    public EmbeddingStore embeddingStore(){
        return RedisEmbeddingStore.builder()
                .host(redisClientHost)
                .user(redisClientUser)
                .password(redisClientPassword)
                .port(redisClientPort)
                .indexName(redisIndexName)
                .dimension(embeddingDimensions)
                .build();
    }

    // redis://mi_usuario:mi_contraseña@localhost:6379
    @Bean
    public UnifiedJedis unifiedJedis() {
        JedisClientConfig config = DefaultJedisClientConfig.builder()
                .user(redisClientUser)
                .password(redisClientPassword)
                .build();

        UnifiedJedis jedis = new UnifiedJedis(
                new HostAndPort(redisClientHost, redisClientPort),
                config
        );
        return jedis;
    }
}