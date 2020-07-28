package com.cateye.android.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.Serializable;
import java.util.List;

public class MapSourceFromNet implements Serializable {

    /**
     * success : true
     * errcode : 0
     * errmsg : success
     * data : [{"id":8,"serviceId":1,"title":"china_city_polygon","_abstract":"中国城市面","srs":"EPSG:900913","profile":"mercator","href":"http://39.107.104.63:8080/xyz/1.0.0/china_city_polygon@EPSG:900913@png","minX":73.1985473632812,"minY":3.58836889266968,"maxX":135.403656005859,"maxY":53.8118324279785,"originX":73.1985473632812,"originY":3.58836889266968,"minZoom":0,"maxZoom":11,"width":256,"height":256,"mimeType":"image/png","extension":"png","kind":2,"source":"http://${geoserver}/geoserver/gwc/service/tms/1.0.0/lbi:s_ods_city_simplify@EPSG:900913@png","fileExtension":"png","abstract":"中国城市面"},{"id":9,"serviceId":1,"title":"china_dem_tiff","_abstract":"中国DEM高程影像","srs":"EPSG:900913","profile":"mercator","href":"http://39.107.104.63:8080/xyz/1.0.0/china_dem_tiff@EPSG:900913@tif","minX":69.99958357546711,"minY":-6.1712267506E-4,"maxX":140.00002337569293,"maxY":55.00041712672995,"originX":69.99958357546711,"originY":-6.1712267506E-4,"minZoom":2,"maxZoom":10,"width":256,"height":256,"mimeType":"image/tif","extension":"tif","kind":2,"source":null,"fileExtension":"tif","abstract":"中国DEM高程影像"},{"id":16,"serviceId":1,"title":"gujiao_contour_line","_abstract":"古交等高线","srs":"EPSG:4326","profile":"geodetic","href":"http://39.107.104.63:8080/xyz/1.0.0/gujiao_contour_line@EPSG:4326@geojson","minX":111.6810742671935,"minY":37.60462333833393,"maxX":112.52042406708985,"maxY":38.267318646571766,"originX":111.6810742671935,"originY":37.60462333833393,"minZoom":8,"maxZoom":17,"width":256,"height":256,"mimeType":"application/json","extension":"geojson","kind":2,"source":null,"fileExtension":"json","abstract":"古交等高线"},{"id":10,"serviceId":1,"title":"gujiao_dem_tiff","_abstract":"古交DEM高程影像","srs":"EPSG:900913","profile":"mercator","href":"http://39.107.104.63:8080/xyz/1.0.0/gujiao_dem_tiff@EPSG:900913@tif","minX":111.68454591398637,"minY":37.5887014309443,"maxX":112.5209521838217,"maxY":38.26417683250406,"originX":111.68454591398637,"originY":37.5887014309443,"minZoom":8,"maxZoom":12,"width":256,"height":256,"mimeType":"image/tif","extension":"tif","kind":2,"source":null,"fileExtension":"tif","abstract":"古交DEM高程影像"},{"id":7,"serviceId":1,"title":"gujiao_satellite_raster","_abstract":"古交高分影像","srs":"EPSG:900913","profile":"mercator","href":"http://39.107.104.63:8080/xyz/1.0.0/gujiao_satellite_raster@EPSG:900913@png","minX":111.69073008803707,"minY":37.59651465252423,"maxX":112.50720463078976,"maxY":38.25637888992949,"originX":111.69073008803707,"originY":37.59651465252423,"minZoom":0,"maxZoom":17,"width":256,"height":256,"mimeType":"image/png","extension":"png","kind":2,"source":null,"fileExtension":"png","abstract":"古交高分影像"},{"id":6,"serviceId":1,"title":"world_satellite_raster","_abstract":"世界卫星底图","srs":"EPSG:900913","profile":"mercator","href":"http://39.107.104.63:8080/xyz/1.0.0/world_satellite_raster@EPSG:900913@jpeg","minX":-180,"minY":-80.98735315613175,"maxX":179.98190719029006,"maxY":81,"originX":-180,"originY":-80.98735315613175,"minZoom":0,"maxZoom":13,"width":256,"height":256,"mimeType":"image/jpeg","extension":"jpeg","kind":2,"source":null,"fileExtension":"jpg","abstract":"世界卫星底图"},{"id":3,"serviceId":2,"title":"china_city_polygon","_abstract":"中国城市面","srs":"EPSG:900913","profile":"mercator","href":"http://39.107.104.63:8080/tms/1.0.0/china_city_polygon@EPSG:900913@png","minX":73.1985473632812,"minY":3.58836889266968,"maxX":135.403656005859,"maxY":53.8118324279785,"originX":73.1985473632812,"originY":3.58836889266968,"minZoom":0,"maxZoom":11,"width":256,"height":256,"mimeType":"image/png","extension":"png","kind":2,"source":"http://${geoserver}/geoserver/gwc/service/tms/1.0.0/lbi:s_ods_city_simplify@EPSG:900913@png","fileExtension":"png","abstract":"中国城市面"},{"id":4,"serviceId":2,"title":"china_dem_tiff","_abstract":"中国DEM高程影像","srs":"EPSG:900913","profile":"mercator","href":"http://39.107.104.63:8080/tms/1.0.0/china_dem_tiff@EPSG:900913@tif","minX":69.99958357546711,"minY":-6.1712267506E-4,"maxX":140.00002337569293,"maxY":55.00041712672995,"originX":69.99958357546711,"originY":-6.1712267506E-4,"minZoom":2,"maxZoom":10,"width":256,"height":256,"mimeType":"image/tif","extension":"tif","kind":2,"source":null,"fileExtension":"tif","abstract":"中国DEM高程影像"},{"id":5,"serviceId":2,"title":"gujiao_dem_tiff","_abstract":"古交DEM高程影像","srs":"EPSG:900913","profile":"mercator","href":"http://39.107.104.63:8080/tms/1.0.0/gujiao_dem_tiff@EPSG:900913@tif","minX":111.68454591398637,"minY":37.5887014309443,"maxX":112.5209521838217,"maxY":38.26417683250406,"originX":111.68454591398637,"originY":37.5887014309443,"minZoom":8,"maxZoom":12,"width":256,"height":256,"mimeType":"image/tif","extension":"tif","kind":2,"source":null,"fileExtension":"tif","abstract":"古交DEM高程影像"},{"id":2,"serviceId":2,"title":"gujiao_satellite_raster","_abstract":"古交高分影像","srs":"EPSG:900913","profile":"mercator","href":"http://39.107.104.63:8080/tms/1.0.0/gujiao_satellite_raster@EPSG:900913@png","minX":111.69073008803707,"minY":37.59651465252423,"maxX":112.50720463078976,"maxY":38.25637888992949,"originX":111.69073008803707,"originY":37.59651465252423,"minZoom":0,"maxZoom":17,"width":256,"height":256,"mimeType":"image/png","extension":"png","kind":2,"source":null,"fileExtension":"png","abstract":"古交高分影像"},{"id":1,"serviceId":2,"title":"world_satellite_raster","_abstract":"世界卫星底图","srs":"EPSG:900913","profile":"mercator","href":"http://39.107.104.63:8080/tms/1.0.0/world_satellite_raster@EPSG:900913@jpeg","minX":-180,"minY":-80.98735315613175,"maxX":179.98190719029006,"maxY":81,"originX":-180,"originY":-80.98735315613175,"minZoom":0,"maxZoom":13,"width":256,"height":256,"mimeType":"image/jpeg","extension":"jpeg","kind":2,"source":null,"fileExtension":"jpg","abstract":"世界卫星底图"}]
     */

