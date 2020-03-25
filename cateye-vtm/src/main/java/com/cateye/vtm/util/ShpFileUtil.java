package com.cateye.vtm.util;

import com.cateye.android.entity.DrawPointLinePolygonEntity;
import com.vtm.library.tools.GeometryTools;

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

import java.io.File;
import java.util.List;

//将空间数据写入shp文件
public class ShpFileUtil {
    public static void writeShp(File shpFile, String shpType, List<DrawPointLinePolygonEntity> entityList) throws Exception{

        //这个可以看到支持哪些格式的驱动，不用的时候可以注释掉
//        int count =ogr.GetDriverCount();
//        for(int i=0;i<count;i++){
//            System.out.println(ogr.GetDriver(i).GetName());
//        }
        //配置GDAL_DATA路径（gdal根目录下的bin\gdal-data）
        gdal.SetConfigOption("GDAL_DATA",shpFile.getParent());
        // 为了支持中文路径，请添加下面这句代码
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","YES");
        // 为了使属性表字段支持中文，请添加下面这句
        gdal.SetConfigOption("SHAPE_ENCODING","CP936");
        //创建数据，这里以创建ESRI的shp文件为例
        String strDriverName = "ESRI Shapefile";

        //创建一个文件，根据strDriverName扩展名自动判断是创建shp文件或其他文件
        Driver oDriver =ogr.GetDriverByName(strDriverName);
        if (oDriver == null) {
            System.out.println(strDriverName+ " 驱动不可用！\n");
            throw new Exception("驱动不可用！");
        }

        // 创建数据源
        DataSource oDS = oDriver.CreateDataSource(shpFile.getAbsolutePath(),null);
        if (oDS == null) {
//            System.out.println("创建矢量文件【"+ shpFile.getAbsolutePath() +"】失败！\n" );
            throw new Exception("创建矢量文件【"+ shpFile.getAbsolutePath() +"】失败！");
        }

        // 创建图层，创建一个多边形图层，这里没有指定空间参考，如果需要的话，需要在这里进行指定
        //如果是mif或者tab，其实是可以多元素混合存放的，shp每个图层只能存放点线面中的一种。
        //所以tab创建图层的时候，选择什么都不影响后面的操作和存储结果
        SpatialReference spatialReference = new SpatialReference();
        spatialReference.SetWellKnownGeogCS("WGS84");
        Layer oLayer = null;
        String shpName = shpFile.getName().substring(0,shpFile.getName().lastIndexOf("."));
        if (GeometryTools.POINT_GEOMETRY_TYPE.equals(shpType)) {
            oLayer =oDS.CreateLayer(shpName, spatialReference, ogr.wkbPoint, null);
        } else if (GeometryTools.LINE_GEOMETRY_TYPE.equals(shpType)) {
            oLayer =oDS.CreateLayer(shpName, spatialReference, ogr.wkbLineString, null);
        } else if (GeometryTools.POLYGON_GEOMETRY_TYPE.equals(shpType)) {
            oLayer =oDS.CreateLayer(shpName, spatialReference, ogr.wkbPolygon, null);
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

        FeatureDefn oDefn =oLayer.GetLayerDefn();

        for (DrawPointLinePolygonEntity entity:entityList) {
            Feature oFeatureTriangle = new Feature(oDefn);
            oFeatureTriangle.SetField("id",entity.get_id());
            oFeatureTriangle.SetField("name",entity.getName());
            oFeatureTriangle.SetField("remark",entity.getRemark());
            oFeatureTriangle.SetField("userName",entity.getUserName());
            oFeatureTriangle.SetField("projectId",entity.getProjectId());
            oFeatureTriangle.SetField("imgUrlListStr",entity.getImgUrlListStr());
            oFeatureTriangle.SetGeometry(Geometry.CreateFromWkt(entity.getGeometry()));
            oLayer.CreateFeature(oFeatureTriangle);
        }
        //写入文件
        oLayer.SyncToDisk();
        oDS.SyncToDisk();

        System.out.println("\n数据集创建完成！\n");
    }
}
