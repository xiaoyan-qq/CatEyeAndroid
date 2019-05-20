package com.cateye.vtm.util;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.cateye.android.entity.AirPlanDBEntity;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.AirPlanDrawFragment;
import com.cateye.vtm.fragment.AirPlanParamListFragment;
import com.cateye.vtm.fragment.AirPlanSelectPolygonListFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vondear.rxtool.view.RxToast;
import com.vtm.library.layers.MultiPolygonLayer;
import com.vtm.library.tools.GeometryTools;
import com.vtm.library.tools.OverlayerManager;

import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.map.Map;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/12/19.
 * 用来处理航区规划相关功能的集合类
 */

public class AirPlanUtils {
    private BaseFragment mainFragment;
    private Map mMap;
    private static AirPlanUtils instance;
    private View img_chk_set_airplan;

    public static AirPlanUtils getInstance(BaseFragment mainFragment, Map mMap, View img_chk_set_airplan) {
        if (instance == null) {
            instance = new AirPlanUtils(mainFragment, mMap, img_chk_set_airplan);
        }
        return instance;
    }

    private AirPlanUtils(BaseFragment mainFragment, Map mMap, View img_chk_set_airplan) {
        this.mainFragment = mainFragment;
        this.mMap = mMap;
        this.img_chk_set_airplan = img_chk_set_airplan;
    }

