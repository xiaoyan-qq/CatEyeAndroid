package com.cateye.android.vtm;

import android.app.Application;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.cateye.vtm.util.SystemConstant;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.MemoryCookieStore;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.umeng.commonsdk.UMConfigure;
import com.vondear.rxtool.RxTool;

import org.xutils.x;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import me.yokeyword.fragmentation.Fragmentation;
import okhttp3.OkHttpClient;

/**
 * Created by xiaoxiao on 2018/3/21.
 */

public class MainApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);

        //加载航飞轨迹生成的so
        System.loadLibrary("geos");
        System.loadLibrary("proj");
        System.loadLibrary("AirPhotoPlanner");

        RxTool.init(this);

        Fragmentation.builder()
                // 显示悬浮球 ; 其他Mode:SHAKE: 摇一摇唤出   NONE：隐藏
                .stackViewMode(Fragmentation.NONE)
//                .debug(BuildConfig.DEBUG)
                .install();

        //Okhttp的配置
//        OkGo.getInstance().init(this);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
        //log颜色级别，决定了log在控制台显示的颜色
        loggingInterceptor.setColorLevel(Level.INFO);
        builder.addInterceptor(loggingInterceptor);

        //全局的读取超时时间
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的写入超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的连接超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //使用内存保持cookie，app退出后，cookie消失
        builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));

        OkGo.getInstance().init(this)                       //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置将使用默认的
                .setCacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3)                               //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
                /*.addCommonHeaders(headers)                      //全局公共头
                .addCommonParams(params)*/;                       //全局公共参数

        //友盟初始化
        UMConfigure.init(this, null, null, UMConfigure.DEVICE_TYPE_PHONE, null);

        //为Bootstrap注册默认图标集
        TypefaceProvider.registerDefaultIconSets();

        //自动创建应用目录
        File rootPathDir = new File(SystemConstant.APP_ROOT_DATA_PATH);
        if (!rootPathDir.exists()) {
            rootPathDir.mkdirs();
        }

        x.Ext.init(this);
    }
}
