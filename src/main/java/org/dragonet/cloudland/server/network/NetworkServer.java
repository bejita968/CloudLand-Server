package org.dragonet.cloudland.server.network;

import org.dragonet.cloudland.net.protocol.*;
import org.dragonet.cloudland.server.CloudLandServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import org.dragonet.cloudland.server.network.handler.*;
import org.dragonet.cloudland.server.network.protocol.CLChannelHandler;
import org.dragonet.cloudland.server.network.protocol.IdBasedProtobufDecoder;
import org.dragonet.cloudland.server.network.protocol.IdBasedProtobufEncoder;
import org.dragonet.cloudland.server.network.protocol.MessageRegister;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author: Kevin Wang
 * CloudLandServer Project
 */
public class NetworkServer  {

    @Getter
    private final CloudLandServer server;

    private final Map<String, Session> players = new ConcurrentHashMap<>();

    private final MessageRegister register = new MessageRegister();

    private NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    private NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    private ChannelFuture listenFuture;


    public NetworkServer(CloudLandServer server) throws IOException {
        this.server = server;
        registerMessages();
        ServerBootstrap bootstrap = new ServerBootstrap();
        listenFuture = bootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new IdBasedProtobufDecoder(register));

                        socketChannel.pipeline().addLast(new IdBasedProtobufEncoder(register));

                        socketChannel.pipeline().addLast(new CLChannelHandler(NetworkServer.this, register));
                    }
                }).bind(server.getPort()).addListener(future -> {
                    if(future.isSuccess()) {
                        //TODO: Bind success
                        server.getLogger().info("Successfully listening on port " + server.getPort());
                    } else {
                        //TODO: Bind failed
                        server.getLogger().error("Failed to listen on port " + server.getPort());
                        server.shutdown();
                        throw new IOException("Failed to bind");
                    }
                });
    }

    public void shutdown(){
        listenFuture.cancel(true);
    }

    public CLSession openSession(String identifier, ChannelHandlerContext ctx) {
        CLSession session = new CLSession(this, identifier, (SocketChannel) ctx.channel());
        this.players.put(identifier, session);
        return session;
    }

    public void closeSession(Session session, String reason) {
        server.getLogger().info("Player [" + (session.isAuthenticated() ? session.getPlayer().getProfile().getUsername() : "<UNKNOWN>") + "] disconnected, reason: " + reason);
        ((CLSession)session).onDisconnect(reason);
        this.players.remove(session.getUniqueIdentifier());
    }

    public void processMessages(){
        players.values().forEach((session) -> {
            ((CLSession)session).processMessages();
        });
    }

    public void registerMessages() {
        // Initialization
        register.register(0xAABBCCDD, Handshake.ClientHandshakeMessage.getDefaultInstance(), new ClientHandshakeHandler());
        register.register(0x11223344, Handshake.ServerHandshakeMessage.getDefaultInstance(), null);
        register.register(0xAF000000, Authentication.ClientAuthenticateMessage.getDefaultInstance(), new ClientAuthenticateHandler());
        register.register(0xAF000001, Authentication.ServerAuthenticateResultMessage.getDefaultInstance(), null);
        register.register(0xFF000000, Initial.ServerDisconnectMessage.getDefaultInstance(), null);

        /* ==== SERVER ==== */
        // General (0xB0......)
        register.register(0xB0000000, Initial.ServerJoinGameMessage.getDefaultInstance(), null);
        register.register(0xB0000001, Initial.ServerUpdateEnvironmentMessage.getDefaultInstance(), null);
        register.register(0xB0000002, org.dragonet.cloudland.net.protocol.Map.ServerChunkMessage.getDefaultInstance(), null);
        register.register(0xB0000003, org.dragonet.cloudland.net.protocol.Map.ServerUpdateBlockMessage.getDefaultInstance(), null);
        register.register(0xB0000004, org.dragonet.cloudland.net.protocol.Map.ServerUpdateBlockBatchMessage.getDefaultInstance(), null);
        register.register(0xB0000005, Chat.ServerChatMessage.getDefaultInstance(), null);
        register.register(0xB0000006, Movement.ServerUpdatePlayerPositionMessage.getDefaultInstance(), null);

        // Entities (0xBE......)
        register.register(0xBE000001, Entity.ServerAddEntityMessage.getDefaultInstance(), null);
        register.register(0xBE000002, Entity.ServerEntityUpdateMessage.getDefaultInstance(), null);
        register.register(0xBE000003, Entity.ServerRemoveEntityMessage.getDefaultInstance(), null);
        register.register(0xBE0000FF, Entity.ServerClearEntitiesMessage.getDefaultInstance(), null);
        register.register(0xBEA00000, Entity.ServerEntityBindingControlMessage.getDefaultInstance(), null);

        // Window (0xBA......)
        register.register(0xBA000000, GUI.ServerWindowOpenMessage.getDefaultInstance(), null);
        register.register(0xBA000001, GUI.ServerWindowCloseMessage.getDefaultInstance(), null);
        register.register(0xBA000002, GUI.ServerUpdateWindowMessage.getDefaultInstance(), null);
        register.register(0xBA000003, GUI.ServerUpdateWindowElementMessage.getDefaultInstance(), null);
        register.register(0xBA000004, GUI.ServerCursorItemMessage.getDefaultInstance(), null);

        /* ==== CLIENT ==== */
        // General (0xE0......)
        register.register(0xE0000000, Chat.ClientChatMessage.getDefaultInstance(), null);
        register.register(0xE0000001, Movement.ClientMovementMessage.getDefaultInstance(), new ClientMovementHandler());
        register.register(0xE0000002, Movement.ClientActionMessage.getDefaultInstance(), new ClientActionHandler());
        register.register(0xE0000003, org.dragonet.cloudland.net.protocol.Map.ClientRemoveBlockMessage.getDefaultInstance(), new ClientRemoveBlockHandler());
        register.register(0xE0000004, Movement.ClientUseItemMessage.getDefaultInstance(), new ClientUseItemHandler());

        // Window (0xE1......)
        register.register(0xE1000000, Inventory.ClientHotbarSelectionMessage.getDefaultInstance(), new ClientHotbarSelectionHandler());
        register.register(0xE1000001, Inventory.ClientPickUpItemMessage.getDefaultInstance(), new ClientPickUpItemHandler());
        register.register(0xE1FF0000, GUI.ClientWindowInteractMessage.getDefaultInstance(), new ClientWindowInteractHandler());
        register.register(0xE1FEFFFF, GUI.ClientWindowCloseMessage.getDefaultInstance(), new ClientWindowCloseHandler());

        // Entities (0xBE......)
        register.register(0xBE000000, Entity.ClientEntityInteractMessage.getDefaultInstance(), null); // TODO
    }
}
