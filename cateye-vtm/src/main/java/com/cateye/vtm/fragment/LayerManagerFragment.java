package com.cateye.vtm.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.cateye.android.entity.ContourMPData;
import com.cateye.android.entity.MapSourceFromNet;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.adapter.LayerManagerAdapter;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.CatEyeMapManager;
import com.cateye.vtm.util.SystemConstant;
import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.GeoJSONObject;
import com.cocoahero.android.geojson.LineString;
import com.cocoahero.android.geojson.Position;
import com.cocoahero.android.geojson.Ring;
import com.vondear.rxtool.view.RxToast;
import com.vtm.library.layers.MultiPathLayer;
import com.vtm.library.layers.MultiPolygonLayer;
import com.vtm.library.tools.GeometryTools;
import com.vtm.library.tools.OverlayerManager;

import org.json.JSONException;
import org.oscim.android.filepicker.FilePicker;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapElement;
import org.oscim.core.Tag;
import org.oscim.layers.Layer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.tile.MapTile;
import org.oscim.layers.tile.vector.OsmTileLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.map.Map;
import org.oscim.renderer.bucket.RenderBuckets;
import org.oscim.theme.ExternalRenderTheme;
import org.oscim.theme.ThemeUtils;
import org.oscim.theme.styles.AreaStyle;
import org.oscim.theme.styles.RenderStyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LayerManagerFragment extends BaseFragment {
    private Map mMap;
    private ExpandableListView expandableListView;
    private LayerManagerAdapter layerManagerAdapter;
    private List<MapSourceFromNet.DataBean> layerDataBeanList;

    public static final int SELECT_MAP_FILE = 0;
    public static final int SELECT_THEME_FILE = SELECT_MAP_FILE + 1;
    public static final int SELECT_GEOJSON_FILE = SELECT_MAP_FILE + 2;

    private static final Tag ISSEA_TAG = new Tag("natural", "issea");
    private static final Tag NOSEA_TAG = new Tag("natural", "nosea");
    private static final Tag SEA_TAG = new Tag("natural", "sea");

    private ItemizedLayer<MarkerItem> geoJsonMarkerLayer;
    private MultiPathLayer geoJsonMultiPathLayer;
    private MultiPolygonLayer geoJsonMultiPolygonLayer;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMap = CatEyeMapManager.getMapView().map();
        if (savedInstanceState!=null) {
            layerDataBeanList= (List<MapSourceFromNet.DataBean>) savedInstanceState.getSerializable(SystemConstant.BUNDLE_LAYER_MANAGER_DATA);
        }
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            //获取等高线的数据
            layerDataBeanList = (List<MapSourceFromNet.DataBean>) bundle.getSerializable(SystemConstant.BUNDLE_LAYER_MANAGER_DATA);
        }
    }

    @Override
    public void onNewBundle(Bundle args) {
        super.onNewBundle(args);
        if (args!=null) {
            layerDataBeanList= (List<MapSourceFromNet.DataBean>) args.getSerializable(SystemConstant.BUNDLE_LAYER_MANAGER_DATA);
        }
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_layer_manager;
    }

    @Override
    public void initView(View rootView) {
        expandableListView = (ExpandableListView) rootView.findViewById(R.id.sadLv_layerlist);
        layerManagerAdapter = new LayerManagerAdapter(getActivity(), layerDataBeanList);
        expandableListView.setAdapter(layerManagerAdapter);

        //增加map按钮
        TextView tv_add = (TextView) rootView.findViewById(R.id.tv_layerlist_add);
        tv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), MainActivity.MapFilePicker.class),
                        SELECT_MAP_FILE);
            }
        });

        //增加geojson按钮
        TextView tv_geojson = (TextView) rootView.findViewById(R.id.tv_layerlist_geojson);
        tv_geojson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), MainActivity.ContourFilePicker.class),
                        SELECT_GEOJSON_FILE);
            }
        });
    }

    public static BaseFragment newInstance(Bundle bundle) {
        LayerManagerFragment layerManagerFragment = new LayerManagerFragment();
        layerManagerFragment.setArguments(bundle);
        return layerManagerFragment;
    }

    /**
     * 显示geoJson文件的数据
     * */
    private void showGeoJsonFileData(List<com.cocoahero.android.geojson.Geometry> geometryList){
        if (geometryList!=null&&!geometryList.isEmpty()) {
            for (com.cocoahero.android.geojson.Geometry geometry:geometryList) {
                String geometryType = geometry.getType();
                if (geometryType == "Point"){
                    com.cocoahero.android.geojson.Point point= (com.cocoahero.android.geojson.Point) geometry;
                    MarkerItem markerItem=new MarkerItem("","", GeometryTools.position2GeoPoint(point.getPosition()));
                    if (geoJsonMarkerLayer == null) {
                        geoJsonMarkerLayer = (ItemizedLayer<MarkerItem>) OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.LAYER_NAME_GEOJSON_POINT);
                    }
                    if (geoJsonMarkerLayer != null) {
                        geoJsonMarkerLayer.addItem(markerItem);
                        geoJsonMarkerLayer.update();
                    }
                } else if (geometryType == "LineString"){
                    LineString lineString= (LineString) geometry;
                    List<Position> positionList=lineString.getPositions();
                    List<GeoPoint> pointList = new ArrayList<>();
                    for (Position position:positionList) {
                        pointList.add(GeometryTools.position2GeoPoint(position));
                    }
                    if (geoJsonMultiPathLayer == null) {
                        geoJsonMultiPathLayer = (MultiPathLayer) OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.LAYER_NAME_GEOJSON_LINE);
                    }
                    if (geoJsonMultiPathLayer != null) {
                        geoJsonMultiPathLayer.addPathDrawable(pointList);
                        geoJsonMultiPathLayer.update();
                    }
                } else if (geometryType == "Polygon"){
                    com.cocoahero.android.geojson.Polygon polygon= (com.cocoahero.android.geojson.Polygon) geometry;
                    List<Ring> positionList=polygon.getRings();
                    if (geoJsonMultiPolygonLayer == null) {
                        geoJsonMultiPolygonLayer = (MultiPolygonLayer) OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.LAYER_NAME_GEOJSON_POLYGON);
                    }
                    for (Ring ring:positionList) {
                        List<GeoPoint> pointList = new ArrayList<>();
                        if (ring!=null&&ring.getPositions()!=null){
                            for (Position position:ring.getPositions()) {
                                pointList.add(GeometryTools.position2GeoPoint(position));
                            }
                        }
                        if (geoJsonMultiPolygonLayer != null) {
                            geoJsonMultiPolygonLayer.addPolygonDrawable(pointList);
                        }
                    }
                    if (geoJsonMultiPolygonLayer != null) {
                        geoJsonMultiPolygonLayer.update();
                    }
                }
            }
            mMap.updateMap(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SELECT_MAP_FILE) {//选择本地地图文件显示
            if (resultCode != getActivity().RESULT_OK || intent == null || intent.getStringExtra(FilePicker.SELECTED_FILE) == null) {
//                finish();
                return;
            }
            String file = intent.getStringExtra(FilePicker.SELECTED_FILE);

            //增加本地layer的dataBean到dataBeanList中
            if (file != null) {
                //判断当前图层中是否已经存在选择的文件，如果存在，则不再添加
                if (layerDataBeanList != null && !layerDataBeanList.isEmpty()) {
                    for (MapSourceFromNet.DataBean dataBean : layerDataBeanList) {
                        if (dataBean.getMaps() != null) {
                            for (MapSourceFromNet.DataBean.MapsBean mapsBean : dataBean.getMaps()) {
                                if (file.equals(mapsBean.getHref())) {
                                    RxToast.info("已经添加过相同的图层！无法再次添加！");
                                    return;
                                }
                            }
                        }
                    }
                }
                File mapFile = new File(file);
                if (mapFile.exists()) {
                    MapSourceFromNet.DataBean.MapsBean mapFileDataBean = new MapSourceFromNet.DataBean.MapsBean();
                    mapFileDataBean.setAbstractX(mapFile.getName());
                    mapFileDataBean.setHref(file);
                    String fileName = mapFile.getName();
                    String suffix = fileName.substring(fileName.lastIndexOf("."));
                    mapFileDataBean.setExtension(suffix);
                    if (suffix != null) {
                        MapSourceFromNet.DataBean localDataBean = new MapSourceFromNet.DataBean();
                        if (suffix.toLowerCase().endsWith("map")) {
                            mapFileDataBean.setGroup(MainActivity.LAYER_GROUP_ENUM.BASE_VECTOR_GROUP.name);
                            localDataBean.setGroup(MainActivity.LAYER_GROUP_ENUM.BASE_VECTOR_GROUP.name);
                        } else if (suffix.toLowerCase().endsWith("json")) {
                            mapFileDataBean.setGroup(MainActivity.LAYER_GROUP_ENUM.PROJ_VECTOR_GROUP.name);
                            localDataBean.setGroup(MainActivity.LAYER_GROUP_ENUM.PROJ_VECTOR_GROUP.name);
                        }
                        localDataBean.setMemo(mapFile.getName());
                        localDataBean.setMaps(new ArrayList<MapSourceFromNet.DataBean.MapsBean>());
                        localDataBean.getMaps().add(mapFileDataBean);
                        if (layerDataBeanList != null) {
                            layerDataBeanList.add(localDataBean);
                            if (layerManagerAdapter != null) {
                                layerManagerAdapter.sortListDataAndGroup(layerDataBeanList);
                                layerManagerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        }else if (requestCode == SELECT_GEOJSON_FILE) { // 用户选择本地geojson文件
            if (resultCode != getActivity().RESULT_OK || intent == null || intent.getStringExtra(FilePicker.SELECTED_FILE) == null) {
                return;
            }
            String filePath = intent.getStringExtra(FilePicker.SELECTED_FILE);
            try {
                FileInputStream geoJsonStream = new FileInputStream(new File(filePath));
                GeoJSONObject geoJSONObject= GeoJSON.parse(geoJsonStream);
                List<com.cocoahero.android.geojson.Geometry> geometryList = new ArrayList<>();
                if (geoJSONObject.getType() == "FeatureCollection") {
                    FeatureCollection featureCollection= (FeatureCollection) geoJSONObject;
                    for (Feature feature:featureCollection.getFeatures()) {
                        geometryList.add(feature.getGeometry());
                    }
                } else if (geoJSONObject.getType() == "Feature") {
                    Feature feature= (Feature) geoJSONObject;
                    geometryList.add(feature.getGeometry());
                } else if (geoJSONObject.getType() == "Point" || geoJSONObject.getType() == "LineString" || geoJSONObject.getType() == "Polygon") {
                    com.cocoahero.android.geojson.Point point= (com.cocoahero.android.geojson.Point) geoJSONObject;
                    geometryList.add(point);
                } else if (geoJSONObject.getType() == "LineString") {
                    com.cocoahero.android.geojson.LineString lineString= (com.cocoahero.android.geojson.LineString) geoJSONObject;
                    geometryList.add(lineString);
                } else if (geoJSONObject.getType() == "Polygon") {
                    com.cocoahero.android.geojson.Polygon polygon= (com.cocoahero.android.geojson.Polygon) geoJSONObject;
                    geometryList.add(polygon);
                }
                showGeoJsonFileData(geometryList);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (requestCode == SELECT_THEME_FILE) {//选择本地style文件显示
            if (resultCode != getActivity().RESULT_OK || intent == null || intent.getStringExtra(FilePicker.SELECTED_FILE) == null) {
                return;
            }

            String file = intent.getStringExtra(FilePicker.SELECTED_FILE);
            ExternalRenderTheme externalRenderTheme = new ExternalRenderTheme(file);

            // Use tessellation with sea and land for Mapsforge themes
            if (ThemeUtils.isMapsforgeTheme(externalRenderTheme)) {
                //遍历所有的地图图层，添加hook
                if (mMap.layers() != null && !mMap.layers().isEmpty()) {
                    for (Layer layer : mMap.layers()) {
                        if (layer.isEnabled() && layer instanceof OsmTileLayer)
                            ((OsmTileLayer) layer).addHook(new VectorTileLayer.TileLoaderThemeHook() {
                                @Override
                                public boolean process(MapTile tile, RenderBuckets buckets, MapElement element, RenderStyle style, int level) {
                                    if (element.tags.contains(ISSEA_TAG) || element.tags.contains(SEA_TAG) || element.tags.contains(NOSEA_TAG)) {
                                        if (style instanceof AreaStyle)
                                            ((AreaStyle) style).mesh = true;
                                    }
                                    return false;
                                }

                                @Override
                                public void complete(MapTile tile, boolean success) {
                                }
                            });
                    }
                }

            }
            mMap.setTheme(externalRenderTheme);
        }
    }
}
