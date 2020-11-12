package com.cateye.vtm.util;

import android.content.Context;

import com.cateye.android.entity.DrawPointLinePolygonEntity;
import com.cateye.android.vtm.MainActivity;
import com.vtm.library.kml.Serializer;
import com.vtm.library.kml.model.Boundary;
import com.vtm.library.kml.model.Coordinate;
import com.vtm.library.kml.model.Coordinates;
import com.vtm.library.kml.model.Data;
import com.vtm.library.kml.model.Document;
import com.vtm.library.kml.model.ExtendedData;
import com.vtm.library.kml.model.Folder;
import com.vtm.library.kml.model.Kml;
import com.vtm.library.kml.model.LineString;
import com.vtm.library.kml.model.LinearRing;
import com.vtm.library.kml.model.Placemark;
import com.vtm.library.kml.model.Point;
import com.vtm.library.kml.model.Polygon;
import com.vtm.library.layers.MultiPathLayer;
import com.vtm.library.layers.MultiPolygonLayer;
import com.vtm.library.tools.CatEyeMapManager;
import com.vtm.library.tools.GeometryTools;
import com.vtm.library.tools.OverlayerManager;

import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Driver;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.core.Tile;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.OsmTileLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.tiling.source.mapfile.MapInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//将空间数据写入shp文件
public class LocalGisFileUtil {
    private static LocalGisFileUtil instance;
    public static LocalGisFileUtil getInstance() {
        if (instance == null) {
            instance = new LocalGisFileUtil();
        }
        //这个可以看到支持哪些格式的驱动，不用的时候可以注释掉
//        int count =ogr.GetDriverCount();
//        for(int i=0;i<count;i++){
//            System.out.println(ogr.GetDriver(i).GetName());
//        }
        // 为了支持中文路径，请添加下面这句代码
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
        return instance;
    }

