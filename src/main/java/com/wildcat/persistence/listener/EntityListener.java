package com.wildcat.persistence.listener;

import com.wildcat.persistence.service.SequenceGeneratorService;
import com.wildcat.persistence.util.AppModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class EntityListener extends AbstractMongoEventListener<AppModel> {

    private SequenceGeneratorService sequenceGenerator;

    @Autowired
    public EntityListener(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<AppModel> event) {
        if (event.getSource().getId() == null || event.getSource().getId() < 1) {
            event.getSource().setId(sequenceGenerator.generateSequence(event.getSource().getSequencerName()));
        }
    }
}
