package org.dragonet.cloudland.server.network.protocol;

import org.dragonet.cloudland.server.network.CLSession;
import org.dragonet.cloudland.server.network.NetworkServer;
import org.dragonet.cloudland.server.network.Session;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.atomic.AtomicReference;


/**
 * Created on 2017/1/8.
 */
public class CLChannelHandler extends ChannelHandlerAdapter {

    private final NetworkServer network;

    private final MessageRegister register;

    private String identifier;
    private AtomicReference<Session> session = new AtomicReference<>(null);

    public CLChannelHandler(NetworkServer network, MessageRegister register) {
        this.network = network;
        this.register = register;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        identifier = "CONNECTION/" + ctx.channel().remoteAddress().toString() + "/" + System.currentTimeMillis();
        session.set(network.openSession(identifier, ctx));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        network.closeSession(session.get(), "remote disconnect");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        network.closeSession(session.get(), "error<" + cause.getClass().getSimpleName() + ">: " + cause.getMessage());
        cause.printStackTrace();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg == null) return;
        ReferenceCountUtil.release(msg);
        if(!Message.class.isAssignableFrom(msg.getClass())) {
            return;
        }
        CLMessageHandler<Message> handler = register.getHandler((Message)msg);
        if(handler == null){
            network.closeSession(getSession(), "illegal message received");
            return;
        }
        ((CLSession)getSession()).putIncomingMessage((Message) msg, handler);
    }

    public Session getSession() {
        return session.get();
    }

    public MessageRegister getRegister() {
        return register;
    }
}
