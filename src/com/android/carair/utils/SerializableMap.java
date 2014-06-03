
package com.android.carair.utils;

import java.io.Serializable;
import java.util.Map;

/**
 * 序列化map供Bundle传递map使用 Created on 13-12-9.
 */
public class SerializableMap implements Serializable {

    private Map<Double, Double> map;

    public Map<Double, Double> getMap() {
        return map;
    }

    public void setMap(Map<Double, Double> map) {
        this.map = map;
    }
}
