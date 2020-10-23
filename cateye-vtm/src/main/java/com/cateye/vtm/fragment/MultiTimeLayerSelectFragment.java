package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.os.Message;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.cateye.android.entity.MapSourceFromNet;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.SystemConstant;
import com.xw.repo.BubbleSeekBar;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Created by xiaoxiao on 2018/8/31.
 */

public class MultiTimeLayerSelectFragment extends BaseFragment {
    private Spinner btn_layer_select;
    private ViewGroup layer_seekbar;
    List<MapSourceFromNet.DataBean> dataBeanList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (bundle != null) {
            dataBeanList = (ArrayList<MapSourceFromNet.DataBean>) bundle.getSerializable(SystemConstant.BUNDLE_MULTI_TIME_SELECTOR_DATA);
        }
    }

    public static BaseFragment newInstance(Bundle bundle) {
        MultiTimeLayerSelectFragment multiTimeLayerSelectFragment = new MultiTimeLayerSelectFragment();
        multiTimeLayerSelectFragment.setArguments(bundle);
        return multiTimeLayerSelectFragment;
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_multi_time_select;
    }

    @Override
    public void initView(View rootView) {
        btn_layer_select = rootView.findViewById(R.id.tv_multi_time_layer_name);
        layer_seekbar = rootView.findViewById(R.id.layer_multi_time_seekbar);

        if (dataBeanList != null && !dataBeanList.isEmpty()) {
            List<String> multiLayerNameList = new ArrayList<>();
            for (MapSourceFromNet.DataBean dataBean : dataBeanList) {
                multiLayerNameList.add(dataBean.getMemo());
            }

            if (!multiLayerNameList.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, multiLayerNameList);
                btn_layer_select.setAdapter(adapter);
                btn_layer_select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, final int group, long l) {
                        //用户选中某个选项，自动绘制右侧的滑动控件
                        BubbleSeekBar seekBar = new BubbleSeekBar(getContext());
                        seekBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        seekBar.getConfigBuilder()
                                .min(0.0f)
                                .max(dataBeanList.get(group).getMaps().size() - 1)
                                .progress(0)
                                .sectionCount(dataBeanList.get(group).getMaps().size() - 1)
                                .trackColor(ContextCompat.getColor(getContext(), R.color.slategray))
                                .secondTrackColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                                .thumbColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                                .showSectionText()
                                .sectionTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                                .sectionTextSize(18)
                                .showThumbText()
                                .thumbTextColor(ContextCompat.getColor(getContext(), R.color.red))
                                .thumbTextSize(18)
                                .bubbleColor(ContextCompat.getColor(getContext(), R.color.green))
                                .bubbleTextSize(18)
                                .hideBubble()
                                .showSectionMark()
                                .seekBySection()
                                .touchToSeek()//支持点击滑动
                                .autoAdjustSectionMark()//自动依附到最近的标志位上
                                .seekStepSection()
                                .sectionTextPosition(BubbleSeekBar.TextPosition.BELOW_SECTION_MARK)
                                .build();
                        seekBar.setCustomSectionTextArray(new BubbleSeekBar.CustomSectionTextArray() {
                            @NonNull
                            @Override
                            public SparseArray<String> onCustomize(int sectionCount, @NonNull SparseArray<String> array) {
                                for (int i = 0; i < dataBeanList.get(group).getMaps().size(); i++) {
                                    array.put(i, dataBeanList.get(group).getMaps().get(i).getRecordDate());
                                }
                                return array;
                            }
                        });
                        seekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
                            @Override
                            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                                //用户修改progress，自动替换指定的图层
                                Message msg = new Message();
                                msg.what = SystemConstant.MSG_WHAT_DRAW_LAYER_TIME_SELECT;
                                MapSourceFromNet.DataBean dataBean = dataBeanList.get(group);
                                msg.arg1 = dataBean.getId();//将当前图层的id传递给主Fragment
                                msg.arg2 = progress;
                                EventBus.getDefault().post(msg);
                            }

                            @Override
                            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

                            }

                            @Override
                            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

                            }
                        });
                        layer_seekbar.removeAllViews();
                        layer_seekbar.addView(seekBar);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }
            btn_layer_select.setSelection(0);
        }
    }
}
