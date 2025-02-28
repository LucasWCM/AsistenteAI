package com.wildcat.component.chat;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.wildcat.utils.dto.response.AiResponse;

import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class AiChatMessage extends HorizontalLayout {
    public AiChatMessage(Message message) {
        this.getStyle().set("padding", "5px");
        this.getStyle().set("border", "3px dashed black");
        this.setWidthFull();

        VerticalLayout messageMetadataContainer = new VerticalLayout();
        messageMetadataContainer.getStyle().set("border", "2px solid black");
        H3 userName = new H3(message.getUserName());
        userName.setWidthFull();
        messageMetadataContainer.add(userName);
        H5 createdDate = new H5(message.getCreatedDate().toString());
        createdDate.setWidthFull();
        messageMetadataContainer.add(createdDate);
        messageMetadataContainer.setSizeUndefined();
        this.add(messageMetadataContainer);

        VerticalLayout messageContainer = new VerticalLayout();
        messageContainer.getStyle().set("border", "2px solid black");

        if(isNull(message.getAiResponse())) {
            messageContainer.add(new Html("""
                        <span>%s</span>
                    """.formatted(message.getContent())));
        } else {
            AiResponse aiResponse = message.getAiResponse();
            switch (aiResponse.getSearchType()) {
                case LINK:
                    fillWithLinks(aiResponse, messageContainer);
                    break;
                case DOCUMENT:
                    messageContainer.add(new Html("""
                                <span>%s</span>
                            """.formatted(aiResponse.getUserInformation().getContent())));
                    break;
                case LINK_AND_DOCUMENT:
                    fillWithDocumentData(aiResponse, messageContainer);
                    messageContainer.add(new Html("<br>"));
                    fillWithLinks(aiResponse, messageContainer);
                    break;
            }
        }


        this.add(messageContainer);
        this.setFlexGrow(1, messageContainer);
    }

    private void fillWithDocumentData(AiResponse aiResponse, VerticalLayout messageContainer) {

    }

    private void fillWithLinks(AiResponse aiResponse, VerticalLayout messageContainer) {
        String rawLinks = aiResponse.getUserLinks()
                .stream().map(userLink -> {
                    return "<a target='_blank' href='%s'>%s</a>".formatted(userLink.getLink(), userLink.getTitle());
                }).collect(Collectors.joining("\n"));
        String oneOrMore = aiResponse.getUserLinks().size() == 1 ? "el siguiente link" : "los siguientes links";
        Html htmlLink = new Html("""
                                <span>
                                <b>Puedes visitar %s:</b><br>
                                %s
                                </span>
                            """.formatted(oneOrMore, rawLinks));
        messageContainer.add(htmlLink);
    }
}
