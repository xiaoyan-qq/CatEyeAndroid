package com.cateye.vtm.util;

import android.content.Context;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.lzy.okrx2.adapter.ObservableResponse;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.tools.Util;
import com.vtm.library.utils.SystemConstant;

import io.reactivex.Observable;

public class TecentMapUtil {
    private static TecentMapUtil instance;

    public static TecentMapUtil getInstance() {
        if (instance == null) {
            instance = new TecentMapUtil();
        }
        return instance;
    }

//    public Observable<String> getCurrentCity(Context context) {
//        String tecentMapKey = Util.getMetaKey(context, "TencentMapSDK");
//        return OkGo.<String>get(SystemConstant.TECENT_MAP_TRANSLATE)
//                .params("key", tecentMapKey)
//                .params("locations",new StringBuilder().append(currentLocation.getLatitude()).append(",").append(currentLocation.getLongitude()).toString())
//                .params("type", 1)
//                .tag("SystemConstant.TECENT_MAP_TRANSLATE")
//                .adapt(new ObservableResponse<String>());
//    }
//
    public Observable<Response<String>> translateLocation(Context context, TencentLocation currentLocation) {
        String tecentMapKey = Util.getMetaKey(context, "TencentMapSDK");
        // 坐标转换
        return OkGo.<String>get(SystemConstant.TECENT_MAP_TRANSLATE)
                .params("key", tecentMapKey)
                .params("locations",new StringBuilder().append(currentLocation.getLatitude()).append(",").append(currentLocation.getLongitude()).toString())
                .params("type", 1)
                .tag("SystemConstant.TECENT_MAP_TRANSLATE")
                .adapt(new ObservableResponse<String>());
    }
}
