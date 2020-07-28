package com.vtm.library.layers;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import org.oscim.jeo.JeoUtils;
import org.oscim.layers.OSMIndoorLayer;
import org.oscim.map.Map;
import org.oscim.renderer.bucket.LineBucket;
import org.oscim.renderer.bucket.MeshBucket;
import org.oscim.renderer.bucket.TextBucket;
import org.oscim.renderer.bucket.TextItem;
import org.oscim.theme.styles.AreaStyle;
import org.oscim.theme.styles.LineStyle;
import org.oscim.theme.styles.TextStyle;

import java.io.IOException;

import io.jeo.geom.Geom;
import io.jeo.map.CartoCSS;
import io.jeo.map.RGB;
import io.jeo.map.Rule;
import io.jeo.map.RuleList;
import io.jeo.map.Style;
import io.jeo.vector.Feature;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.VectorQuery;

public class GeoJsonLayer extends OSMIndoorLayer {

    private final VectorDataset mDataset;
    private final RuleList mRules;

    private double mMinX;
    private double mMinY;
    private static final boolean dbg = false;

    public GeoJsonLayer(Map map, VectorDataset data, Style style, TextStyle textStyle) {
        super(map, data, style, textStyle);
        this.mDataset = data;
        this.mRules = style.getRules().flatten();
        this.mText = textStyle;
    }

    @Override
    protected void processFeatures(Task t, Envelope b) {
        mTextLayer = new TextBucket();

        t.buckets.set(mTextLayer);

        if (mDropPointDistance > 0) {
            /* reduce lines points min distance */
            mMinX = ((b.getMaxX() - b.getMinX()) / mMap.getWidth());
            mMinY = ((b.getMaxY() - b.getMinY()) / mMap.getHeight());
            mMinX *= mDropPointDistance;
            mMinY *= mDropPointDistance;
        }

        try {
            VectorQuery q = new VectorQuery().bounds(b);
            if (dbg)
                log.debug("query {}", b);
            for (Feature f : mDataset.read(q)) {
                if (dbg)
                    log.debug("feature {}", f);

                RuleList rs = mRules.match(f);
                if (rs.isEmpty())
                    continue;

                Rule r = rs.collapse();
                if (r == null)
                    continue;

                Geometry g = f.geometry();
                if (g == null)
                    continue;

                switch (Geom.Type.from(g)) {
                    case POINT:
                        addPoint(t, f, r, g);
                        break;
                    case MULTIPOINT:
                        for (int i = 0, n = g.getNumGeometries(); i < n; i++)
                            addPoint(t, f, r, g.getGeometryN(i));
                        break;
                    case LINESTRING:
                        addLine(t, f, r, g);
                        break;
                    case MULTILINESTRING:
                        for (int i = 0, n = g.getNumGeometries(); i < n; i++)
                            addLine(t, f, r, g.getGeometryN(i));
                        break;
                    case POLYGON:
                        addPolygon(t, f, r, g);
                        break;
                    case MULTIPOLYGON:
                        for (int i = 0, n = g.getNumGeometries(); i < n; i++)
                            addPolygon(t, f, r, g.getGeometryN(i));
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            log.error("Error querying layer " + mDataset.name() + e);
        }
        //render TextItems to a bitmap and prepare vertex buffer data.
        mTextLayer.prepare();
    }

    protected void addLine(Task t, Feature f, Rule rule, Geometry g) {

        if (((LineString) g).isClosed()) {
            addPolygon(t, f, rule, g);
            return;
        }
        LineBucket ll = t.buckets.getLineBucket(0);
        if (ll.line == null) {
            RGB color = rule.color(f, CartoCSS.LINE_COLOR, RGB.black);
            float width = rule.number(f, CartoCSS.LINE_WIDTH, 1.2f);
            ll.line = new LineStyle(0, JeoUtils.color(color), width);
            ll.heightOffset = 0 * 4;
            ll.setDropDistance(0);
        }
        addLine(t, g, ll);

        Object o = f.get("name");
        if (o instanceof String) {
            float x = 0;
            float y = 0;
            int n = mGeom.index[0];
            if (n > 0) {
                for (int i = 0; i < n / 2; ) {
                    x = mGeom.points[i++];
                    y = mGeom.points[i++];
                }

                TextItem ti = TextItem.pool.get();
                ti.set(x, y, (String) o, mText);

                mTextLayer.addText(ti);
            }
        }
    }

    protected void addPolygon(Task t, Feature f, Rule rule, Geometry g) {
        int level = 0;

        LineBucket ll = t.buckets.getLineBucket(level * 3 + 1);

        boolean active = activeLevels[level + 1];

        if (ll.line == null) {
            float width = rule.number(f, CartoCSS.LINE_WIDTH, 1.2f);
            //int color = Color.rainbow((level + 1) / 10f);
            int color = JeoUtils.color(rule.color(f, CartoCSS.LINE_COLOR, RGB.black));

            if (/*level > -2 && */!active)
                color = getInactiveColor(color);

            ll.line = new LineStyle(0, color, width);
            ll.heightOffset = level * 4;
            ll.setDropDistance(0);
        }

        MeshBucket mesh = t.buckets.getMeshBucket(level * 3);
        if (mesh.area == null) {
            int color = JeoUtils.color(rule.color(f, CartoCSS.POLYGON_FILL, RGB.red));
            if (/*level > -2 && */!active)
                color = getInactiveColor(color);

            mesh.area = new AreaStyle(color);
            //mesh.area = new Area(Color.fade(Color.DKGRAY, 0.1f));
            mesh.heightOffset = level * 4f;
        }

        addPolygon(t, g, mesh, ll);

        if (active) {
            Object o = f.get("name");
            if (o instanceof String) {
                float x = 0;
                float y = 0;
                int n = mGeom.index[0];
                if (n > 0) {
                    for (int i = 0; i < n; ) {
                        x += mGeom.points[i++];
                        y += mGeom.points[i++];
                    }

                    TextItem ti = TextItem.pool.get();
                    ti.set(x / (n / 2), y / (n / 2), (String) o, mText);

                    mTextLayer.addText(ti);
                }
            }
        }
    }

    @Override
    protected void addPoint(Task t, Feature f, Rule rule, Geometry g) {

    }

}
