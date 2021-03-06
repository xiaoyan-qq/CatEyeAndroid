package com.vtm.library.tools;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Build;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.vtm.library.R;
import com.vtm.library.utils.SystemConstant;

import org.greenrobot.eventbus.EventBus;
import org.oscim.backend.CanvasAdapter;
import org.oscim.core.GeoPoint;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Tile;
import org.oscim.layers.Layer;
import org.oscim.layers.tile.bitmap.BitmapTileLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.map.Map;
import org.oscim.tiling.ITileCache;
import org.oscim.tiling.source.HttpEngine;
import org.oscim.tiling.source.UrlTileSource;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

public class TileDownloader {
    private static TileDownloader instance;
    private Dialog dialog;
    private CatEyeTileDownloadReceiver receiver;
    private NotificationManager notificationManager; // 顶部标题栏管理类
    private NotificationCompat.Builder builder;

    public static TileDownloader getInstance() {
        if (instance == null) {
            instance = new TileDownloader();
        }
        return instance;
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

    public void showDialog() {
        if (dialog!=null) {
            dialog.show();
            // 通知主界面，重新显示下载区域的绘制框
            Message msg = Message.obtain();
            msg.what = SystemConstant.MSG_DOWNLOAD_TILE_REOPEN;
            EventBus.getDefault().post(msg);
        }
    }

    public void openDownloadTileDialog(final Activity mContext, final List<Tile> tileList, final List<Layer> mapLayerList) {
        View downloadProgressView = LayoutInflater.from(mContext).inflate(R.layout.layer_tile_download_progress, null);
        dialog = new Dialog(mContext);
        dialog.setCancelable(false);
        dialog.setContentView(downloadProgressView);
        dialog.setTitle("缓存tile地图数据");
        dialog.show();
//        dialog = new CanDialog.Builder(mContext).setCancelable(false).setView(downloadProgressView).setTitle("缓存tile地图数据").show();
        IntentFilter filter = new IntentFilter();
        receiver = new CatEyeTileDownloadReceiver();
        mContext.registerReceiver(receiver, filter);

        final ProgressBar pb_download = downloadProgressView.findViewById(R.id.pb_tile_download);
        final TextView tv_download = downloadProgressView.findViewById(R.id.tv_tile_download);
        final BootstrapButton bbtn_mini = downloadProgressView.findViewById(R.id.bbtn_tile_download_mini);
        final BootstrapButton bbtn_ok = downloadProgressView.findViewById(R.id.bbtn_tile_download_ok);

        final List<UrlTileSource> urlTileSourceList = new ArrayList<>();

        Flowable tileDownloadFlowable = Flowable.create(new FlowableOnSubscribe<Tile>() {
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
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io());

        Flowable<Long> delayConsumer = Flowable.interval(80, TimeUnit.MILLISECONDS).observeOn(Schedulers.io());

//        Flowable.zip(tileDownloadFlowable, delayConsumer, new BiFunction<Tile, Long, Tile>() {
//
//            @NonNull
//            @Override
//            public Tile apply(@NonNull Tile tile, @NonNull Long aLong) throws Exception {
//                return tile;
//            }
//        })
        tileDownloadFlowable.observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Tile>() {
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

                d.request(50);
                builder = createProgressNotificationBuilder(mContext, 0, pb_download.getMax());
            }

            @Override
            public void onNext(Tile tile) {
                pb_download.setProgress(pb_download.getProgress() < pb_download.getMax() ? pb_download.getProgress() + 1 : pb_download.getMax());
                tv_download.setText(pb_download.getProgress() + "/" + pb_download.getMax());
                System.out.println("进度:" + pb_download.getProgress() + "/" + pb_download.getMax());

                if (builder!=null) {
                    builder.setProgress(pb_download.getMax(), pb_download.getProgress(), false);
                    if (pb_download.getProgress()<pb_download.getMax()) {
                        builder.setContentText("正在下载:"+pb_download.getProgress()+"/"+pb_download.getMax());
                    } else {
                        builder.setContentText("下载完成:"+pb_download.getProgress()+"/"+pb_download.getMax());
                    }
                }
                if (notificationManager!=null) {
                    notificationManager.notify(SystemConstant.MSG_DOWNLOAD_TILE_FINISH, builder.build());
                }
                subscription.request(5);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                bbtn_ok.setEnabled(true);
                System.out.println("下载结束:" + pb_download.getProgress() + "/" + pb_download.getMax());

                if (builder!=null) {
                    builder.setProgress(pb_download.getMax(), pb_download.getMax(), false);
                    builder.setContentText("下载完成!");
                }
                if (notificationManager!=null) {
                    notificationManager.notify(SystemConstant.MSG_DOWNLOAD_TILE_FINISH, builder.build());
                }

                // 结束下载后，择缓存地图按钮置为可用，直到下载完成后才可以进行下一次下载
                Message msg = Message.obtain();
                msg.what = 0x1017; // SystemConstant.MSG_WHAT_TILE_DOWNLAOD_ENABLE
                msg.obj = true;
                EventBus.getDefault().post(msg);


                Intent tileDownloaderIntent = new Intent("catEye_tile_download");
                tileDownloaderIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                tileDownloaderIntent.setPackage(mContext.getPackageName());
                mContext.sendBroadcast(tileDownloaderIntent);
            }
        });
//        Flowable.create(new FlowableOnSubscribe<Tile>() {
//            @Override
//            public void subscribe(FlowableEmitter<Tile> emitter) throws Exception {
//                if (urlTileSourceList != null && !urlTileSourceList.isEmpty()) {
//                    b:
//                    for (int i = 0; i < urlTileSourceList.size(); i++) {
//                        UrlTileSource mUrlTileSource = urlTileSourceList.get(i);
//                        HttpEngine httpEngine = mUrlTileSource.getHttpEngine();
//                        for (int j = 0; j < tileList.size(); j++) {
//                            Tile tile = tileList.get(j);
//                            if (!emitter.isCancelled()) {
//                                while (emitter.requested() == 0) {
//                                    if (emitter.isCancelled()) {
//                                        System.out.println("这里break了urlTileSourceList:===" + i + "/" + urlTileSourceList.size() + "  tileList:===" + j + "/" + tileList.size());
//                                        break b;
//                                    }
//                                }
//                                download(tile, mUrlTileSource, httpEngine);
//                                emitter.onNext(tile);
//                            }
//                        }
//                    }
//                }
//                emitter.onComplete();
//            }
//        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Tile>() {
//            @Override
//            public void onSubscribe(Subscription d) {
//                subscription = d;
//                if (mapLayerList != null && !mapLayerList.isEmpty()) {
//                    for (Layer layer : mapLayerList) {
//
//                        if (layer instanceof BitmapTileLayer && ((BitmapTileLayer) layer).getTileSource() instanceof UrlTileSource) {
//                            urlTileSourceList.add((UrlTileSource) ((BitmapTileLayer) layer).getTileSource());
//                        }
//                        if (layer instanceof VectorTileLayer && ((VectorTileLayer) layer).getTileSource() instanceof UrlTileSource) {
//                            urlTileSourceList.add((UrlTileSource) ((VectorTileLayer) layer).getTileSource());
//                        }
//                    }
//                }
//                pb_download.setMax(tileList.size() * urlTileSourceList.size());
//                pb_download.setProgress(0);
//                bbtn_mini.setEnabled(true);
//                bbtn_ok.setEnabled(false);
//
//                d.request(1);
//                builder = createProgressNotificationBuilder(mContext, 0, pb_download.getMax());
//            }
//
//            @Override
//            public void onNext(Tile tile) {
//                pb_download.setProgress(pb_download.getProgress() < pb_download.getMax() ? pb_download.getProgress() + 1 : pb_download.getMax());
//                tv_download.setText(pb_download.getProgress() + "/" + pb_download.getMax());
//                System.out.println("进度:" + pb_download.getProgress() + "/" + pb_download.getMax());
//
//                if (builder!=null) {
//                    builder.setProgress(pb_download.getMax(), pb_download.getProgress(), false);
//                }
//                if (notificationManager!=null) {
//                    notificationManager.notify(MSG_DOWNLOAD_TILE_FINISH, builder.build());
//                }
//
//                subscription.request(1);
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onComplete() {
//                bbtn_ok.setEnabled(true);
//                System.out.println("下载结束:" + pb_download.getProgress() + "/" + pb_download.getMax());
//
//                if (builder!=null) {
//                    builder.setProgress(pb_download.getMax(), pb_download.getMax(), false);
//                    builder.setContentText("下载完成!");
//                    builder.setContentIntent(null);
//                    builder.setFullScreenIntent(null, false);
//                }
//                if (notificationManager!=null) {
//                    notificationManager.notify(MSG_DOWNLOAD_TILE_FINISH, builder.build());
//                }
//
//                // 结束下载后，择缓存地图按钮置为可用，直到下载完成后才可以进行下一次下载
//                Message msg = Message.obtain();
//                msg.what = 0x1017; // SystemConstant.MSG_WHAT_TILE_DOWNLAOD_ENABLE
//                msg.obj = true;
//                EventBus.getDefault().post(msg);
//
//            }
//        });

        View.OnClickListener dissmissClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if (builder!=null) {
                    builder.setProgress(pb_download.getMax(), pb_download.getMax(), false);
                    builder.setContentText("下载完成!");
                }
                if (notificationManager!=null) {
                    notificationManager.notify(SystemConstant.MSG_DOWNLOAD_TILE_FINISH, builder.build());
                    notificationManager.cancel(SystemConstant.MSG_DOWNLOAD_TILE_FINISH);
                }

                // 通知主界面，清除下载区域的绘制框
                Message msg = Message.obtain();
                msg.what = SystemConstant.MSG_DOWNLOAD_TILE_FINISH;
                EventBus.getDefault().post(msg);
            }
        };
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (receiver != null) {
                    mContext.unregisterReceiver(receiver);
                }
            }
        });
        bbtn_ok.setOnClickListener(dissmissClickListener);
        bbtn_mini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏对话框
                dialog.hide();
                // 通知主界面，隐藏下载区域的绘制框
                Message msg = Message.obtain();
                msg.what = SystemConstant.MSG_DOWNLOAD_TILE_HIDE;
                EventBus.getDefault().post(msg);
            }
        });
    }

    private NotificationCompat.Builder createProgressNotificationBuilder(Context mContext, int progress, int max) {
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = "cateye";
            CharSequence channelName = "cateye_map_cache";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setSmallIcon(R.mipmap.ic_launcher_foreground);
        builder.setContentTitle("缓存地图");
        builder.setContentText("正在下载");

        Intent tileDownloaderIntent = new Intent("catEye_tile_download");
        tileDownloaderIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        tileDownloaderIntent.setPackage(mContext.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, SystemConstant.MSG_DOWNLOAD_TILE_FINISH, tileDownloaderIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
//        builder.setFullScreenIntent(pendingIntent, false);

        builder.setProgress(max,progress,false);
        notificationManager.notify(SystemConstant.MSG_DOWNLOAD_TILE_FINISH, builder.build());
        return builder;
    }
}
