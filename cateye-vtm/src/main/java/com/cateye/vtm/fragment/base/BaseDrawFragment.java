package com.cateye.vtm.fragment.base;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cateye.android.vtm.MainActivity;
import com.cateye.vtm.util.LayerStyle;
import com.cateye.vtm.util.SystemConstant;
import com.vtm.library.layers.PolygonLayer;
import com.vtm.library.tools.CatEyeMapManager;

import org.greenrobot.eventbus.EventBus;
import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.vector.PathLayer;
import org.oscim.map.Map;

import java.io.Serializable;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * Created by xiaoxiao on 2018/5/24.
 * 地图绘制形状的父类，需要绘制点线面时，可继承该fragment
 */

public class BaseDrawFragment extends BaseFragment {

    protected DRAW_STATE currentDrawState = DRAW_STATE.DRAW_NONE;
    protected Map mMap;

    //overLayer图层
    protected ItemizedLayer markerLayer;
    protected PathLayer polylineOverlay;
    protected PolygonLayer polygonOverlay;

    @Override
    public int getFragmentLayoutId() {
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMap = CatEyeMapManager.getInstance().getCatEyeMap();

        initDrawLayers();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * 添加绘制所需要的图层
     */
    public void initDrawLayers() {
        if (markerLayer == null) {
            //打开该fragment，则自动向地图中添加marker的overlay
            markerLayer = new ItemizedLayer(mMap, LayerStyle.getDefaultMarkerSymbol(getActivity()));
            mMap.layers().add(markerLayer, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);
        }

        if (polylineOverlay == null) {
            //自动添加pathLayer
            polylineOverlay = new PathLayer(CatEyeMapManager.getInstance().getCatEyeMap(), LayerStyle.getDefaultLineStyle());
            mMap.layers().add(polylineOverlay, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);
        }

        if (polygonOverlay == null) {
            polygonOverlay = new PolygonLayer(CatEyeMapManager.getInstance().getCatEyeMap(), LayerStyle.getDefaultPolygonStyle());
            mMap.layers().add(polygonOverlay, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);
        }
    }

    /**
     * 清空绘制图层
     */
    public void clearDrawLayers() {
        if (markerLayer != null) {
            mMap.layers().remove(markerLayer);
        }
        if (polylineOverlay != null) {
            mMap.layers().remove(polylineOverlay);
        }
        if (polygonOverlay != null) {
            mMap.layers().remove(polygonOverlay);
        }
        mMap.updateMap(true);

        markerLayer = null;
        polylineOverlay = null;
        polygonOverlay = null;
    }

    @Override
    public void initView(View rootView) {
    }

    /**
     * Author : xiaoxiao
     * Describe : 获取当前的绘制状态
     * param :
     * return : 返回绘制状态的枚举类型，如果为NONE则当前没有进行绘制
     * Date : 2018/3/22
     */
    public DRAW_STATE getCurrentDrawState() {
        return currentDrawState;
    }

    public enum DRAW_STATE implements Serializable {
        DRAW_NONE("DRAW_NONE"), DRAW_POINT("DRAW_POINT"), DRAW_LINE("DRAW_LINE"), DRAW_POLYGON("DRAW_POLYGON");

        DRAW_STATE(String name) {
            this.name = name;
        }

        private String name;

        public String getName() {
            return name;
        }
    }

    //根据polyline的点位自动绘制线
    protected void redrawPolyline(PathLayer polylineOverlay) {
        if (polylineOverlay.getPoints() != null && polylineOverlay.getPoints().size() > 1) {
            polylineOverlay.setLineString(getPointArray(polylineOverlay.getPoints()));
        } else if (polylineOverlay.getPoints() != null && polylineOverlay.getPoints().size() == 1) {
            GeoPoint firstPoint = polylineOverlay.getPoints().get(0);
            polylineOverlay.clearPath();
            polylineOverlay.addPoint(firstPoint);
        } else {
            polylineOverlay.clearPath();
        }
        polylineOverlay.update();
    }

    //根据polygon的点位自动绘制线或者面
    protected void redrawPolygon(PolygonLayer polygonOverlay) {
        if (polygonOverlay.getPoints() != null) {
            if (polygonOverlay.getPoints().size() > 2) {
                polygonOverlay.setPolygonString(polygonOverlay.getPoints(), true);
            } else if (polygonOverlay.getPoints().size() == 2) {
                polygonOverlay.removeCurrentPolygonDrawable();
                polygonOverlay.setLineString(getPointArray(polygonOverlay.getPoints()));
            } else if (polygonOverlay.getPoints() != null && polygonOverlay.getPoints().size() == 1) {
                polygonOverlay.removeCurrentPolygonDrawable();
                polygonOverlay.removeCurrentPolylineDrawable();
            } else {
                polygonOverlay.removeCurrentPolygonDrawable();
                polygonOverlay.removeCurrentPolylineDrawable();
                polygonOverlay.clearPath();
            }
            polygonOverlay.update();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    protected double[] getPointArray(List<GeoPoint> geoPointList) {
        if (geoPointList != null && !geoPointList.isEmpty()) {
            double[] lonLatArray = new double[geoPointList.size() * 2];
            for (int i = 0; i < geoPointList.size(); i++) {
                lonLatArray[i * 2] = geoPointList.get(i).getLongitude();
                lonLatArray[i * 2 + 1] = geoPointList.get(i).getLatitude();
            }
            return lonLatArray;
        }
        return null;
    }

    /**
     * @param :
     * @return :
     * @method : obtainNewMarker
     * @Author : xiaoxiao
     * @Describe : 生成一个新的marker
     * @Date : 2018/5/28
     */
    protected MarkerItem obtainMarker(Object uid, String title, String des, GeoPoint geoPoint) {
        MarkerItem markerItem = new MarkerItem(uid, title, des, geoPoint);
        markerItem.setMarker(LayerStyle.getDefaultMarkerSymbol(getActivity()));
        return markerItem;
    }

    /**
     * @param :
     * @return :
     * @method : clearMapOverlayer
     * @Author : xiaoxiao
     * @Describe : 清空绘制的点线面
     * @Date : 2018/5/24
     */
    protected void clearMapOverlayer() {
        Map map = null;
        if (markerLayer != null && markerLayer.getItemList() != null) {
            map = markerLayer.map();
            markerLayer.getItemList().clear();
            markerLayer.update();
            markerLayer.map().layers().remove(markerLayer);
        }
        if (polylineOverlay != null && polylineOverlay.getPoints() != null) {
            map = polylineOverlay.map();
            polylineOverlay.getPoints().clear();
            polylineOverlay.update();
            markerLayer.map().layers().remove(polylineOverlay);
        }
        if (polygonOverlay != null && polygonOverlay.getPoints() != null) {
            map = polygonOverlay.map();
            polygonOverlay.getPoints().clear();
            polygonOverlay.update();
            markerLayer.map().layers().remove(polygonOverlay);
        }
        if (map != null) {
            markerLayer.map().updateMap(true);
        }
    }

    public class MapEventsReceiver extends Layer implements GestureListener {

        public MapEventsReceiver(Map map) {
            super(map);
        }

        @Override
        public boolean onGesture(Gesture g, MotionEvent e) {
            if (g instanceof Gesture.Tap) {
                GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
                DRAW_STATE currentState = getCurrentDrawState();

                if (currentState != DRAW_STATE.DRAW_NONE) {//如果当前是绘制模式，则自动添加marker
                    markerLayer.addItem(new MarkerItem("", "", p));
                    markerLayer.update();
                    //如果当前是绘制线模式，则增加pathLayer
                    if (currentState == DRAW_STATE.DRAW_LINE) {
                        polylineOverlay.addPoint(p);
                        redrawPolyline(polylineOverlay);
                    }
                    if (currentState == DRAW_STATE.DRAW_POLYGON) {
                        polygonOverlay.addPoint(p);
                        redrawPolygon(polygonOverlay);
                    }
                    markerLayer.map().updateMap(true);
                    if (currentDrawState == DRAW_STATE.DRAW_POINT) { // 如果当前正在绘制点
                        Message msg = Message.obtain();
                        msg.what = SystemConstant.MSG_WHAT_DRAW_POINT;
                        msg.obj = p;
                        EventBus.getDefault().post(msg);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //如果点位的layer没有数据，则移除
        if (markerLayer != null && markerLayer.size() <= 0) {
            CatEyeMapManager.getInstance().getCatEyeMap().layers().remove(markerLayer);
            markerLayer = null;
        }
        //如果线的layer没有数据，则移除
        if (polylineOverlay != null && polylineOverlay.getPoints() != null && polylineOverlay.getPoints().size() < 2) {
            CatEyeMapManager.getInstance().getCatEyeMap().layers().remove(polylineOverlay);
            polylineOverlay = null;
            if (currentDrawState == DRAW_STATE.DRAW_LINE) {
                CatEyeMapManager.getInstance().getCatEyeMap().layers().remove(markerLayer);
                markerLayer = null;
            }
        }
        //如果面的layer没有数据，则移除
        if (polygonOverlay != null && polygonOverlay.getPoints() != null && polygonOverlay.getPoints().size() < 3) {
            CatEyeMapManager.getInstance().getCatEyeMap().layers().remove(polygonOverlay);
            polygonOverlay = null;
            if (currentDrawState == DRAW_STATE.DRAW_POLYGON) {
                CatEyeMapManager.getInstance().getCatEyeMap().layers().remove(markerLayer);
                markerLayer = null;
            }
        }
        CatEyeMapManager.getInstance().getCatEyeMap().updateMap(true);

        //通知主界面绘制点线面结束
        Message msg = new Message();
        msg.what = SystemConstant.MSG_WHAT_DRAW_POINT_LINE_POLYGON_DESTROY;
        EventBus.getDefault().post(msg);
    }
}
