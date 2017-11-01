package com.zh.xplan.ui.pulltorefresh.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by cuieney on 17/2/26.
 */

public class RVBean implements Parcelable, MultiItemEntity {
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
    }

    public RVBean() {
    }

    protected RVBean(Parcel in) {
        this.type = in.readString();
    }

    public static final Creator<RVBean> CREATOR = new Creator<RVBean>() {
        @Override
        public RVBean createFromParcel(Parcel source) {
            return new RVBean(source);
        }

        @Override
        public RVBean[] newArray(int size) {
            return new RVBean[size];
        }
    };

    @Override
    public String toString() {
        return "ItemListBean{" +
                "type='" + type + '\'' +
                '}';
    }

    @Override
    public int getItemType() {
        return 0;
    }
}
