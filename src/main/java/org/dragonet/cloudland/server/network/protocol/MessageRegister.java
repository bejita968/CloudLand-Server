package org.dragonet.cloudland.server.network.protocol;

import com.google.protobuf.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2017/1/8.
 */
public class MessageRegister {

    private final Map<Integer, Message> messageMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<Class<? extends Message>, Integer> idMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<Class<? extends Message>, CLMessageHandler<?>> messageHandler = Collections.synchronizedMap(new HashMap<>());

    public void register(int id, Message prototype, CLMessageHandler<?> handler) {
        messageMap.put(id, prototype.getDefaultInstanceForType());
        idMap.put(prototype.getClass(), id);
        if(handler != null) messageHandler.put(prototype.getClass(), handler);
    }

    public Message getDesiredMessagePrototype(int id) {
        return messageMap.get(id);
    }

    public int getTypeId(Class<? extends Message> clazz){
        if(!idMap.containsKey(clazz)) return Integer.MIN_VALUE;
        return idMap.get(clazz);
    }

    public <T extends Message> CLMessageHandler<T> getHandler(T message) {
        if(message == null) return null;
        return (CLMessageHandler<T>) messageHandler.get(message.getClass());
    }

}