    public void writeShp(File shpFile, String shpType, List<DrawPointLinePolygonEntity> entityList) throws Exception {
        // 为了使属性表字段支持中文，请添加下面这句
        gdal.SetConfigOption("SHAPE_ENCODING", "CP936");
        //配置GDAL_DATA路径（gdal根目录下的bin\gdal-data）
        gdal.SetConfigOption("GDAL_DATA", shpFile.getParent());
        //创建数据，这里以创建ESRI的shp文件为例
        String strDriverName = "ESRI Shapefile";

        //创建一个文件，根据strDriverName扩展名自动判断是创建shp文件或其他文件
        Driver oDriver = ogr.GetDriverByName(strDriverName);
        if (oDriver == null) {
            System.out.println(strDriverName + " 驱动不可用！\n");
            throw new Exception("驱动不可用！");
        }

        // 创建数据源
        DataSource oDS = oDriver.CreateDataSource(shpFile.getAbsolutePath(), null);
        if (oDS == null) {
//            System.out.println("创建矢量文件【"+ shpFile.getAbsolutePath() +"】失败！\n" );
            throw new Exception("创建矢量文件【" + shpFile.getAbsolutePath() + "】失败！");
        }

        // 创建图层，创建一个多边形图层，这里没有指定空间参考，如果需要的话，需要在这里进行指定
        //如果是mif或者tab，其实是可以多元素混合存放的，shp每个图层只能存放点线面中的一种。
        //所以tab创建图层的时候，选择什么都不影响后面的操作和存储结果
        SpatialReference spatialReference = new SpatialReference();
        spatialReference.SetWellKnownGeogCS("WGS84");
        Layer oLayer = null;
        String shpName = shpFile.getName().substring(0, shpFile.getName().lastIndexOf("."));
        if (GeometryTools.POINT_GEOMETRY_TYPE.equals(shpType)) {
            oLayer = oDS.CreateLayer(shpName, spatialReference, ogr.wkbPoint, null);
        } else if (GeometryTools.LINE_GEOMETRY_TYPE.equals(shpType)) {
            oLayer = oDS.CreateLayer(shpName, spatialReference, ogr.wkbLineString, null);
        } else if (GeometryTools.POLYGON_GEOMETRY_TYPE.equals(shpType)) {
            oLayer = oDS.CreateLayer(shpName, spatialReference, ogr.wkbPolygon, null);
        }
        if (oLayer == null) {
            System.out.println("图层创建失败！\n");
            throw new Exception("图层创建失败！");
        }

        // 下面创建属性表
        // 先创建一个叫FieldID的整型属性
        FieldDefn oFieldID = new FieldDefn("id", ogr.OFTString);
        oLayer.CreateField(oFieldID, 1);

        // 再创建一个叫名称的字符型属性，字符长度为100
        FieldDefn oFieldName = new FieldDefn("name", ogr.OFTString);
        oFieldName.SetWidth(100);
        oLayer.CreateField(oFieldName, 1);

        FieldDefn oFieldRemark = new FieldDefn("remark", ogr.OFTString);
        oFieldRemark.SetWidth(1000);
        oLayer.CreateField(oFieldRemark, 1);

        FieldDefn oFieldUserName = new FieldDefn("userName", ogr.OFTString);
        oFieldUserName.SetWidth(100);
        oLayer.CreateField(oFieldUserName, 1);

        FieldDefn oFieldProjectId = new FieldDefn("projectId", ogr.OFTString);
        oFieldProjectId.SetWidth(100);
        oLayer.CreateField(oFieldProjectId, 1);

        FieldDefn oFieldImgUrlListStr = new FieldDefn("imgUrlListStr", ogr.OFTString);
        oFieldImgUrlListStr.SetWidth(100);
        oLayer.CreateField(oFieldImgUrlListStr, 1);

        FeatureDefn oDefn = oLayer.GetLayerDefn();

        for (DrawPointLinePolygonEntity entity : entityList) {
            Feature oFeatureTriangle = new Feature(oDefn);
            oFeatureTriangle.SetField("id", entity.get_id());
            oFeatureTriangle.SetField("name", entity.getName());
            oFeatureTriangle.SetField("remark", entity.getRemark());
            oFeatureTriangle.SetField("userName", entity.getUserName());
            oFeatureTriangle.SetField("projectId", entity.getProjectId());
            oFeatureTriangle.SetField("imgUrlListStr", entity.getImgUrlListStr());
            oFeatureTriangle.SetGeometry(Geometry.CreateFromWkt(entity.getGeometry()));
            oLayer.CreateFeature(oFeatureTriangle);
        }
        //写入文件
        oLayer.SyncToDisk();
        oDS.SyncToDisk();



        System.out.println("\n数据集创建完成！\n");
    }

