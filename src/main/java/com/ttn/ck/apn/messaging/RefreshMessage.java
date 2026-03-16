package com.ttn.ck.apn.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Message POJO sent through RabbitMQ for the opportunity refresh flow.
 * <p>Serialized as JSON via Jackson (configured in {@link com.ttn.ck.apn.config.RabbitMQConfig}).</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshMessage implements Serializable {

    /**
     * Timestamp when the refresh was triggered.
     */
    private Date triggeredAt;

    /**
     * partner name for which refresh was triggered.
     */
    private String partnerName;

}
