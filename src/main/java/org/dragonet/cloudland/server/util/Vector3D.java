package org.dragonet.cloudland.server.util;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Created on 2017/1/10.
 */
@AllArgsConstructor
@NoArgsConstructor
public class Vector3D {

    public double x;
    public double y;
    public double z;

    public long getBlockX() {
        return (long)x;
    }

    public long getBlockY(){
        return (long)y;
    }

    public long getBlockZ(){
        return (long)z;
    }

    public double getDistanceSquared(Vector3D target) {
        if(target == null) return Double.MAX_VALUE;
        double dxs = (target.x - x) * (target.x - x);
        double dys = (target.y - y) * (target.y - y);
        double dzs = (target.z - z) * (target.z - z);
        return dxs + dys + dzs;
    }

    public Vector3D add(Vector3D v) {
        Vector3D ret = new Vector3D(x, y, z);
        ret.x += v.x;
        ret.y += v.y;
        ret.z += v.z;
        return ret;
    }

    public Vector3D substract(Vector3D v) {
        Vector3D ret = new Vector3D(x, y, z);
        ret.x -= v.x;
        ret.y -= v.y;
        ret.z -= v.z;
        return ret;
    }
}
