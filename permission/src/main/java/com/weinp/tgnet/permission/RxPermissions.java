package com.weinp.tgnet.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by weinp
 * on 2017/11/22.
 */

public class RxPermissions {

    public static final String TAG = "RxPermissions";
    public static final Object TRIGER = new Object();

    public RxPermissionFragment mRxPermissionsFragment;
    private int i = 0;

    public RxPermissions(Activity activity) {
        mRxPermissionsFragment = getRxPermissionsFragment(activity);
    }

    private RxPermissionFragment getRxPermissionsFragment(Activity activity) {
        RxPermissionFragment rxPermissionsFragment = null;
        try {
            rxPermissionsFragment = findRxPermissionsFragment(activity);
            boolean isNewInstance = rxPermissionsFragment == null;
            if (isNewInstance) {
                rxPermissionsFragment = new RxPermissionFragment();
                android.app.FragmentManager fragmentManager = activity.getFragmentManager();
                fragmentManager
                        .beginTransaction()
                        .add(rxPermissionsFragment, TAG)
                        .commitAllowingStateLoss();
                fragmentManager.executePendingTransactions();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rxPermissionsFragment;
    }

    private RxPermissionFragment findRxPermissionsFragment(Activity activity) {
        return (RxPermissionFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }

    public Observable<Boolean> request(final String... permission) {
        return Observable.just(TRIGER).compose(ensure(permission));
    }

    public Observable<Permission> requestEach(final String... permission) {
        return Observable.just(TRIGER).compose(ensureEach(permission));
    }

    private <T> ObservableTransformer<T, Permission> ensureEach(final String[] permission) {
        return new ObservableTransformer<T, Permission>() {
            @Override
            public ObservableSource<Permission> apply(Observable<T> upstream) {
                return request(upstream, permission);
            }
        };
    }

    public <T> ObservableTransformer<T, Boolean> ensure(final String... permission) {

        return new ObservableTransformer<T, Boolean>() {
            @Override
            public ObservableSource<Boolean> apply(Observable<T> upstream) {
                return request(upstream, permission)
                        .buffer(permission.length)
                        .flatMap(new Function<List<Permission>, ObservableSource<Boolean>>() {
                            @Override
                            public ObservableSource<Boolean> apply(List<Permission> permissions) throws Exception {
                                Logutil.log("[#flatMap-----Method:apply--->DO COUNT=]" + (++i) + "permission.size=" + permissions.size());
                                if (permission.length == 0) {
                                    return Observable.empty();
                                }

                                for (Permission p : permissions) {
                                    //p.granted表示这个Permission是否已经允许，如果没有被允许就返回false
                                    //只要其中一个Permission不被允许那么，就返回false
                                    if (!p.granted) {
                                        Logutil.log("[#flatMap-----Method:apply--->had no reject]");
                                        return Observable.just(false);
                                    }
                                }
                                Logutil.log("[#flatMap-----Method:apply--->had all reject]");
                                return Observable.just(true);
                            }
                        });
            }
        };
    }

    private Observable<Permission> request(final Observable<?> triger, final String... permission) {
        if (permission == null || permission.length == 0) {
            throw new IllegalArgumentException("RxPermissions.request/requestEach requires at least one input permission");
        }
        return requestImplementation(permission);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private Observable<Permission> requestImplementation(String... permission) {
        List<Observable<Permission>> list = new ArrayList<>();
        List<String> unregisterPermission = new ArrayList<>();
        Observable<Permission> observable = null;

        List<String> list_permission = Arrays.asList(permission);

        for (String in_permission : list_permission) {

            if (isGranted(in_permission)) {
                observable = Observable.just(new Permission(in_permission, true, false));
                list.add(observable);
                continue;
            }

            if (isRevoked(in_permission)) {
                observable = Observable.just(new Permission(in_permission, false, false));
                list.add(observable);
                continue;
            }

            PublishSubject<Permission> subject = mRxPermissionsFragment.getSubjectByPermission(in_permission);
            if (subject == null) {
                subject = PublishSubject.create();
                mRxPermissionsFragment.setSubjectForPermission(in_permission, subject);
                unregisterPermission.add(in_permission);
            }
            list.add(subject);
        }

        if (unregisterPermission.size() > 0) {
            String[] array = unregisterPermission.toArray(new String[unregisterPermission.size()]);
            requestPermissionsFromFragment(array);
        }

        return Observable.concat(Observable.fromIterable(list));

    }

    @TargetApi(Build.VERSION_CODES.M)
    void requestPermissionsFromFragment(String[] permissions) {
        mRxPermissionsFragment.requestPermissions(permissions);
    }

    public boolean isGranted(String permission) {
        return !isMarshmallow() || mRxPermissionsFragment.isGranted(permission);
    }

    boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }


    @SuppressWarnings("WeakerAccess")
    public boolean isRevoked(String permission) {
        return isMarshmallow() && mRxPermissionsFragment.isRevoked(permission);
    }
}
