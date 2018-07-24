package com.tencent.ilivedemo.demos;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tencent.TIMMessage;
import com.tencent.TIMUserProfile;
import com.tencent.av.sdk.AVVideoCtrl;
import com.tencent.ilivedemo.R;
import com.tencent.ilivedemo.model.Constants;
import com.tencent.ilivedemo.model.MessageObservable;
import com.tencent.ilivedemo.model.StatusObservable;
import com.tencent.ilivedemo.model.UserInfo;
import com.tencent.ilivedemo.uiutils.DemoFunc;
import com.tencent.ilivedemo.uiutils.DlgMgr;
import com.tencent.ilivedemo.view.DemoEditText;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.adapter.CommonConstants;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.core.ILiveRoomOption;
import com.tencent.ilivesdk.tools.quality.ILiveQualityData;
import com.tencent.ilivesdk.tools.quality.LiveInfo;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.ilivesdk.view.ILiveRootView;
import com.tencent.livesdk.ILVCustomCmd;
import com.tencent.livesdk.ILVLiveConfig;
import com.tencent.livesdk.ILVLiveManager;
import com.tencent.livesdk.ILVLiveRoomOption;
import com.tencent.livesdk.ILVText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by xkazerzhang on 2017/5/24.
 */
public class DemoRender extends Activity implements ILiveLoginManager.TILVBStatusListener{
    private final String TAG = "DemoRender";

    // 最多展示三路画面
    private ILiveRootView irvArr[] = new ILiveRootView[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_render);

        UserInfo.getInstance().getCache(getApplicationContext());

        irvArr[0] = (ILiveRootView)findViewById(R.id.ilrv_back);
        irvArr[1] = (ILiveRootView)findViewById(R.id.ilrv_first);
        irvArr[2] = (ILiveRootView)findViewById(R.id.ilrv_second);

        ILiveRoomManager.getInstance().initRootViewArr(irvArr);
        StatusObservable.getInstance().addObserver(this);

        // 背景画面显示自己
        irvArr[0].render(ILiveLoginManager.getInstance().getMyUserId(), CommonConstants.Const_VideoType_Camera);
        // 加入公开房间
        enterAVRoom(1234);

        // 设置点击画面
        irvArr[1].setGestureListener(new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                swapVideo(irvArr[1]);
                return super.onSingleTapConfirmed(e);
            }
        });
        irvArr[2].setGestureListener(new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                swapVideo(irvArr[2]);
                return super.onSingleTapConfirmed(e);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        ILiveRoomManager.getInstance().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ILiveRoomManager.getInstance().onResume();

        irvArr[1].setZOrderMediaOverlay(true);
        irvArr[2].setZOrderMediaOverlay(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StatusObservable.getInstance().deleteObserver(this);
        ILiveRoomManager.getInstance().onDestory();
    }
    @Override
    public void onForceOffline(int error, String message) {
        finish();
    }

    private Context getContenxt(){
        return this;
    }

    // 加入房间
    private void enterAVRoom(int roomId){
        ILiveRoomManager.getInstance().joinRoom(roomId, new ILiveRoomOption().imsupport(false),
                new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        DlgMgr.showMsg(getContenxt(), "create failed:"+module+"|"+errCode+"|"+errMsg);
                    }
                });
    }

    private Context getContext(){
        return this;
    }

    private void swapVideo(ILiveRootView irvView){
        if (irvView.isRendering()) {
            String userId = irvView.getIdentifier();
            int srcType = irvView.getVideoSrcType();

            //irvView.closeVideo();
            irvView.render(irvArr[0].getIdentifier(), irvArr[0].getVideoSrcType());
            //irvArr[0].closeVideo();
            irvArr[0].render(userId, srcType);
        }
    }
}