    // https://www.jianshu.com/p/957721b42bd4
    public List<java.util.Map<String,String>> readShpFile(File shpFile) throws Exception{
        Map mMap = CatEyeMapManager.getInstance().getCatEyeMap();

        // 为了使属性表字段支持中文，请添加下面这句
        gdal.SetConfigOption("SHAPE_ENCODING", "CP936");
        //配置GDAL_DATA路径（gdal根目录下的bin\gdal-data）
        gdal.SetConfigOption("GDAL_DATA", shpFile.getParent());
        //创建数据，这里以创建ESRI的shp文件为例
        String strDriverName = "ESRI Shapefile";

        //创建一个文件，根据strDriverName扩展名自动判断是创建shp文件或其他文件
        Driver oDriver = ogr.GetDriverByName(strDriverName);
        if (oDriver == null) {
            System.out.println(strDriverName + " 驱动不可用！\n");
            throw new Exception("驱动不可用！");
        }

        DataSource dataSource=ogr.Open(shpFile.getAbsolutePath());
        if (dataSource == null) {
            System.out.println("打开文件失败！" );
            return null;
        }
        if (dataSource.GetLayerCount() > 0) {
            List<java.util.Map<String, String>> shpMapList = new ArrayList<>();
            for (int j = 0; j < dataSource.GetLayerCount(); j++) {
                Layer layer = dataSource.GetLayer(j);
                if (layer!=null) {
                    String layerName = layer.GetName();
                    System.out.println("图层名称："+layerName);

                    SpatialReference spatialReference = layer.GetSpatialRef();
                    //System.out.println(spatialReference);
                    System.out.println("空间参考坐标系："+spatialReference.GetAttrValue("AUTHORITY",0)+spatialReference.GetAttrValue("AUTHORITY",1));

                    double[] layerExtent = layer.GetExtent();
                    System.out.println("图层范围：minx:"+layerExtent[0]+",maxx:"+layerExtent[1]+",miny:"+layerExtent[2]+",maxy:"+layerExtent[3]);

                    FeatureDefn featureDefn = layer.GetLayerDefn();
                    int fieldCount = featureDefn.GetFieldCount();
                    java.util.Map<String,Object> fieldMap = new HashMap();
                    for(int i=0; i<fieldCount; i++){
                        FieldDefn fieldDefn = featureDefn.GetFieldDefn(i);
                        //得到属性字段类型
                        int fieldType = fieldDefn.GetFieldType();
                        String fieldTypeName = fieldDefn.GetFieldTypeName(fieldType);
                        //得到属性字段名称
                        String fieldName = fieldDefn.GetName();
                        fieldMap.put(fieldName,fieldTypeName);
                    }

                    long featureCount = layer.GetFeatureCount();
                    System.out.println("图层要素个数："+featureCount);
                    for(int i=0; i<featureCount; i++){
                        Feature feature = layer.GetFeature(i);
                        System.out.println("GeometryRef:"+feature.GetGeometryRef().ExportToWkt());
                        java.util.Map shpMap = new HashMap();
                        shpMap.put(SystemConstant.GEOMETRY_COLUMN, feature.GetGeometryRef().ExportToWkt());

                        Object[] arr = fieldMap.keySet().toArray();
                        for(int k=0; k<arr.length; k++){
                            String fvalue = feature.GetFieldAsString(arr[k].toString());
//                            System.out.print(" 属性名称:"+arr[k].toString()+",属性值:"+fvalue);
                            shpMap.put(arr[k], fvalue);
                        }
                        shpMapList.add(shpMap);
                    }
                }
            }
            return shpMapList;
        }
        return null;
    }

    /**
     * 读取kml文件
     * */
    public List<Placemark> readKmlFile(File kmlFile) throws Exception {
        Serializer serializer = new Serializer();
        Kml kmlModel = serializer.read(kmlFile);
        List<Placemark> placemarkList=getAllPlacemark(kmlModel.getFeature(), new ArrayList<Placemark>());
        return placemarkList;
    }

    /**
     * 写入kml文件
     * */
    public boolean writeKmlFile(File kmlFile, String kmlType, List<DrawPointLinePolygonEntity> entityList) throws Exception {
        Serializer serializer = new Serializer();
        Kml kmlModel = new Kml();
        Document kmlDocument = new Document();
        List<com.vtm.library.kml.model.Feature> placemarkList = new ArrayList<>();
        kmlDocument.setFeatureList(placemarkList);
        kmlModel.setFeature(kmlDocument);
        if (entityList!=null&&!entityList.isEmpty()){
            for (int i = 0; i < entityList.size(); i++) {
                Placemark kmlPlacemark = new Placemark();
                placemarkList.add(kmlPlacemark);
                DrawPointLinePolygonEntity drawPointLinePolygonEntity = entityList.get(i);
                ExtendedData kmlExtendData = new ExtendedData();
                kmlPlacemark.setExtendedData(kmlExtendData);
                List<Data> extendDataList = new ArrayList<>();
                extendDataList.add(new Data("name","name",drawPointLinePolygonEntity.getName()));
                extendDataList.add(new Data("remark","remark",drawPointLinePolygonEntity.getRemark()));
                extendDataList.add(new Data("userName","userName",drawPointLinePolygonEntity.getUserName()));
                extendDataList.add(new Data("projectId","projectId",drawPointLinePolygonEntity.getProjectId()+""));
                extendDataList.add(new Data("imgUrlListStr","imgUrlListStr",drawPointLinePolygonEntity.getImgUrlListStr()));
                kmlExtendData.setDataList(extendDataList);

                List<com.vtm.library.kml.model.Geometry> geometryList = new ArrayList<>();
                kmlPlacemark.setGeometryList(geometryList);
                if (GeometryTools.POINT_GEOMETRY_TYPE.equals(kmlType)) {
                    GeoPoint geoPoint = GeometryTools.createGeoPoint(drawPointLinePolygonEntity.getGeometry());
                    Point pointGeometry = new Point();
                    pointGeometry.setCoordinates(new Coordinate(geoPoint.getLatitude(),geoPoint.getLongitude()));
                    geometryList.add(pointGeometry);
                } else if (GeometryTools.LINE_GEOMETRY_TYPE.equals(kmlType)) {
                    LineString lineGeometry = new LineString();
                    geometryList.add(lineGeometry);
                    List<GeoPoint> geoPointList = GeometryTools.getGeoPoints(drawPointLinePolygonEntity.getGeometry());
                    ArrayList<Coordinate> coordinateList = new ArrayList<>();
                    for (GeoPoint geoPoint: geoPointList) {
                        coordinateList.add(new Coordinate(geoPoint.getLongitude(), geoPoint.getLatitude()));
                    }
                    Coordinates coordinates = new Coordinates("");
                    coordinates.setList(coordinateList);
                    lineGeometry.setCoordinates(coordinates);
                } else if (GeometryTools.POLYGON_GEOMETRY_TYPE.equals(kmlType)) {
                    Polygon polygonGeometry = new Polygon();
                    geometryList.add(polygonGeometry);
                    List<GeoPoint> geoPointList = GeometryTools.getGeoPoints(drawPointLinePolygonEntity.getGeometry());
                    ArrayList<Coordinate> coordinateList = new ArrayList<>();
                    for (GeoPoint geoPoint: geoPointList) {
                        coordinateList.add(new Coordinate(geoPoint.getLongitude(), geoPoint.getLatitude()));
                    }
                    Coordinates coordinates = new Coordinates("");
                    coordinates.setList(coordinateList);

                    LinearRing linearRing = new LinearRing();
                    linearRing.setCoordinates(coordinates);

                    Boundary boundary = new Boundary();
                    boundary.setLinearRing(linearRing);
                    polygonGeometry.setOuterBoundaryIs(boundary);
                }
            }
        }
        serializer.write(kmlModel, kmlFile);
        return true;
    }

