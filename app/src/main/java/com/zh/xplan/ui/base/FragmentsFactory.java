package com.zh.xplan.ui.base;

import android.util.SparseArray;
import com.zh.xplan.ui.menupicture.PictureFragment;
import com.zh.xplan.ui.menusetting.SettingFragment;

/**
 * Fragments工厂
 * Created by zh
 */
public class FragmentsFactory  {

    public static final int MENU1 = 0;
    public static final int MENU2 = 1;
    // private static Map<Integer, BaseFragment> mFragments = new HashMap<Integer, BaseFragment>();
    // android  APi SparseArray代替HashMap 更为高效
    private static SparseArray<BaseFragment> mFragments = new SparseArray<BaseFragment>();
    
    public static BaseFragment createFragment(int position) {
        BaseFragment fragment = mFragments.get(position);
        if(fragment == null){
            switch (position) {
                case MENU1:
                    fragment = new PictureFragment();
                    break;
                case MENU2:
                    fragment = new SettingFragment();
                    break;
                default:
                    break;
            }
            mFragments.put(position, fragment);
        }
        return fragment;
    }

    public static BaseFragment getFragment(int position) {
        return mFragments.get(position,null);
    }
}
