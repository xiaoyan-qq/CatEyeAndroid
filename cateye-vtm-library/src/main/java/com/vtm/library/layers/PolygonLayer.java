package com.vtm.library.layers;

import com.vividsolutions.jts.geom.Polygon;
import com.vtm.library.tools.GeometryTools;

import org.oscim.core.GeoPoint;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.vector.geometries.PolygonDrawable;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;

import java.util.List;

/**
 * Created by xiaoxiao on 2018/3/26.
 */

public class PolygonLayer extends PathLayer {
    private PolygonDrawable polygonDrawable;

    public PolygonLayer(Map map, Style style) {
        super(map, style);
        mStyle = style;
    }

    public PolygonLayer(Map map, int lineColor, float lineWidth) {
        this(map, Style.builder()
                .fixed(true)
                .strokeColor(lineColor)
                .strokeWidth(lineWidth)
                .build());
    }

    public PolygonLayer(Map map, int lineColor) {
        this(map, lineColor, 2);
    }

    /**
     * 设置polygon的点位
     */
    public void setPolygonString(List<GeoPoint> pointList, boolean isClose) {
        if (pointList == null || pointList.size() < 3) {
            return;
        }
        if (isClose && pointList != null && !GeometryTools.createGeometry(pointList.get(0)).equals(GeometryTools.createGeometry(pointList.get(pointList.size() - 1)))) {
            pointList.add(pointList.get(0));
        }
        synchronized (this) {
            if (mDrawable != null) {
                remove(mDrawable);
                mDrawable = null;
            }
            if (polygonDrawable != null) {
                remove(polygonDrawable);
            }
            polygonDrawable = new PolygonDrawable(pointList, mStyle);
            add(polygonDrawable);

//            mPoints.clear();
            if (pointList.get(0) == pointList.get(pointList.size() - 1)) {
                pointList.remove(pointList.size() - 1);
            }
//            for (int i = 0; i < pointList.size(); i++)
//                mPoints.addAll(pointList);
        }
        mWorker.submit(0);
    }

    /**
     * 移除正在绘制的polygon的图形
     */
    public void removeCurrentPolygonDrawable() {
        if (polygonDrawable != null) {
            remove(polygonDrawable);
            polygonDrawable = null;
        }
    }

    /**
     * 移除正在绘制的polyline的图形
     */
    public void removeCurrentPolylineDrawable() {
        if (mDrawable != null) {
            remove(mDrawable);
            mDrawable = null;
        }
    }

    /**
     * @param :
     * @return :
     * @method : getPolygon
     * @Author : xiaoxiao
     * @Describe : 获取当前polygon的wkt
     * @Date : 2018/12/18
     */
    public Polygon getPolygon() {
        if (mPoints == null || mPoints.size() < 3) {
            return null;
        }
        if (mPoints.get(0).distance(mPoints.get(mPoints.size() - 1)) > 0) {
            mPoints.add(mPoints.get(0));
        }
        return GeometryTools.createPolygon(mPoints);
    }
}