    private boolean success;
    private int errcode;
    private String errmsg;
    private List<DataBean> data;

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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    @JSONType(serialzeFeatures = SerializerFeature.BeanToArray, parseFeatures = Feature.SupportArrayToBean)
    public static class DataBean implements Serializable {
        /**
         * group : L2
         * id : 3
         * kind : 1
         * maps : [{"abstract":"中国城市面","extension":"png","fileExtension":"png","group":"L2","height":256,"href":"http://39.107.104.63:8080/xyz/1.0.0/china_city_polygon@EPSG:900913@png","id":8,"maxX":135.403656005859,"maxY":53.8118324279785,"maxZoom":11,"mimeType":"image/png","minX":73.1985473632812,"minY":3.58836889266968,"minZoom":0,"originX":73.1985473632812,"originY":3.58836889266968,"profile":"mercator","serviceId":1,"srs":"EPSG:900913","title":"china_city_polygon","width":256}]
         * memo : 中国城市面
         * name : china_city_polygon
         * type : 0
         */

        private String group;
        private int id;
        private int kind;
        private String memo;
        private String name;
        private int type;
        private List<MapsBean> maps;
        private boolean isShow = false;//是否显示，只在本地使用

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getKind() {
            return kind;
        }

        public void setKind(int kind) {
            this.kind = kind;
        }

        public String getMemo() {
            return memo;
        }

