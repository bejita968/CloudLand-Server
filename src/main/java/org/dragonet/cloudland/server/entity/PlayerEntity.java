package org.dragonet.cloudland.server.entity;

import com.google.protobuf.Message;
import org.dragonet.cloudland.net.protocol.Entity;
import org.dragonet.cloudland.net.protocol.GUI;
import org.dragonet.cloudland.net.protocol.Movement;
import org.dragonet.cloudland.server.behavior.BlockBehavior;
import org.dragonet.cloudland.server.gui.GUIWindow;
import org.dragonet.cloudland.server.gui.InternalGUIElement;
import org.dragonet.cloudland.server.inventory.InventoryHolder;
import org.dragonet.cloudland.server.inventory.PlayerInventory;
import org.dragonet.cloudland.server.item.Item;
import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.map.LoadedChunk;
import org.dragonet.cloudland.server.network.BinaryMetadata;
import org.dragonet.cloudland.server.network.Session;
import org.dragonet.cloudland.server.network.tasks.SendChunkTask;
import lombok.Getter;
import org.dragonet.cloudland.server.scheduler.ScheduledTask;
import org.dragonet.cloudland.server.util.LongSet;
import org.dragonet.cloudland.server.util.PlayerProfile;
import org.dragonet.cloudland.server.util.UnsignedLongSet;
import org.dragonet.cloudland.server.util.Vector3D;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2017/1/10.
 */
public class PlayerEntity extends StandaloneEntity implements HumanEntity, InventoryHolder {

    public final static long BREAK_LAG_TOLERATION = 200;

    @Getter
    private final Session session;
    @Getter
    private final PlayerProfile profile;

    private final LongSet usedChunks = new LongSet(true);
    private final UnsignedLongSet usedEntities = new UnsignedLongSet(true);

    @Getter
    private SendChunkTask chunkTask;
    private ScheduledTask chunkTaskFuture;

    @Getter
    private PlayerInventory inventory;

    private final Set<Long> openedWindows = new HashSet<>();
    private final AtomicInteger nextWindowId = new AtomicInteger(1); //0 is reserved for player inventory
    private final java.util.Map<Integer, GUIWindow> windows = new HashMap<>();

    private long breakTime;
    private int breakX, breakY, breakZ;

    @Getter
    private Item cursorItem;

    public PlayerEntity(Session session, PlayerProfile profile) {
        this.session = session;
        this.profile = profile;
        chunkTask = new SendChunkTask(this);
        // chunkTaskFuture = session.getServer().getThreadPool().scheduleAtFixedRate(chunkTask, 0L, 10L, TimeUnit.MILLISECONDS);
        chunkTaskFuture = session.getServer().getScheduler().runTaskTimer(null, chunkTask, 0L, 5L);

        // TODO: Load inventory from data store
        inventory = new PlayerInventory(this);
        inventory.sendContents();

        setCursorItem(null);

        getMeta().putString(BinaryMetadata.Keys.NAMETAG_STRING, profile.getUsername());
        getMeta().putInt32(BinaryMetadata.Keys.HOLDING_SLOT_INT, inventory.getSelectedSlot());
    }

    @Override
    public void setMap(GameMap map) {
        usedChunks.clear();
        if(map == null) {
            chunkTaskFuture.cancel();
            if(getMap() != null){
                setRawPosition(null);
                getChunk().updateEntityReference(this);
            }
            return;
        }
        super.setMap(map);
    }

    public boolean isUsingChunk(int cx, int cz){
        return usedChunks.contains(cx, cz);
    }

