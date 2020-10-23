package com.vtm.library.tools;

import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerInterface;

import java.util.Iterator;
import java.util.List;

public class DrawLayerUtils {
    private static DrawLayerUtils instance;

    public static DrawLayerUtils getInstance() {
        if (instance == null) {
            instance = new DrawLayerUtils();
        }
        return instance;
    }

    public GeoPoint removeItemFromList(GeoPoint geoPoint, List<MarkerInterface> itemList) {
        if (itemList != null && !itemList.isEmpty()) {
            Iterator<MarkerInterface> iterator = itemList.iterator();
            while (iterator.hasNext()) {
                MarkerInterface item = iterator.next();
                if (item.getPoint().getLatitude() == geoPoint.getLatitude() && item.getPoint().getLongitude() == geoPoint.getLongitude()) {
                    iterator.remove();
                    return geoPoint;
                }
            }
        }
        return null;
    }

    public List<GeoPoint> removeGeoPointListFromMultiPath(List<GeoPoint> geoPointList, List<List<GeoPoint>> multiPathList) {
        if (geoPointList == null || geoPointList.isEmpty()) {
            return null;
        }
        if (multiPathList != null && !multiPathList.isEmpty()) {
            Iterator<List<GeoPoint>> listIterator = multiPathList.iterator();
            while (listIterator.hasNext()) {
                List<GeoPoint> path = listIterator.next();
                if (geoPointList.size() != path.size()) {
                    continue;
                }
                for (int i = 0; i < geoPointList.size(); i++) {
                    if (geoPointList.get(i).getLatitude() == path.get(i).getLatitude() && geoPointList.get(i).getLongitude() == path.get(i).getLongitude()) {
                        listIterator.remove();
                        return geoPointList;
                    }
                }
            }
        }
        return null;
    }
}
