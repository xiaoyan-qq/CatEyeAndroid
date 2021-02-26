package com.cateye.vtm.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.cateye.android.entity.MapSourceFromNet;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.LayerStyle;
import com.tamsiree.rxkit.view.RxToast;
import com.tencent.lbssearch.object.result.SearchResultObject;
import com.vtm.library.tools.CatEyeMapManager;
import com.vtm.library.tools.GpsCoordinateUtils;
import com.vtm.library.utils.SystemConstant;

import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerInterface;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.map.Map;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchResultListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class SearchResultListFragment extends BaseFragment {
    private Map mMap;
    private ListView lvSearchResult;
    private SearchResultObject searchResultObject;
    private List<SearchResultObject.SearchResultData> resultListData;
    private ItemizedLayer markerLayer; // 用来显示marker 的图层
    private BoundingBox boundingBox;
    private SearchPoiResultAdapter searchPoiResultAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchResultListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchResultListFragment newInstance(Bundle bundle) {
        SearchResultListFragment fragment = new SearchResultListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public SearchResultListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMap = CatEyeMapManager.getInstance().getMapView().map();
        this.markerLayer = new ItemizedLayer(this.mMap, LayerStyle.getDefaultMarkerSymbol(getActivity()), "POIMarkerLayer");
        this.mMap.layers().add(this.markerLayer);
        this.resultListData = new ArrayList<>();
        if (getArguments() != null) {
            //获取搜索结果
            String searchResultDataStr = bundle.getString(SystemConstant.BUNDLE_SEARCH_POI_RESULT_LIST);
            searchResultObject = JSON.parseObject(searchResultDataStr, SearchResultObject.class);
//            if (searchResultObject!=null) {
//                this.resultListData.addAll(searchResultObject.data);
//            }
        }
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_search_result_list;
    }

    @Override
    public void initView(View rootView) {
        ((TextView)rootView.findViewById(R.id.tv_common_title)).setText("地址查询");
        ((ImageView)rootView.findViewById(R.id.img_common_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressedSupport();
            }
        });
        lvSearchResult = rootView.findViewById(R.id.lv_search_result);
        searchPoiResultAdapter = new SearchPoiResultAdapter(getActivity(), resultListData);
        lvSearchResult.setAdapter(searchPoiResultAdapter);
        lvSearchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchResultObject.SearchResultData resultData = resultListData.get(position);
                if (view.isSelected()) {
                    view.setSelected(false);
                    // 遍历所有的marker，取消高亮
                    for (MarkerInterface markerInterface: markerLayer.getItemList()) {
                        MarkerItem markerItem = (MarkerItem) markerInterface;
                        markerItem.setMarker(null);
                    }
                } else {
                    view.setSelected(true);
                    for (MarkerInterface markerInterface: markerLayer.getItemList()) {
                        MarkerItem markerItem = (MarkerItem) markerInterface;
                        if (markerItem.title.equals(resultData.title)) {
                            markerItem.setMarker(LayerStyle.getDefaultPoiMarkerSymbol(getActivity()));
                        } else {
                            markerItem.setMarker(null);
                        }
                    }
                }
                // 地图定位到指定的位置
                mMap.animator().animateTo(700, new GeoPoint(resultData.latLng.getLatitude(), resultData.latLng.getLongitude()), 18, true);
            }
        });
        refreshData(searchResultObject);
    }

    private void refreshData(SearchResultObject searchResultObject) {
        // 根据listData数据，地图加载marker图层，显示列表数据
        if (searchResultObject.count>0) {
            Observable.fromIterable(searchResultObject.data)
                    .subscribeOn(Schedulers.computation())
                    .map(new Function<SearchResultObject.SearchResultData, GeoPoint>() {
                        @Override
                        public GeoPoint apply(@NonNull SearchResultObject.SearchResultData searchResultData){
                            resultListData.add(searchResultData);
                            // 转换坐标系，将数据从gcj02转为wgs84坐标
                            double[] latLonDoubles = GpsCoordinateUtils.calGCJ02toWGS84(searchResultData.latLng.getLatitude(),searchResultData.latLng.getLongitude());
                            GeoPoint geoPoint = null;
                            if (latLonDoubles!=null&&latLonDoubles.length>1) {
                                geoPoint = new GeoPoint(latLonDoubles[0], latLonDoubles[1]);
                            } else {
                                geoPoint = new GeoPoint(searchResultData.latLng.getLatitude(),searchResultData.latLng.getLongitude());
                            }

                            if (mMap!=null && markerLayer!=null) {
                                MarkerItem markerItem = new MarkerItem(searchResultData.id, searchResultData.title, searchResultData.address, geoPoint);
                                markerLayer.addItem(markerItem);
                            }
                            return geoPoint;
                        }
                    })
                    .toList()
                    .delay(30, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<List<GeoPoint>>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            resultListData.clear();
                            markerLayer.removeAllItems();
                        }

                        @Override
                        public void onSuccess(@NonNull List<GeoPoint> geoPoints) {
                            boundingBox = new BoundingBox(geoPoints);
                            mMap.animator().animateTo(boundingBox);
                            markerLayer.update();
                            mMap.updateMap();
                            searchPoiResultAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            RxToast.error(e.getMessage());
                        }
                    });
        } else {
            // 没有搜索到数据，提示用户
            RxToast.warning("没有搜索到数据");
            onBackPressedSupport();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.markerLayer.removeAllItems();
        this.mMap.layers().remove(markerLayer);
        this.mMap.updateMap();
    }

    class SearchPoiResultAdapter extends BaseAdapter {
        private Context mContext;
        private List<SearchResultObject.SearchResultData> listData;

        public SearchPoiResultAdapter(Context mContext, List<SearchResultObject.SearchResultData> listData) {
            this.mContext = mContext;
            this.listData = listData;
        }

        @Override
        public int getCount() {
            return listData!=null?listData.size():0;
        }

        @Override
        public Object getItem(int position) {
            return listData!=null?listData.get(position):null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_search_poi_result, null);
                viewHolder = new ViewHolder();
                viewHolder.img=convertView.findViewById(R.id.img_item_search_poi_result);
                viewHolder.tvName=convertView.findViewById(R.id.tv_item_search_poi_name);
                viewHolder.tvAddress=convertView.findViewById(R.id.tv_item_search_poi_address);
                viewHolder.tvContact=convertView.findViewById(R.id.tv_item_search_poi_contact);
                viewHolder.tvKind=convertView.findViewById(R.id.tv_item_search_poi_kindType);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            SearchResultObject.SearchResultData resultData = listData.get(position);
            if (resultData!=null) {
                viewHolder.tvName.setText(resultData.title);
                viewHolder.tvAddress.setText(resultData.address);
                viewHolder.tvContact.setText(resultData.tel);
                viewHolder.tvKind.setText(resultData.category);
            }
            viewHolder.tvName.setTag(resultData);
            return convertView;
        }

        private class ViewHolder {
            ImageView img;
            TextView tvName;
            TextView tvAddress;
            TextView tvContact;
            TextView tvKind;
        }
    }

    @Override
    public boolean onBackPressedSupport() {
        popTo(CatEyeMainFragment.class, false);
        ((MainActivity) getActivity()).hiddenSlidingLayout(true);//同时隐藏右侧面板
        return true;
    }
}