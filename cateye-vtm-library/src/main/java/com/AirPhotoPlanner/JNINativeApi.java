package com.AirPhotoPlanner;

/**
 * Created by xiaoxiao on 2019/1/3.
 */

public class JNINativeApi {
    /**
     * 调用底层C代码，自动生成飞机航摄的规划路径
     *
     * @param code 要生成的航区polygon的json
     * @param path 输出文件的位置
     * @return 结果
     */
    public static native String airPlannerOutput(String code, String path);
}
