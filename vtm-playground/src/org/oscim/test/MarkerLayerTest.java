/*
 * Copyright 2016-2020 devemux86
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.oscim.test;

import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.gdx.GdxMapApp;
import org.oscim.layers.Layer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerInterface;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.bitmap.BitmapTileLayer;
import org.oscim.map.Map;
import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.UrlTileSource;
import org.oscim.tiling.source.bitmap.DefaultSources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.oscim.layers.marker.MarkerSymbol.HotspotPlace;

public class MarkerLayerTest extends GdxMapApp implements ItemizedLayer.OnItemGestureListener<MarkerInterface> {

    static final boolean BILLBOARDS = true;
    MarkerSymbol mFocusMarker;
    ItemizedLayer mMarkerLayer;

    @Override
    public void createLayers() {
        try {
            // Map events receiver
            mMap.layers().add(new MapEventsReceiver(mMap));

            UrlTileSource tileSource = DefaultSources.OPENSTREETMAP
                    .httpFactory(new OkHttpEngine.OkHttpFactory())
                    .build();
            tileSource.setHttpRequestHeaders(Collections.singletonMap("User-Agent", "vtm-playground"));
            mMap.layers().add(new BitmapTileLayer(mMap, tileSource));

            mMap.setMapPosition(0, 0, 1 << 2);

            Bitmap bitmapPoi = CanvasAdapter.decodeBitmap(getClass().getResourceAsStream("/res/marker_poi.png"));
            MarkerSymbol symbol;
            if (BILLBOARDS)
                symbol = new MarkerSymbol(bitmapPoi, HotspotPlace.BOTTOM_CENTER);
            else
                symbol = new MarkerSymbol(bitmapPoi, HotspotPlace.CENTER, false);

            Bitmap bitmapFocus = CanvasAdapter.decodeBitmap(getClass().getResourceAsStream("/res/marker_focus.png"));
            if (BILLBOARDS)
                mFocusMarker = new MarkerSymbol(bitmapFocus, HotspotPlace.BOTTOM_CENTER);
            else
                mFocusMarker = new MarkerSymbol(bitmapFocus, HotspotPlace.CENTER, false);

            mMarkerLayer = new ItemizedLayer(mMap, new ArrayList<MarkerInterface>(), symbol, this);
            mMap.layers().add(mMarkerLayer);

            List<MarkerInterface> pts = new ArrayList<>();
            for (double lat = -90; lat <= 90; lat += 5) {
                for (double lon = -180; lon <= 180; lon += 5)
                    pts.add(new MarkerItem(lat + "/" + lon, "", new GeoPoint(lat, lon)));
            }
            mMarkerLayer.addItems(pts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onItemSingleTapUp(int index, MarkerInterface item) {
        MarkerItem markerItem = (MarkerItem) item;
        if (markerItem.getMarker() == null)
            markerItem.setMarker(mFocusMarker);
        else
            markerItem.setMarker(null);

        System.out.println("Marker tap " + markerItem.getTitle());
        return true;
    }

    @Override
    public boolean onItemLongPress(int index, MarkerInterface item) {
        MarkerItem markerItem = (MarkerItem) item;
        if (markerItem.getMarker() == null)
            markerItem.setMarker(mFocusMarker);
        else
            markerItem.setMarker(null);

        System.out.println("Marker long press " + markerItem.getTitle());
        return true;
    }

    public static void main(String[] args) {
        GdxMapApp.init();
        GdxMapApp.run(new MarkerLayerTest());
    }

    class MapEventsReceiver extends Layer implements GestureListener {

        MapEventsReceiver(Map map) {
            super(map);
        }

        @Override
        public boolean onGesture(Gesture g, MotionEvent e) {
            if (g instanceof Gesture.Tap) {
                GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
                System.out.println("Map tap " + p);
                return true;
            }
            if (g instanceof Gesture.LongPress) {
                GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
                System.out.println("Map long press " + p);
                return true;
            }
            if (g instanceof Gesture.TripleTap) {
                GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
                System.out.println("Map triple tap " + p);
                return true;
            }
            return false;
        }
    }
}
