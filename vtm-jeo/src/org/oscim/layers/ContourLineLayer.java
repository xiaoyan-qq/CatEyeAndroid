package org.oscim.layers;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.oscim.jeo.JeoUtils;
import org.oscim.map.Map;
import org.oscim.renderer.bucket.LineBucket;
import org.oscim.renderer.bucket.TextBucket;
import org.oscim.renderer.bucket.TextItem;
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

/**
 * Created by xiaoxiao on 2018/4/3.
 */

public class ContourLineLayer extends JtsLayer {
    protected TextBucket mTextLayer;
    protected TextStyle mText;

    private final VectorDataset mDataset;
    private final RuleList mRules;

    protected double mDropPointDistance = 0.01;
    private double mMinX;
    private double mMinY;

    public ContourLineLayer(Map map, VectorDataset data, Style style) {
        super(map);
        this.mDataset = data;
        this.mRules = style.getRules().flatten();
    }

    public ContourLineLayer(Map map, VectorDataset data, Style style, TextStyle textStyle) {
        super(map);
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
            for (Feature f : mDataset.read(q)) {

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
                    case LINESTRING:
                        addLine(t, f, r, g);
                        break;
                    default:
                        break;
                }
            }
            mTextLayer.prepare();
        } catch (IOException e) {
            log.error("Error querying layer " + mDataset.name() + e);
        }
    }

    protected void addLine(Task t, Feature f, Rule rule, Geometry g) {

        int lineId = getContourLineId(f);

        LineBucket ll = t.buckets.getLineBucket(0);
        if (ll.line == null) {
            RGB color = rule.color(f, CartoCSS.LINE_COLOR, RGB.black);
            float width = rule.number(f, CartoCSS.LINE_WIDTH, 1.2f);
            ll.line = new LineStyle(0, JeoUtils.color(color), width);
            ll.heightOffset = lineId * 4;
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

    private int getContourLineId(Feature f) {
        /* not sure if one could match these geojson properties with cartocss */
        Object o = f.get("ID");
        if (o != null) {
            return Integer.parseInt(o.toString());
        }
        return 0;
    }
}
