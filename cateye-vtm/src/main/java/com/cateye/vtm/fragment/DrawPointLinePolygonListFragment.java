package com.cateye.vtm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.canyinghao.candialog.CanDialog;
import com.canyinghao.candialog.CanDialogInterface;
import com.cateye.android.entity.DrawPointLinePolygonEntity;
import com.cateye.android.entity.UploadRecordEntity;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.CatEyeMapManager;
import com.cateye.vtm.util.LayerStyle;
import com.cateye.vtm.util.ShpFileUtil;
import com.cateye.vtm.util.SystemConstant;
import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okrx2.adapter.ObservableResponse;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vondear.rxtool.RxDataTool;
import com.vondear.rxtool.RxFileTool;
import com.vondear.rxtool.RxRecyclerViewDividerTool;
import com.vondear.rxtool.view.RxToast;
import com.vondear.rxui.view.dialog.RxDialogLoading;
import com.vondear.rxui.view.dialog.RxDialogSureCancel;
import com.vtm.library.layers.MultiPathLayer;
import com.vtm.library.layers.MultiPolygonLayer;
import com.vtm.library.tools.GeometryTools;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.oscim.core.BoundingBox;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.map.Map;
import org.xutils.DbManager;
import org.xutils.common.util.KeyValue;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;

/**
 * Created by xiaoxiao on 2018/8/31.
 * 从数据库中选择polygon的列表fragment
 */

public class DrawPointLinePolygonListFragment extends BaseDrawFragment {
    private Map mMap;
    private RefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private DrawPointLinePolygonAdapter adapter;
    private List<DrawPointLinePolygonEntity> listData;
    private DbManager dbManager;

    private final int PAGE_SIZE = 10;
    private int page = 0;

    //    private AirPlanMultiPolygonLayer airPlanDrawLayer;
    private ImageView img_back;

    private ItemizedLayer highLightPointLayer;
    private MultiPathLayer highLightPathLayer;
    private MultiPolygonLayer highLightPolygonLayer;

    private AwesomeTextView atv_upload, atv_download, atv_export;
    private RxDialogLoading rxDialogLoading;

    private List<DrawPointLinePolygonEntity> checkedListData;
//    private Set<Integer> checkedSet;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMap = CatEyeMapManager.getMapView().map();
        this.dbManager = ((MainActivity) getActivity()).getDbManager();
        this.rxDialogLoading = new RxDialogLoading(getActivity());

        //初始化点线面的显示图层
        if (highLightPointLayer == null) {
            //打开该fragment，则自动向地图中添加marker的overlay
            highLightPointLayer = new ItemizedLayer(mMap, LayerStyle.getHighLightMarkerSymbol(getActivity()), SystemConstant.LAYER_NAME_DRAW_POINT_HIGHLIGHT);
            mMap.layers().add(highLightPointLayer);

        }

        if (highLightPathLayer == null) {
            //自动添加pathLayer
            highLightPathLayer = new MultiPathLayer(mMap, LayerStyle.getHighLightLineStyle(), SystemConstant.LAYER_NAME_DRAW_LINE_HIGHLIGHT);
            mMap.layers().add(highLightPathLayer);
        }