    @Override
    public void tick() {
        super.tick();

        if(getMap() == null) return;
        //TODO: Player::tick()
        int currX = ((int)getPosition().x) >> 4;
        int currZ = ((int)getPosition().z) >> 4;
        // Unlock unused chunks
        {
            LongSet removeUsedChunks = new LongSet(false);
            usedChunks.forEach((xz) -> {
                int usedX = xz[0];
                int usedZ = xz[1];
                int dX = currX - usedX;
                int dZ = currZ - usedZ;
                if ((dX * dX) + (dZ * dZ) > getSession().getViewDistanceSquared() + 4) {
                    getMap().getChunkManager().unlockChunk(usedX, usedZ, this);
                    removeUsedChunks.add(usedX, usedZ);
                }
            });
            removeUsedChunks.forEach((v) -> usedChunks.remove(v[0], v[1]));
        }
        // Remove unused entities
        {
            UnsignedLongSet removeUnusedEntities = new UnsignedLongSet(false);
            usedEntities.forEach((eid) -> {
                if(eid != this.getEntityId() && (getMap().getEntity(eid) == null || getMap().getEntity(eid).getPosition().getDistanceSquared(this.getPosition()) > (getSession().getBlockViewDistanceSquared()))) {
                    removeUnusedEntities.add(eid);
                }
            });
            removeUnusedEntities.forEach((eid) -> {
                BaseEntity e = (BaseEntity)getMap().getEntity(eid);
                if (e != null) e.entityHolders.remove(getEntityId());
                usedEntities.remove(eid);
                getSession().sendNetworkMessage(Entity.ServerRemoveEntityMessage.newBuilder()
                        .setEntityId(eid)
                        .build());
            });
        }
        int loadCount = 0;
        // Load and lock new chunks
        for(int d = 1; d <= getSession().getViewDistance(); d++) {
            for (int cx = currX - d - 1; cx < currX + d + 1; cx++) {
                for (int cz = currZ - d - 1; cz < currZ + d + 1; cz++) {
                    if(loadCount > 16) break;
                    if(!getMap().getChunkManager().isChunkLoaded(cx ,cz)) {
                        loadCount++;
                    }
                    LoadedChunk c = getMap().getChunkManager().loadChunk(cx, cz, true);
                    if(!c.isPopulated()) {
                        getMap().getChunkManager().populateChunk(cx, cz);
                    }
                    c.getEntities().forEachValue((e) -> {
                        if (e == this) return;
                        //System.out.println("CHECKING USAGE E[" + e.getEntityId() + "] = " + usedEntities.contains(e.getEntityId()));
                        if (!usedEntities.contains(e.getEntityId())) {
                            e.spawnTo(this);
                            ((BaseEntity)e).entityHolders.put(getEntityId(), this);
                            usedEntities.add(e.getEntityId());
                        }
                    });

                    // Map sending
                    if (usedChunks.contains(cx, cz)) continue;
                    int dX = currX - cx;
                    int dZ = currZ - cz;
                    if ((dX * dX) + (dZ * dZ) > getSession().getViewDistanceSquared()) continue;
                    boolean sent = getChunkTask().queue(c);
                    if (sent) {
                        getMap().getChunkManager().lockChunk(cx, cz, this);
                        usedChunks.add(cx, cz);
                    }
                }
                if (loadCount > 16) break;
            }
            if (loadCount > 16) break;
        }
    }

    public void setRawPosition(Vector3D position) {
        super.setPosition(position);
    }

    @Override
    public void setEntityId(long entityId) {
        if(this.getEntityId() != -1 && entityId == -1) {
            // unlock chunks
            usedChunks.forEach((pair) -> {
                getMap().getChunkManager().unlockChunk(pair[0], pair[1], this);
                // System.out.println("clearing reference at " + String.format("(%d, %d)", pair[0], pair[1]));
            });
        }
        super.setEntityId(entityId);
    }

    @Override
    public void setPosition(Vector3D position) {
        //super.setPosition(position);
        setRawPosition(position);

        session.sendNetworkMessage(Movement.ServerUpdatePlayerPositionMessage.newBuilder()
                .setX(position.x)
                .setY(position.y)
                .setZ(position.z)
                .setYaw(getYaw())
                .setPitch(getPitch())
                .build());
    }

    @Override
    public void spawnTo(PlayerEntity player) {
        // Overridden base method
        Vector3D pos = getPosition();
        player.getSession().sendNetworkMessage(Entity.ServerAddEntityMessage.newBuilder()
                .setEntityId(getEntityId())
                .setEntityType(EntityType.toId("player"))
                .setX(pos.x)
                .setY(pos.y)
                .setZ(pos.z)
                .setYaw(getYaw())
                .setPitch(getPitch())
                .setMeta(getMeta().serialize())
                .build());
    }

    public int getNextWindowId() {
        return nextWindowId.getAndIncrement();
    }

    public void cancelBreaking(){
        breakTime = -1;
        breakX = Integer.MAX_VALUE;
        breakY = Integer.MAX_VALUE;
        breakZ = Integer.MAX_VALUE;
    }

    public void startBreaking(int x, int y, int z){
        if(getPosition().getDistanceSquared(new Vector3D(x, y, z)) > 100) return;
        breakTime = System.currentTimeMillis();
        breakX = x;
        breakY = y;
        breakZ = z;
        LoadedChunk c = getMap().getChunkManager().getChunk(x >> 4, z >> 4, true);

        if(c == null) return; // not likely to happen
        int bx = x & 0xF;
        int bz = z & 0xF;
        int id = c.getBlock(bx, y, bz);
        BlockBehavior behavior = BlockBehavior.get(id);
        if(behavior != null) {
            boolean succ = behavior.onStartBreak(this, getMap(), x, y, z, inventory.getHoldingItem());
            if(!succ) {
                breakTime = -1L;
            }
        }
        //System.out.println("START BREAKING " + String.format("(%d, %d, %d)", x, y, z));
    }

