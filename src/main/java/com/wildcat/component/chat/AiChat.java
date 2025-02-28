package com.wildcat.component.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flowingcode.vaadin.addons.chatassistant.ChatAssistant;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.wildcat.ai.services.assistant.AiAssistantService;
import com.wildcat.persistence.model.ApiCall;
import com.wildcat.persistence.model.ChatMessage;
import com.wildcat.persistence.service.apicall.ApiCallService;
import com.wildcat.persistence.service.message.ChatMessageService;
import com.wildcat.utils.dto.response.AiResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.apache.tika.utils.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.wildcat.utils.AiUtils.buildApiCall;
import static com.wildcat.utils.AiUtils.buildModelResponse;
import static com.wildcat.utils.AiUtils.buildUserDbMessage;
import static com.wildcat.utils.AiUtils.executeCommand;

public class AiChat extends VerticalLayout {
    private H1 aiChatTitle;
    private ChatMessageService chatMessageService;
    private AiAssistantService aiAssistantService;
    private Scroller scroller;
    private ApiCallService apiCallService;

    public AiChat(String title, List<Message> chatMessages, Long userId, String userName, ApiCallService apiCallService, OpenAiChatModel chatLanguageModel) {
        this.apiCallService = apiCallService;
        aiChatTitle = new H1(title);
        aiChatTitle.setWidthFull();
        this.add(aiChatTitle);

        VerticalLayout messagesContainer = new VerticalLayout();
        chatMessages.forEach(message -> {
            messagesContainer.add(new AiChatMessage(message));
        });
        messagesContainer.setWidthFull();

        HorizontalLayout inputMessageContainer = new HorizontalLayout();
        TextField inputMessageTxt = new TextField();
        inputMessageTxt.setPrefixComponent(new Icon(VaadinIcon.CHAT));
        inputMessageTxt.setPlaceholder("Ingrese su consulta...");
        inputMessageTxt.getStyle().set("border", "1px solid black");
        Button btnSave = new Button("Ingresar");
        btnSave.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        inputMessageContainer.setFlexGrow(1, inputMessageTxt);
        inputMessageContainer.add(inputMessageTxt, btnSave);
        inputMessageContainer.setWidthFull();

        this.scroller = new Scroller(new VerticalLayout(messagesContainer));
        this.scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        this.scroller.setHeight("500px");
        this.scroller.setWidthFull();
        this.add(this.scroller);
        this.add(inputMessageContainer);
        this.scroller.scrollToBottom();

        btnSave.addClickListener(evt -> {
            btnSave.setText("Consultando...");
            btnSave.setEnabled(false);

            UI ui = UI.getCurrent();
            try {
                String content = inputMessageTxt.getValue();

                Message newUserMessage = saveUserSearchRequest(userId, userName, true, content, LocalDateTime.now());
                messagesContainer.add(new AiChatMessage(newUserMessage));
                inputMessageTxt.setValue("");

                this.scroller.scrollToBottom();

                aiAssistantService.queryLargeLanguageModel(content)
                        .addCallback(aiResponse -> {
                            executeCommand(ui,  () -> {
                                inputMessageTxt.setValue(StringUtils.EMPTY);
                                btnSave.setText("Save");
                                btnSave.setEnabled(true);

                                Message newAssistantMessage = null;
                                try {
                                    newAssistantMessage = saveAiResponse(userId, "Asistente IA", false, aiResponse, LocalDateTime.now());
                                } catch (JsonProcessingException e) {

                                }

                                executeApiCallSave(aiResponse, chatLanguageModel);

                                messagesContainer.add(new AiChatMessage(newAssistantMessage));
                                this.scroller.scrollToBottom();
                            });
                        }, (err) -> {
                            executeCommand(ui,  () -> {
                                btnSave.setText("Save");
                                btnSave.setEnabled(true);

                                this.scroller.scrollToBottom();
                                Notification.show("Se Ha Producido Un Error Al Consultar El Modelo.", 2000, Notification.Position.TOP_CENTER);
                            });
                        });
            } catch (Exception ex) {
                executeCommand(ui,  () -> {
                    this.scroller.scrollToBottom();
                    Notification.show("Se Ha Producido Un Error Al Consultar El Modelo.", 2000, Notification.Position.TOP_CENTER);
                });
            }
        });

    }

    private void executeApiCallSave(AiResponse aiResponse, OpenAiChatModel chatLanguageModel) {
        ApiCall apiCall = buildApiCall(aiResponse, chatLanguageModel);
        CompletableFuture.runAsync(() -> {
            try {
                this.apiCallService.save(apiCall);
            } catch (Exception ex) {

            }
        });
    }

    private Message saveAiResponse(Long userId, String userName, boolean fromUser, AiResponse aiResponse, LocalDateTime createdDate) throws JsonProcessingException {
        ChatMessage dbMessage = buildModelResponse(userId, fromUser, aiResponse, createdDate);
        this.chatMessageService.saveChatMessage(dbMessage);

        return Message.builder()
                      .userName(userName)
                      .createdDate(createdDate)
                      .aiResponse(aiResponse)
                      .build();
    }

    private Message saveUserSearchRequest(Long userId, String userName, boolean fromUser, String userRequest, LocalDateTime createdDate) throws JsonProcessingException {
        ChatMessage dbMessage = buildUserDbMessage(userId, fromUser, userRequest, createdDate);
        this.chatMessageService.saveChatMessage(dbMessage);

        return Message.builder()
                      .userName(userName)
                      .createdDate(createdDate)
                      .content(userRequest)
                      .build();
    }

    public void setServices(ChatMessageService chatMessageService,
                            AiAssistantService aiAssistantService,
                            ApiCallService apiCallService) {
        this.chatMessageService = chatMessageService;
        this.aiAssistantService = aiAssistantService;
        this.apiCallService = apiCallService;
    }

}
