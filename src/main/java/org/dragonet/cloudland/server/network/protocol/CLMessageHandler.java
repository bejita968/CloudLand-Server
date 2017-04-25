package org.dragonet.cloudland.server.network.protocol;

import org.dragonet.cloudland.server.network.Session;
import com.google.protobuf.Message;

/**
 * Created on 2017/1/8.
 */
public interface CLMessageHandler<T extends Message> {

    void handle(Session session, T message);
}
