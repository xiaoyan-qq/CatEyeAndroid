package com.cateye.vtm.fragment;

import android.content.Context;
import android.os.Bundle;
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

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.canyinghao.candialog.CanDialog;
import com.canyinghao.candialog.CanDialogInterface;
import com.cateye.android.entity.AirPlanDBEntity;
import com.cateye.android.entity.TravelLocation;
import com.cateye.android.entity.TravelRecord;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.AirPlanMultiPolygonLayer;
import com.cateye.vtm.util.CatEyeMapManager;
import com.cateye.vtm.util.LayerUtils;
import com.cateye.vtm.util.SystemConstant;
import com.cateye.vtm.util.TrailRecordMultiPathLayer;
import com.desmond.ripple.RippleCompat;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.vividsolutions.jts.geom.Polygon;
import com.vondear.rxtool.RxRecyclerViewDividerTool;
import com.vondear.rxtool.view.RxToast;
import com.vondear.rxui.view.dialog.RxDialogShapeLoading;
import com.vtm.library.tools.GeometryTools;
import com.vtm.library.tools.OverlayerManager;

import org.oscim.core.GeoPoint;
import org.oscim.map.Map;
import org.oscim.map.Viewport;
import org.xutils.DbManager;
import org.xutils.db.sqlite.SqlInfo;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xiaoxiao on 2018/8/31.
 * 从数据库中选择polygon的列表fragment
 */

public class TrailRecordListFragment extends BaseDrawFragment {
    private Map mMap;
    private RefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private TrailRecordAdapter adapter;
    private List<TravelRecord> listData;
    private DbManager dbManager;

    private final int PAGE_SIZE = 20;
    private int page = 0;

    private TrailRecordMultiPathLayer trailRecordMultiPathLayer;
    private ImageView img_back;
    private RxDialogShapeLoading shapeLoading;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMap = CatEyeMapManager.getMapView().map();
        this.dbManager = ((MainActivity) getActivity()).getDbManager();
        this.shapeLoading = new RxDialogShapeLoading(getActivity());
    }

    public static BaseFragment newInstance(Bundle bundle) {
        TrailRecordListFragment trailRecordListFragment = new TrailRecordListFragment();
        trailRecordListFragment.setArguments(bundle);
        return trailRecordListFragment;
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_air_plan_polygon_list;
    }

    @Override
    public void initView(View rootView) {
        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        recyclerView = rootView.findViewById(R.id.rv_air_plan_polygon);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        listData = new ArrayList<>();
        adapter = new TrailRecordAdapter(getActivity(), listData);
        recyclerView.setAdapter(adapter);
        //设置 Footer 为 球脉冲 样式
        refreshLayout.setRefreshFooter(new BallPulseFooter(getActivity()).setSpinnerStyle(SpinnerStyle.Scale));
        refreshLayout.setEnableRefresh(false);
        recyclerView.addItemDecoration(new RxRecyclerViewDividerTool(0, 0, 2, 2));
        //默认加载前20条数据
        try {
            List<TravelRecord> dbEntityList = dbManager.selector(TravelRecord.class).limit(PAGE_SIZE).offset(page * PAGE_SIZE).orderBy("_id", true).findAll();
            if (dbEntityList != null && !dbEntityList.isEmpty()) {
                listData.addAll(dbEntityList);
            } else {
                RxToast.warning("没有存储的polygon数据");
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
                    List<TravelRecord> dbEntityList = dbManager.selector(TravelRecord.class).limit(PAGE_SIZE).offset(page * PAGE_SIZE).findAll();
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
        //初始化该列表时，自动清除此前绘制的polygon，由用户通过勾选来添加或删除polygon
        trailRecordMultiPathLayer = LayerUtils.getTrailRecordLayer(mMap);
        if (trailRecordMultiPathLayer != null) {
            trailRecordMultiPathLayer.clearPath();
            trailRecordMultiPathLayer.clearAllPath();
            trailRecordMultiPathLayer.update();
            mMap.updateMap(true);
        }

        img_back = (ImageView) findViewById(R.id.tv_air_plan_list_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressedSupport();
            }
        });
    }


    private class TrailRecordAdapter extends RecyclerView.Adapter<TrailRecordAdapter.ViewHolder> {
        private List<TravelRecord> listData;
        private Context mContext;

        public TrailRecordAdapter(Context mContext, List<TravelRecord> listData) {
            this.listData = listData;
            this.mContext = mContext;
        }


        @NonNull
        @Override
        public TrailRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_travel_record, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder(rootView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final TrailRecordAdapter.ViewHolder viewHolder, final int i) {
            viewHolder.tv_polygonName.setText(listData.get(i).getTravelName());
            viewHolder.chk_name.setChecked(false);
            viewHolder.tv_updateTime.setText(listData.get(i).getsTime() + "-" + listData.get(i).geteTime());
            viewHolder.chk_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v instanceof CheckBox) {
                        CheckBox chk = (CheckBox) v;
                        if (chk.isChecked()) {
                            //根据用户记录的轨迹时间读取轨迹数据
                            Observable.create(new ObservableOnSubscribe<List<TravelLocation>>() {
                                @Override
                                public void subscribe(ObservableEmitter<List<TravelLocation>> emitter) throws Exception {
                                    List<TravelLocation> travelLocationList = dbManager.selector(TravelLocation.class).where(WhereBuilder.b().and("locationTime", "<=", listData.get(i).geteTime()).and("locationTime", ">=", listData.get(i).getsTime())).findAll();
                                    if (travelLocationList != null && !travelLocationList.isEmpty()) {
                                        emitter.onNext(travelLocationList);
                                    } else {
                                        emitter.onError(new Throwable("没有对应的轨迹数据！"));
                                    }
                                    emitter.onComplete();
                                }
                            }).subscribeOn(Schedulers.io()).map(new Function<List<TravelLocation>, List<GeoPoint>>() {
                                @Override
                                public List<GeoPoint> apply(List<TravelLocation> travelLocationList) throws Exception {
                                    List<GeoPoint> geoPointList = new ArrayList<>();
                                    for (TravelLocation travelLocation : travelLocationList) {
                                        geoPointList.add(GeometryTools.createGeoPoint(travelLocation.getGeometry()));
                                    }
                                    return geoPointList;
                                }
                            }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<GeoPoint>>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                    shapeLoading.show();
                                }

                                @Override
                                public void onNext(List<GeoPoint> geoPoints) {
                                    //显示指定数据到layer
                                    trailRecordMultiPathLayer.addPathDrawable(listData.get(i).getsTime() + "-" + listData.get(i).geteTime(), geoPoints);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    RxToast.warning("该时间段内没有可显示的轨迹数据！");
                                }

                                @Override
                                public void onComplete() {
                                    shapeLoading.dismiss();
                                }
                            });
                        } else {
                            //取消选中，将指定polygon从地图移除
                            trailRecordMultiPathLayer.removeTrailRecordDrawable(listData.get(i).getsTime() + "-" + listData.get(i).geteTime());
                        }
                        mMap.updateMap(true);
                    }
                }
            });
