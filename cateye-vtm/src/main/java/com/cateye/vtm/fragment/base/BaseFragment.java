package com.cateye.vtm.fragment.base;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cateye.vtm.fragment.CatEyeMainFragment;
import com.vtm.library.utils.SystemConstant;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * Created by zhangdezhi1702 on 2018/3/15.
 */

//@Puppet
public abstract class BaseFragment extends SupportFragment implements BaseFragmentInterface {
    protected View rootView;//当前fragment的根View
    protected Context mContext;
    protected Bundle bundle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bundle = getArguments();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(getFragmentLayoutId(), null);
        initView(rootView);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    protected View findViewById(@IdRes int id) {
        return rootView.findViewById(id);
    }

    protected String getFragmentTag() {
        return getTag();
    }

    protected void setMainFragmentAreaVisible(CatEyeMainFragment.BUTTON_AREA button_area, boolean isVisible) {//设置主界面中某些区域的显隐状态
        //通知主界面隐藏部分重新显示
        Message visibleMsg = new Message();
        visibleMsg.what = SystemConstant.MSG_WHAT_MAIN_AREA_HIDEN_VISIBLE;
        Bundle bundle = new Bundle();
        bundle.putSerializable(SystemConstant.BUNDLE_BUTTON_AREA, button_area);
        bundle.putBoolean(SystemConstant.BUNDLE_AREA_HIDEN_STATE, isVisible);
        visibleMsg.setData(bundle);
        EventBus.getDefault().post(visibleMsg);
    }

    @Override
    public boolean onBackPressedSupport() {
        pop();
        return true;
    }

}
