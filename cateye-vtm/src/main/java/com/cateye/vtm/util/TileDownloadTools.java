package com.cateye.vtm.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;

import com.canyinghao.candialog.CanDialog;
import com.cateye.android.vtm.R;

import org.oscim.core.MercatorProjection;
import org.oscim.core.Tile;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class TileDownloadTools {
    /**
     * 根据屏幕中的rect坐标计算需要下载的tile的集合
     */
    public List<Tile> getRectLatitudeArray(Rect rect, byte currentZoomLevel, byte startZoomLevel, byte endZoomLevel) {
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


    public void downloadTile(Tile tile){

    }

    /**
     * 打开下载tile的对话框
     * */
    public void openDownloadTileDialog(Activity mContext, List<Tile> tileList){
        View downloadProgressView = mContext.findViewById(R.id.tile_download_progress);
        new CanDialog.Builder(mContext).setCancelable(false).setView(R.layout.tile_download_progress).show();
        Observable.create(new ObservableOnSubscribe<Tile>() {
            @Override
            public void subscribe(ObservableEmitter<Tile> emitter) throws Exception {

            }
        })
    }
}
