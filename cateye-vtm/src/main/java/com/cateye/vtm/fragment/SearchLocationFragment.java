package com.cateye.vtm.fragment;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.alibaba.fastjson.JSON;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.github.lazylibrary.util.StringUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okrx2.adapter.ObservableResponse;
import com.tamsiree.rxkit.view.RxToast;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.object.param.SuggestionParam;
import com.tencent.lbssearch.object.result.SuggestionResultObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.tools.Util;
import com.tencent.map.tools.net.http.HttpResponseListener;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.vtm.library.utils.SystemConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchLocationFragment extends BaseFragment {
    private EditText edt_search_location;
    private ImageView img_search_location;
    private ListView lvSuggest;
    private TencentSearch tencentSearch;
    private SimpleAdapter suggestAdapter;
    private List<Map<String, Object>> listData;
//    private Gson gson;

    public SearchLocationFragment() {
        // Required empty public constructor
//        this.gson = new Gson();
        this.listData = new ArrayList<>();
    }
    // TODO: Rename and change types and number of parameters
    public static SearchLocationFragment newInstance() {
        SearchLocationFragment fragment = new SearchLocationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_search_location;
    }

    @Override
    public void initView(View rootView) {
        tencentSearch = new TencentSearch(getActivity());
        edt_search_location = rootView.findViewById(R.id.edt_search_location);
        img_search_location = rootView.findViewById(R.id.img_search_location);
        lvSuggest = rootView.findViewById(R.id.lv_search_location);
        suggestAdapter = new SimpleAdapter(getActivity(), listData, android.R.layout.simple_list_item_2, new String[]{"title", "address"}, new int[]{android.R.id.text1, android.R.id.text2});
        lvSuggest.setAdapter(suggestAdapter);
        lvSuggest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < listData.size()) {
                    if (listData.get(position).get("obj")!=null) {
                        // 用户点击指定数据，直接在地图上用mark标识选定的POI数据
                        SuggestionResultObject.SuggestionData suggestionData = (SuggestionResultObject.SuggestionData) listData.get(position).get("obj");
                        Bundle bundle = new Bundle();
                        bundle.putString("suggestData", JSON.toJSONString(suggestionData));
                        setFragmentResult(SystemConstant.RESULT_CODE_SEARCH_LOCATION_SELECT_ONE, bundle);
                        pop();
                    } else {
                        // 用户点击获取更多，则使用keyword获取更多推荐数据，在地图上展示获取到的POI数据
                        String keyWord = edt_search_location.getText().toString();
                        Bundle bundle = new Bundle();
                        bundle.putString("keyword", keyWord);
                        setFragmentResult(SystemConstant.RESULT_CODE_SEARCH_LOCATION_GET_MORE, bundle);
                        pop();
                    }
                }
            }
        });
        // 首先获取当前位置的城市
        TencentLocation currentLocation = ((MainActivity)getActivity()).getCurrentLocation();

        edt_search_location.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String keyWord = s.toString();
                if (!StringUtils.isEmpty(keyWord)) {
                    SuggestionParam suggestionParam = new SuggestionParam();
                    suggestionParam.keyword(keyWord);
                    suggestionParam.region("北京市");
                    suggestionParam.regionFix(true);
                    suggestionParam.pageSize(20);
                    suggestionParam.location(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

                    tencentSearch.suggestion(suggestionParam, new HttpResponseListener<SuggestionResultObject>() {
                        @Override
                        public void onSuccess(int i, SuggestionResultObject resultObject) {
                            listData.clear();
                            if (resultObject == null||resultObject.count<=0) {
                                suggestAdapter.notifyDataSetChanged();
                                return;
                            }
                            List<SuggestionResultObject.SuggestionData> list = resultObject.data;
                            if (list!=null&&!list.isEmpty()) {
                                for (SuggestionResultObject.SuggestionData suggestionData: list) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("title", suggestionData.title);
                                    map.put("address", suggestionData.address);
                                    map.put("obj", suggestionData);
                                    listData.add(map);
                                }
                                Map<String, Object> map = new HashMap<>();
                                map.put("title", "获取更多+");
                                map.put("address", "点击获取更多");
                                map.put("obj", null);
                                listData.add(map);
                            }
                            suggestAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(int i, String s, Throwable throwable) {
                            listData.clear();
                            suggestAdapter.notifyDataSetChanged();
                            RxToast.error(s);
                        }
                    });
                } else {
                    listData.clear();
                    suggestAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}