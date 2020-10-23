package com.cateye.android.vtm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.cateye.vtm.util.SystemConstant;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.Response;
import com.lzy.okrx2.adapter.ObservableResponse;
import com.vondear.rxtool.RxDataTool;
import com.vondear.rxtool.RxLogTool;
import com.vondear.rxtool.RxSPTool;
import com.vondear.rxtool.view.RxToast;
import com.vondear.rxui.view.dialog.RxDialogLoading;
import com.vondear.rxui.view.dialog.RxDialogSure;
import com.vondear.rxui.view.dialog.RxDialogSureCancel;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by xiaoxiao on 2018/7/19.
 */

public class LoginActivity extends Activity {
    private EditText edt_name, edt_pwd;
    private TextView btn_login;
    private ImageView img_logo;
    private CheckBox chk_remember_pwd;//记住密码的多选框

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        initView();

        //读取缓存，如果有缓存的用户名和密码，自动填写
        String userNameStr = RxSPTool.getContent(LoginActivity.this, SystemConstant.SP_LOGIN_USERNAME);
        if (!RxDataTool.isEmpty(userNameStr)) {
            edt_name.setText(userNameStr);
        }
        String pwdStr = RxSPTool.getContent(LoginActivity.this, SystemConstant.SP_LOGIN_PWD);
        if (!RxDataTool.isEmpty(pwdStr)) {
            try {
                edt_pwd.setText(pwdStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        chk_remember_pwd.setChecked(RxSPTool.getBoolean(LoginActivity.this, SystemConstant.SP_LOGIN_PWD_IS_REMEMBER));

        //动画加载logo
//        RxAnimationTool.popup(img_logo, 1200);
        final RxDialogLoading rxDialogLoading = new RxDialogLoading(this);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = edt_name.getText().toString().trim();
                String passWord = edt_pwd.getText().toString().trim();
                OkGo.<String>post(SystemConstant.URL_LOGIN).params("username", userName).params("password", passWord).tag(this).converter(new StringConvert()).adapt(new ObservableResponse<String>()).subscribeOn(Schedulers.newThread()).doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        rxDialogLoading.show();
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Response<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<String> stringResponse) {
                        if (stringResponse != null && stringResponse.body() != null) {
                            System.out.println(stringResponse.body());
                            Map<String, Object> loginResponseBody = (Map<String, Object>) JSON.parse(stringResponse.body());
                            if (loginResponseBody != null && loginResponseBody.get("data") != null) {
                                //将token设置为okGO的全局变量
                                HttpHeaders headers = new HttpHeaders();
                                headers.put("Authorization", loginResponseBody.get("data").toString());
                                OkGo.getInstance().addCommonHeaders(headers);

                                //记录用户名
                                RxSPTool.putContent(LoginActivity.this, SystemConstant.SP_LOGIN_USERNAME, edt_name.getText().toString());
                                if (chk_remember_pwd.isChecked()) {
                                    String pwdStr = edt_pwd.getText().toString();
                                    if (!RxDataTool.isEmpty(pwdStr)) {
                                        try {
                                            RxSPTool.putContent(LoginActivity.this, SystemConstant.SP_LOGIN_PWD, pwdStr);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    RxSPTool.remove(LoginActivity.this, SystemConstant.SP_LOGIN_PWD);
                                }

                                RxSPTool.putBoolean(LoginActivity.this, SystemConstant.SP_LOGIN_PWD_IS_REMEMBER, chk_remember_pwd.isChecked());//记录密码的勾选框是否选中的缓存

                                //申请所需要的权限
                                AndPermission.with(LoginActivity.this).permission(Permission.Group.LOCATION/*定位权限*/, Permission.Group.STORAGE/*存储权限*/, Permission.Group.CAMERA /*, Permission.Group.PHONE*//*电话相关权限*//*, Permission.Group.MICROPHONE*//*录音权限*/)
                                        .onGranted(new Action() {//用户允许
                                            @Override
                                            public void onAction(List<String> permissions) {
                                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(mainIntent);
                                                LoginActivity.this.finish();
                                            }
                                        })
                                        .onDenied(new Action() {//用户拒绝
                                            @Override
                                            public void onAction(List<String> permissions) {
                                                if (permissions != null && !permissions.isEmpty()) {
                                                    String permissionSB = getPermissionStr(permissions);
                                                    // 这些权限被用户总是拒绝。
                                                    RxDialogSure sureDialog = new RxDialogSure(LoginActivity.this);
                                                    sureDialog.setContent("您拒绝了" + permissionSB + "权限，程序无法正常使用，请重新授予程序权限!");
                                                    sureDialog.setTitle("提示");
                                                    sureDialog.getSureView().setEnabled(true);
                                                    sureDialog.setSureListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            sureDialog.dismiss();
                                                        }
                                                    });
                                                    sureDialog.show();
                                                }
                                            }
                                        }).rationale(new Rationale() {
                                    @Override
                                    public void showRationale(Context context, List<String> permissions, RequestExecutor executor) {
                                        RxDialogSureCancel sureCancelDialog = new RxDialogSureCancel(LoginActivity.this);
                                        String permissionSB = getPermissionStr(permissions);
                                        sureCancelDialog.setContent("程序需要" + permissionSB + "等权限才可以正常运行，请授权程序获取权限!");
                                        sureCancelDialog.setTitle("提示");
                                        sureCancelDialog.getSureView().setEnabled(true);
                                        sureCancelDialog.setCancelListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                executor.cancel();
                                                sureCancelDialog.dismiss();
                                            }
                                        });
                                        sureCancelDialog.setSureListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                executor.execute();
                                                sureCancelDialog.dismiss();
                                            }
                                        });
                                        sureCancelDialog.show();
                                    }
                                }).start();
                            } else {
                                RxToast.error("无法获取用户token！");
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        RxToast.error("请检查网络!");
                        RxLogTool.saveLogFile(e.toString());
                        rxDialogLoading.dismiss();
                    }

                    @Override
                    public void onComplete() {
                        rxDialogLoading.dismiss();
                    }
                });
            }
        });
    }

    private String getPermissionStr(List<String> permissions) {
        StringBuilder permissionSB = new StringBuilder();
        if (permissions != null && !permissions.isEmpty()) {
            for (String p : permissions) {
                permissionSB.append(Permission.transformText(LoginActivity.this, p));
                permissionSB.append(",");
            }
        }
        if (permissionSB.toString().endsWith(",")) {
            permissionSB.delete(permissionSB.length() - 1, permissionSB.length());
        }
        return permissionSB.toString();
    }

    private void initView() {
        img_logo = (ImageView) findViewById(R.id.img_login_logo);
        edt_name = (EditText) findViewById(R.id.edt_login_userName);
        edt_pwd = (EditText) findViewById(R.id.edt_login_pwd);
        btn_login = (TextView) findViewById(R.id.btn_login);
        chk_remember_pwd = findViewById(R.id.chk_remeberPwd);
    }
}
