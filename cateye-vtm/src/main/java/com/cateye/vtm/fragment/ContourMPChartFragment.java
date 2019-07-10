package com.cateye.vtm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.cateye.android.entity.ContourMPData;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.CatEyeMapManager;
import com.cateye.vtm.util.SystemConstant;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.vondear.rxtool.RxDeviceTool;
import com.vondear.rxui.view.dialog.RxDialogLoading;

import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static org.oscim.android.canvas.AndroidGraphics.drawableToBitmap;

/**
 * Created by xiaoxiao on 2018/5/23.
 */

public class ContourMPChartFragment extends BaseDrawFragment {
    private LineChart contourChart;//显示等高线的图表控件
    private List<ContourMPData> mpChartDataList;//需要显示等高线的数据
    private ImageView img_close;
    private MapPosition currentMapPosition;

    protected MapEventsReceiver mapEventsReceiver;

    private final double TAP_DISTANCE = 0.005;//点击捕捉的距离阈值=5米
    private PathLayer currentChartLine;//当前正在折线图中显示的线型
    private RxDialogLoading rxDialogLoading;//绘制地图上的line时显示的加载对话框
    private boolean isMoveOrScaleChart = false;


    public static BaseFragment newInstance(Bundle bundle) {
        ContourMPChartFragment contourMPChartFragment = new ContourMPChartFragment();
        contourMPChartFragment.setArguments(bundle);
        return contourMPChartFragment;
    }

