package com.ttn.ck.apn.messaging;

import com.ttn.ck.apn.config.RabbitMQConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message POJO sent through RabbitMQ for the opportunity refresh flow.
 * <p>Serialized as JSON via Jackson (configured in {@link RabbitMQConfig}).</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshMessage {

    /**
     * Timestamp when the refresh was triggered.
     */
    private String triggeredAt;

    /**
     * partner name for which refresh was triggered.
     */
    private String partnerName;

}