    /**
     * 将kml文件读取出的数据显示在地图上
     * */
    private void drawKmlData2Map(Context mContext, List<Placemark> placemarkList, String layerName) {
        if (placemarkList!=null&&!placemarkList.isEmpty()){
            for (Placemark placemark:placemarkList) {
                List<com.vtm.library.kml.model.Geometry> geometryList=placemark.getGeometryList();
                for (com.vtm.library.kml.model.Geometry geometry:geometryList) {
                    if (geometry instanceof Point) {
                        Point point = (Point) geometry;
                        Coordinate pointCoordinate=point.getCoordinates();
                        if (pointCoordinate!=null){
                            // 在地图上绘制点
                            addLayer2Map(mContext, GeometryTools.createGeometry(new GeoPoint(pointCoordinate.getLatitude(), pointCoordinate.getLongitude())).toString(), layerName);
                        }
                    } else if (geometry instanceof LineString) {
                        LineString lineString = (LineString) geometry;
                        List<Coordinate> lineCoordinates=lineString.getCoordinates().getList();
                        if (lineCoordinates!=null&&lineCoordinates.size()>1){
                            List<GeoPoint> geoPointList = new ArrayList<>();
                            // 在地图上绘制线
                            for (Coordinate coordinate:lineCoordinates) {
                                geoPointList.add(new GeoPoint(coordinate.getLatitude(),coordinate.getLongitude()));
                            }
                            addLayer2Map(mContext, GeometryTools.getLineString(geoPointList), layerName);
                        }
                    } else if (geometry instanceof LinearRing) {
                        LinearRing linearRing = (LinearRing) geometry;
                        List<Coordinate> lineCoordinates=linearRing.getCoordinates().getList();
                        if (lineCoordinates!=null&&lineCoordinates.size()>1){
                            // 在地图上绘制线
                            List<GeoPoint> geoPointList = new ArrayList<>();
                            // 在地图上绘制线
                            for (Coordinate coordinate:lineCoordinates) {
                                geoPointList.add(new GeoPoint(coordinate.getLatitude(),coordinate.getLongitude()));
                            }
                            addLayer2Map(mContext, GeometryTools.getLineString(geoPointList), layerName);
                        }
                    } else if (geometry instanceof Polygon) {
                        Polygon polygon = (Polygon) geometry;
                        if (polygon.getInnerBoundaryIs()!=null&&polygon.getInnerBoundaryIs().getLinearRing()!=null){
                            List<Coordinate> lineCoordinates=polygon.getInnerBoundaryIs().getLinearRing().getCoordinates().getList();
                            if (lineCoordinates!=null&&lineCoordinates.size()>2){
                                // 在地图上绘制面
                                List<GeoPoint> geoPointList = new ArrayList<>();
                                for (Coordinate coordinate:lineCoordinates) {
                                    geoPointList.add(new GeoPoint(coordinate.getLatitude(),coordinate.getLongitude()));
                                }
                                addLayer2Map(mContext, GeometryTools.createPolygon(geoPointList).toString(), layerName);
                            }
                        } else if (polygon.getOuterBoundaryIs()!=null&&polygon.getOuterBoundaryIs().getLinearRing()!=null){
                            List<Coordinate> lineCoordinates=polygon.getOuterBoundaryIs().getLinearRing().getCoordinates().getList();
                            if (lineCoordinates!=null&&lineCoordinates.size()>2){
                                // 在地图上绘制面
                                List<GeoPoint> geoPointList = new ArrayList<>();
                                for (Coordinate coordinate:lineCoordinates) {
                                    geoPointList.add(new GeoPoint(coordinate.getLatitude(),coordinate.getLongitude()));
                                }
                                addLayer2Map(mContext, GeometryTools.createPolygon(geoPointList).toString(), layerName);
                            }
                        }
                    }
                }
                CatEyeMapManager.getInstance().getCatEyeMap().updateMap();
            }
        }
    }

