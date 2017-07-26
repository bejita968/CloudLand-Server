package org.dragonet.cloudland.server.util.math;

import org.dragonet.cloudland.server.util.NukkitRandom;

import java.util.Random;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class NukkitMath {

    public static int floorDouble(double n) {
        int i = (int) n;
        return n >= i ? i : i - 1;
    }

    public static int ceilDouble(double n) {
        int i = (int) (n + 1);
        return n >= i ? i : i - 1;
    }

    public static int floorFloat(float n) {
        int i = (int) n;
        return n >= i ? i : i - 1;
    }

    public static int ceilFloat(float n) {
        int i = (int) (n + 1);
        return n >= i ? i : i - 1;
    }

    public static int randomRange(NukkitRandom random) {
        return randomRange(random, 0);
    }

    public static int randomRange(NukkitRandom random, int start) {
        return randomRange(random, start, 0x7fffffff);
    }

    public static int randomRange(NukkitRandom random, int start, int end) {
        return start + (random.nextInt() % (end + 1 - start));
    }

    public static int randomRange(Random random, int start, int end) {
        return start + (random.nextInt() % (end + 1 - start));
    }

    public static double round(double d) {
        return round(d, 0);
    }

    public static double round(double d, int precision) {
        return ((double) Math.round(d * Math.pow(10, precision))) / Math.pow(10, precision);
    }

}
