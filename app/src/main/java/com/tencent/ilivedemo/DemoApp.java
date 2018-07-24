package com.tencent.ilivedemo;

import android.app.Application;

import com.tencent.ilivedemo.model.MessageObservable;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.adapter.CommonConstants;
import com.tencent.ilivesdk.core.ILiveLog;
import com.tencent.livesdk.ILVLiveConfig;
import com.tencent.livesdk.ILVLiveManager;
import com.tencent.qalsdk.sdk.MsfSdkUtils;

/**
 * Created by xkazerzhang on 2017/5/23.
 */
public class DemoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if(MsfSdkUtils.isMainProcess(this)){    // 仅在主线程初始化
            // 初始化LiveSDK
            ILiveSDK.getInstance().setCaptureMode(ILiveConstants.CAPTURE_MODE_SURFACETEXTURE);
            ILiveLog.setLogLevel(ILiveLog.TILVBLogLevel.DEBUG);
            ILiveSDK.getInstance().initSdk(this, 1400028096, 11851);
            // 老用户使用IMSDK通道
            ILiveSDK.getInstance().setChannelMode(CommonConstants.E_ChannelMode.E_ChannelIMSDK);
            ILVLiveManager.getInstance().init(new ILVLiveConfig()
                    .setLiveMsgListener(MessageObservable.getInstance()));
        }
    }
}
