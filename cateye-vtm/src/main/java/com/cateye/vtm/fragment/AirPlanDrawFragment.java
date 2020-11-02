package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.canyinghao.candialog.CanDialog;
import com.canyinghao.candialog.CanDialogInterface;
import com.cateye.android.entity.AirPlanDBEntity;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.AirPlanMultiPolygonLayer;
import com.cateye.vtm.util.LayerUtils;
import com.litesuits.common.assist.Check;
import com.vondear.rxtool.RxLogTool;
import com.vondear.rxtool.RxTimeTool;
import com.vondear.rxtool.view.RxToast;
import com.vtm.library.tools.CatEyeMapManager;

import org.oscim.map.Map;
import org.xutils.ex.DbException;

import androidx.annotation.Nullable;

/**
 * Created by xiaoxiao on 2018/8/31.
 */

public class AirPlanDrawFragment extends BaseDrawFragment {
    private Map mMap;

    private ImageView img_airplan_draw/*绘制按钮*/, img_airplan_previous/*上一笔*/, img_airplan_clear/*清空*/;
    protected MapEventsReceiver mapEventsReceiver;//用户操作的回调
    private AirPlanMultiPolygonLayer multiPolygonLayer;//用于展示用户绘制的polygon

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMap = CatEyeMapManager.getInstance().getMapView().map();
    }

    public static BaseFragment newInstance(Bundle bundle) {
        AirPlanDrawFragment airPlanDrawFragment = new AirPlanDrawFragment();
        airPlanDrawFragment.setArguments(bundle);
        return airPlanDrawFragment;
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_layer_airplan_draw;
    }

    @Override
    public void initView(View rootView) {
        img_airplan_draw = rootView.findViewById(R.id.img_air_plan_draw);
        img_airplan_previous = rootView.findViewById(R.id.img_air_plan_previous);
        img_airplan_clear = rootView.findViewById(R.id.img_air_plan_clear);

        clearDrawLayers();
        img_airplan_clear.setEnabled(true);
        img_airplan_previous.setEnabled(true);

        img_airplan_draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!view.isSelected()) {
                    //开始绘制
                    view.setSelected(true);
                    currentDrawState = DRAW_STATE.DRAW_POLYGON;
                    //初始化绘制图层
                    initDrawLayers();
                } else {
                    completeDrawAirPlan(false);
                }
            }
        });


        img_airplan_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (polygonOverlay == null) {
                    RxToast.warning("绘制状态下才可以操作上一笔");
                }
                if (currentDrawState != DRAW_STATE.DRAW_NONE) {
                    if (markerLayer.getItemList() == null || markerLayer.getItemList().isEmpty()) {
                        RxToast.info("没有需要清除的点!", Toast.LENGTH_SHORT);
                        return;
                    }
                    if (markerLayer.getItemList() != null && !markerLayer.getItemList().isEmpty()) {
                        markerLayer.removeItem(markerLayer.getItemList().size() - 1);
//                        markerLayer.map().updateMap(true);//重绘
                        markerLayer.update();
                    }
                    if (currentDrawState == DRAW_STATE.DRAW_LINE) {//绘制线
                        if (polylineOverlay.getPoints() != null && !polylineOverlay.getPoints().isEmpty()) {
                            polylineOverlay.getPoints().remove(polylineOverlay.getPoints().size() - 1);
                            redrawPolyline(polylineOverlay);
                        }
                    } else if (currentDrawState == DRAW_STATE.DRAW_POLYGON) {//绘制面
                        if (polygonOverlay.getPoints() != null && !polygonOverlay.getPoints().isEmpty()) {
                            polygonOverlay.getPoints().remove(polygonOverlay.getPoints().size() - 1);
                            redrawPolygon(polygonOverlay);
                        }
                    }
                }
            }
        });

        img_airplan_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (polygonOverlay == null) {
                    RxToast.warning("绘制状态下才可以操作清空");
                }
                if (markerLayer.getItemList() == null || markerLayer.getItemList().isEmpty()) {
                    RxToast.info("没有需要清除的点!", Toast.LENGTH_SHORT);
                    return;
                }
                if (markerLayer.getItemList() != null && !markerLayer.getItemList().isEmpty()) {
                    markerLayer.removeAllItems();
                    markerLayer.map().updateMap(true);//重绘
                }
                if (currentDrawState == DRAW_STATE.DRAW_LINE) {
                    polylineOverlay.getPoints().clear();
                    redrawPolyline(polylineOverlay);
                } else if (currentDrawState == DRAW_STATE.DRAW_POLYGON) {
                    polygonOverlay.getPoints().clear();
                    redrawPolygon(polygonOverlay);
                }
            }
        });

        //添加一个操作图层，监听用户在地图上的点击事件
        mapEventsReceiver = new MapEventsReceiver(CatEyeMapManager.getInstance().getCatEyeMap());
        CatEyeMapManager.getInstance().getCatEyeMap().layers().add(mapEventsReceiver, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);

        //如果当前地图不存在multiPolygon的图层，则自动生成添加到地图上
        multiPolygonLayer = LayerUtils.getAirPlanDrawLayer(mMap);
    }

    //绘制结束一个polygon
    public void completeDrawAirPlan(final boolean isPop/*标识是否编辑结束后回退当前fragment*/) {
        if (img_airplan_draw != null) {
            img_airplan_draw.setSelected(false);
        }
        currentDrawState = DRAW_STATE.DRAW_NONE;
        if (polygonOverlay != null) {
            if (polygonOverlay.getPoints() == null || polygonOverlay.getPoints().isEmpty()) {
                //复制点位到展示图层，则清除绘制面的所有数据
                clearDrawLayers();
                if (isPop) {
                    pop();
                }
            } else if (polygonOverlay.getPoints().size() >= 3) {
                //绘制结束，提示用户设置该polygon的参数，弹出对话框
                //弹出参数设置对话框
                final View airPlanRootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_air_plan_set_param, null);
                new CanDialog.Builder(getActivity()).setView(airPlanRootView).setNeutralButton("取消", true, new CanDialogInterface.OnClickListener() {
                    @Override
                    public void onClick(CanDialog dialog, int checkItem, CharSequence text, boolean[] checkItems) {
                        clearDrawLayers();
                        if (isPop) {
                            pop();
                        }
                    }
                }).setPositiveButton("确定", true, new CanDialogInterface.OnClickListener() {
                    @Override
                    public void onClick(CanDialog dialog, int checkItem, CharSequence text, boolean[] checkItems) {
                        //用户点击确定，首先检查用户输入的内容是否合规
                        BootstrapEditText edt_name = airPlanRootView.findViewById(R.id.edt_air_plan_name);//名称
                        BootstrapEditText edt_altitude = airPlanRootView.findViewById(R.id.edt_air_plan_altitude);//海拔
                        BootstrapEditText edt_seqnum = airPlanRootView.findViewById(R.id.edt_air_plan_seqnum);//顺序
                        BootstrapEditText edt_describe = airPlanRootView.findViewById(R.id.edt_air_plan_describe);//描述

                        String altitude = edt_altitude.getText().toString();
                        if (Check.isEmpty(altitude)) {
                            RxToast.error("海拔数据不能为空");
                            return;
                        }

                        String name = edt_name.getText().toString();
                        if (Check.isEmpty(name)) {
                            RxToast.error("名称不能为空");
                            return;
                        }

                        //自动保存用户输入的参数和Polygon保存到数据库中
                        AirPlanDBEntity entity = new AirPlanDBEntity();
                        entity.setName(name);
                        entity.setAltitude(Integer.parseInt(altitude));
                        String currentTime = RxTimeTool.getCurTimeString();
                        entity.setLastUpdate(currentTime);
                        entity.setDescriptor(edt_describe.getText().toString());
                        entity.setGeometry(polygonOverlay.getPolygon().toString());

                        try {
                            ((MainActivity) AirPlanDrawFragment.this.getActivity()).getDbManager().saveBindingId(entity);
                            RxToast.success("保存polygon成功!");

                            //绘制结束，将绘制的数据添加到airplan的图层内
                            multiPolygonLayer.addPolygon(entity);
                            //复制点位到展示图层，则清除绘制面的所有数据
                            clearDrawLayers();
                            if (isPop) {
                                pop();
                            }
                        } catch (DbException e) {
                            e.printStackTrace();
                            RxToast.error("保存polygon失败!");
                            RxLogTool.saveLogFile("保存航线polygon到数据库失败：" + e.toString());
                            //保存失败
                            clearDrawLayers();
                            if (isPop) {
                                pop();
                            }
                        }
                    }
                }).show();

            } else {
                RxToast.warning("绘制的点无法组成面！");
            }
        } else {
            clearDrawLayers();
            if (isPop) {
                pop();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapEventsReceiver != null) {
            mMap.layers().remove(mapEventsReceiver);
        }
    }
}
