package com.cateye.vtm.util;

import android.content.Context;

import com.cateye.android.vtm.R;

import org.oscim.backend.canvas.Bitmap;
import org.oscim.backend.canvas.Color;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.vector.geometries.Style;

import static org.oscim.android.canvas.AndroidGraphics.drawableToBitmap;

public class LayerStyle {
    private static MarkerSymbol defaultMarkerSymbol;
    private static Style defaultLineStyle;
    private static Style defaultPolygonStyle;
    private static MarkerSymbol highlightMarkerSymbol;
    private static Style highlightLineStyle;
    private static Style highlightPolygonStyle;
    private static MarkerSymbol geoJsonMarkerSymbol;
    private static MarkerSymbol defaultPoiMarkerSymbol;
    private static Style geoJsonLineStyle;
    private static Style geoJsonPolygonStyle;

    public static MarkerSymbol getDefaultMarkerSymbol(Context mContext) {
        if (defaultMarkerSymbol == null) {
            Bitmap bitmapPoi = drawableToBitmap(mContext.getResources().getDrawable(R.drawable.marker_poi));
            defaultMarkerSymbol = new MarkerSymbol(bitmapPoi, MarkerSymbol.HotspotPlace.CENTER);
        }
        return defaultMarkerSymbol;
    }

    public static Style getDefaultLineStyle() {
        if (defaultLineStyle == null) {
            defaultLineStyle = Style.builder()
                    .stippleColor(Color.RED)
                    .stipple(24)
                    .stippleWidth(1)
                    .strokeWidth(2)
                    .strokeColor(Color.RED)
                    .fixed(true)
                    .randomOffset(false)
                    .build();
        }
        return defaultLineStyle;
    }

    public static Style getDefaultPolygonStyle() {
        if (defaultPolygonStyle == null) {
            defaultPolygonStyle = Style.builder()
                    .stippleColor(Color.RED)
                    .stipple(24)
                    .stippleWidth(1)
                    .strokeWidth(2)
                    .strokeColor(Color.RED).fillColor(Color.RED).fillAlpha(0.5f)
                    .fixed(true)
                    .randomOffset(false)
                    .build();
        }
        return defaultPolygonStyle;
    }

    public static MarkerSymbol getGeoJsonMarkerSymbol(Context mContext) {
        if (geoJsonMarkerSymbol == null) {
            Bitmap bitmapPoi = drawableToBitmap(mContext.getDrawable(R.drawable.geojson_point));
            geoJsonMarkerSymbol = new MarkerSymbol(bitmapPoi, MarkerSymbol.HotspotPlace.CENTER);
        }
        return geoJsonMarkerSymbol;
    }

    public static MarkerSymbol getDefaultPoiMarkerSymbol(Context mContext) {
        if (defaultPoiMarkerSymbol == null) {
            Bitmap bitmapPoi = drawableToBitmap(mContext.getDrawable(R.drawable.icon_poi_marker));
            defaultPoiMarkerSymbol = new MarkerSymbol(bitmapPoi, MarkerSymbol.HotspotPlace.BOTTOM_CENTER);
        }
        return defaultPoiMarkerSymbol;
    }

    public static Style getGeoJsonLineStyle() {
        if (geoJsonLineStyle == null) {
            geoJsonLineStyle = Style.builder()
                    .stippleColor(Color.parseColor("#A020F0"))
                    .stipple(24)
                    .stippleWidth(1)
                    .strokeWidth(2)
                    .strokeColor(Color.parseColor("#A020F0"))
                    .fixed(true)
                    .randomOffset(false)
                    .build();
        }
        return geoJsonLineStyle;
    }

    public static Style getGeoJsonPolygonStyle() {
        if (geoJsonPolygonStyle == null) {
            geoJsonPolygonStyle = Style.builder()
                    .stippleColor(Color.parseColor("#A020F0"))
                    .stipple(24)
                    .stippleWidth(1)
                    .strokeWidth(2)
                    .strokeColor(Color.parseColor("#A020F0")).fillColor(Color.parseColor("#A020F0")).fillAlpha(0.5f)
                    .fixed(true)
                    .randomOffset(false)
                    .build();
        }
        return geoJsonPolygonStyle;
    }

    public static MarkerSymbol getHighLightMarkerSymbol(Context mContext) {
        if (highlightMarkerSymbol == null) {
            Bitmap bitmapPoi = drawableToBitmap(mContext.getDrawable(R.drawable.marker_focus));
            highlightMarkerSymbol = new MarkerSymbol(bitmapPoi, MarkerSymbol.HotspotPlace.CENTER);
        }
        return highlightMarkerSymbol;
    }

    public static Style getHighLightLineStyle() {
        if (highlightLineStyle == null) {
            highlightLineStyle = Style.builder()
                    .stippleColor(Color.YELLOW)
                    .stipple(24)
                    .stippleWidth(1)
                    .strokeWidth(2)
                    .strokeColor(Color.YELLOW)
                    .fixed(true)
                    .randomOffset(false)
                    .build();
        }
        return highlightLineStyle;
    }

    public static Style getHighLightPolygonStyle() {
        if (highlightPolygonStyle == null) {
            highlightPolygonStyle = Style.builder()
                    .stippleColor(Color.YELLOW)
                    .stipple(24)
                    .stippleWidth(1)
                    .strokeWidth(2)
                    .strokeColor(Color.YELLOW).fillColor(Color.YELLOW).fillAlpha(0.5f)
                    .fixed(true)
                    .randomOffset(false)
                    .build();
        }
        return highlightPolygonStyle;
    }
}