    @Override
    public void onNewBundle(Bundle args) {
        super.onNewBundle(args);
        if (args != null) {
            //获取等高线的数据
            mpChartDataList = (List<ContourMPData>) args.getSerializable(SystemConstant.DATA_CONTOUR_CHART);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            //获取等高线的数据
            mpChartDataList = (List<ContourMPData>) savedInstanceState.getSerializable(SystemConstant.DATA_CONTOUR_CHART);
        }
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            //获取等高线的数据
            mpChartDataList = (List<ContourMPData>) bundle.getSerializable(SystemConstant.DATA_CONTOUR_CHART);
        }
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_contour_mp_chart;
    }

    @Override
    public void initView(View rootView) {
        rxDialogLoading = new RxDialogLoading(getActivity());
        rxDialogLoading.setTitle("捕捉界面区域");
        currentMapPosition = new MapPosition();
        contourChart = (LineChart) rootView.findViewById(R.id.contour_chart);
        img_close = (ImageView) rootView.findViewById(R.id.img_contour_chart_close);
        contourChart.setMinimumHeight(((int) (RxDeviceTool.getScreenHeight(getActivity()) * 0.3)));
        //设置折线图的x轴显示在底部
        contourChart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        Description chartDes = new Description();
        chartDes.setText("纵断面");
        contourChart.setDescription(chartDes);
        initChartData(mpChartDataList);

        //自动添加pathLayer,
        initPolylineOverlayer();
        //初始化绘制当前图表中正在显示的线型
        initCurrentPolylineOverlayer();
        //自动添加绘制marker的overlayer，当用户滑动折线图时，自动定位到折线图上对应的点位
        initMarkerOverlayer();

        //添加一个操作图层，监听用户在地图上的点击事件
        mapEventsReceiver = new MapEventsReceiver(CatEyeMapManager.getInstance(getActivity()).getCatEyeMap());
        CatEyeMapManager.getInstance(getActivity()).getCatEyeMap().layers().add(mapEventsReceiver, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);

        //绘制所有数据
        if (mpChartDataList != null && polylineOverlay != null && currentChartLine != null) {
            if (polylineOverlay.getPoints() == null) {
                polylineOverlay.setPoints(new ArrayList<GeoPoint>());
            }
            polylineOverlay.getPoints().clear();

            if (currentChartLine.getPoints() == null) {
                currentChartLine.setPoints(new ArrayList<GeoPoint>());
            }
            currentChartLine.getPoints().clear();

            for (ContourMPData data : mpChartDataList) {
                GeoPoint geoPoint = data.getGeoPoint();
                polylineOverlay.getPoints().add(geoPoint);
                currentChartLine.getPoints().add(geoPoint);
            }
            redrawPolyline(polylineOverlay);
            redrawPolyline(currentChartLine);

            //自动将地图缩放级别提高，并将中心点位置定位到第一个点的位置
            polylineOverlay.map().getMapPosition(currentMapPosition);
            currentMapPosition.setPosition(polylineOverlay.getPoints().get(0).getLatitude(), polylineOverlay.getPoints().get(0).getLongitude());
            if (currentMapPosition.getZoomLevel() < 14) {
                currentMapPosition.setZoomLevel(14);
            }
            polylineOverlay.map().animator().animateTo(currentMapPosition);
        }

        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop();
            }
        });
    }

    /**
     * @param :
     * @return :
     * @method : initPolylineOverlayer
     * @Author : xiaoxiao
     * @Describe : 初始化绘制线的图层
     * @Date : 2018/5/24
     */
    private void initPolylineOverlayer() {
        //自动添加pathLayer
        int c = getResources().getColor(R.color.turquoise);
        Style lineStyle = Style.builder()
                .stippleColor(c)
                .stipple(24)
                .stippleWidth(1)
                .strokeWidth(2)
                .strokeColor(c)
                .fixed(true)
                .randomOffset(false)
                .build();
        polylineOverlay.setStyle(lineStyle);
    }

    /**
     * @param :
     * @return :
     * @method : initPolylineOverlayer
     * @Author : xiaoxiao
     * @Describe : 初始化绘制线的图层
     * @Date : 2018/5/24
     */
    private void initCurrentPolylineOverlayer() {
        //自动添加pathLayer
        int c = getResources().getColor(R.color.tomato);
        Style lineStyle = Style.builder()
                .stippleColor(c)
                .stipple(24)
                .stippleWidth(1)
                .strokeWidth(2)
                .strokeColor(c)
                .fixed(true)
                .randomOffset(false)
                .build();
        currentChartLine = new PathLayer(CatEyeMapManager.getInstance(getActivity()).getCatEyeMap(), lineStyle);
        CatEyeMapManager.getInstance(getActivity()).getCatEyeMap().layers().add(currentChartLine, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);
    }

    /**
     * @param :
     * @return :
     * @method : initMarkerOverlayer
     * @Author : xiaoxiao
     * @Describe : 初始化线的点击layer
     * @Date : 2018/5/24
     */
    private void initMarkerOverlayer() {
        //重置默认的marker图标，新增marker时以当前设置的marker显示
        Bitmap bitmapPoi = drawableToBitmap(getResources().getDrawable(R.drawable.marker_poi));
        defaultMarkerSymbol = new MarkerSymbol(bitmapPoi, MarkerSymbol.HotspotPlace.CENTER);
    }


    /**
     * @param :
     * @return :
     * @method : initChartData
     * @Author : xiaoxiao
     * @Describe : 初始化表格所需要的数据内容
     * @Date : 2018/5/23
     */
    private void initChartData(final List<ContourMPData> contourMPDataList) {
        if (contourMPDataList != null && !contourMPDataList.isEmpty()) {
            List<Entry> entries = new ArrayList<Entry>();
            for (int i = 0; i < contourMPDataList.size(); i++) {
                entries.add(new Entry(i, contourMPDataList.get(i).getmHeight()));
            }
            LineDataSet dataSet = new LineDataSet(entries, "海拔"); // add entries to dataset
            dataSet.setColor(getResources().getColor(R.color.color_blue_alpha_400));
            dataSet.setValueTextColor(getResources().getColor(R.color.primary_text)); // styling, ...
            dataSet.setFillColor(getResources().getColor(R.color.color_blue_alpha_200));
            dataSet.setHighlightEnabled(true);
            dataSet.setHighLightColor(getResources().getColor(R.color.red));
            dataSet.setHighlightLineWidth(1f);
            dataSet.setCircleRadius(1.5f);
            dataSet.setCircleColor(getResources().getColor(R.color.colorPrimaryLight));

            LineData lineData = new LineData(dataSet);
            contourChart.setData(lineData);
            contourChart.animateXY(1000, 1500, Easing.EasingOption.Linear, Easing.EasingOption.Linear);
            contourChart.invalidate(); // refresh
            contourChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    int pointIndex = (int) e.getX();
                    contourChart.highlightValue(h);
                    if (markerLayer != null && mpChartDataList != null && mpChartDataList.size() > pointIndex) {
                        markerLayer.removeAllItems();
                        markerLayer.addItem(obtainMarker(null, "", "", new GeoPoint(mpChartDataList.get(pointIndex).getGeoPoint().getLatitude(), mpChartDataList.get(pointIndex).getGeoPoint().getLongitude())));
                        markerLayer.update();
                        markerLayer.map().updateMap(true);

                        //地图自动以当前选中的点位为中心
                        polylineOverlay.map().getMapPosition(currentMapPosition);
                        currentMapPosition.setPosition(mpChartDataList.get(pointIndex).getGeoPoint().getLatitude(), mpChartDataList.get(pointIndex).getGeoPoint().getLongitude());
                        polylineOverlay.map().animator().animateTo(currentMapPosition);
                    }
                }

                @Override
                public void onNothingSelected() {
                    if (markerLayer != null) {
                        markerLayer.getItemList().clear();
                        markerLayer.update();
                        markerLayer.map().updateMap(true);
                    }
                }
            });
            contourChart.setOnChartGestureListener(new OnChartGestureListener() {
                @Override
                public void onChartGestureStart(android.view.MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

                }

                @Override
                public void onChartGestureEnd(android.view.MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                    if (isMoveOrScaleChart) {
                        Observable.create(new ObservableOnSubscribe<PathLayer>() {
                            @Override
                            public void subscribe(ObservableEmitter<PathLayer> emitter) throws Exception {
                                //操作结束
                                float min = contourChart.getLowestVisibleX();
                                float max = contourChart.getHighestVisibleX();
                                if (min < max && currentChartLine != null && mpChartDataList != null && mpChartDataList.size() > max) {
                                    currentChartLine.clearPath();
                                    for (int m = (int) min; m < max; m++) {
                                        currentChartLine.addPoint(mpChartDataList.get(m).getGeoPoint());
                                    }
                                    emitter.onNext(currentChartLine);
                                    emitter.onComplete();
                                }
                            }
                        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<PathLayer>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                if (!rxDialogLoading.isShowing()) {
                                    rxDialogLoading.show();
                                }
                            }

                            @Override
                            public void onNext(PathLayer pathLayer) {
                                redrawPolyline(currentChartLine);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (rxDialogLoading.isShowing()) {
                                    rxDialogLoading.dismiss();
                                }
                            }

                            @Override
                            public void onComplete() {
                                if (rxDialogLoading.isShowing()) {
                                    rxDialogLoading.dismiss();
                                    isMoveOrScaleChart = false;
                                }
                            }
                        });
                    }
                }

                @Override
                public void onChartLongPressed(android.view.MotionEvent me) {

                }

                @Override
                public void onChartDoubleTapped(android.view.MotionEvent me) {

                }

                @Override
                public void onChartSingleTapped(android.view.MotionEvent me) {

                }

                @Override
                public void onChartFling(android.view.MotionEvent me1, android.view.MotionEvent me2, float velocityX, float velocityY) {
                }

                @Override
                public void onChartScale(android.view.MotionEvent me, float scaleX, float scaleY) {
                    isMoveOrScaleChart = true;
                }

                @Override
                public void onChartTranslate(android.view.MotionEvent me, float dX, float dY) {
                    isMoveOrScaleChart = true;
                }
            });
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //通知主界面隐藏部分重新显示
        setMainFragmentAreaVisible(CatEyeMainFragment.BUTTON_AREA.ALL, false);
    }

    @Override
    public void onStop() {
        super.onStop();
        //当前界面被返回时，自动移除所有的overlayer
        if (mapEventsReceiver != null) {
            CatEyeMapManager.getInstance(getActivity()).getCatEyeMap().layers().remove(mapEventsReceiver);
        }
        if (currentChartLine != null) {
            CatEyeMapManager.getInstance(getActivity()).getCatEyeMap().layers().remove(currentChartLine);
        }
        clearMapOverlayer();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //通知主界面隐藏部分重新显示
        setMainFragmentAreaVisible(CatEyeMainFragment.BUTTON_AREA.ALL, true);
    }

    private class MapEventsReceiver extends Layer implements GestureListener {

        MapEventsReceiver(Map map) {
            super(map);
        }

        @Override
        public boolean onGesture(Gesture g, MotionEvent e) {
            if (g instanceof Gesture.Tap) {
                GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
                //获取到当前的点击事件，判断离点击事件最近的点
                if (mpChartDataList != null && !mpChartDataList.isEmpty()) {
                    double distance = p.distance(mpChartDataList.get(0).getGeoPoint());
                    int selectedPointIndex = 0;
                    for (int i = 0; i < mpChartDataList.size(); i++) {
                        double distanceIter = p.distance(mpChartDataList.get(i).getGeoPoint());
                        if (distanceIter < distance) {
                            selectedPointIndex = i;
                            distance = distanceIter;
                        }
                    }
                    if (distance <= TAP_DISTANCE) {
                        markerLayer.removeAllItems();
                        markerLayer.addItem(obtainMarker(null, "", "", new GeoPoint(mpChartDataList.get(selectedPointIndex).getGeoPoint().getLatitude(), mpChartDataList.get(selectedPointIndex).getGeoPoint().getLongitude())));
                        markerLayer.update();
                        markerLayer.map().updateMap(true);
                        //重新设置选中状态
                        contourChart.highlightValue(selectedPointIndex, mpChartDataList.get(selectedPointIndex).getmHeight(), 0, false);
                        //自动以选中的元素居中
                        contourChart.centerViewToAnimated(selectedPointIndex, mpChartDataList.get(selectedPointIndex).getmHeight(), YAxis.AxisDependency.LEFT, 1200);
                    }
                }
                return true;
            }
            return false;
        }
    }
}