        public void setMemo(String memo) {
            this.memo = memo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public List<MapsBean> getMaps() {
            return maps;
        }

        public void setMaps(List<MapsBean> maps) {
            this.maps = maps;
        }

        public boolean isShow() {
            return isShow;
        }

        public void setShow(boolean show) {
            isShow = show;
        }

        public static class MapsBean implements Serializable {
            /**
             * abstract : 中国城市面
             * extension : png
             * fileExtension : png
             * group : L2
             * height : 256
             * href : http://39.107.104.63:8080/xyz/1.0.0/china_city_polygon@EPSG:900913@png
             * id : 8
             * maxX : 135.403656005859
             * maxY : 53.8118324279785
             * maxZoom : 11
             * mimeType : image/png
             * minX : 73.1985473632812
             * minY : 3.58836889266968
             * minZoom : 0
             * originX : 73.1985473632812
             * originY : 3.58836889266968
             * profile : mercator
             * serviceId : 1
             * srs : EPSG:900913
             * title : china_city_polygon
             * width : 256
             */

            @JSONField(name = "abstract")
            private String abstractX;
            private String extension;
            private String fileExtension;
            private String group;
            private int height;
            private String href;
            private int id;
            private double maxX;
            private double maxY;
            private int maxZoom;
            private String mimeType;
            private double minX;
            private double minY;
            private int minZoom;
            private double originX;
            private double originY;
            private String profile;
            private int serviceId;
            private String srs;
            private String title;
            private int width;
            private String recordDate;

            @JSONField(name = "abstract")
            public String getAbstractX() {
                return abstractX;
            }

            @JSONField(name = "abstract")
            public void setAbstractX(String abstractX) {
                this.abstractX = abstractX;
            }

            public String getExtension() {
                return extension;
            }

            public void setExtension(String extension) {
                this.extension = extension;
            }

            public String getFileExtension() {
                return fileExtension;
            }

            public void setFileExtension(String fileExtension) {
                this.fileExtension = fileExtension;
            }

            public String getGroup() {
                return group;
            }

            public void setGroup(String group) {
                this.group = group;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            public String getHref() {
                return href;
            }

            public void setHref(String href) {
                this.href = href;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public double getMaxX() {
                return maxX;
            }

            public void setMaxX(double maxX) {
                this.maxX = maxX;
            }

            public double getMaxY() {
                return maxY;
            }

            public void setMaxY(double maxY) {
                this.maxY = maxY;
            }

            public int getMaxZoom() {
                return maxZoom;
            }

            public void setMaxZoom(int maxZoom) {
                this.maxZoom = maxZoom;
            }

            public String getMimeType() {
                return mimeType;
            }

            public void setMimeType(String mimeType) {
                this.mimeType = mimeType;
            }

            public double getMinX() {
                return minX;
            }

            public void setMinX(double minX) {
                this.minX = minX;
            }

            public double getMinY() {
                return minY;
            }

            public void setMinY(double minY) {
                this.minY = minY;
            }

            public int getMinZoom() {
                return minZoom;
            }

            public void setMinZoom(int minZoom) {
                this.minZoom = minZoom;
            }

            public double getOriginX() {
                return originX;
            }

            public void setOriginX(double originX) {
                this.originX = originX;
            }

            public double getOriginY() {
                return originY;
            }

            public void setOriginY(double originY) {
                this.originY = originY;
            }

            public String getProfile() {
                return profile;
            }

            public void setProfile(String profile) {
                this.profile = profile;
            }

            public int getServiceId() {
                return serviceId;
            }

            public void setServiceId(int serviceId) {
                this.serviceId = serviceId;
            }

            public String getSrs() {
                return srs;
            }

            public void setSrs(String srs) {
                this.srs = srs;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public String getRecordDate() {
                return recordDate;
            }

            public void setRecordDate(String recordDate) {
                this.recordDate = recordDate;
            }
        }
        /*{
			"group":"L2",
			"id":3,
			"kind":1,
			"maps":[
				{
					"abstract":"中国城市面",
					"extension":"png",
					"fileExtension":"png",
					"group":"L2",
					"height":256,
					"href":"http://39.107.104.63:8080/xyz/1.0.0/china_city_polygon@EPSG:900913@png",
					"id":8,
					"maxX":135.403656005859,
					"maxY":53.8118324279785,
					"maxZoom":11,
					"mimeType":"image/png",
					"minX":73.1985473632812,
					"minY":3.58836889266968,
					"minZoom":0,
					"originX":73.1985473632812,
					"originY":3.58836889266968,
					"profile":"mercator",
					"serviceId":1,
					"srs":"EPSG:900913",
					"title":"china_city_polygon",
					"width":256
				}
			],
			"memo":"中国城市面",
			"name":"china_city_polygon",
			"type":0
		}*/

    }
}
