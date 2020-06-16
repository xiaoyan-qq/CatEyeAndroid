/*
 * Copyright 2014 Hannes Janetzek
 * Copyright 2016-2017 devemux86
 *
 * This file is part of the OpenScienceMap project (http://www.opensciencemap.org).
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

import com.vividsolutions.jts.geom.Geometry;
import io.jeo.carto.Carto;
import io.jeo.data.Dataset;
import io.jeo.data.mem.MemVectorDataset;
import io.jeo.data.mem.MemWorkspace;
import io.jeo.geojson.GeoJSONDataset;
import io.jeo.geojson.GeoJSONReader;
import io.jeo.geom.GeomBuilder;
import io.jeo.map.Style;
import io.jeo.vector.*;
import org.oscim.backend.canvas.Color;
import org.oscim.layers.OSMIndoorLayer;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.map.Map;
import org.oscim.renderer.MapRenderer;
import org.oscim.theme.styles.TextStyle;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import java.io.*;

public class JeoTest {

    public static void indoorSketch(Map map, String file) {
        MapRenderer.setBackgroundColor(0xff909090);
        VectorTileLayer baseLayer = map.setBaseMap(new OSciMap4TileSource());
        map.layers().add(new BuildingLayer(map, baseLayer));

        VectorDataset data = null;
        try {
            data = JeoTest.readGeoJson(new FileInputStream(new File(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Style style = JeoTest.getStyle();
        TextStyle textStyle = TextStyle.builder()
                .isCaption(true)
                .fontSize(16).color(Color.BLACK)
                .strokeWidth(2.2f).strokeColor(Color.WHITE)
                .build();
        map.layers().add(new OSMIndoorLayer(map, data, style, textStyle));
    }

    public static Style getStyle() {
        Style style = null;

        try {
            style = Carto.parse("" +
                    "#way {" +
                    "  line-width: 2;" +
                    "  line-color: #c80;" +
                    "  polygon-fill: #44111111;" +
                    "  " +
                    "}" +
                    "#states {" +
                    "  line-width: 2.2;" +
                    "  line-color: #c80;" +
                    "  polygon-fill: #44111111;" +
                    "  " +
                    "}"
            );

            return style;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static VectorDataset readGeoJson(InputStream is) {
        GeoJSONReader r = new GeoJSONReader();

        @SuppressWarnings("resource")
        MemWorkspace mem = new MemWorkspace("");

        //mem.put("layer", data);
        try {
            Schema s = new SchemaBuilder("way").schema();

            MemVectorDataset memData = mem.create(s);

            for (Feature f : r.features(is)) {
                //System.out.println("loaded: " + f);
                memData.add(f);
            }
            return memData;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Dataset getJsonData(String file, boolean memory) {
        GeoJSONDataset data = null;

        try {
            data = new GeoJSONDataset(new File(file));
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (memory) {
            @SuppressWarnings("resource")
            MemWorkspace mem = new MemWorkspace("");

            //mem.put("layer", data);
            try {

                Schema s = data.schema();
                VectorQuery q = new VectorQuery();

                MemVectorDataset memData = mem.create(s);

                for (Feature f : data.read(q)) {
                    memData.add(f);
                }

                //return mem.get("layer");
                return memData;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static Dataset getMemWorkspace(String layer) {
        GeomBuilder gb = new GeomBuilder(4326);

        @SuppressWarnings("resource")
        MemWorkspace mem = new MemWorkspace("");
        Schema schema = new SchemaBuilder(layer)
                .field("geometry", Geometry.class)
                .field("id", Integer.class)
                .field("name", String.class)
                .field("cost", Double.class).schema();

        MemVectorDataset data;
        try {
            data = mem.create(schema);
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Geometry g = gb.point(0, 0).toPoint();
        //g.setSRID(4326);


        data.add(new ListFeature(data.schema(),
                g, 1, "anvil",
                10.99));

        data.add(new ListFeature(data.schema(),
                gb.points(10, 10, 20, 20).toLineString(),
                2, "bomb", 11.99));

        data.add(new ListFeature(data.schema(),
                gb.point(100, 10).toPoint().buffer(10),
                3, "dynamite", 12.99));

        //Dataset jsonData = new GeoJSONDataset(new File("states.json"));
        //mem.put("states", jsonData);

        try {
            return mem.get(layer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
