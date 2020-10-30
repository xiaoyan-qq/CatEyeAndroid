package com.vtm.library.kml;

public class MultiGeometry {
    private Polygon Polygon;


    public MultiGeometry(Polygon polygon) {
        Polygon = polygon;

    }

    public Polygon getPolygon() {
        return Polygon;
    }

    public void setPolygon(Polygon Polygon) {
        this.Polygon = Polygon;
    }

    @Override
    public String toString() {
        return "ClassPojo [Polygon = " + Polygon + "]";
    }
}

