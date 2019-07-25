package com.cateye.vtm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.AirPhotoPlanner.JNINativeApi;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.canyinghao.candialog.CanDialog;
import com.canyinghao.candialog.CanDialogInterface;
import com.cateye.android.entity.AirPlanDBEntity;
import com.cateye.android.entity.Airport;
import com.cateye.android.entity.DigitalCameraInfo;
import com.cateye.android.entity.FlightParameter;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.CatEyeMapManager;
import com.cateye.vtm.util.LayerUtils;
import com.cateye.vtm.util.SystemConstant;
import com.desmond.ripple.RippleCompat;
import com.github.lazylibrary.util.StringUtils;
import com.vondear.rxtool.view.RxToast;
import com.vtm.library.layers.MultiPolygonLayer;
import com.vtm.library.tools.GeometryTools;
import com.vtm.library.tools.OverlayerManager;
import com.yydcdut.sdlv.Menu;
import com.yydcdut.sdlv.SlideAndDragListView;

import org.json.JSONException;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.map.Map;
import org.wololo.geojson.GeoJSON;
import org.xutils.DbManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Created by xiaoxiao on 2018/8/31.
 * 设置航飞参数时，选择的polygon的列表
 */

public class AirPlanParamListFragment extends BaseDrawFragment {
    private Map mMap;
    private DbManager dbManager;

    private SlideAndDragListView slideAndDragListView;
    private AirPlanParamAdapter adapter;
    private List<AirPlanDBEntity> listData;
    private AirPlanDBEntity dragDBEntity;

