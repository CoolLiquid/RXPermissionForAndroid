package com.weinp.tgnet.permission;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.subjects.PublishSubject;

/**
 * Created by weinp
 * on 2017/11/22.
 */

public class RxPermissionFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_CODE = 42;

    private Map<String, PublishSubject<Permission>> mSubjects = new HashMap<>();
    private int i = 0;

    public RxPermissionFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @TargetApi(Build.VERSION_CODES.M)
    void requestPermissions(@NonNull String[] permissions) {
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSIONS_REQUEST_CODE) {
            return;
        }

        boolean[] shouldShowRequestPermissionRationale = new boolean[permissions.length];

        for (int i = 0; i < permissions.length; i++) {
            shouldShowRequestPermissionRationale[i] = shouldShowRequestPermissionRationale(permissions[i]);
        }

        onRequestPermissionsResult(permissions, grantResults, shouldShowRequestPermissionRationale);
    }

    void onRequestPermissionsResult(String permissions[], int[] grantResults, boolean[] shouldShowRequestPermissionRationale) {
        Logutil.log("[OnRequestPermissionsResult---Do count=]" + (++this.i));
        for (int i = 0, size = permissions.length; i < size; i++) {

            PublishSubject<Permission> subject = mSubjects.get(permissions[i]);

            if (subject == null) {
                return;
            }

            mSubjects.remove(permissions[i]);
            boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            subject.onNext(new Permission(permissions[i], granted, shouldShowRequestPermissionRationale[i]));
            subject.onComplete();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    boolean isGranted(String permission) {
        return getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    boolean isRevoke(String permission) {
        return getActivity().getPackageManager().isPermissionRevokedByPolicy(permission, getActivity().getPackageName());
    }

    public PublishSubject<Permission> getSubjectByPermission(String permission) {
        return mSubjects.get(permission);
    }

    public boolean containsByPermission(String permission) {
        return mSubjects.containsKey(permission);
    }

    public PublishSubject<Permission> setSubjectForPermission(String permission, PublishSubject<Permission> subject) {
        return mSubjects.put(permission, subject);
    }

    @TargetApi(Build.VERSION_CODES.M)
    boolean isRevoked(String permission) {
        return getActivity().getPackageManager().isPermissionRevokedByPolicy(permission, getActivity().getPackageName());
    }
}
