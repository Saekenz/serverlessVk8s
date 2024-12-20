package at.ac.univie.inventorymgmtservice.util;

import at.ac.univie.inventorymgmtservice.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageProcessingControl {
    private static final String PROCESSING_ENABLED = "inv.mgmt.processing.enabled";

    @Autowired
    private ConfigurationRepository configRepository;

    @Value("${inv.mgmt.processing.enabled}")
    private boolean processingEnabled;

    private void updateProcessingStatus(boolean status) {
        configRepository.updateConfigEntryValueByName(PROCESSING_ENABLED, String.valueOf(status));
        processingEnabled = status;
    }

    public boolean isProcessingEnabledLocally() {
        return processingEnabled;
    }

    public boolean isProcessingEnabledServiceWide() {
        String configValue = configRepository.findByNameEquals(PROCESSING_ENABLED).getValue();
        processingEnabled = Boolean.parseBoolean(configValue);

        return processingEnabled;
    }

    public void pauseProcessing() {
        updateProcessingStatus(false);
    }

    public void resumeProcessing() {
        updateProcessingStatus(true);
    }
}
