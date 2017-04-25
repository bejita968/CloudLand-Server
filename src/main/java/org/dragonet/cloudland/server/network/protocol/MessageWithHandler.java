package org.dragonet.cloudland.server.network.protocol;

import com.google.protobuf.Message;
import lombok.Data;

/**
 * Created on 2017/2/16.
 */
@Data
public final class MessageWithHandler {

    private final Message message;
    private final CLMessageHandler<Message> handler;

}
