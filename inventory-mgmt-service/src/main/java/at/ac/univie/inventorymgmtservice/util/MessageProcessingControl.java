package at.ac.univie.inventorymgmtservice.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class MessageProcessingControl {
    private final AtomicBoolean processingEnabled = new AtomicBoolean(true);

    public boolean isProcessingEnabled() {
        return processingEnabled.get();
    }

    public void pauseProcessing() {
        processingEnabled.set(false);
    }

    public void resumeProcessing() {
        processingEnabled.set(true);
    }
}
