package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.SystemConstant;
import com.vtm.library.tools.TileDownloadRectDrawView;

import org.greenrobot.eventbus.EventBus;

public class DrawDownloadTileFragment extends BaseFragment {
    private BootstrapButton bbtn_finish;
    private TileDownloadRectDrawView tileDownloadRectDrawView;

    public static BaseFragment newInstance(Bundle bundle) {
        DrawDownloadTileFragment drawDownloadTileFragment = new DrawDownloadTileFragment();
        drawDownloadTileFragment.setArguments(bundle);
        return drawDownloadTileFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //当前界面显示时，自动隐藏主界面的所有按钮
        Message msg=new Message();
        msg.what=SystemConstant.MSG_WHAT_DRAW_TILE_DOWNLOAD_RECT_START;
        EventBus.getDefault().post(msg);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        //绘制结束，将绘制的rect传递给主界面，开始下载rect对应的tile数据
        Message msg=new Message();
        msg.what=SystemConstant.MSG_WHAT_DRAW_TILE_DOWNLOAD_RECT_FINISH;
        msg.obj=tileDownloadRectDrawView.getRect();
        EventBus.getDefault().post(msg);
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_draw_download_tile;
    }

    @Override
    public void initView(View rootView) {
        bbtn_finish = rootView.findViewById(R.id.bbtn_tile_download_finish);
        tileDownloadRectDrawView = rootView.findViewById(R.id.rect_draw_tile_download);
        bbtn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawDownloadTileFragment.this.onBackPressedSupport();
            }
        });
    }
}
