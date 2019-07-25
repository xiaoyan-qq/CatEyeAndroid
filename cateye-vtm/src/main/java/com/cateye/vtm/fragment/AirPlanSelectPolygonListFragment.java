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
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.AirPlanMultiPolygonLayer;
import com.cateye.vtm.util.CatEyeMapManager;
import com.cateye.vtm.util.LayerUtils;
import com.desmond.ripple.RippleCompat;
import com.desmond.ripple.RippleConfig;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.vividsolutions.jts.geom.Polygon;
import com.vondear.rxtool.RxRecyclerViewDividerTool;
import com.vondear.rxtool.view.RxToast;
import com.vtm.library.tools.GeometryTools;

import org.oscim.core.GeoPoint;
import org.oscim.map.Map;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/8/31.
 * 从数据库中选择polygon的列表fragment
 */

public class AirPlanSelectPolygonListFragment extends BaseDrawFragment {
    private Map mMap;
    private RefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private AirPlanPolygonAdapter adapter;
    private List<AirPlanDBEntity> listData;
    private DbManager dbManager;

    private final int PAGE_SIZE = 10;
    private int page = 0;

    private AirPlanMultiPolygonLayer airPlanDrawLayer;
    private ImageView img_back;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMap = CatEyeMapManager.getMapView().map();
        this.dbManager = ((MainActivity) getActivity()).getDbManager();
    }

    public static BaseFragment newInstance(Bundle bundle) {
        AirPlanSelectPolygonListFragment airPlanDrawFragment = new AirPlanSelectPolygonListFragment();
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        listData = new ArrayList<>();
        adapter = new AirPlanPolygonAdapter(getActivity(), listData);
        recyclerView.setAdapter(adapter);
        //设置 Footer 为 球脉冲 样式
        refreshLayout.setRefreshFooter(new BallPulseFooter(getActivity()).setSpinnerStyle(SpinnerStyle.Scale));
        refreshLayout.setEnableRefresh(false);
        recyclerView.addItemDecoration(new RxRecyclerViewDividerTool(0, 0, 2, 2));
        //默认加载前20条数据
        try {
            List<AirPlanDBEntity> dbEntityList = dbManager.selector(AirPlanDBEntity.class).limit(PAGE_SIZE).offset(page * PAGE_SIZE).orderBy("_id", true).findAll();
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
                    List<AirPlanDBEntity> dbEntityList = dbManager.selector(AirPlanDBEntity.class).limit(PAGE_SIZE).offset(page * PAGE_SIZE).findAll();
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
        airPlanDrawLayer = LayerUtils.getAirPlanDrawLayer(mMap);
        if (airPlanDrawLayer != null && airPlanDrawLayer.getAllPolygonList() != null) {
            airPlanDrawLayer.getAllPolygonList().clear();
            airPlanDrawLayer.update();
            mMap.updateMap(true);
            RxToast.info("已自动清除绘制层所有polygon，请在右侧面板选择要显示的polygon");
        }

        img_back = (ImageView) findViewById(R.id.tv_air_plan_list_back);
//        RippleConfig config = new RippleConfig();
//        config.setIsEnablePalette(true);
//        config.setIsFull(true);
//        config.setIsSpin(true);
//        RippleCompat.apply(img_back, config);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressedSupport();
            }
        });
    }


    private class AirPlanPolygonAdapter extends RecyclerView.Adapter<AirPlanPolygonAdapter.ViewHolder> {
        private List<AirPlanDBEntity> listData;
        private Context mContext;

        public AirPlanPolygonAdapter(Context mContext, List<AirPlanDBEntity> listData) {
            this.listData = listData;
            this.mContext = mContext;
        }


        @NonNull
        @Override
        public AirPlanPolygonAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_air_plan_polygon_dblist, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder(rootView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull AirPlanPolygonAdapter.ViewHolder viewHolder, final int i) {
            viewHolder.tv_polygonName.setText(listData.get(i).getName());
            viewHolder.chk_name.setChecked(false);
            viewHolder.tv_updateTime.setText(listData.get(i).getLastUpdate());
            viewHolder.chk_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v instanceof CheckBox) {
                        Polygon polygon = (Polygon) GeometryTools.createGeometry(listData.get(i).getGeometry());
                        CheckBox chk = (CheckBox) v;
                        if (chk.isChecked()) {
                            //选中指定polygon，将其添加到地图图层上
                            airPlanDrawLayer.addPolygon(listData.get(i));
                        } else {
                            //取消选中，将指定polygon从地图移除
                            airPlanDrawLayer.removePolygonDrawable(polygon);
                        }
                        mMap.updateMap(true);
                        //获取指定polygon,将地图定位到该polygon所在的位置
                        List<GeoPoint> geoPointList = GeometryTools.getGeoPoints(polygon.toString());
                        if (geoPointList != null) {
                            mMap.setMapPosition(geoPointList.get(geoPointList.size() - 1).getLatitude(), geoPointList.get(geoPointList.size() - 1).getLongitude(), mMap.getMapPosition().scale);
                        }
                    }
                }
            });
            viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new CanDialog.Builder(getActivity()).setMessage("确定删除该polygon吗?").setPositiveButton("确定", true, new CanDialogInterface.OnClickListener() {
                        @Override
                        public void onClick(CanDialog dialog, int checkItem, CharSequence text, boolean[] checkItems) {
                            try {
                                ((MainActivity) getActivity()).getDbManager().deleteById(AirPlanDBEntity.class, listData.get(i).getId());
                                listData.remove(i);//移除当前数据

                                //图层上删除已添加的polygon数据
                                Polygon polygon = (Polygon) GeometryTools.createGeometry(listData.get(i).getGeometry());
                                LayerUtils.getAirPlanDrawLayer(mMap).removePolygonDrawable(polygon);
                                //删除成功，提示用户
                                RxToast.info(getActivity(), "删除成功！");
                                AirPlanPolygonAdapter.this.notifyDataSetChanged();
                            } catch (DbException e) {
                                e.printStackTrace();
                            }
                        }
                    }).setNegativeButton("取消", true, null).show();
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
        return true;
    }
}
