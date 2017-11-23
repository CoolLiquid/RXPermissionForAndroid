package com.weinp.tgnet.customesimplepermissionapplication;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.weinp.tgnet.permission.Logutil;
import com.weinp.tgnet.permission.Permission;
import com.weinp.tgnet.permission.RxPermissions;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        log("onCrate");

        RxPermissions permissions = new RxPermissions(this);
        permissions
                .requestEach(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Permission>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Permission permission) {
                        Logutil.log("[permission---->result]" + permission.toString());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        log("onStop");
    }

    void log(String content) {
        Log.i("wnp", content);
    }
}
