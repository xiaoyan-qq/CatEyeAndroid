package com.cateye.vtm.util;

import com.cateye.android.entity.AirPlanDBEntity;
import com.cateye.android.entity.TravelLocation;
import com.cateye.android.entity.TravelRecord;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vtm.library.layers.MultiPathLayer;
import com.vtm.library.tools.GeometryTools;

import org.oscim.core.GeoPoint;
import org.oscim.layers.vector.geometries.LineDrawable;
import org.oscim.layers.vector.geometries.PolygonDrawable;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/3/26.
 */

public class TrailRecordMultiPathLayer extends MultiPathLayer {
    private java.util.Map<String, LineDrawable> trailLocationDBEntityMap;

    public TrailRecordMultiPathLayer(Map map, Style style) {
        super(map, style);
        this.mStyle = style;
        this.trailLocationDBEntityMap = new HashMap<>();
    }

    public TrailRecordMultiPathLayer(Map map, Style style, String name) {
        this(map, style);
        this.mName = name;
    }

    public TrailRecordMultiPathLayer(Map map, int lineColor, float lineWidth, int fillColor, float fillAlpha, String name) {
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

    public TrailRecordMultiPathLayer(Map map, int lineColor, int fillColor, float fillAlpha, String name) {
        this(map, lineColor, 0.5f, fillColor, fillAlpha, name);
    }

    public boolean addPathDrawable(String name, List<GeoPoint> pointList) {
        if (trailLocationDBEntityMap != null) {
            if (trailLocationDBEntityMap.containsKey(name)) {
                return false;
            }
            if (pointList == null || pointList.size() < 2) {
                return false;
            }
            synchronized (this) {
                LineDrawable pathDrawable = new LineDrawable(pointList, mStyle);
                add(pathDrawable);
                trailLocationDBEntityMap.put(name, pathDrawable);
            }
            mWorker.submit(0);
            update();
        }
        return true;
    }

    public void clearAllPath(){
        if (trailLocationDBEntityMap!=null){
            Iterator keyIterator=trailLocationDBEntityMap.keySet().iterator();
            while (keyIterator.hasNext()){
                String key= (String) keyIterator.next();
                LineDrawable lineDrawable=trailLocationDBEntityMap.get(key);
                if (lineDrawable!=null){
                    remove(lineDrawable);
                }
                trailLocationDBEntityMap.remove(key);
            }
        }
    }

    public void removeTrailRecordDrawable(String name) {
        if (trailLocationDBEntityMap != null) {
            if (trailLocationDBEntityMap.containsKey(name)) {
                LineDrawable pathDrawable = trailLocationDBEntityMap.get(name);
                remove(pathDrawable);
                trailLocationDBEntityMap.remove(name);
                mWorker.submit(0);
                update();
            }
        }
    }
}