        if (highLightPolygonLayer == null) {
            highLightPolygonLayer = new MultiPolygonLayer(mMap, LayerStyle.getHighLightPolygonStyle(), SystemConstant.LAYER_NAME_DRAW_POLYGON_HIGHLIGHT);
            mMap.layers().add(highLightPolygonLayer);
        }
    }

    public static BaseFragment newInstance(Bundle bundle) {
        DrawPointLinePolygonListFragment airPlanDrawFragment = new DrawPointLinePolygonListFragment();
        airPlanDrawFragment.setArguments(bundle);
        return airPlanDrawFragment;
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_air_plan_polygon_list;
    }

    @Override
    public void initView(View rootView) {
        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        recyclerView = rootView.findViewById(R.id.rv_air_plan_polygon);

        atv_upload = rootView.findViewById(R.id.atv_draw_upload);
        atv_download = rootView.findViewById(R.id.atv_draw_download);
        atv_export = rootView.findViewById(R.id.atv_draw_export);
        atv_export.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        listData = new ArrayList<>();
        checkedListData = new ArrayList<>();
//        checkedSet=new HashSet<>();
        adapter = new DrawPointLinePolygonAdapter(getActivity(), listData);
        recyclerView.setAdapter(adapter);
        //设置 Footer 为 球脉冲 样式
        refreshLayout.setRefreshFooter(new BallPulseFooter(getActivity()).setSpinnerStyle(SpinnerStyle.Scale));
        refreshLayout.setEnableRefresh(false);
        recyclerView.addItemDecoration(new RxRecyclerViewDividerTool(0, 0, 2, 2));
        //默认加载前20条数据
        try {
            List<DrawPointLinePolygonEntity> dbEntityList = dbManager.selector(DrawPointLinePolygonEntity.class).where("projectId", "=", SystemConstant.CURRENT_PROJECTS_ID).limit(PAGE_SIZE).offset(page * PAGE_SIZE).findAll();
            if (dbEntityList != null/* && !dbEntityList.isEmpty()*/) {
                listData.addAll(dbEntityList);
            } else {
                RxToast.warning("本地没有存储的绘制数据");
                onBackPressedSupport();
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        //上拉加载更多
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                page++;
                try {
                    List<DrawPointLinePolygonEntity> dbEntityList = dbManager.selector(DrawPointLinePolygonEntity.class).where("projectId", "=", SystemConstant.CURRENT_PROJECTS_ID).limit(PAGE_SIZE).offset(page * PAGE_SIZE).findAll();
                    if (dbEntityList != null && !dbEntityList.isEmpty()) {
                        listData.addAll(dbEntityList);
                        adapter.notifyDataSetChanged();
                    } else {
                        RxToast.warning("没有更多的数据!");
                        refreshLayout.setEnableLoadMore(false);//没有更多数据，设置不可再通过上拉加载数据
                        page--;
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                } finally {
                    refreshLayout.finishLoadMore();
                }
            }
        });

        img_back = (ImageView) findViewById(R.id.tv_air_plan_list_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressedSupport();
            }
        });

        //上传用户绘制的数据
        atv_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<DrawPointLinePolygonEntity> uploadListData = DrawPointLinePolygonListFragment.this.dbManager.selector(DrawPointLinePolygonEntity.class).where("isUpload", "=", false).findAll();
                    if (uploadListData != null && !uploadListData.isEmpty()) {
                        Observable.fromIterable(uploadListData).subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread())
                                .map(new Function<DrawPointLinePolygonEntity, DrawPointLinePolygonEntity>() {
                                    @Override
                                    public DrawPointLinePolygonEntity apply(DrawPointLinePolygonEntity drawPointLinePolygonEntity) throws Exception {
                                        if (!RxDataTool.isEmpty(drawPointLinePolygonEntity.getImgUrlList())) {
                                            List imgList = drawPointLinePolygonEntity.getImgUrlList();
                                            ListIterator iterator = imgList.listIterator();
                                            while (iterator.hasNext()) {
                                                String imgStr = (String) iterator.next();
                                                if (!RxDataTool.isNullString(imgStr) && !imgStr.startsWith("http://") && !imgStr.startsWith("https://")) {
                                                    // 处理照片，当存在照片时，使用同步方式上传该照片，并且同步更新到本地数据库中
                                                    Response imgUploadResponse = OkGo.<String>post(SystemConstant.IMG_UPLOAD).tag(this).params("projectId", SystemConstant.CURRENT_PROJECTS_ID).params("file", new File(imgStr)).execute();
                                                    String imgUploadResult = imgUploadResponse.body().string();
                                                    if (imgUploadResult != null) {
                                                        java.util.Map resultMap = (java.util.Map) JSON.parse(imgUploadResult);
                                                        if (resultMap != null && resultMap.containsKey("errcode") && resultMap.get("errcode").toString().equals("0")) {
                                                            iterator.set(resultMap.get("data").toString()); // 更新照片路径为网络路径
                                                        }
                                                    }
                                                }
                                            }
                                            drawPointLinePolygonEntity.setImgUrlList(imgList);
                                            DrawPointLinePolygonListFragment.this.dbManager.saveOrUpdate(drawPointLinePolygonEntity); // 更新到数据库中
                                        }
                                        return drawPointLinePolygonEntity;
                                    }
                                })
                                .subscribeOn(Schedulers.computation())
                                .map(new Function<DrawPointLinePolygonEntity, UploadRecordEntity>() {
                                    @Override
                                    public UploadRecordEntity apply(DrawPointLinePolygonEntity drawPointLinePolygonEntity) throws Exception {
                                        if (drawPointLinePolygonEntity != null) {
                                            UploadRecordEntity uploadRecordEntity = new UploadRecordEntity();
                                            uploadRecordEntity.setUuid(drawPointLinePolygonEntity.get_id());
                                            uploadRecordEntity.setName(drawPointLinePolygonEntity.getName());
                                            uploadRecordEntity.setProjectId(drawPointLinePolygonEntity.getProjectId());
                                            uploadRecordEntity.setWkt(drawPointLinePolygonEntity.getGeometry());

                                            java.util.Map propMap = new HashMap();
                                            if (drawPointLinePolygonEntity.getImgUrlListStr() != null && !drawPointLinePolygonEntity.getImgUrlListStr().isEmpty()) {
                                                propMap.put(SystemConstant.PARAM_PROP_KEY_IMG, drawPointLinePolygonEntity.getImgUrlListStr());
                                            }
                                            if (!RxDataTool.isEmpty(drawPointLinePolygonEntity.getRemark())) {
                                                propMap.put(SystemConstant.PARAM_PROP_KEY_REMARK, drawPointLinePolygonEntity.getRemark());
                                            }
                                            uploadRecordEntity.setProp(JSON.toJSONString(propMap));
                                            return uploadRecordEntity;
                                        }
                                        return null;
                                    }
                                })
                                .subscribeOn(Schedulers.computation())
                                .collect(new Callable<ArrayList<UploadRecordEntity>>() {
                                             @Override
                                             public ArrayList<UploadRecordEntity> call() throws Exception {
                                                 return new ArrayList<UploadRecordEntity>();
                                             }
                                         },
                                        new BiConsumer<ArrayList<UploadRecordEntity>, UploadRecordEntity>() {
                                            @Override
                                            public void accept(ArrayList<UploadRecordEntity> uploadRecordEntities, UploadRecordEntity uploadRecordEntity) throws Exception {
                                                uploadRecordEntities.add(uploadRecordEntity);
                                            }
                                        })
                                .subscribeOn(Schedulers.newThread())
                                .map(new Function<ArrayList<UploadRecordEntity>, ArrayList<UploadRecordEntity>>() { // 调用网络接口，批量保存数据
                                    @Override
                                    public ArrayList<UploadRecordEntity> apply(ArrayList<UploadRecordEntity> uploadRecordEntities) throws Exception {
                                        Response uploadDataResponse = OkGo.<String>post(SystemConstant.BATCH_SAVE_WKT).upJson(JSONArray.toJSONString(uploadRecordEntities)).execute();
                                        String result = uploadDataResponse.body().string();
                                        if (result != null) {
                                            java.util.Map resultMap = (java.util.Map) JSON.parse(result);
                                            if (resultMap.containsKey("errcode") && resultMap.get("errcode").toString().equals("0")) {
                                                // 批量更新所有的数据为已上传
                                                KeyValue keyValue = new KeyValue("isUpload", true);
                                                DrawPointLinePolygonListFragment.this.dbManager.update(DrawPointLinePolygonEntity.class, WhereBuilder.b("1", "=", "1"), keyValue);
                                            }
                                        }
                                        return uploadRecordEntities;
                                    }
                                })
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<ArrayList<UploadRecordEntity>>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        rxDialogLoading.show();
                                    }

                                    @Override
                                    public void onSuccess(ArrayList<UploadRecordEntity> uploadRecordEntities) {
                                        rxDialogLoading.dismiss();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        rxDialogLoading.dismiss();
                                    }
                                });
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        });

        atv_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RxDialogSureCancel rxDialogSureCancel = new RxDialogSureCancel(getActivity());
                rxDialogSureCancel.show();
                rxDialogSureCancel.setTitle("提示");
                rxDialogSureCancel.setContent("下载同步数据后，本地未上传的数据可能会被覆盖，确认仍然下载吗？");
                rxDialogSureCancel.setCancel("取消");
                rxDialogSureCancel.setCancelListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rxDialogSureCancel.dismiss();
                    }
                });
                rxDialogSureCancel.setSure("确定");
                rxDialogSureCancel.setSureListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rxDialogSureCancel.dismiss();
                        OkGo.<String>get(SystemConstant.DATA_LIST).params("projectId", SystemConstant.CURRENT_PROJECTS_ID).tag(this).converter(new StringConvert())
                                .adapt(new ObservableResponse<String>())
                                .subscribeOn(Schedulers.newThread())
                                .doOnSubscribe(new Consumer<Disposable>() {
                                    @Override
                                    public void accept(Disposable disposable) throws Exception {
                                        rxDialogLoading.show();
                                    }
                                }).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<com.lzy.okgo.model.Response<String>>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onNext(com.lzy.okgo.model.Response<String> stringResponse) {
                                        if (stringResponse != null && stringResponse.body() != null) {
                                            String result = stringResponse.body();
                                            if (result != null) {
//                                System.out.println(result);
                                                java.util.Map<String, Object> resultMap = (java.util.Map<String, Object>) JSON.parse(result);
                                                if (resultMap != null && resultMap.get("errcode").toString().equals("0") && resultMap.get("data") != null) {
                                                    List<java.util.Map> dataList = (List<java.util.Map>) JSONArray.parse(resultMap.get("data").toString());
                                                    if (dataList != null && !dataList.isEmpty()) {
                                                        for (java.util.Map<String, Object> data : dataList) {
                                                            if (Integer.parseInt(data.get("type").toString()) == 0) { // 只处理type=0的数据，即用户绘制数据
                                                                DrawPointLinePolygonEntity entity = new DrawPointLinePolygonEntity();
                                                                if (data.get("uuid") == null) {
                                                                    continue;
                                                                }
                                                                entity.set_id(data.get("uuid").toString());
                                                                entity.setUserName(data.get("userName").toString());
                                                                if (data.get("wkt") == null) {
                                                                    continue;
                                                                }
                                                                entity.setGeometry(data.get("wkt").toString());
                                                                entity.setName(data.get("name").toString());
                                                                entity.setProjectId(SystemConstant.CURRENT_PROJECTS_ID);
                                                                if (!RxDataTool.isEmpty(data.get("prop"))) {
                                                                    java.util.Map<String, Object> propMap = (java.util.Map<String, Object>) JSON.parse(data.get("prop").toString());
                                                                    if (propMap.containsKey("img") && propMap.get("img") != null) {
                                                                        List<String> imgList = Arrays.asList(propMap.get("img").toString().split(";"));
                                                                        entity.setImgUrlList(imgList);
                                                                    }
                                                                    if (propMap.containsKey("remark")) {
                                                                        entity.setRemark(propMap.get("remark").toString());
                                                                    }
                                                                }
                                                                entity.setUpload(true);
                                                                try {
                                                                    DrawPointLinePolygonListFragment.this.dbManager.saveOrUpdate(entity);
                                                                } catch (DbException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }
                                                        listData.clear();
                                                        page = 0;
                                                        List<DrawPointLinePolygonEntity> dbEntityList = null;
                                                        try {
                                                            dbEntityList = dbManager.selector(DrawPointLinePolygonEntity.class).where("projectId", "=", SystemConstant.CURRENT_PROJECTS_ID).limit(PAGE_SIZE).offset(page * PAGE_SIZE).orderBy("_id", false).findAll();
                                                        } catch (DbException e) {
                                                            e.printStackTrace();
                                                        }
                                                        if (dbEntityList != null && !dbEntityList.isEmpty()) {
                                                            listData.addAll(dbEntityList);
                                                        }
                                                        adapter.notifyDataSetChanged();

                                                        //通知地图重新绘制数据
                                                        Message msg = Message.obtain();
                                                        msg.what = SystemConstant.MSG_WHAT_REDRAW_USER_DRAW_DATA;
                                                        EventBus.getDefault().post(msg);
                                                    }
                                                } else {
                                                    RxToast.error("无法获取数据！");
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        rxDialogLoading.dismiss();
                                    }

                                    @Override
                                    public void onComplete() {
                                        rxDialogLoading.dismiss();
                                    }
                                });
                    }
                });

            }
        });

        atv_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedListData == null || checkedListData.isEmpty()) {
                    RxToast.error("请至少勾选一条数据");
                    return;
                }
                final RxDialogSureCancel rxDialogSureCancel = new RxDialogSureCancel(getActivity());
                View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_export_customer_data, null);
                rxDialogSureCancel.setContentView(rootView);
                rxDialogSureCancel.show();
                final AppCompatSpinner spinnerFormate = rootView.findViewById(R.id.spn_file_format);
                final EditText edt_fileName = rootView.findViewById(R.id.edt_export_file_name);
