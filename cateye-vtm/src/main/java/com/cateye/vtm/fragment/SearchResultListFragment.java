package com.cateye.vtm.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.cateye.android.entity.MapSourceFromNet;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.tamsiree.rxkit.view.RxToast;
import com.tencent.lbssearch.object.result.SearchResultObject;
import com.vtm.library.tools.CatEyeMapManager;
import com.vtm.library.utils.SystemConstant;

import org.oscim.map.Map;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchResultListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchResultListFragment extends BaseFragment {
    private Map mMap;
    private ListView lvSearchResult;
    private SearchResultObject searchResultObject;
    private List<SearchResultObject.SearchResultData> listData;

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
        if (getArguments() != null) {
            //获取搜索结果
            String searchResultDataStr = bundle.getString(SystemConstant.BUNDLE_SEARCH_POI_RESULT_LIST);
            searchResultObject = JSON.parseObject(searchResultDataStr, SearchResultObject.class);
            if (searchResultObject!=null) {
                listData = searchResultObject.data;
            }
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
        // 根据listData数据，地图加载marker图层，显示列表数据
        if (searchResultObject.count>0) {
//            Observable.fromIterable(listData)
//                    .subscribeOn(Schedulers.computation())
//                    .doOnNext(null);
        } else {
            // 没有搜索到数据，提示用户
            RxToast.warning("没有搜索到数据");
            onBackPressedSupport();
        }
    }
}