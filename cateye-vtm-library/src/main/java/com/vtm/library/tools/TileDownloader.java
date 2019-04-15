package com.vtm.library.tools;

import android.app.Activity;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapProgressBar;
import com.canyinghao.candialog.CanDialog;
import com.canyinghao.candialog.CanDialogInterface;
import com.example.cateye_vtm_library.R;

import org.oscim.core.MercatorProjection;
import org.oscim.core.Tile;
import org.oscim.tiling.ITileCache;
import org.oscim.tiling.source.HttpEngine;
import org.oscim.tiling.source.LwHttp;
import org.oscim.tiling.source.UrlTileSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class TileDownloader {
    private UrlTileSource mUrlTileSource;
    private ITileCache mTileCache;
    private HttpEngine mHttpEngine;

    public TileDownloader(UrlTileSource mUrlTileSource, ITileCache mTileCache) {
        this.mUrlTileSource = mUrlTileSource;
        this.mTileCache = mTileCache;
        this.mHttpEngine = new LwHttp.LwHttpFactory().create(mUrlTileSource);
    }

    /**
     * 下载指定的tile文件
     */
    public boolean download(Tile mTile) {
        ITileCache.TileReader c = mTileCache.getTile(mTile);
        if (c != null) {
            return true;
        }
        ITileCache.TileWriter cacheWriter = null;
        try {
            mHttpEngine.sendRequest(mTile);
            cacheWriter = mTileCache.writeTile(mTile);
            mHttpEngine.setCache(cacheWriter.getOutputStream());
            mHttpEngine.read();
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cacheWriter != null) {
                cacheWriter.complete(true);
            }
            mHttpEngine.requestCompleted(true);
        }
        return true;
    }

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


    public void downloadTile(Tile tile) {

    }

    /**
     * 打开下载tile的对话框
     */
    public void openDownloadTileDialog(final Activity mContext, final List<Tile> tileList) {
        View downloadProgressView = LayoutInflater.from(mContext).inflate(R.layout.layer_tile_download_progress, null);
        final CanDialog dialog = new CanDialog.Builder(mContext).setCancelable(false).setView(downloadProgressView).show();
        final BootstrapProgressBar bpb_download = downloadProgressView.findViewById(R.id.bpb_tile_download);
        final TextView tv_download = downloadProgressView.findViewById(R.id.tv_tile_download);
        final BootstrapButton bbtn_cancel = downloadProgressView.findViewById(R.id.bbtn_tile_download_cancel);
        final BootstrapButton bbtn_ok = downloadProgressView.findViewById(R.id.bbtn_tile_download_ok);

        Observable.fromIterable(tileList).map(new Function<Tile, Tile>() {

            @Override
            public Tile apply(Tile tile) throws Exception {
                download(tile);
                return tile;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Tile>() {
            @Override
            public void onSubscribe(Disposable d) {
                bpb_download.setMaxProgress(tileList.size());
                bpb_download.setProgress(0);
            }

            @Override
            public void onNext(Tile tile) {
                bpb_download.setProgress(bpb_download.getProgress() + 1);
                tv_download.setText(bpb_download + "/" + bpb_download.getMaxProgress());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                bbtn_cancel.setEnabled(true);
                bbtn_ok.setEnabled(true);
            }
        });

        View.OnClickListener dissmissClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        };
        bbtn_cancel.setOnClickListener(dissmissClickListener);
        bbtn_ok.setOnClickListener(dissmissClickListener);
    }
}