//                rxDialogSureCancel.setCancel("取消");
                rxDialogSureCancel.findViewById(R.id.btn_export_shp_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        rxDialogSureCancel.dismiss();
                    }
                });
//                rxDialogSureCancel.setSure("确定");
                rxDialogSureCancel.findViewById(R.id.btn_export_shp_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String fileName = edt_fileName.getText().toString().trim();
                        if (fileName.equals("")) {
                            RxToast.error("文件名不能为空！");
                            return;
                        }
                        if (".json".equals(spinnerFormate.getSelectedItem().toString())) {
                            File saveFile = new File(SystemConstant.CACHE_EXPORT_GEOJSON_PATH + File.separator + fileName + ".geojson");
                            if (!saveFile.getParentFile().exists()) {
                                saveFile.getParentFile().mkdirs();
                            }
                            if (saveFile.exists()) {
                                RxToast.error("存在同名文件，请重新命名！");
                                return;
                            }
                            FeatureCollection featureCollection = new FeatureCollection();
                            for (DrawPointLinePolygonEntity entity : checkedListData) {
                                Feature feature = GeometryTools.wkt2Feature(GeometryTools.createGeometry(entity.getGeometry()));
                                feature.setIdentifier(entity.get_id());
                                JSONObject prop = new JSONObject();
                                try {
                                    prop.put("remark", entity.getRemark());
                                    prop.put("userName", entity.getUserName());
                                    prop.put("id", entity.get_id());
                                    prop.put("img", entity.getImgUrlListStr());
                                    prop.put("name", entity.getName());
                                    prop.put("projectId", entity.getProjectId());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                feature.setProperties(prop);
                                featureCollection.addFeature(feature);
                            }
                            try {
                                JSONObject jsonObject = featureCollection.toJSON();
                                jsonObject.put("crs", new JSONObject("{ \"type\": \"name\", \"properties\": { \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\" } }"));
                                jsonObject.put("name", fileName);
                                RxFileTool.write(saveFile.getAbsolutePath(), jsonObject.toString());
                                RxToast.info("保存成功：" + saveFile.getAbsolutePath());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else { // 导出为shp文件
                            // 分别筛选勾选的数据中的点、线、面数据
                            List<DrawPointLinePolygonEntity> pointEntityList = new ArrayList<>(), lineEntityList = new ArrayList<>(), polygonEntityList = new ArrayList<>();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                            for (DrawPointLinePolygonEntity entity : checkedListData) {
                                Geometry geometry = GeometryTools.createGeometry(entity.getGeometry());
                                if (GeometryTools.POINT_GEOMETRY_TYPE.equals(geometry.getGeometryType())) {
                                    pointEntityList.add(entity);
                                } else if (GeometryTools.LINE_GEOMETRY_TYPE.equals(geometry.getGeometryType())) {
                                    lineEntityList.add(entity);
                                } else if (GeometryTools.POLYGON_GEOMETRY_TYPE.equals(geometry.getGeometryType())) {
                                    polygonEntityList.add(entity);
                                }
                            }
                            if (!pointEntityList.isEmpty()) {
                                StringBuilder fileNameBuilder = new StringBuilder(SystemConstant.CACHE_EXPORT_SHP_PATH).append(File.separator).append(fileName).append("_point").append(sdf.format(new Date())).append(".shp");
                                File saveFile = new File(fileNameBuilder.toString());
                                if (!saveFile.getParentFile().exists()) {
                                    saveFile.getParentFile().mkdirs();
                                }
                                try {
                                    ShpFileUtil.writeShp(saveFile, GeometryTools.POINT_GEOMETRY_TYPE, pointEntityList);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                RxToast.info("保存成功,文件保存在:" + saveFile.getParent());
                            }
                            if (!lineEntityList.isEmpty()) {
                                StringBuilder fileNameBuilder = new StringBuilder(SystemConstant.CACHE_EXPORT_SHP_PATH).append(File.separator).append(fileName).append("_line").append(sdf.format(new Date())).append(".shp");
                                File saveFile = new File(fileNameBuilder.toString());
                                if (!saveFile.getParentFile().exists()) {
                                    saveFile.getParentFile().mkdirs();
                                }
                                try {
                                    ShpFileUtil.writeShp(saveFile, GeometryTools.LINE_GEOMETRY_TYPE, lineEntityList);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                RxToast.info("保存成功,文件保存在:" + saveFile.getParent());
                            }
                            if (!polygonEntityList.isEmpty()) {
                                StringBuilder fileNameBuilder = new StringBuilder(SystemConstant.CACHE_EXPORT_SHP_PATH).append(File.separator).append(fileName).append("_polygon").append(sdf.format(new Date())).append(".shp");
                                File saveFile = new File(fileNameBuilder.toString());
                                if (!saveFile.getParentFile().exists()) {
                                    saveFile.getParentFile().mkdirs();
                                }
                                try {
                                    ShpFileUtil.writeShp(saveFile, GeometryTools.POLYGON_GEOMETRY_TYPE, polygonEntityList);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                RxToast.info("保存成功,文件保存在:" + saveFile.getParent());
                            }
                        }
                        rxDialogSureCancel.dismiss();
                    }
                });
            }
        });
    }


    private void write2ShpFile(String fileName, String geometryType) {
        try {
            if (fileName == null || "".equals(fileName)) {
                RxToast.error("文件名不能为空！");
                return;
            }
            if (geometryType == null || "".equals(geometryType)) {
                RxToast.error("文件类型不能为空！");
                return;
            }
            File saveFile = null;
            if (GeometryTools.POINT_GEOMETRY_TYPE.equals(geometryType)) {
                saveFile = new File(SystemConstant.CACHE_EXPORT_GEOJSON_PATH + File.separator + fileName + "_point" + ".shp");
            } else if (GeometryTools.LINE_GEOMETRY_TYPE.equals(geometryType)) {
                saveFile = new File(SystemConstant.CACHE_EXPORT_GEOJSON_PATH + File.separator + fileName + "_line" + ".shp");
            } else if (GeometryTools.LINE_GEOMETRY_TYPE.equals(geometryType)) {
                saveFile = new File(SystemConstant.CACHE_EXPORT_GEOJSON_PATH + File.separator + fileName + "_polygon" + ".shp");
            }
            if (saveFile != null) {
                java.util.Map<String, Serializable> params = new HashMap<String, Serializable>();
                params.put(ShapefileDataStoreFactory.URLP.key, saveFile.toURI().toURL());
                ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
                //定义图形信息和属性信息
                SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
                tb.setCRS(DefaultGeographicCRS.WGS84);
                tb.setName(fileName);
                tb.add("id", String.class);
                tb.add("name", String.class);
                tb.add("userName", String.class);
                tb.add("remark", String.class);
                tb.add("img", String.class);
                tb.add("projectId", String.class);
                if (GeometryTools.POINT_GEOMETRY_TYPE.equals(geometryType)) {
                    tb.add("geometry", Point.class);
                } else if (GeometryTools.LINE_GEOMETRY_TYPE.equals(geometryType)) {
                    tb.add("geometry", LineString.class);
                } else if (GeometryTools.LINE_GEOMETRY_TYPE.equals(geometryType)) {
                    tb.add("geometry", Polygon.class);
                }
                ds.createSchema(tb.buildFeatureType());
                ds.setCharset(Charset.forName("UTF-8"));
                //设置Writer
                FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(ds.getTypeNames()[0], Transaction.AUTO_COMMIT);
                for (DrawPointLinePolygonEntity entity : checkedListData) {
                    Geometry geometry = GeometryTools.createGeometry(entity.getGeometry());
                    SimpleFeature feature = writer.next();
                    feature.setAttribute("id", entity.get_id());
                    feature.setAttribute("name", entity.getName());
                    feature.setAttribute("userName", entity.getUserName());
                    feature.setAttribute("remark", entity.getRemark());
                    feature.setAttribute("img", entity.getImgUrlListStr());
                    feature.setAttribute("projectId", entity.getProjectId());
                    if (geometryType.equals(geometry.getGeometryType())) {
                        feature.setAttribute("geometry", geometry);
                    }
                    writer.write();
                }
                writer.close();
                ds.dispose();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class DrawPointLinePolygonAdapter extends RecyclerView.Adapter<DrawPointLinePolygonAdapter.ViewHolder> {
        private List<DrawPointLinePolygonEntity> listData;
        private Context mContext;

        public DrawPointLinePolygonAdapter(Context mContext, List<DrawPointLinePolygonEntity> listData) {
            this.listData = listData;
            this.mContext = mContext;
        }


        @NonNull
        @Override
        public DrawPointLinePolygonAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_draw_point_line_polygon_dblist, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder(rootView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull DrawPointLinePolygonAdapter.ViewHolder viewHolder, int index) {
            final int i = index;
            viewHolder.tv_name.setText(listData.get(i).getName());
            viewHolder.chk_name.setOnCheckedChangeListener(null);
            viewHolder.chk_name.setChecked(checkedListData.contains(listData.get(i))); // 设置当前勾选框是否选中
            viewHolder.tv_isUpload.setText(listData.get(i).isUpload() ? "已同步" : " 未同步");
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DrawPointLinePolygonEntity polygonEntity = listData.get(i);
                    String geometryType = GeometryTools.createGeometry(listData.get(i).getGeometry()).getGeometryType();
                    if (geometryType == "Point") {
                        DrawPointLinePolygonDialog.getInstance(getActivity()).showDialog(DRAW_STATE.DRAW_POINT, polygonEntity);
                    } else if (geometryType == "LineString") {
                        DrawPointLinePolygonDialog.getInstance(getActivity()).showDialog(DRAW_STATE.DRAW_LINE, polygonEntity);
                    } else if (geometryType == "Polygon") {
                        DrawPointLinePolygonDialog.getInstance(getActivity()).showDialog(DRAW_STATE.DRAW_POLYGON, polygonEntity);
                    }

                }
            });
            viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new CanDialog.Builder(getActivity()).setMessage("确定删除吗?").setPositiveButton("确定", true, new CanDialogInterface.OnClickListener() {
                        @Override
                        public void onClick(CanDialog dialog, int checkItem, CharSequence text, boolean[] checkItems) {
                            try {
                                String deleteId = listData.get(i).get_id();
                                // 通知主界面，从地图上删除指定的元素
                                Message msg = Message.obtain();
                                msg.what = SystemConstant.MSG_WHAT_DELETE_DRAW_DATA;
                                msg.obj = listData.get(i).getGeometry();
                                EventBus.getDefault().post(msg);

                                DrawPointLinePolygonListFragment.this.dbManager.deleteById(DrawPointLinePolygonEntity.class, listData.get(i).get_id());
                                listData.remove(i);//移除当前数据
                                //删除成功，提示用户
                                RxToast.info(getActivity(), "删除成功！");
                                DrawPointLinePolygonListFragment.DrawPointLinePolygonAdapter.this.notifyDataSetChanged();

//                                OkGo.<String>delete(SystemConstant.DATA_DELETE).params("projectId",SystemConstant.CURRENT_PROJECTS_ID).params("uuid",deleteId).tag(this).converter(new StringConvert())
//                                        .adapt(new ObservableResponse<String>())
//                                        .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<com.lzy.okgo.model.Response<String>>() {
//                                    @Override
//                                    public void accept(com.lzy.okgo.model.Response<String> stringResponse) throws Exception {
//                                        //删除成功，提示用户
//                                        RxToast.info(getActivity(), "删除成功！");
//                                        DrawPointLinePolygonListFragment.DrawPointLinePolygonAdapter.this.notifyDataSetChanged();
//                                    }
//                                });
                            } catch (DbException e) {
                                e.printStackTrace();
                            }
                        }
                    }).setNegativeButton("取消", true, null).show();
                }
            });

            viewHolder.btn_location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    highLightPointLayer.removeAllItems();
                    highLightPathLayer.removeAllPathDrawable();
                    highLightPolygonLayer.removeAllPathDrawable();

                    String geometryStr = listData.get(i).getGeometry();
                    Geometry geometry = GeometryTools.createGeometry(geometryStr);
                    if (geometry.getGeometryType() == GeometryTools.POINT_GEOMETRY_TYPE) {
                        mMap.animator().animateTo(500, GeometryTools.createGeoPoint(geometryStr));

                        highLightPointLayer.addItem(new MarkerItem(listData.get(i).getName(), listData.get(i).getRemark(), GeometryTools.createGeoPoint(geometryStr)));
                    } else {
                        mMap.animator().animateTo(500, new BoundingBox(GeometryTools.getGeoPoints(geometryStr)));

                        highLightPointLayer.addItem(new MarkerItem(listData.get(i).getName(), listData.get(i).getRemark(), GeometryTools.getGeoPoints(geometryStr).get(0)));
                        if (geometry.getGeometryType() == GeometryTools.LINE_GEOMETRY_TYPE) {
                            highLightPathLayer.addPathDrawable(GeometryTools.getGeoPoints(geometryStr));
                        } else if (geometry.getGeometryType() == GeometryTools.POLYGON_GEOMETRY_TYPE) {
                            highLightPolygonLayer.addPolygonDrawable(GeometryTools.getGeoPoints(geometryStr));
                        }
                    }
                    mMap.updateMap(true);
                }
            });

            viewHolder.chk_name.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        checkedListData.add(listData.get(i));
