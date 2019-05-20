package com.vtm.library.layers;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vtm.library.tools.GeometryTools;

import org.oscim.core.GeoPoint;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.vector.geometries.LineDrawable;
import org.oscim.layers.vector.geometries.PolygonDrawable;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/3/26.
 */

public class MultiPathLayer extends PathLayer {
    private List<LineDrawable> pathDrawableList;

    public MultiPathLayer(Map map, Style style) {
        super(map, style);
        mStyle = style;
        pathDrawableList = new ArrayList<>();
    }

    public MultiPathLayer(Map map, Style style, String name) {
        this(map, style);
        this.mName = name;
    }

    public MultiPathLayer(Map map, int lineColor, float lineWidth, int fillColor, float fillAlpha, String name) {
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

    public MultiPathLayer(Map map, int lineColor, int fillColor, float fillAlpha, String name) {
        this(map, lineColor, 0.5f, fillColor, fillAlpha, name);
    }

    /**
     * 设置polygon的点位
     */
    public void setPathList(List<List<GeoPoint>> pointListList) {
        if (pointListList == null || pointListList.isEmpty()) {
            return;
        }
        for (List<GeoPoint> pointList : pointListList) {
            if (pointList == null || pointList.size() < 2) {
                return;
            }
            synchronized (this) {
                LineDrawable lineDrawable = new LineDrawable(pointList, mStyle);
                add(lineDrawable);
                pathDrawableList.add(lineDrawable);
            }
            mWorker.submit(0);
            update();
        }
    }

    /**
     * 移除正在绘制的polygon的图形
     */
    public void removePathDrawable(int i) {
        if (pathDrawableList != null && pathDrawableList.size() > i) {
            remove(pathDrawableList.get(i));
            update();
        }
    }

    public void removePathDrawable(List<GeoPoint> geoPointList) {
        LineString path = GeometryTools.getLineStrinGeo(geoPointList);
        removePolygonDrawable(path);
    }

    public void removePolygonDrawable(String pathStr) {
        LineString path = (LineString) GeometryTools.createGeometry(pathStr);
        removePolygonDrawable(path);
    }

    public void removePolygonDrawable(Geometry path) {
        if (pathDrawableList != null && !pathDrawableList.isEmpty()) {
            Iterator iterator = pathDrawableList.iterator();
            while (iterator.hasNext()) {
                LineDrawable lineDrawable = (LineDrawable) iterator.next();
                if (GeometryTools.createGeometry(lineDrawable.getGeometry().toString()).equals(path)) {
                    remove(lineDrawable);
                    iterator.remove();
                    break;
                }
            }
            update();
        }
    }

    public void addPathDrawable(List<GeoPoint> pointList) {
        if (pathDrawableList != null) {
            if (pointList == null || pointList.size() < 2) {
                return;
            }
            synchronized (this) {
                LineDrawable pathDrawable = new LineDrawable(pointList, mStyle);
                add(pathDrawable);
                pathDrawableList.add(pathDrawable);
            }
            mWorker.submit(0);
        }
        update();
    }

    public void addPathDrawable(LineString lineString) {
        List<GeoPoint> geoPointList = GeometryTools.getGeoPoints(lineString.toString());
        addPathDrawable(geoPointList);
    }

    public List<LineString> getAllPathList() {
        if (pathDrawableList != null && !pathDrawableList.isEmpty()) {
            List<LineString> pathList = new ArrayList<>();
            for (LineDrawable pathDrawable : pathDrawableList) {
                pathList.add((LineString) GeometryTools.createGeometry(pathDrawable.getGeometry().toString()));
            }
            return pathList;
        }
        return null;
    }

    public List<List<GeoPoint>> getAllPathGeoPointList() {
        List<LineString> pathList = getAllPathList();
        if (pathList != null) {
            List<List<GeoPoint>> geopointList = new ArrayList<>();
            for (LineString path : pathList) {
                geopointList.add(GeometryTools.getGeoPoints(path.toString()));
            }
            return geopointList;
        }
        return null;
    }

    public List<LineDrawable> getPolygonDrawableList() {
        return pathDrawableList;
    }

    public void setPolygonDrawableList(List<LineDrawable> pathDrawableList) {
        this.pathDrawableList = pathDrawableList;
    }

}