    /**
     * 添加图层到地图
     * */
    private void addLayer2Map(Context mContext, String wkt, String layerName) {
        Map mMap = CatEyeMapManager.getInstance().getCatEyeMap();
        com.vividsolutions.jts.geom.Geometry geometry = GeometryTools.createGeometry(wkt);
        if (GeometryTools.POINT_GEOMETRY_TYPE.equals(geometry.getGeometryType())) {
            ItemizedLayer markerLayer = null;
            if (OverlayerManager.getInstance(mMap).getLayerByName(layerName, ItemizedLayer.class)!=null){
                markerLayer = (ItemizedLayer) OverlayerManager.getInstance(mMap).getLayerByName(layerName, ItemizedLayer.class);
            } else {
                markerLayer= new ItemizedLayer(mMap, LayerStyle.getDefaultMarkerSymbol(mContext), layerName);
                CatEyeMapManager.getInstance().getCatEyeMap().layers().add(markerLayer, MainActivity.LAYER_GROUP_ENUM.OTHER_GROUP.orderIndex);
            }
            markerLayer.addItem(new MarkerItem(wkt,wkt,GeometryTools.createGeoPoint(wkt)));
        } else if (GeometryTools.LINE_GEOMETRY_TYPE.equals(geometry.getGeometryType())) {
            MultiPathLayer multiPathLayer = null;
            if (OverlayerManager.getInstance(mMap).getLayerByName(layerName, MultiPathLayer.class)!=null){
                multiPathLayer = (MultiPathLayer) OverlayerManager.getInstance(mMap).getLayerByName(layerName, MultiPathLayer.class);
            } else {
                multiPathLayer= new MultiPathLayer(mMap, LayerStyle.getDefaultLineStyle(), layerName);
                CatEyeMapManager.getInstance().getCatEyeMap().layers().add(multiPathLayer, MainActivity.LAYER_GROUP_ENUM.OTHER_GROUP.orderIndex);
            }
            multiPathLayer.addPathDrawable(GeometryTools.getGeoPoints(wkt));
        } else if (GeometryTools.POLYGON_GEOMETRY_TYPE.equals(geometry.getGeometryType())) {
            MultiPolygonLayer multiPolygonLayer = null;
            if (OverlayerManager.getInstance(mMap).getLayerByName(layerName, MultiPolygonLayer.class)!=null){
                multiPolygonLayer = (MultiPolygonLayer) OverlayerManager.getInstance(mMap).getLayerByName(layerName, MultiPolygonLayer.class);
            } else {
                multiPolygonLayer= new MultiPolygonLayer(mMap, LayerStyle.getDefaultLineStyle(), layerName);
                CatEyeMapManager.getInstance().getCatEyeMap().layers().add(multiPolygonLayer, MainActivity.LAYER_GROUP_ENUM.OTHER_GROUP.orderIndex);
            }
            multiPolygonLayer.addPolygonDrawable(GeometryTools.getGeoPoints(wkt));
        }
    }