    private ImageView img_back;
    private BootstrapButton btn_ok;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMap = CatEyeMapManager.getMapView().map();
        this.dbManager = ((MainActivity) getActivity()).getDbManager();
    }

    public static BaseFragment newInstance(Bundle bundle) {
        AirPlanParamListFragment airPlanDrawFragment = new AirPlanParamListFragment();
        airPlanDrawFragment.setArguments(bundle);
        return airPlanDrawFragment;
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_air_plan_param_list;
    }

    @Override
    public void initView(View rootView) {
        slideAndDragListView = rootView.findViewById(R.id.sadLv_paramPolygonList);
        listData = new ArrayList<>();
        adapter = new AirPlanParamAdapter(getActivity(), listData);
        Menu menu = new Menu(true, 0);//第1个参数表示滑动 item 是否能滑的过头，像弹簧那样( true 表示过头，就像 Gif 中显示的那样；false 表示不过头，就像 Android QQ 中的那样)
        slideAndDragListView.setMenu(menu);
        slideAndDragListView.setAdapter(adapter);
        slideAndDragListView.setOnDragDropListener(new SlideAndDragListView.OnDragDropListener() {//用户拖动listview的item上下滑动
            @Override
            public void onDragViewStart(int beginPosition) {
                dragDBEntity = listData.get(beginPosition);
            }

            @Override
            public void onDragDropViewMoved(int fromPosition, int toPosition) {
                AirPlanDBEntity removeEntity = listData.remove(fromPosition);
                listData.add(toPosition, removeEntity);
            }

            @Override
            public void onDragViewDown(int finalPosition) {
                listData.set(finalPosition, dragDBEntity);
            }
        });

        img_back = (ImageView) findViewById(R.id.tv_air_plan_list_back);
//        RippleCompat.apply(img_back, R.color.gray);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressedSupport();
            }
        });

        btn_ok = (BootstrapButton) findViewById(R.id.btn_air_plan_param_set_ok);
        //用户点击确定，开始航区规划
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //首先获取用户规划航区的layer，获取起飞位置
                final ItemizedLayer markerLayer = LayerUtils.getAirPlanMarkerLayer(getContext(), mMap);
                if (markerLayer == null || markerLayer.getItemList() == null || markerLayer.getItemList().isEmpty()) {
                    RxToast.error("没有选择起飞点，请在地图上非规划区域选择起飞点");
                    return;
                }
                if (listData == null || listData.isEmpty()) {
                    RxToast.error("没有选择飞行区域，请在地图上点选航飞区域");
                    return;
                }

                //显示参数设置对话框，用户填写完参数后调用底层接口
                new CanDialog.Builder(getActivity()).setView(R.layout.dialog_air_plan_set_fly).setNegativeButton("取消", true, null).setPositiveButton("确定", false, new CanDialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final CanDialog dialog, int checkItem, CharSequence text, boolean[] checkItems) {
                        //获取对话框中用户输入的参数，如果有输入错误，提示用户
                        View rootView = dialog.getRootView();
                        if (rootView != null) {

                            //检查焦距
                            EditText edt_F = rootView.findViewById(R.id.edt_air_plan_F);
                            TextInputLayout til_F = rootView.findViewById(R.id.til_F);
                            boolean camera_f_result = checkAirPlanParamWidget(edt_F, til_F, "焦距不能为空");

                            //检查照片高
                            EditText edt_PicHeight = rootView.findViewById(R.id.edt_air_plan_picHeight);
                            TextInputLayout til_PicHeight = rootView.findViewById(R.id.til_PicHeight);
                            boolean camera_PicHeight_result = checkAirPlanParamWidget(edt_PicHeight, til_PicHeight, "照片高度不能为空");

                            //检查照片宽
                            EditText edt_PicWidth = rootView.findViewById(R.id.edt_air_plan_picWidth);
                            TextInputLayout til_PicWidth = rootView.findViewById(R.id.til_picWidth);
                            boolean camera_PicWidth_result = checkAirPlanParamWidget(edt_PicWidth, til_PicWidth, "照片宽度不能为空");

                            //检查相元大小
                            EditText edt_pixelSize = rootView.findViewById(R.id.edt_air_plan_pixelSize);
                            TextInputLayout til_pixelSize = rootView.findViewById(R.id.til_pixelSize);
                            boolean camera_pixelSize_result = checkAirPlanParamWidget(edt_pixelSize, til_pixelSize, "相元大小不能为空");


                            //检查地面分辨率
                            EditText edt_ratio = rootView.findViewById(R.id.edt_air_plan_ratio);
                            TextInputLayout til_ratio = rootView.findViewById(R.id.til_ratio);
                            boolean ratio_result = checkAirPlanParamWidget(edt_ratio, til_ratio, "地面分辨率不能为空");

                            //检查航向重叠率
                            EditText edt_overlap = rootView.findViewById(R.id.edt_air_plan_overlap);
                            TextInputLayout til_overlap = rootView.findViewById(R.id.til_overlap);
                            boolean overlap_result = checkAirPlanParamWidget(edt_overlap, til_overlap, "航向重叠率不能为空");

                            //检查旁向重叠率
                            EditText edt_overlap_crossStrip = rootView.findViewById(R.id.edt_air_plan_overlap_crossStrip);
                            TextInputLayout til_overlap_crossStrip = rootView.findViewById(R.id.til_overlap_crossStrip);
                            boolean overlap_crossStrip_result = checkAirPlanParamWidget(edt_overlap_crossStrip, til_overlap_crossStrip, "旁向重叠率不能为空");

                            //检查引导点距离
                            EditText edt_guidePointDistance = rootView.findViewById(R.id.edt_air_plan_guidePointDistance);
                            TextInputLayout til_guidePointDistance = rootView.findViewById(R.id.til_guidePointDistance);
                            boolean guidePointDistance_result = checkAirPlanParamWidget(edt_guidePointDistance, til_guidePointDistance, "引导点距离不能为空");

                            //保存名称
                            EditText edt_save_name = rootView.findViewById(R.id.edt_air_plan_save_name);
                            TextInputLayout til_save_name = rootView.findViewById(R.id.til_save_name);
                            boolean save_name_result = checkAirPlanParamWidget(edt_save_name, til_save_name, "文件保存名称不能为空");

                            //基准面高度
                            String baseLineStr = ((EditText) rootView.findViewById(R.id.edt_air_plan_base_altitude)).getText().toString();

                            if (camera_f_result && camera_PicHeight_result && camera_PicWidth_result && camera_pixelSize_result && ratio_result && overlap_result && overlap_crossStrip_result && guidePointDistance_result && save_name_result) {
                                try {
                                    //判断当前参数设置图层是否有polygon，如果存在，则弹出对话框提示用户设置参数
                                    MultiPolygonLayer airplanParamOverlayer = (MultiPolygonLayer) OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.AIR_PLAN_MULTI_POLYGON_PARAM);
                                    if (airplanParamOverlayer != null) {
                                        Vector<JSONObject> flightRegionList = new Vector<>();
                                        Vector<Double> flightHeightVector = new Vector<>();
                                        if (listData!=null&&!listData.isEmpty()){
                                            for (AirPlanDBEntity dbEntity:listData){
                                                flightRegionList.add(JSONObject.parseObject(GeometryTools.getGeoJson(GeometryTools.createGeometry(dbEntity.getGeometry())).toString()));
                                                flightHeightVector.add((double)dbEntity.getAltitude());
                                            }
                                        }

                                        FlightParameter parameter = new FlightParameter();
                                        //设置机场参数
                                        GeoPoint airPlanStartPoint = ((MarkerItem) markerLayer.getItemList().get(0)).getPoint();
                                        Airport airport = new Airport();
                                        airport.setGeoJson(JSONObject.parseObject(GeometryTools.getGeoJson(GeometryTools.createGeometry(airPlanStartPoint)).toString()));
                                        airport.setAltitude(StringUtils.isBlank(baseLineStr) ? 0 : Double.parseDouble(baseLineStr));
                                        parameter.setAirport(airport);

                                        DigitalCameraInfo cameraInfo = new DigitalCameraInfo();
                                        String cameraF=edt_F.getText().toString();
                                        cameraInfo.setF(StringUtils.isBlank(cameraF) ? 0 : Double.parseDouble(cameraF));
                                        String cameraHeight=edt_PicHeight.getText().toString();
                                        cameraInfo.setHeight(StringUtils.isBlank(cameraHeight) ? 0 : Long.parseLong(cameraHeight));
                                        String cameraWidth=edt_PicWidth.getText().toString();
                                        cameraInfo.setWidth(StringUtils.isBlank(cameraWidth) ? 0 : Long.parseLong(cameraWidth));
                                        String cameraPixel=edt_pixelSize.getText().toString();
                                        cameraInfo.setPixelsize(StringUtils.isBlank(cameraPixel) ? 0 : Double.parseDouble(cameraPixel));
                                        parameter.setCameraInfo(cameraInfo);

                                        parameter.setAverageElevation(StringUtils.isBlank(baseLineStr) ? 0 : Double.parseDouble(baseLineStr));//航区平均地面高程

                                        String guidePointDistance=edt_guidePointDistance.getText().toString();
                                        parameter.setGuidanceEntrancePointsDistance(StringUtils.isBlank(guidePointDistance) ? 0 : Double.parseDouble(guidePointDistance));//引导点距离

                                        String overlap=edt_overlap.getText().toString();
                                        parameter.setOverlap(StringUtils.isBlank(overlap) ? 0 : Double.parseDouble(overlap));//航向重叠度
                                        String overlap_crossStrip=edt_overlap_crossStrip.getText().toString();
                                        parameter.setOverlap_crossStrip(StringUtils.isBlank(overlap_crossStrip) ? 0 : Double.parseDouble(overlap_crossStrip));//旁向重叠度


                                        parameter.setFightRegion(flightRegionList);
                                        parameter.setFightHeight_Vec(flightHeightVector);
                                        final String jsonResult = JSON.toJSONString(parameter);
                                        final File outputDirectory=new File(SystemConstant.AIR_PLAN_OUTPUT_PATH+ File.separator + ((EditText) rootView.findViewById(R.id.edt_air_plan_save_name)).getText().toString() + ".skw");
                                        if (!outputDirectory.getParentFile().exists()){
                                            outputDirectory.getParentFile().mkdirs();
                                        }
                                        if (outputDirectory.exists()){
                                            //如果已经存在该文件名称，提示用户
                                            new CanDialog.Builder(getActivity()).setTitle("提示!").setMessage("存在同名文件，是否覆盖？").setPositiveButton("是", true, new CanDialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(CanDialog dialog, int checkItem, CharSequence text, boolean[] checkItems) {
                                                    JNINativeApi.airPlannerOutput(jsonResult, outputDirectory.getAbsolutePath());
                                                }
                                            }).setNegativeButton("否",true,null).show();
                                        }else {
                                            JNINativeApi.airPlannerOutput(jsonResult, outputDirectory.getAbsolutePath());
                                            dialog.dismiss();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }finally {

                                }

                            }
                        }
                    }
                }).show();

            }
        });
    }


    private class AirPlanParamAdapter extends BaseAdapter {
        private Context mContext;
        private List<AirPlanDBEntity> listData;
        private MultiPolygonLayer airPlanParamLayer;

        public AirPlanParamAdapter(Context mContext, List<AirPlanDBEntity> listData) {
            this.mContext = mContext;
            this.listData = listData;
            this.airPlanParamLayer = LayerUtils.getAirPlanParamLayer(mMap);
        }

        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * addData 向当前listview中添加polygon，更新列表
         *
         * @param airPlanDBEntity
         */
        public void addData(AirPlanDBEntity airPlanDBEntity) {
            listData.add(airPlanDBEntity);
            notifyDataSetChanged();
        }

        /**
         * addAllData 批量向当前listview中添加polygon，更新列表
         *
         * @param airPlanDBEntityList
         */
        public void addAllData(List<AirPlanDBEntity> airPlanDBEntityList) {
            listData.addAll(airPlanDBEntityList);
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_air_plan_polygon_param, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final AirPlanDBEntity airPlanDBEntity = listData.get(position);
            if (airPlanDBEntity != null) {
                viewHolder.tv_polygonName.setText(airPlanDBEntity.getName());
                viewHolder.tv_updateTime.setText(airPlanDBEntity.getLastUpdate());
                viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {//移除指定的polygon
                    @Override
                    public void onClick(View v) {
                        airPlanParamLayer.removePolygonDrawable(airPlanDBEntity.getGeometry());
                        RxToast.info(mContext, "移除" + airPlanDBEntity.getName());
                        listData.remove(position);
                        notifyDataSetChanged();
                    }
                });
            }

            return convertView;
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tv_updateTime;//最后更新时间
            public TextView tv_polygonName;//polygon名称
            public BootstrapButton btn_delete;//删除

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_updateTime = itemView.findViewById(R.id.tv_polygon_updatetime);
                tv_polygonName = itemView.findViewById(R.id.tv_polygon_name);
                btn_delete = itemView.findViewById(R.id.btn_polygon_delete);

                RippleCompat.apply(btn_delete, R.color.gray);
            }
        }
    }

    /**
     * addData 向当前listview中添加polygon，更新列表
     *
     * @param airPlanDBEntity
     */
    public void addData(AirPlanDBEntity airPlanDBEntity) {
        adapter.addData(airPlanDBEntity);
    }

    /**
     * addAllData 批量向当前listview中添加polygon，更新列表
     *
     * @param airPlanDBEntityList
     */
    public void addAllData(List<AirPlanDBEntity> airPlanDBEntityList) {
        adapter.addAllData(airPlanDBEntityList);
    }

    public void removeData(AirPlanDBEntity airPlanDBEntity) {
        if (listData != null && airPlanDBEntity != null) {
            Iterator iterator = listData.iterator();
            while (iterator.hasNext()) {
                AirPlanDBEntity entity = (AirPlanDBEntity) iterator.next();
                if (entity != null && airPlanDBEntity.getId() == entity.getId()) {
                    iterator.remove();
                    adapter.notifyDataSetChanged();
                    break;
                }
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

    private boolean checkAirPlanParamWidget(EditText edt, TextInputLayout til, String errorMessage) {
        boolean checkResult = true;
        String edt_value = edt.getText().toString();
        if (StringUtils.isBlank(edt_value)) {
            til.setError(errorMessage);
            til.setErrorEnabled(true);
            checkResult = false;
        } else {
            til.setError("");
            til.setErrorEnabled(false);
        }
        return checkResult;
    }

    /**
     * 显示航线飞行的轨迹
     */
    private void showAirPlanLine(){

    }
}
