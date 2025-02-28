package com.wildcat.utils.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wildcat.utils.enums.SearchType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@Data
public class AiResponse {
    @JsonProperty("search-type")
    private SearchType searchType;
    @JsonProperty("user-information")
    private UserInformation userInformation;
    @JsonProperty("user-links")
    private List<UserLink> userLinks;

    private Integer inputTokens;
    private Integer outputTokens;

    public List<UserLink> getUserLinks() {
        if (isNull(userLinks)){
            this.userLinks = new ArrayList<>();
        }
        return userLinks;
    }
}
