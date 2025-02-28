package com.wildcat.persistence.model;

import com.wildcat.persistence.util.AppModel;
import com.wildcat.utils.enums.Token;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "api_costs")
public class ApiCost  implements AppModel {

    @Transient
    public static final String SEQUENCE_NAME = "api_costs_sequence";

    @Id
    private Long id;
    private Long usertId;
    private String modelName;
    private Long amount;
    private Long quantity;
    private Token token;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getSequencerName() {
        return SEQUENCE_NAME;
    }

    public void resetValues() {
        this.id = null;
        this.usertId = null;
        this.modelName = null;
        this.amount = null;
        this.quantity = null;
        this.token = null;
    }
}