    /**
     * 通过递归获取到kml文件中所有的placemarker数据
     * */
    private List<Placemark> getAllPlacemark(com.vtm.library.kml.model.Feature feature, List<Placemark> placemarkList) {
        if (placemarkList == null) {
            placemarkList = new ArrayList<>();
        }
        if (feature instanceof Folder) {
            Folder folder = (Folder)feature;
            for (com.vtm.library.kml.model.Feature feature1: folder.getFeatureList()){
                getAllPlacemark(feature1, placemarkList);
            }
        } else if (feature instanceof Document) {
            Document document = (Document)feature;
            for (com.vtm.library.kml.model.Feature feature1: document.getFeatureList()){
                getAllPlacemark(feature1, placemarkList);
            }
        } else if (feature instanceof Placemark) {
            Placemark placemark = (Placemark)feature;
            placemarkList.add(placemark);
        }
        return placemarkList;
    }


    /**
     * 增加本地Map文件的地图layer
     */
    public void addLocalMapFileLayer(String localMapFilePath) {
        Map mMap = CatEyeMapManager.getInstance().getCatEyeMap();
        MapFileTileSource mTileSource = new MapFileTileSource();
        mTileSource.setPreferredLanguage("zh");

        if (mTileSource.setMapFile(localMapFilePath)) {
            //设置当前的文件选择的layer为地图的基础图层(第一层)==此处去掉此设置
            VectorTileLayer mTileLayer = new OsmTileLayer(mMap);
            mTileLayer.setTileSource(mTileSource);
            mMap.layers().add(mTileLayer, MainActivity.LAYER_GROUP_ENUM.BASE_VECTOR_GROUP.orderIndex);
            mMap.layers().add(new LabelLayer(mMap, mTileLayer), MainActivity.LAYER_GROUP_ENUM.BASE_VECTOR_GROUP.orderIndex);
            mMap.layers().add(new BuildingLayer(mMap, mTileLayer), MainActivity.LAYER_GROUP_ENUM.BASE_VECTOR_GROUP.orderIndex);
            MapInfo info = mTileSource.getMapInfo();
            MapPosition pos = new MapPosition();
            pos.setByBoundingBox(info.boundingBox, Tile.SIZE * 4, Tile.SIZE * 4);
            mMap.animator().animateTo(pos);
            mMap.updateMap(true);
        }
    }

    /**
     * 增加本地Kml文件的地图layer
     */
    public void addLocalKmlFileLayer(String localKmlFilePath, Context mContext) {
        Map mMap = CatEyeMapManager.getInstance().getCatEyeMap();
        File kmlFile = new File(localKmlFilePath);
        if (kmlFile!=null&&kmlFile.exists()) {
            try {
                List<Placemark> placemarkList = readKmlFile(kmlFile);
                if (placemarkList!=null) {
                    drawKmlData2Map(mContext, placemarkList, kmlFile.getAbsolutePath());
                }
            } catch (Exception e) {

            }
            mMap.updateMap(true);
        }
    }
    /**
     * 增加本地Kml文件的地图layer
     */
    public void addLocalShpFileLayer(String localShpFilePath, Context mContext) {
        Map mMap = CatEyeMapManager.getInstance().getCatEyeMap();
        File shpFile = new File(localShpFilePath);
        if (shpFile!=null&&shpFile.exists()) {
            try {
                List<java.util.Map<String, String>> shpDataList = readShpFile(shpFile);
                if (shpDataList!=null&&!shpDataList.isEmpty()) {
                    for (java.util.Map map:shpDataList) {
                        addLayer2Map(mContext, map.get(SystemConstant.GEOMETRY_COLUMN).toString(), shpFile.getAbsolutePath());
                    }
                }
            } catch (Exception e) {

            }
            mMap.updateMap(true);
        }
    }

}
