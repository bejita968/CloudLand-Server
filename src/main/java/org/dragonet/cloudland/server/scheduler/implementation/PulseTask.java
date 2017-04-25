package org.dragonet.cloudland.server.scheduler.implementation;

import org.dragonet.cloudland.server.item.Items;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.map.LoadedChunk;
import org.dragonet.cloudland.server.scheduler.BukkitTask;
import lombok.Getter;
import lombok.Setter;

public class PulseTask implements Runnable {
    private final GameMap map;
    private final int[] location;
    private final int[] chunkPosition;
    private long delay;
    private boolean single;

    private Items original;

    @Setter
    @Getter
    private BukkitTask task;

    public PulseTask(GameMap map, int[] location, long delay, boolean single) {
        this.map = map;
        this.location = location;
        chunkPosition = new int[]{this.location[0] >> 4, this.location[1] >> 4};
        this.delay = delay;
        this.single = single;
    }


    @Override
    public void run() {
        long fb = map.getFullBlockAt(location[0], location[1], location[2]);
        Items block = Items.get((int)(fb >> 32), (int)(fb & 0xFFFFFFFF));
        if (block == null) {
            task.cancel();
            return;
        }
        LoadedChunk c = map.getChunkManager().getChunk(chunkPosition[0], chunkPosition[1], false, false);
        if (c == null) {
            return;
        }
        if (original != null && !block.equals(original)) {
            task.cancel();
            return;
        }
        original = block;
        /*
        ItemTable table = ItemTable.instance();
        BlockType type = table.getBlock(originalMaterial);
        if (type != null) {
            type.receivePulse((GlowBlock) block);
        }
        */
    }
}
