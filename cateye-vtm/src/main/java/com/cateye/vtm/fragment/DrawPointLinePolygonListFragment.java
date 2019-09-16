package com.cateye.vtm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.canyinghao.candialog.CanDialog;
import com.canyinghao.candialog.CanDialogInterface;
import com.cateye.android.entity.AirPlanDBEntity;
import com.cateye.android.entity.DrawPointLinePolygonEntity;
import com.cateye.android.entity.UploadRecordEntity;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.CatEyeMapManager;
import com.cateye.vtm.util.LayerStyle;
import com.cateye.vtm.util.SystemConstant;
import com.lzy.okgo.OkGo;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.vividsolutions.jts.geom.Geometry;
import com.vondear.rxtool.RxDataTool;
import com.vondear.rxtool.RxRecyclerViewDividerTool;
import com.vondear.rxtool.RxTextTool;
import com.vondear.rxtool.view.RxToast;
import com.vtm.library.layers.MultiPathLayer;
import com.vtm.library.layers.MultiPolygonLayer;
import com.vtm.library.tools.GeometryTools;

import org.greenrobot.eventbus.EventBus;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.map.Map;
import org.xutils.DbManager;
import org.xutils.common.util.KeyValue;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
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

    private AwesomeTextView atv_upload,atv_download;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMap = CatEyeMapManager.getMapView().map();
        this.dbManager = ((MainActivity) getActivity()).getDbManager();

        //初始化点线面的显示图层
        if (highLightPointLayer == null) {
            //打开该fragment，则自动向地图中添加marker的overlay
            highLightPointLayer = new ItemizedLayer<MarkerItem>(mMap, LayerStyle.getHighLightMarkerSymbol(getActivity()), SystemConstant.LAYER_NAME_DRAW_POINT_HIGHLIGHT);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        listData = new ArrayList<>();
        adapter = new DrawPointLinePolygonAdapter(getActivity(), listData);
        recyclerView.setAdapter(adapter);
        //设置 Footer 为 球脉冲 样式
        refreshLayout.setRefreshFooter(new BallPulseFooter(getActivity()).setSpinnerStyle(SpinnerStyle.Scale));
        refreshLayout.setEnableRefresh(false);
        recyclerView.addItemDecoration(new RxRecyclerViewDividerTool(0, 0, 2, 2));
        //默认加载前20条数据
        try {
            List<DrawPointLinePolygonEntity> dbEntityList = dbManager.selector(DrawPointLinePolygonEntity.class).limit(PAGE_SIZE).offset(page * PAGE_SIZE).orderBy("_id", false).findAll();
            if (dbEntityList != null && !dbEntityList.isEmpty()) {
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
                    List<DrawPointLinePolygonEntity> dbEntityList = dbManager.selector(DrawPointLinePolygonEntity.class).limit(PAGE_SIZE).offset(page * PAGE_SIZE).findAll();
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
                    List<DrawPointLinePolygonEntity> uploadListData=((MainActivity)getActivity()).getDbManager().selector(DrawPointLinePolygonEntity.class).where("isUpload","=",false).findAll();
                    if (uploadListData!=null&&!uploadListData.isEmpty()){
                        Observable.fromIterable(uploadListData).subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread())
                                .map(new Function<DrawPointLinePolygonEntity, DrawPointLinePolygonEntity>() {
                                    @Override
                                    public DrawPointLinePolygonEntity apply(DrawPointLinePolygonEntity drawPointLinePolygonEntity) throws Exception {
                                        if (!RxDataTool.isEmpty(drawPointLinePolygonEntity.getImgUrlList())){
                                            List imgList=drawPointLinePolygonEntity.getImgUrlList();
                                            ListIterator iterator= imgList.listIterator();
                                            while (iterator.hasNext()){
                                                String imgStr= (String) iterator.next();
                                                if (!RxDataTool.isNullString(imgStr)&&!imgStr.startsWith("http://")&&!imgStr.startsWith("https://")){
                                                    // 处理照片，当存在照片时，使用同步方式上传该照片，并且同步更新到本地数据库中
                                                    Response imgUploadResponse=OkGo.<String>post(SystemConstant.IMG_UPLOAD).tag(this).params("projectId",SystemConstant.CURRENT_PROJECTS_ID).params("file",new File(imgStr)).execute();
                                                    String imgUploadResult=imgUploadResponse.body().string();
                                                    if (imgUploadResult!=null){
                                                        java.util.Map resultMap= (java.util.Map) JSON.parse(imgUploadResult);
                                                        if (resultMap!=null&&resultMap.containsKey("errcode")&&resultMap.get("errcode").toString().equals("0")) {
                                                            iterator.set(resultMap.get("data").toString()); // 更新照片路径为网络路径
                                                        }
                                                    }
                                                }
                                            }
                                            drawPointLinePolygonEntity.setImgUrlList(imgList);
                                            ((MainActivity)getActivity()).getDbManager().saveOrUpdate(drawPointLinePolygonEntity); // 更新到数据库中
                                        }
                                        return drawPointLinePolygonEntity;
                                    }
                                })
                                .subscribeOn(Schedulers.computation())
                                .map(new Function<DrawPointLinePolygonEntity, UploadRecordEntity>() {
                                    @Override
                                    public UploadRecordEntity apply(DrawPointLinePolygonEntity drawPointLinePolygonEntity) throws Exception {
                                        if (drawPointLinePolygonEntity!=null){
                                            UploadRecordEntity uploadRecordEntity = new UploadRecordEntity();
                                            uploadRecordEntity.setUuid(drawPointLinePolygonEntity.get_id());
                                            uploadRecordEntity.setName(drawPointLinePolygonEntity.getName());
                                            uploadRecordEntity.setProjectId(drawPointLinePolygonEntity.getProjectId());

                                            if (drawPointLinePolygonEntity.getImgUrlListStr()!=null&&!drawPointLinePolygonEntity.getImgUrlListStr().isEmpty()){
                                                java.util.Map propMap=new HashMap();
                                                propMap.put(SystemConstant.PARAM_PROP_KEY_IMG,drawPointLinePolygonEntity.getImgUrlListStr());
                                                uploadRecordEntity.setProp(JSON.toJSONString(propMap));
                                            }
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
                                        Response uploadDataResponse=OkGo.<String>post(SystemConstant.BATCH_SAVE_WKT).upJson(JSONArray.toJSONString(uploadRecordEntities)).execute();
                                        String result=uploadDataResponse.body().string();
                                        if (result!=null){
                                            java.util.Map resultMap= (java.util.Map) JSON.parse(result);
                                            if (resultMap.containsKey("errcode")&&resultMap.get("errcode").toString().equals("0")) {
                                                // 批量更新所有的数据为已上传
                                                KeyValue keyValue=new KeyValue("isUpload",true);
                                                ((MainActivity)getActivity()).getDbManager().update(DrawPointLinePolygonEntity.class, WhereBuilder.b("1","=","1"),keyValue);
                                            }
                                        }
                                        return null;
                                    }
                                })
                                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<ArrayList<UploadRecordEntity>>() {
                            @Override
                            public void accept(ArrayList<UploadRecordEntity> uploadRecordEntities) throws Exception {

                            }
                        });
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        });
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
        public void onBindViewHolder(@NonNull DrawPointLinePolygonAdapter.ViewHolder viewHolder, final int i) {
            viewHolder.tv_name.setText(listData.get(i).getName());
            viewHolder.chk_name.setChecked(false);
            viewHolder.tv_isUpload.setText(listData.get(i).isUpload()?"已同步":" 未同步");
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DrawPointLinePolygonEntity polygonEntity=listData.get(i);
                    String geometryType = GeometryTools.createGeometry(listData.get(i).getGeometry()).getGeometryType();
                    if (geometryType == "Point"){
                        DrawPointLinePolygonDialog.getInstance(getActivity()).showDialog(DRAW_STATE.DRAW_POINT, polygonEntity);
                    } else if (geometryType == "LineString"){
                        DrawPointLinePolygonDialog.getInstance(getActivity()).showDialog(DRAW_STATE.DRAW_LINE, polygonEntity);
                    } else if (geometryType == "Polygon"){
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
                                // 通知主界面，从地图上删除指定的元素
                                Message msg = Message.obtain();
                                msg.what= SystemConstant.MSG_WHAT_DELETE_DRAW_DATA;
                                msg.obj = listData.get(i).getGeometry();
                                EventBus.getDefault().post(msg);

                                ((MainActivity) getActivity()).getDbManager().deleteById(DrawPointLinePolygonEntity.class, listData.get(i).get_id());
                                listData.remove(i);//移除当前数据

                                //删除成功，提示用户
                                RxToast.info(getActivity(), "删除成功！");
                                DrawPointLinePolygonListFragment.DrawPointLinePolygonAdapter.this.notifyDataSetChanged();
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

                    String geometryStr=listData.get(i).getGeometry();
                    Geometry geometry=GeometryTools.createGeometry(geometryStr);
                    if (geometry.getGeometryType() == GeometryTools.POINT_GEOMETRY_TYPE){
                        mMap.animator().animateTo(500,GeometryTools.createGeoPoint(geometryStr));

                        highLightPointLayer.addItem(new MarkerItem(listData.get(i).getName(),listData.get(i).getRemark(),GeometryTools.createGeoPoint(geometryStr)));
                    } else {
                        mMap.animator().animateTo(500,new BoundingBox(GeometryTools.getGeoPoints(geometryStr)));

                        highLightPointLayer.addItem(new MarkerItem(listData.get(i).getName(),listData.get(i).getRemark(),GeometryTools.getGeoPoints(geometryStr).get(0)));
                        if (geometry.getGeometryType() == GeometryTools.LINE_GEOMETRY_TYPE){
                            highLightPathLayer.addPathDrawable(GeometryTools.getGeoPoints(geometryStr));
                        } else if (geometry.getGeometryType() == GeometryTools.POLYGON_GEOMETRY_TYPE) {
                            highLightPolygonLayer.addPolygonDrawable(GeometryTools.getGeoPoints(geometryStr));
                        }
                    }
                    mMap.updateMap(true);
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
