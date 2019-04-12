package com.cateye.vtm.util;

import android.graphics.Rect;

import org.oscim.core.MercatorProjection;
import org.oscim.core.Tile;

import java.util.ArrayList;
import java.util.List;

public class TileDownloadTools {
    /**
     * 根据屏幕中的rect坐标计算需要下载的tile的集合
     */
    private List<Tile> getRectLatitudeArray(Rect rect, byte currentZoomLevel, byte startZoomLevel, byte endZoomLevel) {
        List<Tile> tileList = new ArrayList<>();
        long mapSize = MercatorProjection.getMapSize(currentZoomLevel);
        if (rect != null) {
            double longitudeLeft = MercatorProjection.pixelXToLongitude(rect.left, mapSize);
            double longitudeRight = MercatorProjection.pixelXToLongitude(rect.right, mapSize);
            double latitudeTop = MercatorProjection.pixelYToLatitude(rect.top, mapSize);
            double latitudeBottom = MercatorProjection.pixelYToLatitude(rect.bottom, mapSize);

            for (byte i = startZoomLevel; i <= endZoomLevel; i++) {
                int tileNumLeft = MercatorProjection.longitudeToTileX(longitudeLeft, i);
                int tileNumRight = MercatorProjection.longitudeToTileX(longitudeRight, i);
                int tileNumTop = MercatorProjection.latitudeToTileY(latitudeTop, i);
                int tileNumBottom = MercatorProjection.latitudeToTileY(latitudeBottom, i);

                int currentMaxXY = 2 << i;
                if (tileNumRight < tileNumLeft) {
                    tileNumRight += tileNumRight + currentMaxXY;
                }
                if (tileNumBottom < tileNumTop) {
                    tileNumBottom += tileNumBottom + currentMaxXY;
                }

                for (int tileX = tileNumLeft; tileX <= tileNumRight; tileX++) {
                    for (int tileY = tileNumTop; tileY <= tileNumBottom; tileY++) {
                        tileList.add(new Tile(tileX % currentMaxXY, tileY % currentMaxXY, i));
                    }
                }
            }
        }
        return tileList;
    }
}
