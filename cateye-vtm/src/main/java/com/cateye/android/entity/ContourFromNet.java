package com.cateye.android.entity;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;

public class ContourFromNet {

    /**
     * {"success":true,"errcode":0,"errmsg":"success","data":[{"longitude":112.0906,"latitude":37.9434,"height":1040,"kind":1},{"longitude":112.0907211303711,"latitude":37.943763732910156,"height":1036,"kind":2},{"longitude":112.0907211303711,"latitude":37.94477462768555,"height":1050,"kind":2},{"longitude":112.0907211303711,"latitude":37.945281982421875,"height":1048,"kind":2},{"longitude":112.0907211303711,"latitude":37.946292877197266,"height":1081,"kind":2},{"longitude":112.0907211303711,"latitude":37.94679641723633,"height":1078,"kind":2},{"longitude":112.09098052978516,"latitude":37.947052001953125,"height":1085,"kind":2},{"longitude":112.09098052978516,"latitude":37.947303771972656,"height":1085,"kind":2},{"longitude":112.09098052978516,"latitude":37.947811126708984,"height":1088,"kind":2},{"longitude":112.09098052978516,"latitude":37.948062896728516,"height":1083,"kind":2},{"longitude":112.09098052978516,"latitude":37.9493293762207,"height":1136,"kind":2},{"longitude":112.09098052978516,"latitude":37.949832916259766,"height":1136,"kind":2},{"longitude":112.09098052978516,"latitude":37.95084762573242,"height":1083,"kind":2},{"longitude":112.09098052978516,"latitude":37.95109939575195,"height":1084,"kind":2},{"longitude":112.09098052978516,"latitude":37.95185852050781,"height":1066,"kind":2},{"longitude":112.09098052978516,"latitude":37.952110290527344,"height":1072,"kind":2},{"longitude":112.09098052978516,"latitude":37.952362060546875,"height":1072,"kind":2},{"longitude":112.09123229980469,"latitude":37.95514678955078,"height":1149,"kind":2},{"longitude":112.09123229980469,"latitude":37.95793151855469,"height":1058,"kind":2},{"longitude":112.09123229980469,"latitude":37.95944595336914,"height":1102,"kind":2},{"longitude":112.09123229980469,"latitude":37.95970153808594,"height":1102,"kind":2},{"longitude":112.09148406982422,"latitude":37.961219787597656,"height":1056,"kind":2},{"longitude":112.09148406982422,"latitude":37.96324157714844,"height":1119,"kind":2},{"longitude":112.0916,"latitude":37.9634,"height":1110,"kind":1}]}
     */

    private boolean success;
    private int errcode;
    private String errmsg;
    private List<Contour> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public List<Contour> getData() {
        return data;
    }

    public void setData(List<Contour> data) {
        this.data = data;
    }

    @JSONType(serialzeFeatures = SerializerFeature.BeanToArray, parseFeatures = Feature.SupportArrayToBean)
    public class Contour {
        private double longitude;
        private double latitude;
        private int height;
        private int kind;

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getKind() {
            return kind;
        }

        public void setKind(int kind) {
            this.kind = kind;
        }
    }
}
