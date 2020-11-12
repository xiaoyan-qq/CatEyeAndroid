package com.cateye.vtm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.SystemConstant;
import com.tamsiree.rxkit.view.RxToast;
import com.vtm.library.tools.CatEyeMapManager;

import org.greenrobot.eventbus.EventBus;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerInterface;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * Created by xiaoxiao on 2018/3/21.
 */
//@Puppet
public class DrawPointLinePolygonFragment extends BaseDrawFragment {
    private TextView tv_last, tv_clear, tv_finish;
    protected MapEventsReceiver mapEventsReceiver;

    private int drawUsage = -1;

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_draw_point_line_polygon;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            //获取当前的绘制状态
            currentDrawState = (DRAW_STATE) savedInstanceState.getSerializable(DRAW_STATE.class.getSimpleName());
            drawUsage = savedInstanceState.getInt(SystemConstant.DRAW_USAGE);
        }
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            currentDrawState = (DRAW_STATE) bundle.getSerializable(DRAW_STATE.class.getSimpleName());
            drawUsage = bundle.getInt(SystemConstant.DRAW_USAGE);
        }
    }

    @Override
    public void onNewBundle(Bundle args) {
        super.onNewBundle(args);
        if (args != null) {
            //获取当前的绘制状态
            currentDrawState = (DRAW_STATE) args.getSerializable(DRAW_STATE.class.getSimpleName());
        }
    }

    @Override
    public void initView(View rootView) {
        tv_last = (TextView) rootView.findViewById(R.id.tv_draw_last);
        tv_clear = (TextView) rootView.findViewById(R.id.tv_draw_clear);
        tv_finish = (TextView) rootView.findViewById(R.id.tv_draw_finish);

        if (currentDrawState == DRAW_STATE.DRAW_POINT) {
            tv_last.setVisibility(View.GONE);
            tv_clear.setVisibility(View.GONE);
        }

        tv_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        tv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    polygonOverlay.removeCurrentPolygonDrawable();
                    redrawPolygon(polygonOverlay);
                }

            }
        });

        tv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle drawBundle = new Bundle();
                List<GeoPoint> geoPointList = new ArrayList<>();
                if (markerLayer != null && markerLayer.getItemList() != null && !markerLayer.getItemList().isEmpty()) {
                    for (MarkerInterface item : markerLayer.getItemList()) {
                        geoPointList.add(item.getPoint());
                    }
                    drawBundle.putSerializable(SystemConstant.DRAW_POINT_LIST, (Serializable) geoPointList);
                }

                Message msg = Message.obtain();
                if (drawUsage == SystemConstant.DRAW_CONTOUR_LINE) { // 如果当前正在绘制等高线
                    msg.what = SystemConstant.MSG_WHAT_DRAW_RESULT;
                    msg.obj = geoPointList;
                    msg.arg1 = drawUsage;
                } else {
//                    if (getCurrentDrawState() == DRAW_STATE.DRAW_POINT) {
//                        List<MarkerItem> itemList=markerLayer.getItemList();
//                        if (itemList!=null&&!itemList.isEmpty()){
//                            msg.what = SystemConstant.MSG_WHAT_DRAW_POINT;
//                            List<GeoPoint> pointList = new ArrayList<>();
//                            for (MarkerItem item:itemList) {
//                                pointList.add(item.geoPoint);
//                            }
//                            msg.obj = pointList;
//                            EventBus.getDefault().post(msg);
//                        }
//                    } else
                    if (getCurrentDrawState() == DRAW_STATE.DRAW_LINE) {
                        msg.what = SystemConstant.MSG_WHAT_DRAW_LINE;
                        msg.obj = polylineOverlay.getPoints();
                        EventBus.getDefault().post(msg);
                    } else if (getCurrentDrawState() == DRAW_STATE.DRAW_POLYGON) {
                        msg.what = SystemConstant.MSG_WHAT_DRAW_POLYGON;
                        msg.obj = polygonOverlay.getPoints();
                        EventBus.getDefault().post(msg);
                    }
                }

                //清空绘制图层
                clearDrawLayers();
                pop();//退出当前界面
            }
        });

        //添加一个操作图层，监听用户在地图上的点击事件
        mapEventsReceiver = new MapEventsReceiver(CatEyeMapManager.getInstance().getCatEyeMap());
        CatEyeMapManager.getInstance().getCatEyeMap().layers().add(mapEventsReceiver, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);
    }

    /**
     * @param :
     * @return :
     * @method :
     * @Author : xiaoxiao
     * @Describe :
     * @Date : 2018/5/28
     */


    public static BaseFragment newInstance(Bundle bundle) {
        DrawPointLinePolygonFragment drawPointLinePolygonFragment = new DrawPointLinePolygonFragment();
        drawPointLinePolygonFragment.setArguments(bundle);
        return drawPointLinePolygonFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //通知主界面隐藏部分重新显示
        setMainFragmentAreaVisible(CatEyeMainFragment.BUTTON_AREA.ALL, false);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //判断绘制的用途，某些用途下，绘制结束后就不需要再显示，也需要移除掉layer
        if (drawUsage == SystemConstant.DRAW_CONTOUR_LINE) {
            if (markerLayer != null) {
                CatEyeMapManager.getInstance().getCatEyeMap().layers().remove(markerLayer);
                markerLayer = null;
            }
            if (polylineOverlay != null) {
                CatEyeMapManager.getInstance().getCatEyeMap().layers().remove(polylineOverlay);
                polylineOverlay = null;
            }
            if (polygonOverlay != null) {
                CatEyeMapManager.getInstance().getCatEyeMap().layers().remove(polygonOverlay);
                polygonOverlay = null;
            }
        }

        //当前界面被返回时，移除绘制的图层
        if (mapEventsReceiver != null) {
            CatEyeMapManager.getInstance().getCatEyeMap().layers().remove(mapEventsReceiver);
            mapEventsReceiver = null;
        }
        //通知主界面绘制点线面结束
        Message msg = new Message();
        msg.what = SystemConstant.MSG_WHAT_DRAW_POINT_LINE_POLYGON_DESTROY;
        EventBus.getDefault().post(msg);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //通知主界面隐藏部分重新显示
        setMainFragmentAreaVisible(CatEyeMainFragment.BUTTON_AREA.ALL, true);
    }
}