    public void endBreaking(int x, int y, int z){
        // Validation
        if(getPosition().getDistanceSquared(new Vector3D(x, y, z)) > 100) return;
        if(breakTime < 0 || breakX != x || breakY != y || breakZ != z) {
            sendBlockChange(x, y, z);
            return;
        }
        LoadedChunk c = getMap().getChunkManager().getChunk(x >> 4, z >> 4, false);
        if(c == null) {
            getSession().disconnect("accessing block out of distance");
            return;
        }
        int bx = x & 0xF;
        int bz = z & 0xF;
        int id = c.getBlock(bx, y, bz);
        BlockBehavior behavior = BlockBehavior.get(id);
        boolean result = false; // unknown blocks can not be broken
        if(behavior != null) result = behavior.onEndBreak(this, getMap(), x, y, z, inventory.getHoldingItem(), System.currentTimeMillis() - breakTime + BREAK_LAG_TOLERATION);
        if(result) {
            //System.out.println("SUCCESS BREAKING AT " + String.format("(%d, %d, %d)", x, y, z));
            c.setBlock(bx, y, bz, 0);

            getMap().broadcastBlockUpdate(x, y, z); // function of GameMap

            Random r = new Random(System.currentTimeMillis());
            // Drop item
            Item[] drops = behavior.getDrops(inventory.getHoldingItem());
            if (drops != null && drops.length > 0) {
                for (Item drop : drops) {
                    c.getEntities().forEachValue((e) -> {
                        if(!ItemEntity.class.isAssignableFrom(e.getClass())) return;
                        ItemEntity comparing = (ItemEntity)e;
                        if(!comparing.getItem().equals(drop)) return; // Already filtered non-merge-able items
                        if(comparing.getItem().getCount() + drop.getCount() > ItemPrototype.get(drop).getMaxStack()) return;
                        comparing.getItem().setCount(comparing.getItem().getCount() + drop.getCount());
                        drop.setCount(-1); // drop.count < 0 means already merged
                        comparing.markMetaChanged(); // Mark changed so it will be broadcast to all viewers
                    });
                    if(drop.getCount() > 0) { // if not merged
                        ItemEntity itemEntity = new ItemEntity(drop);
                        getMap().addEntity(itemEntity);
                        itemEntity.setPosition(new Vector3D(x + r.nextFloat(), y, z + r.nextFloat()));
                    }
                }
            }
        } else {
            breakTime = -1;
            sendBlockChange(x, y, z);
        }
    }

    public void sendBlockChange(int x, int y, int z) {
        LoadedChunk c = getMap().getChunkManager().getChunk(x >> 4, z >> 4, false);
        if(c == null) return;
        getSession().sendNetworkMessage(org.dragonet.cloudland.net.protocol.Map.ServerUpdateBlockMessage.newBuilder()
                .setX(x)
                .setY(y)
                .setZ(z)
                .setId(c.getBlock(x & 0xF, y, z & 0xF))
                .build());
    }

    public void setRawHoldingSlot(int slot){
        inventory.setSelectedSlot(slot);
        getMeta().putInt32(BinaryMetadata.Keys.HOLDING_SLOT_INT, slot);
        markMetaChanged();
    }

    public void setHoldingSlot(int slot) {
        setRawHoldingSlot(slot);

        //TODO: Update player's holding slot
    }

    public boolean isWindowOpenedUniqueId(long uniqueId) {
        return openedWindows.contains(uniqueId);
    }

    public boolean isWindowOpenedWindowId(int windowId) {
        if(windowId == 0) return true;
        return windows.containsKey(windowId);
    }

    public boolean isWindowOpened(GUIWindow window) {
        return openedWindows.contains(window.getUniqueId());
    }

    public GUIWindow getWindowByWindowId(int windowId) {
        if(windowId == 0) return inventory;
        return windows.get(windowId);
    }

    public void openWindow(GUIWindow window) {
        if(window.isOpenedTo(this)){
            throw new IllegalStateException("Already owned by " + window.getOwner().getProfile().getUsername());
        }
        window.setWindowId(getNextWindowId());
        ArrayList<GUI.GUIElement> elements = new ArrayList<>();
        for(InternalGUIElement element : window.getElements()) {
            elements.add(element.serialize());
        }
        getSession().sendNetworkMessage((GUI.ServerWindowOpenMessage.newBuilder()
                .setWindowId(window.getWindowId())
                .addAllItems(elements)
                .setWidth(window.getWidth())
                .setHeight(window.getHeight())
                .setTitle(window.getTitle())
                .build()));
        openedWindows.add(window.getUniqueId());
        windows.put(window.getWindowId(), window);
    }

    public void closeWindow(int windowId, boolean fromClient) {
        if(!windows.containsKey(windowId)) return;
        GUIWindow window = windows.get(windowId);
        if(fromClient) {
            window.onClose();
        } else {
            // Already called onClose()
            session.sendNetworkMessage(GUI.ServerWindowCloseMessage.newBuilder().setWindowId(windowId).build());
        }
        openedWindows.remove(window.getUniqueId());
        windows.remove(window.getWindowId());

        if(windows.size() <= 0 && cursorItem != null) {
            if(cursorItem.getId() == 0) {
                setCursorItem(null);
                return;
            }
            // TODO: Throw this item away
            // then finally set this to null
            setCursorItem(null);
        }
    }

    public void setCursorItem(Item cursorItem) {
        this.cursorItem = cursorItem;
        session.sendNetworkMessage(GUI.ServerCursorItemMessage.newBuilder()
                .setItem((cursorItem == null ? Item.AIR : cursorItem).serialize()).build());
    }

    @Override
    public void broadcastToViewers(Message message) {
        getSession().sendNetworkMessage(message);
        super.broadcastToViewers(message);
    }
}
