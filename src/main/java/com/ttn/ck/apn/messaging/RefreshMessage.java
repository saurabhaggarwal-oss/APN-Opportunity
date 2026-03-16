package com.ttn.ck.apn.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Message POJO sent through RabbitMQ for the opportunity refresh flow.
 * Each message carries a single UUID for independent, retryable processing.
 *
 * <p>Serialized as JSON via Jackson (configured in {@link com.ttn.ck.apn.config.RabbitMQConfig}).</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The lineitem UUID to process (corresponds to raw data records).
     */
    private String uuid;

    /**
     * Timestamp when the refresh was triggered.
     */
    private Date triggeredAt;

    /**
     * Retry attempt number (set by the consumer on retry).
     */
    @Builder.Default
    private int retryCount = 0;
}
