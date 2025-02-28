package com.wildcat.ai.services.ui;

import com.wildcat.utils.dto.UiResult;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.function.Supplier;

@Component
@AllArgsConstructor
public class UiOperations {

    @Async
    public ListenableFuture<UiResult> executeUiOperation(Supplier<UiResult> uiOperation){
        try {
            UiResult result = uiOperation.get();
            return new AsyncResult<>(result);
        } catch (Exception ex) {
            return AsyncResult.forExecutionException(ex);
        }
    }
}
