package com.sunday.speeddial.adapter;

import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by Administrator on 2018/8/5.
 */

public class YolandaItemTouchHelper extends ItemTouchHelper {

    private Callback mCallback;

    public YolandaItemTouchHelper(Callback callback) {
        super(callback);
        mCallback = callback;
    }

    public Callback getCallback() {
        return mCallback;
    }
}
