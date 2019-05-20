package com.cateye.vtm.util;

import android.content.Context;

import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.vtm.library.tools.OverlayerManager;

import org.oscim.backend.canvas.Bitmap;
import org.oscim.backend.canvas.Color;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.map.Map;

import static org.oscim.android.canvas.AndroidGraphics.drawableToBitmap;

/**
 * Created by xiaoxiao on 2018/12/27.
 * 维护map上的不同layer的工具类
 */

public class LayerUtils {
    /**
     * 获取用户绘制航摄区域的图层，也可作为从数据库加载polygon的图层
     *
     * @param mMap
     * @return 被加载的图层
     */
    public static AirPlanMultiPolygonLayer getAirPlanDrawLayer(Map mMap) {
        //如果当前地图不存在multiPolygon的图层，则自动生成添加到地图上
        AirPlanMultiPolygonLayer multiPolygonLayer = (AirPlanMultiPolygonLayer) OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.AIR_PLAN_MULTI_POLYGON_DRAW);
        if (multiPolygonLayer == null) {
            //向主界面添加polygon显示的overlayer
            int c = Color.GREEN;
            org.oscim.layers.vector.geometries.Style polygonStyle = org.oscim.layers.vector.geometries.Style.builder()
                    .stippleColor(c)
                    .stipple(24)
                    .stippleWidth(1)
                    .strokeWidth(1)
                    .strokeColor(c).fillColor(c).fillAlpha(0.35f)
                    .fixed(true)
                    .randomOffset(false)
                    .build();
            multiPolygonLayer = new AirPlanMultiPolygonLayer(mMap, polygonStyle, SystemConstant.AIR_PLAN_MULTI_POLYGON_DRAW);
            mMap.layers().add(multiPolygonLayer, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);
        }
        return multiPolygonLayer;
    }

    public static AirPlanMultiPolygonLayer getAirPlanParamLayer(Map mMap) {
        AirPlanMultiPolygonLayer multiPolygonLayer = (AirPlanMultiPolygonLayer) OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.AIR_PLAN_MULTI_POLYGON_PARAM);
        if (multiPolygonLayer == null) {
            //开始编辑参数，增加编辑参数layer，和用户点击layer
            int c = Color.YELLOW;
            org.oscim.layers.vector.geometries.Style polygonStyle = org.oscim.layers.vector.geometries.Style.builder()
                    .stippleColor(c)
                    .stipple(24)
                    .stippleWidth(1)
                    .strokeWidth(1)
                    .strokeColor(Color.BLACK).fillColor(c).fillAlpha(0.35f)
                    .fixed(true)
                    .randomOffset(false)
                    .build();
            multiPolygonLayer = new AirPlanMultiPolygonLayer(mMap, polygonStyle, SystemConstant.AIR_PLAN_MULTI_POLYGON_PARAM);
            mMap.layers().add(multiPolygonLayer, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);
        }
        return multiPolygonLayer;
    }

    /**
     * 获取用户绘制航摄区域的图层，也可作为从数据库加载polygon的图层
     *
     * @param mMap
     * @return 被加载的图层
     */
    public static TrailRecordMultiPathLayer getTrailRecordLayer(Map mMap) {
        //如果当前地图不存在multiPolygon的图层，则自动生成添加到地图上
        TrailRecordMultiPathLayer multiPathLayer = (TrailRecordMultiPathLayer) OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.TRAIL_LOCATION_RECORD);
        if (multiPathLayer == null) {
            //向主界面添加polygon显示的overlayer
            int c = Color.GREEN;
            org.oscim.layers.vector.geometries.Style polygonStyle = org.oscim.layers.vector.geometries.Style.builder()
                    .stippleColor(c)
                    .stipple(24)
                    .stippleWidth(1)
                    .strokeWidth(1)
                    .strokeColor(c).fillColor(c).fillAlpha(0.35f)
                    .fixed(true)
                    .randomOffset(false)
                    .build();
            multiPathLayer = new TrailRecordMultiPathLayer(mMap, polygonStyle, SystemConstant.TRAIL_LOCATION_RECORD);
            mMap.layers().add(multiPathLayer, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);
        }
        return multiPathLayer;
    }
    public static ItemizedLayer getAirPlanMarkerLayer(Context mContext,Map mMap) {
        ItemizedLayer markerLayer= (ItemizedLayer) OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.AIR_PLAN_MARKER_AIR_PORT);
        if (markerLayer == null) {
            //添加绘制marker的图层，用来绘制无人机起飞的位置
            Bitmap bitmapPoi = drawableToBitmap(mContext.getResources().getDrawable(R.drawable.marker_poi));
            MarkerSymbol defaultMarkerSymbol = new MarkerSymbol(bitmapPoi, MarkerSymbol.HotspotPlace.CENTER);
            markerLayer = new ItemizedLayer<MarkerItem>(mMap, defaultMarkerSymbol);
            markerLayer.setName(SystemConstant.AIR_PLAN_MARKER_AIR_PORT);
            mMap.layers().add(markerLayer, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);
        }
        return markerLayer;
    }
}
