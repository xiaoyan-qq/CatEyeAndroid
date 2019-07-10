package com.cateye.android.vtm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cateye.vtm.util.SystemConstant;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.Response;
import com.lzy.okrx2.adapter.ObservableResponse;
import com.vondear.rxtool.RxAnimationTool;
import com.vondear.rxtool.RxLogTool;
import com.vondear.rxtool.view.RxToast;
import com.vondear.rxui.view.dialog.RxDialogLoading;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by xiaoxiao on 2018/7/19.
 */

public class LoginActivity extends Activity {
    private EditText edt_name,edt_pwd;
    private TextView btn_login;
    private ImageView img_logo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        initView();

        //动画加载logo
        RxAnimationTool.popup(img_logo,1200);
        final RxDialogLoading rxDialogLoading = new RxDialogLoading(this);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName=edt_name.getText().toString().trim();
                String passWord = edt_pwd.getText().toString().trim();
                OkGo.<String>post(SystemConstant.URL_LOGIN).params("username",userName).params("password",passWord).tag(this).converter(new StringConvert()).adapt(new ObservableResponse<String>()).subscribeOn(Schedulers.newThread()).doOnSubscribe(new Consumer<Disposable>() {
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
                        if (stringResponse!=null&&stringResponse.body()!=null){
                            System.out.println(stringResponse.body());

                            Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(mainIntent);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        RxToast.error("请检查网络!");
                        RxLogTool.saveLogFile(e.toString());
                    }

                    @Override
                    public void onComplete() {
                        rxDialogLoading.dismiss();
                    }
                });
            }
        });
    }

    private void initView(){
        img_logo= (ImageView) findViewById(R.id.img_login_logo);
        edt_name= (EditText) findViewById(R.id.edt_login_userName);
        edt_pwd= (EditText) findViewById(R.id.edt_login_pwd);
        btn_login= (TextView) findViewById(R.id.btn_login);
    }
}
