package com.cateye.vtm.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.cateye.android.entity.DrawPointLinePolygonEntity;
import com.cateye.android.vtm.LoginActivity;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.util.SystemConstant;
import com.vondear.rxtool.RxDataTool;
import com.vondear.rxtool.RxFileTool;
import com.vondear.rxtool.RxPhotoTool;
import com.vondear.rxtool.RxPictureTool;
import com.vondear.rxtool.RxSPTool;
import com.vondear.rxtool.RxTextTool;
import com.vondear.rxtool.view.RxToast;
import com.vondear.rxui.view.dialog.RxDialog;
import com.vondear.rxui.view.dialog.RxDialogScaleView;
import com.vondear.rxui.view.dialog.RxDialogSureCancel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.oscim.event.Event;
import org.xutils.ex.DbException;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DrawPointLinePolygonDialog {
    private RxDialog currentDialog;
    private Context mContext;
    private static DrawPointLinePolygonDialog instance;

    private BootstrapEditText edt_name,edt_remark;
    private BootstrapButton btn_save,btn_cancel;
    private RecyclerView rcv_img;
    private View layer_photo;
    private ImgRecycleAdapter recycleAdapter;
    private List<String> imgUrlList;
    private Button btn_addImg;
    private ImageOptions imageOptions;
    private DrawPointLinePolygonEntity currentEntity;

    private void initView(Context mContext) {
        this.mContext = mContext;
        this.currentDialog = new RxDialog(mContext);
        this.currentDialog.setContentView(R.layout.dialog_draw_point_line_polygon_info);
        this.layer_photo=this.currentDialog.findViewById(R.id.draw_photo_layer);
        this.edt_name = this.currentDialog.findViewById(R.id.edt_name);
        this.edt_remark = this.currentDialog.findViewById(R.id.edt_remark);
        this.btn_addImg = this.currentDialog.findViewById(R.id.btn_addImg);
        this.rcv_img=this.currentDialog.findViewById(R.id.rcv_img);
        this.btn_save=this.currentDialog.findViewById(R.id.btn_save);
        this.btn_cancel=this.currentDialog.findViewById(R.id.btn_cancel);

        this.imgUrlList = new ArrayList<>();
        this.recycleAdapter = new ImgRecycleAdapter(this.mContext,imgUrlList);
        this.rcv_img.setAdapter(this.recycleAdapter);
        RecyclerView.LayoutManager gridLayoutManager=new GridLayoutManager(this.mContext,3,GridLayoutManager.VERTICAL,false);
        this.rcv_img.setLayoutManager(gridLayoutManager);
        this.imageOptions = new ImageOptions.Builder().setFailureDrawableId(R.drawable.ic_launcher).setFadeIn(true).setForceLoadingDrawable(true).setImageScaleType(ImageView.ScaleType.FIT_CENTER).setUseMemCache(true).build();
    }

    public static DrawPointLinePolygonDialog getInstance(Context mContext){
        if (instance==null){
            instance = new DrawPointLinePolygonDialog();
        }
        instance.mContext = mContext;
        instance.initView(mContext);
        return instance;
    }

    public void showDialog(BaseDrawFragment.DRAW_STATE draw_state, DrawPointLinePolygonEntity entity){
        this.currentEntity = entity;
        if (draw_state == BaseDrawFragment.DRAW_STATE.DRAW_POINT){
            this.layer_photo.setVisibility(View.VISIBLE);
            this.btn_addImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (instance.imgUrlList!=null && instance.imgUrlList.size() >= 9){
                        return;
                    }
                    RxPhotoTool.openCameraImage((MainActivity)mContext);
                }
            });
        }else {
            this.layer_photo.setVisibility(View.GONE);
        }

        if (!EventBus.getDefault().isRegistered(instance)){
            EventBus.getDefault().register(instance);
        }
        this.currentDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (EventBus.getDefault().isRegistered(instance)){
                    EventBus.getDefault().unregister(instance);
                }
            }
        });

        this.btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawPointLinePolygonDialog.this.currentDialog.dismiss();
                if (instance.currentEntity!=null&&instance.currentEntity.getGeometry()!=null&&instance.currentEntity.get_id() <= 0 /*id小于0，说明是新增过程*/){
                    // 通知主界面，从地图上删除指定的元素
                    Message msg = Message.obtain();
                    msg.what= SystemConstant.MSG_WHAT_DELETE_DRAW_DATA;
                    msg.obj = instance.currentEntity.getGeometry();
                    EventBus.getDefault().post(msg);
                }
            }
        });

        this.btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RxDataTool.isNullString(edt_name.getText().toString())) {
                    RxToast.error("名称不能为空！");
                    return;
                }
                if (instance.currentEntity == null) {
                    instance.currentEntity=new DrawPointLinePolygonEntity();
                }
                instance.currentEntity.setName(edt_name.getText().toString().trim());
                instance.currentEntity.setRemark(edt_remark.getText().toString().trim());
                instance.currentEntity.setProjectId(SystemConstant.CURRENT_PROJECTS_ID+"");
                if (instance.rcv_img.isShown()){
                    instance.currentEntity.setImgUrlList(DrawPointLinePolygonDialog.this.imgUrlList);
                }else {
                    instance.currentEntity.setImgUrlList(null);
                }
                instance.currentEntity.setUserName(RxSPTool.getContent(mContext, SystemConstant.SP_LOGIN_USERNAME));
                try {
                    ((MainActivity)mContext).getDbManager().saveOrUpdate(instance.currentEntity);
                    // 保存当前用户填写的信息
                    DrawPointLinePolygonDialog.this.currentDialog.dismiss();
                    RxToast.info("保存成功");
                } catch (DbException e) {
                    e.printStackTrace();
                    RxToast.error("保存失败，请重试！如果多次失败，请退出程序后重试！");
                }
            }
        });

        if (entity!=null){
            if (!RxDataTool.isNullString(entity.getName())) {
                edt_name.setText(entity.getName());
            }
            if (!RxDataTool.isNullString(entity.getRemark())) {
                edt_remark.setText(entity.getRemark());
            }
            if (entity.getImgUrlList()!=null&&!entity.getImgUrlList().isEmpty()){
                for (String imgUrl:entity.getImgUrlList()) {
                    this.imgUrlList.add(imgUrl);
                }
                this.recycleAdapter.notifyDataSetChanged();
            }
        }
        this.currentDialog.show();
    }

    private class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView img;
        public ImageView img_delete;

        public ViewHolder(View itemView) {
            super(itemView);
            img=itemView.findViewById(R.id.img_draw_photo);
            img_delete=itemView.findViewById(R.id.img_delete);
        }
    }

    private class ImgRecycleAdapter extends RecyclerView.Adapter<ViewHolder>{
        private Context mContext;
        private List<String> imgUrlList;

        public ImgRecycleAdapter(Context mContext, List<String> imgUrlList) {
            this.mContext = mContext;
            this.imgUrlList = imgUrlList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View imgViewHolder = LayoutInflater.from(this.mContext).inflate(R.layout.item_draw_photo, null);
            ViewHolder vh=new ViewHolder(imgViewHolder);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder,final int position) {
            x.image().bind(holder.img, imgUrlList.get(position),DrawPointLinePolygonDialog.this.imageOptions);
            holder.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri imgUri = null;
                    if (!imgUrlList.get(position).startsWith("http")){
                        imgUri=RxFileTool.getUriForFile(mContext,new File(imgUrlList.get(position)));
                    } else {
                        imgUri=Uri.parse(imgUrlList.get(position));
                    }
                    RxDialogScaleView imgDialog = new RxDialogScaleView(mContext,imgUri);
                    imgDialog.show();
                }
            });

            holder.img_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final RxDialogSureCancel confirmDialog = new RxDialogSureCancel(mContext);
                    confirmDialog.setTitle("确认");
                    confirmDialog.setContent("确认删除此照片吗？");
                    confirmDialog.setCancelListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmDialog.dismiss();
                        }
                    });
                    confirmDialog.setSureListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmDialog.dismiss();

                            if (!imgUrlList.get(position).startsWith("http")){
//                                boolean deleteResult=RxFileTool.deleteFile(imgUrlList.get(position));
//                                if (deleteResult){
                                    imgUrlList.remove(position);
                                    ImgRecycleAdapter.this.notifyDataSetChanged();
//                                }else {
//                                    RxToast.error("删除失败，请重试!");
//                                }
                            }
                        }
                    });
                    confirmDialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            if (imgUrlList!=null){
                return imgUrlList.size();
            }
            return 0;
        }
    }

    @Subscribe
    public void onEventMainThread(Message msg) {
        switch (msg.what) {
            case SystemConstant.MSG_WHAT_DRAW_PHOTO_FINISH:
                if (msg.obj!=null&&this.currentDialog!=null&&this.currentDialog.isShowing()){
                    this.imgUrlList.add((String) msg.obj);
                    this.recycleAdapter.notifyDataSetChanged();
                }
                break;
        }
    }
}
