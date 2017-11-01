package com.zh.xplan.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Fragment 基类   可以在此基类上扩展
 */
public class BaseFragment extends Fragment {
    protected Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void showShortToast(String message){
        Toast.makeText(mActivity.getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    protected void showLongToast(String message){
        Toast.makeText(mActivity.getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }
}
