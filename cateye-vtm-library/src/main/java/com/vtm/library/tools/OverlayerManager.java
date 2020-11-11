package com.vtm.library.tools;

import org.oscim.layers.Layer;
import org.oscim.map.Map;

/**
 * Created by xiaoxiao on 2018/11/27.
 */

public class OverlayerManager {
    private Map mMap;
    private static OverlayerManager instance;

    public static OverlayerManager getInstance(Map mMap) {
        if (instance == null) {
            if (mMap == null) {
                mMap = CatEyeMapManager.getInstance().getCatEyeMap();
            }
            instance = new OverlayerManager(mMap);
        }

        return instance;
    }

    public OverlayerManager(Map mMap) {
        this.mMap = mMap;
    }

    public Layer getLayerByName(String name) {
        if (mMap != null && mMap.layers() != null) {
            for (Layer layer : mMap.layers()) {
                if (name != null && name.equals(layer.getName())) {
                    return layer;
                }
            }
        }
        return null;
    }

    public Layer getLayerByName(String name, Class layerType) {
        if (mMap != null && mMap.layers() != null) {
            for (Layer layer : mMap.layers()) {
                if (name != null && name.equals(layer.getName()) && layer.getClass().equals(layerType)) {
                    return layer;
                }
            }
        }
        return null;
    }
}
