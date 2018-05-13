package com.example.shizhuan.upload;

import android.support.v4.app.Fragment;

import com.kcode.lib.dialog.UpdateActivity;

/**
 * Created by ShiZhuan on 2018/5/8.
 */

public class CustomsUpdateActivity extends UpdateActivity
{
    protected Fragment getUpdateDialogFragment()
    {
        return CustomsUpdateFragment.newInstance(this.mModel,"发现新版本",true);
//        return super.getUpdateDialogFragment();
    }
}
