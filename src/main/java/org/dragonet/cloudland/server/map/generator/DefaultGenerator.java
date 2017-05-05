package org.dragonet.cloudland.server.map.generator;

import org.dragonet.cloudland.server.item.ItemPrototype;
import org.dragonet.cloudland.server.map.GameMap;
import org.dragonet.cloudland.server.map.chunk.Chunk;
import org.dragonet.cloudland.server.map.populator.CavePopulator;
import org.dragonet.cloudland.server.map.populator.GroundPopulator;
import org.dragonet.cloudland.server.map.populator.Populator;
import org.dragonet.cloudland.server.map.populator.TreePopulator;
import org.dragonet.cloudland.server.util.NukkitRandom;
import org.dragonet.cloudland.server.util.noise.NoiseGenerator;
import org.dragonet.cloudland.server.util.noise.Simplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Created on 2017/1/10.
 */
public class DefaultGenerator implements Generator {

    private final static int WATER_ID = ItemPrototype.toId("cloudland:water");
    private final static int STONE_ID = ItemPrototype.toId("cloudland:stone");

    private final GameMap map;
    private final long seed;

    private Random random;
    private NukkitRandom nukkitRandom;

    private long localSeed1;
    private long localSeed2;
    private Simplex noiseSeaFloor;
    private Simplex noiseLand;
    private Simplex noiseMountains;
    private Simplex noiseBaseGround;
    private Simplex noiseRiver;

    private int heightOffset;

    private final int seaHeight = 62;
    private final int seaFloorHeight = 48;
    private final int beathStartHeight = 60;
    private final int beathStopHeight = 64;
    private final int bedrockDepth = 5;
    private final int seaFloorGenerateRange = 5;
    private final int landHeightRange = 18; // 36 / 2
    private final int mountainHeight = 13; // 26 / 2
    private final int basegroundHeight = 3;

    private List<Populator> populators = new ArrayList<>();

    public DefaultGenerator(GameMap map, long seed) {
        this.map = map;
        this.seed = seed;

        init();
    }

    private void init(){
        this.nukkitRandom = new NukkitRandom(seed);
        this.random = new Random(seed);
        this.localSeed1 = this.random.nextLong();
        this.localSeed2 = this.random.nextLong();
        this.noiseSeaFloor = new Simplex(this.nukkitRandom, 1F, 1F / 8F, 1F / 64F);
        this.noiseLand = new Simplex(this.nukkitRandom, 2F, 1F / 8F, 1F / 512F);
        this.noiseMountains = new Simplex(this.nukkitRandom, 4F, 1F, 1F / 500F);
        this.noiseBaseGround = new Simplex(this.nukkitRandom, 4F, 1F / 4F, 1F / 64F);
        this.noiseRiver = new Simplex(this.nukkitRandom, 2F, 1F, 1F / 512F);
        this.nukkitRandom.setSeed(seed);
        this.heightOffset = new NukkitRandom(seed).nextRange(-5, 3);

        populators.add(new CavePopulator(map));
        populators.add(new GroundPopulator());
        populators.add(new TreePopulator(map, 0, 5));
    }