    public View.OnClickListener airplanClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.chk_draw_airplan) {//绘制航区
                if (!view.isSelected()) {
                    view.setSelected(true);//设置为选中状态，启动绘制fragment，右侧面板显示开始、上一笔、结束按钮
                    //自动弹出绘制点线面的fragment
                    AirPlanDrawFragment fragment = mainFragment.findFragment(AirPlanDrawFragment.class);
                    //自动弹出绘制点线面的fragment
                    Bundle polygonBundle = new Bundle();
                    polygonBundle.putSerializable(com.cateye.vtm.fragment.DrawPointLinePolygonFragment.DRAW_STATE.class.getSimpleName(), com.cateye.vtm.fragment.DrawPointLinePolygonFragment.DRAW_STATE.DRAW_POLYGON);
                    if (fragment != null) {
                        fragment.setArguments(polygonBundle);
                        mainFragment.start(fragment);
                    } else {
                        mainFragment.loadRootFragment(R.id.layer_main_fragment_right_bottom, AirPlanDrawFragment.newInstance(polygonBundle));
                    }
                } else {
                    AirPlanDrawFragment airPlanDrawFragment = mainFragment.findChildFragment(AirPlanDrawFragment.class);
                    if (airPlanDrawFragment != null) {
                        airPlanDrawFragment.completeDrawAirPlan(true);
                    }
                    view.setSelected(false);//设置为未选中状态
                }
            } else if (view.getId() == R.id.chk_set_airplan) {//设置航区参数,设置飞行航区
                if (!view.isSelected()) {
                    //首先判断当前图层列表中是否存在航区显示的图层
                    MultiPolygonLayer airplanDrawOverlayer = (MultiPolygonLayer) OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.AIR_PLAN_MULTI_POLYGON_DRAW);
                    if (airplanDrawOverlayer == null) {
                        RxToast.warning("当前没有需要编辑参数的航区面");
                        return;
                    }

                    view.setSelected(true);
                    //右侧弹出选中的polygon列表，支持上下拖动调整顺序
                    AirPlanParamListFragment airPlanParamListFragment = (AirPlanParamListFragment) AirPlanParamListFragment.newInstance(new Bundle());
                    ((MainActivity) mainFragment.getActivity()).showSlidingLayout(0.4f, airPlanParamListFragment);

                    if (airplanDrawOverlayer != null) {
                        //绘制多个polygon的图层
                        MultiPolygonLayer airPlanParamLayer = LayerUtils.getAirPlanParamLayer(mMap);
                        ItemizedLayer<MarkerItem> airPlanMarkerLayer=LayerUtils.getAirPlanMarkerLayer(mainFragment.getContext(),mMap);

                        if (OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.AIR_PLAN_MULTI_POLYGON_PARAM_EVENT) == null) {
                            mMap.layers().add(new MapEventsReceiver(mMap, SystemConstant.AIR_PLAN_MULTI_POLYGON_PARAM_EVENT), MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);
                        }
                    }
                } else {
                    view.setSelected(false);
                    //判断当前参数设置图层是否有polygon，如果存在，则弹出对话框提示用户设置参数
                    MultiPolygonLayer airplanParamOverlayer = LayerUtils.getAirPlanParamLayer(mMap);
                    if (airplanParamOverlayer == null || airplanParamOverlayer.getAllPolygonList() == null || airplanParamOverlayer.getAllPolygonList().isEmpty()) {
                        RxToast.warning("没有需要设置参数的航区");
                    } else {
                        //需要设置参数的polygon集合
                        final List<Polygon> polygonList = airplanParamOverlayer.getAllPolygonList();
                    }
                }
            } else if (view.getId() == R.id.img_save_airplan) {//保存航区数据

            } else if (view.getId() == R.id.img_open_airplan) {//打开航区数据
                AirPlanSelectPolygonListFragment airPlanSelectPolygonListFragment = (AirPlanSelectPolygonListFragment) AirPlanSelectPolygonListFragment.newInstance(new Bundle());
                ((MainActivity) mainFragment.getActivity()).showSlidingLayout(0.4f, airPlanSelectPolygonListFragment);
            }
        }
    };

    /**
     * @author : xiaoxiao
     * @version V1.0
     * @ClassName : CatEyeMainFragment
     * @Date : 2018/11/28
     * @Description:
     */
    private class MapEventsReceiver extends Layer implements GestureListener {

        public MapEventsReceiver(Map map) {
            super(map);
        }

        public MapEventsReceiver(Map map, String name) {
            this(map);
            setName(name);
        }

        @Override
        public boolean onGesture(Gesture g, MotionEvent e) {
            if (img_chk_set_airplan.isSelected() && g instanceof Gesture.Tap) {
                GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
                Point geometryPoint = (Point) GeometryTools.createGeometry(p);
                //获取当前绘制layer的所有polygon，检查是否与当前点击点位交叉
                AirPlanMultiPolygonLayer drawPolygonLayer = (AirPlanMultiPolygonLayer) OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.AIR_PLAN_MULTI_POLYGON_DRAW);
                List<Polygon> drawPolygonList = drawPolygonLayer.getAllPolygonList();
                if (drawPolygonList != null && !drawPolygonList.isEmpty()) {//点击的区域与polygon交叉，即为点击到了指定的polygon上
                    List<Polygon> tapPolygonList = new ArrayList<>();
                    for (Polygon polygon : drawPolygonList) {
                        if (polygon.contains(geometryPoint)) {//如果点击的点位在polygon的位置上，则认为需要操作当前polygon
                            tapPolygonList.add(polygon);
                        }
                    }

                    AirPlanParamListFragment airPlanParamListFragment = ((MainActivity) mainFragment.getActivity()).findFragment(AirPlanParamListFragment.class);

                    if (tapPolygonList != null && !tapPolygonList.isEmpty()) {
                        AirPlanMultiPolygonLayer paramPolygonLayer = (AirPlanMultiPolygonLayer) OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.AIR_PLAN_MULTI_POLYGON_PARAM);
                        List<Polygon> paramPolygonList = paramPolygonLayer.getAllPolygonList();
                        if (paramPolygonList != null && !paramPolygonList.isEmpty()) {
                            //第一遍遍历-添加polygon：用户有选中的polygon，遍历此列表，如果没有被绘制到参数设置图层，则添加到该图层，如果存在，则从该图层删除
                            Polygon addPolygon = null;
                            a:
                            for (Polygon tapPolygon : tapPolygonList) {
                                for (Polygon paramPolygon : paramPolygonList) {
                                    //如果已经存在点击对应的polygon，则存在此polygon，跳到下一个polygon判断
                                    if (paramPolygon.equals(tapPolygon)) {
                                        addPolygon = null;
                                        continue a;
                                    }
                                }
                                //如果穷举完所有的参数设置中的polygon
                                if (addPolygon == null) {
                                    addPolygon = tapPolygon;
                                }
                            }

                            //！！！！此前没有添加过该polygon，向地图添加polygon
                            if (addPolygon != null) {
                                AirPlanDBEntity airPlanDBEntity = drawPolygonLayer.getAirPlanDBEntityMap().get(addPolygon.toString());
                                if (airPlanDBEntity!=null){
                                    paramPolygonLayer.addPolygon(airPlanDBEntity);
                                    mMap.updateMap(true);
                                }else {
                                    RxToast.error(mainFragment.getContext().getString(R.string.error_info_cant_find_tap_polygon));
                                }

                                //向侧边栏list添加entity
                                airPlanParamListFragment.addData(airPlanDBEntity);
                                return true;
                            }

                            //第二遍遍历-移除polygon
                            for (Polygon tapPolygon : tapPolygonList) {
                                for (Polygon paramPolygon : paramPolygonList) {
                                    //如果已经存在点击对应的polygon，则存在此polygon，跳到下一个polygon判断
                                    if (paramPolygon.equals(tapPolygon)) {
                                        //右侧面板移除此polygon对应的数据
                                        AirPlanDBEntity airPlanDBEntity = paramPolygonLayer.getAirPlanDBEntityMap().get(tapPolygon.toString());
                                        airPlanParamListFragment.removeData(airPlanDBEntity);

                                        //！！！！曾添加过该polygon，移除polygon
                                        paramPolygonLayer.removePolygonDrawable(paramPolygon);
                                        mMap.updateMap(true);
                                        return true;
                                    }
                                }
                            }
                        } else {//不存在参数设置polygon，则直接添加第一个点击的polygon到参数设置layer上

                            //向侧边栏list添加entity
                            AirPlanDBEntity airPlanDBEntity = drawPolygonLayer.getAirPlanDBEntityMap().get(tapPolygonList.get(0).toString());
                            if (airPlanDBEntity!=null){
                                paramPolygonLayer.addPolygon(airPlanDBEntity);
                                mMap.updateMap(true);

                                //向侧边栏list添加entity
                                airPlanParamListFragment.addData(airPlanDBEntity);
                            }else {
                                RxToast.error(mainFragment.getContext().getString(R.string.error_info_cant_find_tap_polygon));
                            }
                        }

                    } else {//点击的位置不在polygon中，即为设置飞机的起飞位置
                        ItemizedLayer airportLayer = (ItemizedLayer) OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.AIR_PLAN_MARKER_AIR_PORT);
                        airportLayer.removeAllItems();
                        airportLayer.addItem(new MarkerItem("机场", "机场", p));
                        mMap.updateMap(true);
                    }
                }

                return true;
            }
            return false;
        }
    }
}
