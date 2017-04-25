package org.dragonet.cloudland.server.network.protocol;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;


/**
 * COPIED DIRECTLY FROM NETTY's PROTOBUF CODEC PACKAGE AND MODIFIED
 * <p>
 * Created on 2016/11/26.
 */
public class IdBasedProtobufEncoder extends MessageToMessageEncoder<MessageOrBuilder> {

    private final MessageRegister register;

    public IdBasedProtobufEncoder(MessageRegister register) {
        this.register = register;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageOrBuilder in, List<Object> out)
            throws Exception {
        Message msg = in instanceof Message ? (Message) in : ((Message.Builder) in).build();

        int typeId = register.getTypeId(msg.getClass());
        if (typeId == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unrecognisable message type, maybe not registered! ");
        }
        byte[] messageData = msg.toByteArray();

        if (messageData.length <= 0) {
            out.add(ByteBufAllocator.DEFAULT.heapBuffer().writeInt(typeId)
                    .writeInt(0));
            return;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream def = new GZIPOutputStream(bos);
        def.write(messageData);
        def.flush();
        def.close();
        byte[] compressedData = bos.toByteArray();
        out.add(ByteBufAllocator.DEFAULT.heapBuffer().writeInt(typeId).writeInt(compressedData.length)
                .writeBytes(compressedData));
    }


}