    @Override
    public Chunk generate(Chunk chunk, boolean populate) {
        int cx = chunk.getX();
        int cz = chunk.getZ();

        if(chunk.isGenerated()) {
            if(!chunk.isPopulated() && populate) populateChunk(chunk);
            return chunk;
        }
        this.nukkitRandom.setSeed(cx * localSeed1 ^ cz * localSeed2 ^ seed);

        double[][] seaFloorNoise = NoiseGenerator.getFastNoise2D(this.noiseSeaFloor, 16, 16, 4, cx << 4, 0, cz << 4);
        double[][] landNoise = NoiseGenerator.getFastNoise2D(this.noiseLand, 16, 16, 4, cx << 4, 0, cz * 16);
        double[][] mountainNoise = NoiseGenerator.getFastNoise2D(this.noiseMountains, 16, 16, 4, cx << 4, 0, cz << 4);
        double[][] baseNoise = NoiseGenerator.getFastNoise2D(this.noiseBaseGround, 16, 16, 4, cx << 4, 0, cz << 4);
        double[][] riverNoise = NoiseGenerator.getFastNoise2D(this.noiseRiver, 16, 16, 4, cx << 4, 0, cz << 4);

        for (int genx = 0; genx < 16; genx++) {
            for (int genz = 0; genz < 16; genz++) {

                // Biome biome;
                boolean canBaseGround = false;
                boolean canRiver = true;

                //using a quadratic function which smooth the world
                //y = (2.956x)^2 - 0.6,  (0 <= x <= 2)
                double landHeightNoise = landNoise[genx][genz] + 1F;
                landHeightNoise *= 2.956;
                landHeightNoise = landHeightNoise * landHeightNoise;
                landHeightNoise = landHeightNoise - 0.6F;
                landHeightNoise = landHeightNoise > 0 ? landHeightNoise : 0;

                //generate mountains
                double mountainHeightGenerate = mountainNoise[genx][genz] - 0.2F;
                mountainHeightGenerate = mountainHeightGenerate > 0 ? mountainHeightGenerate : 0;
                int mountainGenerate = (int) (mountainHeight * mountainHeightGenerate);

                int landHeightGenerate = (int) (landHeightRange * landHeightNoise);
                if (landHeightGenerate > landHeightRange) {
                    if (landHeightGenerate > landHeightRange) {
                        canBaseGround = true;
                    }
                    landHeightGenerate = landHeightRange;
                }

                int genyHeight = seaFloorHeight + landHeightGenerate;
                genyHeight += mountainGenerate;

                //prepare for generate ocean, desert, and land
                if (genyHeight < beathStartHeight) {
                    if (genyHeight < beathStartHeight - 5) {
                        genyHeight += (int) (seaFloorGenerateRange * seaFloorNoise[genx][genz]);
                    }
                    // biome = Biome.getBiome(Biome.OCEAN);
                    if (genyHeight < seaFloorHeight - seaFloorGenerateRange) {
                        genyHeight = seaFloorHeight;
                    }
                    canRiver = false;
                } else if (genyHeight <= beathStopHeight && genyHeight >= beathStartHeight) {
                    // biome = Biome.getBiome(Biome.BEACH);
                } else {
                    // biome = this.pickBiome(chunkX * 16 + genx, chunkZ * 16 + genz);
                    if (canBaseGround) {
                        int baseGroundHeight = (int) (landHeightRange * landHeightNoise) - landHeightRange;
                        int baseGroundHeight2 = (int) (basegroundHeight * (baseNoise[genx][genz] + 1F));
                        if (baseGroundHeight2 > baseGroundHeight) baseGroundHeight2 = baseGroundHeight;
                        if (baseGroundHeight2 > mountainGenerate)
                            baseGroundHeight2 = baseGroundHeight2 - mountainGenerate;
                        else baseGroundHeight2 = 0;
                        genyHeight += baseGroundHeight2;
                    }
                }
                if (canRiver && genyHeight <= seaHeight - 5) {
                    canRiver = false;
                }
                //generate river
                if (canRiver) {
                    double riverGenerate = riverNoise[genx][genz];
                    if (riverGenerate > -0.25F && riverGenerate < 0.25F) {
                        riverGenerate = riverGenerate > 0 ? riverGenerate : -riverGenerate;
                        riverGenerate = 0.25F - riverGenerate;
                        //y=x^2 * 4 - 0.0000001
                        riverGenerate = riverGenerate * riverGenerate * 4F;
                        //smooth again
                        riverGenerate = riverGenerate - 0.0000001F;
                        riverGenerate = riverGenerate > 0 ? riverGenerate : 0;
                        genyHeight -= riverGenerate * 64;
                        if (genyHeight < seaHeight) {
                            //biome = Biome.getBiome(Biome.RIVER);
                            //to generate river floor
                            if (genyHeight <= seaHeight - 8) {
                                int genyHeight1 = seaHeight - 9 + (int) (basegroundHeight * (baseNoise[genx][genz] + 1F));
                                int genyHeight2 = genyHeight < seaHeight - 7 ? seaHeight - 7 : genyHeight;
                                genyHeight = genyHeight1 > genyHeight2 ? genyHeight1 : genyHeight2;
                            }
                        }
                    }
                }
                // chunk.setBiomeId(genx, genz, biome.getId());
                //biome color
                //todo: smooth chunk color
                //int biomecolor = biome.getColor();
                //chunk.setBiomeColor(genx, genz, (biomecolor >> 16), (biomecolor >> 8) & 0xff, (biomecolor & 0xff));
                //generating
                int generateHeight = genyHeight > seaHeight ? genyHeight : seaHeight;
                for (int geny = 0; geny <= generateHeight; geny++) {
                    if (geny <= bedrockDepth && (geny == 0 || nukkitRandom.nextRange(1, 5) == 1)) {
                        // chunk.setBlock(genx, geny, genz, Block.BEDROCK);
                        chunk.setBlock(genx, geny, genz, STONE_ID);
                    } else if (geny > genyHeight) { /*
                        if ((biome.getId() == Biome.ICE_PLAINS || biome.getId() == Biome.TAIGA) && geny == seaHeight) {
                            chunk.setBlock(genx, geny, genz, Block.ICE);
                        } else {
                            chunk.setBlock(genx, geny, genz, Block.STILL_WATER);
                        }*/
                        chunk.setBlock(genx, geny, genz, WATER_ID);
                    } else {
                        // chunk.setBlock(genx, geny, genz, Block.STONE);
                        chunk.setBlock(genx, geny, genz, STONE_ID);
                    }
                }
            }
        }

        chunk.markGenerated();

        if(!chunk.isPopulated() && populate) {
            populateChunk(chunk);
        }

        return chunk;
    }

    private void populateChunk(Chunk chunk) {
        for(Populator p : populators) {
            p.populate(chunk, nukkitRandom);
        }

        chunk.markPopulated();
    }
}
