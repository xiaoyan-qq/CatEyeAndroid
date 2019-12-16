package com.vtm.library.tools;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapProgressBar;
import com.beardedhen.androidbootstrap.BootstrapProgressBarGroup;
import com.canyinghao.candialog.CanDialog;
import com.canyinghao.candialog.CanDialogInterface;
import com.example.cateye_vtm_library.R;
import com.hss01248.notifyutil.NotifyUtil;
import com.vondear.rxtool.RxBarTool;
import com.vondear.rxtool.RxTool;
import com.vondear.rxui.view.dialog.RxDialog;

import org.greenrobot.eventbus.EventBus;
import org.oscim.backend.CanvasAdapter;
import org.oscim.core.GeoPoint;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Tile;
import org.oscim.layers.Layer;
import org.oscim.layers.tile.TileLayer;
import org.oscim.layers.tile.bitmap.BitmapTileLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.map.Map;
import org.oscim.tiling.ITileCache;
import org.oscim.tiling.source.HttpEngine;
import org.oscim.tiling.source.LwHttp;
import org.oscim.tiling.source.UrlTileSource;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
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
    private Dialog dialog;
    private CatEyeTileDownloadReceiver receiver;

    public TileDownloader() {
    }

    /**
     * 下载指定的tile文件
     */
    public boolean download(Tile mTile, UrlTileSource mUrlTileSource, HttpEngine mHttpEngine) {
        if (mUrlTileSource != null) {
            ITileCache mTileCache = mUrlTileSource.tileCache;
            ITileCache.TileReader c = mTileCache.getTile(mTile);
            if (c != null) {
                return true;
            }
            if (mUrlTileSource.getTilePath()[mUrlTileSource.getTilePath().length - 1].endsWith(".tif") || mUrlTileSource.getTilePath()[mUrlTileSource.getTilePath().length - 1].endsWith("json")) {
                return false;
            }
            ITileCache.TileWriter cacheWriter = null;
            try {
                mHttpEngine.sendRequest(mTile);
                InputStream is = mHttpEngine.read();
                if (is != null && is.available() > 0) {
                    cacheWriter = mTileCache.writeTile(mTile);
                    mHttpEngine.setCache(cacheWriter.getOutputStream());
                    CanvasAdapter.decodeBitmap(is);
                } else {
                    return false;
                }
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
        }
        return false;
    }

    /**
     * 根据屏幕中的rect坐标计算需要下载的tile的集合
     */
    public List<Tile> getRectLatitudeArray(Map mMap, Rect rect, byte startZoomLevel, byte endZoomLevel) {
        List<Tile> tileList = new ArrayList<>();
        if (rect != null) {
            GeoPoint leftTopGeoPoint = mMap.viewport().fromScreenPoint(rect.left, rect.top);
            GeoPoint rightBottomGeoPoint = mMap.viewport().fromScreenPoint(rect.right, rect.bottom);

            for (byte i = startZoomLevel; i <= endZoomLevel; i++) {
                int tileNumLeft = MercatorProjection.longitudeToTileX(leftTopGeoPoint.getLongitude(), i);
                int tileNumRight = MercatorProjection.longitudeToTileX(rightBottomGeoPoint.getLongitude(), i);
                int tileNumTop = MercatorProjection.latitudeToTileY(leftTopGeoPoint.getLatitude(), i);
                int tileNumBottom = MercatorProjection.latitudeToTileY(rightBottomGeoPoint.getLatitude(), i);

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


    /**
     * 打开下载tile的对话框
     */
    private Subscription subscription = null;

    public void openDownloadTileDialog(final Activity mContext, final List<Tile> tileList, final List<Layer> mapLayerList) {
        View downloadProgressView = LayoutInflater.from(mContext).inflate(R.layout.layer_tile_download_progress, null);
        dialog = new Dialog(mContext);
        dialog.setCancelable(false);
        dialog.setContentView(downloadProgressView);
        dialog.setTitle("缓存tile地图数据");
        dialog.show();
//        dialog = new CanDialog.Builder(mContext).setCancelable(false).setView(downloadProgressView).setTitle("缓存tile地图数据").show();
        IntentFilter filter=new IntentFilter("catEye_tile_download");
        receiver=new CatEyeTileDownloadReceiver();
        mContext.registerReceiver(receiver,filter);

        final ProgressBar pb_download = downloadProgressView.findViewById(R.id.pb_tile_download);
        final TextView tv_download = downloadProgressView.findViewById(R.id.tv_tile_download);
        final BootstrapButton bbtn_mini = downloadProgressView.findViewById(R.id.bbtn_tile_download_mini);
        final BootstrapButton bbtn_ok = downloadProgressView.findViewById(R.id.bbtn_tile_download_ok);

        final List<UrlTileSource> urlTileSourceList = new ArrayList<>();

        final Intent tileDownloaderIntent=new Intent("catEye_tile_download");
        tileDownloaderIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Flowable.create(new FlowableOnSubscribe<Tile>() {
            @Override
            public void subscribe(FlowableEmitter<Tile> emitter) throws Exception {
                if (urlTileSourceList != null && !urlTileSourceList.isEmpty()) {
                    b:
                    for (int i = 0; i < urlTileSourceList.size(); i++) {
                        UrlTileSource mUrlTileSource = urlTileSourceList.get(i);
                        HttpEngine httpEngine = mUrlTileSource.getHttpEngine();
                        for (int j = 0; j < tileList.size(); j++) {
                            Tile tile = tileList.get(j);
                            if (!emitter.isCancelled()) {
                                while (emitter.requested() == 0) {
                                    if (emitter.isCancelled()) {
                                        System.out.println("这里break了urlTileSourceList:===" + i + "/" + urlTileSourceList.size() + "  tileList:===" + j + "/" + tileList.size());
                                        break b;
                                    }
                                }
                                download(tile, mUrlTileSource, httpEngine);
                                emitter.onNext(tile);
                            }
                        }
                    }
                }
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Tile>() {
            @Override
            public void onSubscribe(Subscription d) {
                subscription = d;
                if (mapLayerList != null && !mapLayerList.isEmpty()) {
                    for (Layer layer : mapLayerList) {

                        if (layer instanceof BitmapTileLayer && ((BitmapTileLayer) layer).getTileSource() instanceof UrlTileSource) {
                            urlTileSourceList.add((UrlTileSource) ((BitmapTileLayer) layer).getTileSource());
                        }
                        if (layer instanceof VectorTileLayer && ((VectorTileLayer) layer).getTileSource() instanceof UrlTileSource) {
                            urlTileSourceList.add((UrlTileSource) ((VectorTileLayer) layer).getTileSource());
                        }
                    }
                }
                pb_download.setMax(tileList.size() * urlTileSourceList.size());
                pb_download.setProgress(0);
                bbtn_mini.setEnabled(true);
                bbtn_ok.setEnabled(false);

                d.request(1);
            }

            @Override
            public void onNext(Tile tile) {
                pb_download.setProgress(pb_download.getProgress() + 1 <= pb_download.getMax() ? pb_download.getProgress() + 1 : pb_download.getMax());
                tv_download.setText(pb_download.getProgress() + "/" + pb_download.getMax());
                System.out.println("进度:" + pb_download.getProgress() + "/" + pb_download.getMax());

                NotifyUtil.buildProgress(MSG_DOWNLOAD_TILE_FINISH,R.mipmap.ic_launcher_foreground,"正在下载",pb_download.getProgress() + 1,pb_download.getMax(),"下载进度:%d%%")
                        .setContentIntent(PendingIntent.getBroadcast(mContext,MSG_DOWNLOAD_TILE_FINISH,tileDownloaderIntent,PendingIntent.FLAG_ONE_SHOT))
                        .setFullScreenIntent(PendingIntent.getBroadcast(mContext,MSG_DOWNLOAD_TILE_FINISH,tileDownloaderIntent,PendingIntent.FLAG_ONE_SHOT)).show();

                subscription.request(1);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                bbtn_ok.setEnabled(true);

                // 结束下载后，择缓存地图按钮置为可用，直到下载完成后才可以进行下一次下载
                Message msg = Message.obtain();
                msg.what = 0x1017; // SystemConstant.MSG_WHAT_TILE_DOWNLAOD_ENABLE
                msg.obj = true;
                EventBus.getDefault().post(msg);

            }
        });

        View.OnClickListener dissmissClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // 通知主界面，清除下载区域的绘制框
                Message msg = Message.obtain();
                msg.what = MSG_DOWNLOAD_TILE_FINISH;
                EventBus.getDefault().post(msg);

                if (receiver!=null){
                    mContext.unregisterReceiver(receiver);
                }
            }
        };
        bbtn_ok.setOnClickListener(dissmissClickListener);
        bbtn_mini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyUtil.buildProgress(MSG_DOWNLOAD_TILE_FINISH,R.mipmap.ic_launcher_foreground,"正在下载",pb_download.getProgress() + 1,pb_download.getMax(),"下载进度:%d%%")
                        .setContentIntent(PendingIntent.getBroadcast(mContext,MSG_DOWNLOAD_TILE_FINISH,tileDownloaderIntent,PendingIntent.FLAG_ONE_SHOT))
                        .setFullScreenIntent(PendingIntent.getBroadcast(mContext,MSG_DOWNLOAD_TILE_FINISH,tileDownloaderIntent,PendingIntent.FLAG_ONE_SHOT)).show();
                dialog.hide();
            }
        });
    }

    public class CatEyeTileDownloadReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == "catEye_tile_download") {
                if (dialog!=null){
                    dialog.show();
                    context.startActivity(intent);//显示主界面，主界面设置了clear_top的tag，如果当前已经在显示了，不会重复显示
                }
            }
        }
    }

    public static final int MSG_DOWNLOAD_TILE_FINISH = 0x10111;
}
