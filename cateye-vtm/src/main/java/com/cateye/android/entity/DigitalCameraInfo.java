package com.cateye.android.entity;

/**
 * Created by xiaoxiao on 2018/12/12.
 */

public class DigitalCameraInfo {
    private double x0, y0;                    //主点坐标为主点的像素坐标
    private double f;                        //focus length, mm
    private double pixelsize;               //像素（象元）大小，mm
    private long height;                  //height, in pixels
    private long width;                   //width , in pixels

    public double getX0() {
        return x0;
    }

    public void setX0(double x0) {
        this.x0 = x0;
    }

    public double getY0() {
        return y0;
    }

    public void setY0(double y0) {
        this.y0 = y0;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public double getPixelsize() {
        return pixelsize;
    }

    public void setPixelsize(double pixelsize) {
        this.pixelsize = pixelsize;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }
}