//                        checkedSet.add(i);
                        atv_export.setVisibility(View.VISIBLE);
                    } else {
                        if (checkedListData != null && checkedListData.contains(listData.get(i))) {
                            checkedListData.remove(listData.get(i));
                            if (checkedListData.isEmpty()) {
                                atv_export.setVisibility(View.GONE);
                            }
                        }
//                        if (checkedSet!=null&&checkedSet.contains(i)){
//                            checkedSet.remove(i);
//                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            if (listData != null) {
                return listData.size();
            }
            return 0;
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {
            public CheckBox chk_name;//勾选状态
            public TextView tv_isUpload;//是否同步
            public TextView tv_name;//名称
            public BootstrapButton btn_delete;//删除
            public BootstrapButton btn_location;//定位

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                chk_name = itemView.findViewById(R.id.chk_name);
                chk_name.setVisibility(View.VISIBLE);
                tv_isUpload = itemView.findViewById(R.id.tv_isUpload);
                tv_name = itemView.findViewById(R.id.tv_name);
                btn_delete = itemView.findViewById(R.id.btn_delete);
                btn_location = itemView.findViewById(R.id.btn_location);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMap.layers().remove(highLightPointLayer);
        mMap.layers().remove(highLightPathLayer);
        mMap.layers().remove(highLightPolygonLayer);
        mMap.updateMap(true);
    }

    @Override
    public boolean onBackPressedSupport() {
        pop();
        ((MainActivity) getActivity()).hiddenSlidingLayout(true);//同时隐藏右侧面板
        return true;
    }
}
