package org.dragonet.cloudland.server.util;

/**
 * Created on 2017/1/24.
 */
public enum Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST,
    UP,
    DOWN;


    public int[] add(int x, int y, int z){
        int newX = x;
        int newY = y;
        int newZ = z;
        switch(this){
            case NORTH:
                newZ++;
                break;
            case EAST:
                newX++;
                break;
            case SOUTH:
                newZ--;
                break;
            case WEST:
                newX--;
                break;
            case UP:
                newY++;
                break;
            case DOWN:
                newY--;
                break;
        }
        return new int[]{newX, newY, newZ};
    }
}
