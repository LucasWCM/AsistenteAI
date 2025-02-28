package com.wildcat.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowingcode.vaadin.addons.chatassistant.model.Message;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.Command;
import com.wildcat.persistence.model.ApiCall;
import com.wildcat.persistence.model.ChatMessage;
import com.wildcat.utils.dto.response.AiResponse;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AiUtils {
    public static Document buildIndexedDocument(String content, Map<String, Object> metadataMap) {
        Document indexedDocument = Document.document(content);
        Metadata metadata = indexedDocument.metadata();

        metadataMap.entrySet().forEach(entry -> {
            metadata.put(entry.getKey(), entry.getValue().toString());
        });
        return indexedDocument;
    }

    public static String fromDataToString(LocalDateTime dateObj){
        DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return dateObj.format(CUSTOM_FORMATTER);
    }

    public static void executeCommand(UI ui, Command command) {
        ui.access(() -> {
            command.execute();
        });
    }

    public static void executeAsync(Runnable command) {
        CompletableFuture.runAsync(command);
    }

    public static Optional<Document> extractDocumentFromPdf(InputStream pdfInputStream) throws IOException {
        Path filePath = convertInputStreamToPath(pdfInputStream, "temp_%s.pdf".formatted(UUID.randomUUID().toString()));
        try (PDDocument pdfDocument = PDDocument.load(filePath.toFile())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setSortByPosition(true);
            String textContent = pdfStripper.getText(pdfDocument);
            Document document = Document.document(textContent);
            return Optional.ofNullable(document);
        } catch (IOException e) {
            return Optional.empty();
        } finally {
            File file = filePath.toFile();
            if(file.exists()){
                file.delete();
            }
        }
    }

    public static String extractTextFromPdf(InputStream pdfInputStream) throws IOException {
        Path filePath = convertInputStreamToPath(pdfInputStream, "temp_%s.pdf".formatted(UUID.randomUUID().toString()));
        try (PDDocument pdfDocument = PDDocument.load(filePath.toFile())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setSortByPosition(true);
            return pdfStripper.getText(pdfDocument);
        } catch (IOException e) {
            return null;
        } finally {
            File file = filePath.toFile();
            if(file.exists()){
                file.delete();
            }
        }
    }

    public static Path convertInputStreamToPath(InputStream inputStream, String fileName) throws IOException {
        Path tempFile = Files.createTempFile(null, fileName);
        Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    public static ConfirmDialog buildAndShowConfirmDialog(String header, String text) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(header);
        dialog.setText(text);
        dialog.setCancelable(true);
        dialog.setCloseOnEsc(false);
        dialog.setConfirmText("OK");
        return dialog;
    }

    public static String toJson(Object object) throws JsonProcessingException {
        return (new ObjectMapper()).writeValueAsString(object);
    }

    public static Dialog buildProcessDialog() {
        Dialog processDialog = new Dialog();
        processDialog.setCloseOnEsc(false);
        processDialog.setCloseOnOutsideClick(false);

        ProgressBar processBar = new ProgressBar();
        processBar.setIndeterminate(true);
        processBar.setWidthFull();

        processDialog.add(processBar);

        return processDialog;
    }

    public static Message buildChatMessage(String name, String content, LocalDateTime createdDate, boolean hasHtml, String htmlWrapper) {
        return Message.builder()
                .name(name)
                .content(content)
                .messageTime(createdDate)
                .build();
    }

    public static ChatMessage buildModelResponse(Long userId, boolean fromUser, AiResponse aiResponse, LocalDateTime createdDate) throws JsonProcessingException {
        return ChatMessage.builder()
                .userId(userId)
                .fromUser(fromUser)
                .searchType(aiResponse.getSearchType())
                .content(toJson(aiResponse))
                .createdDateTime(createdDate)
                .build();
    }

    public static ChatMessage buildUserDbMessage(Long userId, boolean fromUser, String userRequest, LocalDateTime createdDate) throws JsonProcessingException {
        return ChatMessage.builder()
                .userId(userId)
                .fromUser(fromUser)
                .content(userRequest)
                .createdDateTime(createdDate)
                .build();
    }

    @SneakyThrows
    public static <T> T parseAndGet(String rawJson, Class<T> tClass) {
        return (new ObjectMapper()).readValue(rawJson, tClass);
    }

    public static ApiCall buildApiCall(AiResponse aiResponse, ChatLanguageModel chatLanguageModel) {
        OpenAiChatModel openAiChatModel = (OpenAiChatModel) chatLanguageModel;
        return ApiCall.builder()
                .modelName(openAiChatModel.modelName())
                .inputTokens(aiResponse.getInputTokens().longValue())
                .outputTokens(aiResponse.getOutputTokens().longValue())
                .createdDate(LocalDate.now())
                .searchType(aiResponse.getSearchType())
                .build();
    }

    public static List<String> generateMostUsed(String pdfContent) {
        List<String> initialList = Arrays.asList(pdfContent.split(" "));
        Map<String, Long> mostUsedGroups = initialList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return mostUsedGroups
                .entrySet()
                .stream().sorted(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .toList().reversed();
    }

    public static void main(String[] args) {

    }

}
