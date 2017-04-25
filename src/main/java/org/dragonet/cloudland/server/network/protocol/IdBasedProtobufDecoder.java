package org.dragonet.cloudland.server.network.protocol;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Created on 2016/11/25.
 */
public class IdBasedProtobufDecoder extends ByteToMessageDecoder {

    private final MessageRegister register;

    public IdBasedProtobufDecoder(MessageRegister register) {
        this.register = register;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() < 8) return;
        in.markReaderIndex();
        int typeId = in.readInt();
        int len = in.readInt();
        if (in.readableBytes() < len) {
            // System.out.println("[NETTY] Expecting ID " + typeId + " with length " + len);
            in.resetReaderIndex();
            return;
        }
        // System.out.println("[NETTY] Message ID=" + typeId + " received with readableBytes=" + in.readableBytes());

        Message prototype = register.getDesiredMessagePrototype(typeId);
        if(prototype == null) {
            throw new IllegalArgumentException("Unrecognisable message type ID! ");
        }

        // System.out.println("Received message [" + prototype.getClass().getSimpleName() + "]");

        byte[] messageData = new byte[len];
        in.readBytes(messageData);

        if(messageData.length <= 0){
            out.add(prototype.getParserForType().parseFrom(messageData, 0, len));
            return;
        }

        byte[] decompressBuffer = new byte[2048];

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPInputStream inp = new GZIPInputStream(new ByteArrayInputStream(messageData));
        int lBuff;
        while((lBuff = inp.read(decompressBuffer)) != -1) {
            bos.write(decompressBuffer, 0, lBuff);
        }
        inp.close();
        byte[] decompressedData = bos.toByteArray();
        out.add(prototype.getParserForType().parseFrom(decompressedData, 0, decompressedData.length));
    }

}

