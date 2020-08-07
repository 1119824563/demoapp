package com.het.supordemoapp.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import androidx.appcompat.app.AlertDialog;

/**
 * ---------------------------------------------------------------- <br>
 * Copyright (C) 2014-2020, by het, Shenzhen, All rights reserved.  <br>
 * ---------------------------------------------------------------- <br>
 * <p>
 * 描述: NfcUtils <br>
 * 作者: lei <br>
 * 日期: 2020/8/5 <br>
 */
public class NfcUtils {
    //nfc
    public static NfcAdapter mNfcAdapter;
    public static IntentFilter[] mIntentFilter = null;
    public static PendingIntent mPendingIntent = null;
    public static String[][] mTechList = null;
    /**
     * NFC i/o操作类
     */
    public static IsoDep isodep = null;

    public NfcUtils(Activity activity) {
        mNfcAdapter = NfcCheck(activity);
        NfcInit(activity);
    }

    public static void init(Activity activity) {
        mNfcAdapter = NfcCheck(activity);
        NfcInit(activity);
    }

    /**
     * 检查NFC是否打开
     */
    public static NfcAdapter NfcCheck(Activity activity) {
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (mNfcAdapter == null) {
            Toast.makeText(activity, "设备不支持NFC功能!", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            if (!mNfcAdapter.isEnabled()) {
                IsToSet(activity);
            } else {
                Toast.makeText(activity, "NFC功能已打开!", Toast.LENGTH_SHORT).show();
            }
        }
        return mNfcAdapter;
    }

    /**
     * 初始化nfc设置
     */
    public static void NfcInit(Activity activity) {
        Intent intent = new Intent(activity, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
        //--------------lei-s------------
        //做一个IntentFilter过滤你想要的action 这里过滤的是ndef
        // IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        //如果你对action的定义有更高的要求，比如data的要求，你可以使用如下的代码来定义intentFilter
        //        IntentFilter filter2 = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        //        try {
        //            filter.addDataType("*/*");
        //        } catch (IntentFilter.MalformedMimeTypeException e) {
        //            e.printStackTrace();
        //        }
        //        mIntentFilter = new IntentFilter[]{filter, filter2};
        //        mTechList = null;
        // try {
        //     filter.addDataType("*/*");
        // } catch (IntentFilter.MalformedMimeTypeException e) {
        //     e.printStackTrace();
        // }
        // mTechList = new String[][]{{MifareClassic.class.getName()},
        //         {NfcA.class.getName()}};
        // //生成intentFilter
        // mIntentFilter = new IntentFilter[]{filter};
        //-----------lei-e----------
        try {

            mTechList = new String[][]{
                    {IsoDep.class.getName()}, {NfcV.class.getName()},
                    {NfcF.class.getName()},};

            mIntentFilter = new IntentFilter[]{new IntentFilter(
                    NfcAdapter.ACTION_TECH_DISCOVERED, "*/*")};

        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
            Logc.e("debug", "onResume-exception");
        }
    }

    /***
     * isoDep类型发送数据
     *
     * @param cmd 发送内容
     * @return 返回数据
     */
    public static byte[] isoTransceive(byte[] cmd) {
        if (null == isodep) {
            Logc.e("请刷到LED屏再试");
            return null;
        }
        byte[] response;
        try {
            response = isodep.transceive(cmd);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            Logc.e("无响应,请检查卡片位置是否放置正确");
            return null;
        }
    }

    /**
     * 读取NFC的数据
     */
    public static String readNFCFromTag(Intent intent) throws UnsupportedEncodingException {
        Parcelable[] rawArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawArray != null) {
            NdefMessage mNdefMsg = (NdefMessage) rawArray[0];
            NdefRecord mNdefRecord = mNdefMsg.getRecords()[0];
            if (mNdefRecord != null) {
                String readResult = new String(mNdefRecord.getPayload(), "UTF-8");
                return readResult;
            }
        }
        return "";
    }


    /**
     * 往nfc写入数据
     */
    public static void writeNFCToTag(String data, Intent intent) throws IOException,
            FormatException {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        NdefRecord ndefRecord = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ndefRecord = NdefRecord.createTextRecord(null, data);
        }
        NdefRecord[] records = {ndefRecord};
        NdefMessage ndefMessage = new NdefMessage(records);
        ndef.writeNdefMessage(ndefMessage);
    }

    /**
     * 读取nfcID
     */
    public static String readNFCId(Intent intent) throws UnsupportedEncodingException {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String id = ByteArrayToHexString(tag.getId());
        return id;
    }

    /**
     * 将字节数组转换为字符串
     */
    private static String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E"
                , "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    private static void IsToSet(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("是否跳转到设置页面打开NFC功能");
        //        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                goToSet(activity);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private static void goToSet(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
            // 进入设置系统应用权限界面
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            activity.startActivity(intent);
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 运行系统在5.x环境使用
            // 进入设置系统应用权限界面
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            activity.startActivity(intent);
            return;
        }
    }

}