//            viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    new CanDialog.Builder(getActivity()).setMessage("确定删除该轨迹吗?").setPositiveButton("确定", true, new CanDialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(CanDialog dialog, int checkItem, CharSequence text, boolean[] checkItems) {
//                            try {
//                                ((MainActivity) getActivity()).getDbManager().deleteById(AirPlanDBEntity.class, listData.get(i).getId());
//                                listData.remove(i);//移除当前数据
//
//                                //图层上删除已添加的polygon数据
//                                Polygon polygon = (Polygon) GeometryTools.createGeometry(listData.get(i).getGeometry());
//                                LayerUtils.getAirPlanDrawLayer(mMap).removePolygonDrawable(polygon);
//                                //删除成功，提示用户
//                                RxToast.info(getActivity(), "删除成功！");
//                                TrailRecordListFragment.AirPlanPolygonAdapter.this.notifyDataSetChanged();
//                            } catch (DbException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }).setNegativeButton("取消", true, null).show();
//                }
//            });
            //单击item，支持修改轨迹名称
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new CanDialog.Builder(getActivity()).setTitle("修改名称").setEditDialog("请输入名称", false, 0, 0).setCancelable(true)
                            .setNegativeButton("取消", true, null)
                            .setPositiveButton("确定", true, new CanDialogInterface.OnClickListener() {
                                @Override
                                public void onClick(CanDialog dialog, int checkItem, CharSequence text, boolean[] checkItems) {
                                    //同时将该名称保存到数据库中
                                    try {
                                        TravelRecord travelRecord = listData.get(i);
                                        travelRecord.setTravelName(text.toString());
                                        dbManager.saveOrUpdate(travelRecord);
                                        RxToast.info("保存成功！");
                                        viewHolder.tv_polygonName.setText(text);
                                    } catch (DbException e) {
                                        e.printStackTrace();
                                        RxToast.error("保存失败！");
                                    }
                                }
                            }).show();
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
            public CheckBox chk_name;//polygon的勾选状态
            public TextView tv_updateTime;//最后更新时间
            public TextView tv_polygonName;//polygon名称
            public BootstrapButton btn_delete;//删除

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                chk_name = itemView.findViewById(R.id.chk_polygon_name);
                tv_updateTime = itemView.findViewById(R.id.tv_polygon_updatetime);
                tv_polygonName = itemView.findViewById(R.id.tv_polygon_name);
                btn_delete = itemView.findViewById(R.id.btn_polygon_delete);

                RippleCompat.apply(btn_delete, R.color.gray);
                RippleCompat.apply(itemView, R.color.gray);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public boolean onBackPressedSupport() {
        pop();
        ((MainActivity) getActivity()).hiddenSlidingLayout(true);//同时隐藏右侧面板
        //回退时同时移除layer
        mMap.layers().remove(OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.TRAIL_LOCATION_RECORD));
        mMap.updateMap(true);
        return true;
    }
}
