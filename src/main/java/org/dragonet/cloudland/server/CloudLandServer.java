package org.dragonet.cloudland.server;

import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.gui.GUIWindow;
import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.item.crafting.CraftingManager;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.map.generator.DefaultGenerator;
import org.dragonet.cloudland.server.map.generator.Generator;
import org.dragonet.cloudland.server.network.NetworkServer;
import org.dragonet.cloudland.server.scheduler.implementation.GlowScheduler;
import org.dragonet.cloudland.server.scheduler.implementation.WorldScheduler;
import org.dragonet.cloudland.server.util.UnsignedLongKeyMap;
import org.dragonet.cloudland.server.util.Versioning;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created on 2017/1/10.
 */
public class CloudLandServer {

    public final static String DEFAULT_MAP_NAME = "default";

    @Getter
    private static CloudLandServer server;

    public final static int SERVER_PORT = 21098;
    public final static int THREAD_POOL_SIZE = 8;

    public static void main(String[] args) throws Exception {
        server = new CloudLandServer();
        server.start();
    }

    @Getter
    private final Logger logger = LogManager.getLogger("CloudLandServer");

    @Getter
    private NetworkServer network;

    @Getter
    private boolean running = true;

    @Getter
    private int port = SERVER_PORT;

    /**
     * Cached maps
     */
    private Map<String, GameMap> maps;
    private WorldScheduler worldScheduler = new WorldScheduler();

    private AtomicLong entityIdCount = new AtomicLong(1L);

    private final AtomicLong nextWindowUniqueId = new AtomicLong(1L);
    @Getter
    private final UnsignedLongKeyMap<GUIWindow> windowRegister = new UnsignedLongKeyMap<>(false);

    /**
     * Generators
     */
    private Map<String, Class<? extends Generator>> generators;

    @Getter
    private GlowScheduler scheduler = new GlowScheduler(this, worldScheduler);

    @Getter
    private ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

    private void start()  {
        logger.info("Starting CloudLand server " + Versioning.SERVER_VERION + " for game version " + Versioning.GAME_VERSION + "(" + Versioning.GAME_PROTOCOL + ")");

        // Create values
        maps = Collections.synchronizedMap(new HashMap<>());
        generators = Collections.synchronizedMap(new HashMap<>());

        // Initiate stuffs
        ItemPrototype.init();
        BlockBehavior.init();
        CraftingManager.get().init();

        // Register generators
        generators.put("default", DefaultGenerator.class);

        // Load map
        registerMap(new GameMap(this, DEFAULT_MAP_NAME, "default", new Random(System.currentTimeMillis()).nextLong()));
        worldScheduler.addWorld(getMap(DEFAULT_MAP_NAME));

        scheduler.start();

        try {
            network = new NetworkServer(this);
        }catch(IOException e){
            logger.error("Failed to bind on port " + getPort());
            return;
        }

        logger.info("Server is listening at port " + SERVER_PORT);
    }

    public GameMap getMap(String name) {
        return maps.get(name);
    }

    public void registerMap(GameMap map){
        maps.put(map.getName(), map);
    }

    public Class<? extends Generator> getGenerator(String name){
        return generators.get(name);
    }

    public void shutdown(){
        network.shutdown();
    }

    public long getNextWindowUniqueId() {
        return nextWindowUniqueId.getAndIncrement();
    }

    public long getNextEntityId() {
        return entityIdCount.getAndIncrement();
    }
}
