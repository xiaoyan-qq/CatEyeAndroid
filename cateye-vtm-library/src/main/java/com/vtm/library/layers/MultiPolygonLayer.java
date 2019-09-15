package com.vtm.library.layers;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vtm.library.tools.GeometryTools;

import org.oscim.core.GeoPoint;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.vector.geometries.PolygonDrawable;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/3/26.
 */

public class MultiPolygonLayer extends PathLayer {
    private List<PolygonDrawable> polygonDrawableList;

    public MultiPolygonLayer(Map map, Style style) {
        super(map, style);
        mStyle = style;
        polygonDrawableList = new ArrayList<>();
    }

    public MultiPolygonLayer(Map map, Style style, String name) {
        this(map, style);
        this.mName = name;
    }

    public MultiPolygonLayer(Map map, int lineColor, float lineWidth, int fillColor, float fillAlpha, String name) {
        this(map, Style.builder()
                .stippleColor(lineColor)
                .stipple(24)
                .stippleWidth(lineWidth)
                .strokeWidth(lineWidth)
                .strokeColor(lineColor).fillColor(fillColor).fillAlpha(fillAlpha)
                .fixed(true)
                .randomOffset(false)
                .build(), name);
    }

    public MultiPolygonLayer(Map map, int lineColor, int fillColor, float fillAlpha, String name) {
        this(map, lineColor, 0.5f, fillColor, fillAlpha, name);
    }

    /**
     * 设置polygon的点位
     */
    public void setPolygonList(List<List<GeoPoint>> pointListList, boolean isClose) {
        if (pointListList == null || pointListList.isEmpty()) {
            return;
        }
        for (List<GeoPoint> pointList : pointListList) {
            if (pointList == null || pointList.size() < 3) {
                return;
            }
            if (isClose && pointList != null && !GeometryTools.createGeometry(pointList.get(0)).equals(GeometryTools.createGeometry(pointList.get(pointList.size() - 1)))) {
                pointList.add(pointList.get(0));
            }
            synchronized (this) {
                PolygonDrawable polygonDrawable = new PolygonDrawable(pointList, mStyle);
                add(polygonDrawable);
                polygonDrawableList.add(polygonDrawable);
            }
            mWorker.submit(0);
            update();
        }
    }

    /**
     * 移除正在绘制的polygon的图形
     */
    public void removePolygonDrawable(int i) {
        if (polygonDrawableList != null && polygonDrawableList.size() > i) {
            remove(polygonDrawableList.get(i));
            update();
        }
    }

    public void removePolygonDrawable(List<GeoPoint> geoPointList) {
        Polygon polygon = GeometryTools.createPolygon(geoPointList);
        removePolygonDrawable(polygon);
    }

    public void removePolygonDrawable(String polygonStr) {
        Polygon polygon = (Polygon) GeometryTools.createGeometry(polygonStr);
        removePolygonDrawable(polygon);
    }

    public void removePolygonDrawable(Geometry polygon) {
        if (polygonDrawableList != null && !polygonDrawableList.isEmpty()) {
            Iterator iterator = polygonDrawableList.iterator();
            while (iterator.hasNext()) {
                PolygonDrawable polygonDrawable = (PolygonDrawable) iterator.next();
                if (GeometryTools.createGeometry(polygonDrawable.getGeometry().toString()).equals(polygon)) {
                    remove(polygonDrawable);
                    iterator.remove();
                    break;
                }
            }
            update();
        }
    }

    public void addPolygonDrawable(List<GeoPoint> pointList) {
        if (polygonDrawableList != null) {
            if (pointList == null || pointList.size() < 3) {
                return;
            }
            if (pointList != null && !GeometryTools.createGeometry(pointList.get(0)).equals(GeometryTools.createGeometry(pointList.get(pointList.size() - 1)))) {
                pointList.add(pointList.get(0));
            }
            synchronized (this) {
                PolygonDrawable polygonDrawable = new PolygonDrawable(pointList, mStyle);
                add(polygonDrawable);
                polygonDrawableList.add(polygonDrawable);
            }
            mWorker.submit(0);
        }
        update();
    }

    public void addPolygonDrawable(Polygon polygon) {
        List<GeoPoint> geoPointList = GeometryTools.getGeoPoints(polygon.toString());
        addPolygonDrawable(geoPointList);
    }

    public List<Polygon> getAllPolygonList() {
        if (polygonDrawableList != null && !polygonDrawableList.isEmpty()) {
            List<Polygon> polygonList = new ArrayList<>();
            for (PolygonDrawable polygonDrawable : polygonDrawableList) {
                polygonList.add((Polygon) GeometryTools.createGeometry(polygonDrawable.getGeometry().toString()));
            }
            return polygonList;
        }
        return null;
    }

    public List<List<GeoPoint>> getAllPolygonGeoPointList() {
        List<Polygon> polygonList = getAllPolygonList();
        if (polygonList != null) {
            List<List<GeoPoint>> geopointList = new ArrayList<>();
            for (Polygon polygon : polygonList) {
                geopointList.add(GeometryTools.getGeoPoints(polygon.toString()));
            }
            return geopointList;
        }
        return null;
    }

    public List<PolygonDrawable> getPolygonDrawableList() {
        return polygonDrawableList;
    }

    public void setPolygonDrawableList(List<PolygonDrawable> polygonDrawableList) {
        this.polygonDrawableList = polygonDrawableList;
    }

    public void removeAllPathDrawable(){
        if (polygonDrawableList != null && !polygonDrawableList.isEmpty()) {
            Iterator iterator = polygonDrawableList.iterator();
            while (iterator.hasNext()) {
                PolygonDrawable polygonDrawable = (PolygonDrawable) iterator.next();
                remove(polygonDrawable);
                iterator.remove();
            }
            mWorker.submit(0);
            update();
        }
    }
}
