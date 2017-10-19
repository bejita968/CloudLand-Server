package org.dragonet.cloudland.server.network.tasks;

import org.dragonet.cloudland.net.protocol.Map;
import org.dragonet.cloudland.server.entity.PlayerEntity;
import org.dragonet.cloudland.server.map.chunk.Chunk;
import org.dragonet.cloudland.server.map.chunk.ChunkSection;
import com.google.protobuf.ByteString;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created on 2017/1/12.
 */
public final class SendChunkTask implements Runnable{

    public final static int MAX_QUEUE_SIZE = 16;

    private final PlayerEntity player;

    private LinkedBlockingQueue<Chunk> jobs = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);

    public SendChunkTask(PlayerEntity player) {
        this.player = player;
    }

    public boolean queue(Chunk chunk) {
        if (jobs.size() >= MAX_QUEUE_SIZE) return false;
        jobs.add(chunk.clone());
        return true;
    }

    @Override
    public void run() {
        int count = 0;
        while(jobs.size() > 0 && count < 4) {
            sendOne();
            count++;
        }
    }

    public void sendOne() {
        if (jobs.isEmpty()) return;
        Chunk c = jobs.poll();
        if (c == null) return;
        ChunkSection[] s = c.getSections();
        for (int y = 0; y < Chunk.SECTION_COUNT; y++) {
            if (s[y] == null) continue;
            player.getSession().sendNetworkMessage(Map.ServerChunkMessage.newBuilder()
                    .setX(c.getX())
                    .setY(y)
                    .setZ(c.getZ())
                    .setChunk(ByteString.copyFrom(s[y].getIds()))
                    .build());
        }
    }
}
