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
    public static MarkerSymbol getDefaultMarkerSymbol(Context mContext) {
        if (defaultMarkerSymbol == null){
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

    public static Style getDefaultPolygonStyle(){
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

    public static MarkerSymbol getHighLightMarkerSymbol(Context mContext) {
        if (highlightMarkerSymbol == null){
            Bitmap bitmapPoi = drawableToBitmap(mContext.getResources().getDrawable(R.drawable.marker_focus));
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

    public static Style getHighLightPolygonStyle(){
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
