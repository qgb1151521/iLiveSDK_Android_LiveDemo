package com.tencent.ilivedemo.demos;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tencent.TIMConversationType;
import com.tencent.TIMElem;
import com.tencent.TIMElemType;
import com.tencent.TIMGroupSystemElem;
import com.tencent.TIMGroupSystemElemType;
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
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.livesdk.ILVCustomCmd;
import com.tencent.livesdk.ILVLiveConfig;
import com.tencent.livesdk.ILVLiveManager;
import com.tencent.livesdk.ILVLiveRoomOption;
import com.tencent.livesdk.ILVText;

/**
 * Created by xkazerzhang on 2017/5/24.
 */
public class DemoLiveGuest extends Activity implements View.OnClickListener, ILVLiveConfig.ILVLiveMsgListener, ILiveLoginManager.TILVBStatusListener{
    private final String TAG = "DemoLiveGuest";
    private DemoEditText etRoom;
    private TextView tvMsg;
    private ScrollView svScroll;
    private AVRootView arvRoot;

    private boolean isCameraOn = true;
    private boolean isMicOn = true;
    private boolean isFlashOn = false;

    private String strMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_liveguest);

        UserInfo.getInstance().getCache(getApplicationContext());

        arvRoot = (AVRootView)findViewById(R.id.arv_root);
        etRoom = (DemoEditText)findViewById(R.id.et_room);
//        etRoom.setText(""+UserInfo.getInstance().getRoom());
        tvMsg = (TextView)findViewById(R.id.tv_msg);
        svScroll = (ScrollView)findViewById(R.id.sv_scroll);

        ILVLiveManager.getInstance().setAvVideoView(arvRoot);
        MessageObservable.getInstance().addObserver(this);
        StatusObservable.getInstance().addObserver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ILVLiveManager.getInstance().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ILVLiveManager.getInstance().onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageObservable.getInstance().deleteObserver(this);
        StatusObservable.getInstance().deleteObserver(this);
        ILVLiveManager.getInstance().onDestory();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_join:
                joinRoom();
                break;
            case R.id.iv_camera:
                isCameraOn = !isCameraOn;
                ILiveRoomManager.getInstance().enableCamera(ILiveRoomManager.getInstance().getCurCameraId(),
                        isCameraOn);
                ((ImageView)findViewById(R.id.iv_camera)).setImageResource(
                        isCameraOn ? R.mipmap.ic_camera_on : R.mipmap.ic_camera_off);
                break;
            case R.id.iv_switch:
                Log.v(TAG, "switch->cur: "+ILiveRoomManager.getInstance().getActiveCameraId()+"/"+ILiveRoomManager.getInstance().getCurCameraId());
                if (ILiveConstants.NONE_CAMERA != ILiveRoomManager.getInstance().getActiveCameraId()) {
                    ILiveRoomManager.getInstance().switchCamera(1 - ILiveRoomManager.getInstance().getActiveCameraId());
                }else{
                    ILiveRoomManager.getInstance().switchCamera(ILiveConstants.FRONT_CAMERA);
                }
                break;
            case R.id.iv_flash:
                toggleFlash();
                break;
            case R.id.iv_mic:
                isMicOn = !isMicOn;
                ILiveRoomManager.getInstance().enableMic(isMicOn);
                ((ImageView)findViewById(R.id.iv_mic)).setImageResource(
                        isMicOn ? R.mipmap.ic_mic_on : R.mipmap.ic_mic_off);
                break;
            case R.id.iv_return:
                finish();
                break;
        }
    }

    @Override
    public void onNewTextMsg(ILVText text, String SenderId, TIMUserProfile userProfile) {
        addMessage(SenderId, DemoFunc.getLimitString(text.getText(), Constants.MAX_SIZE));
    }

    @Override
    public void onNewCustomMsg(ILVCustomCmd cmd, String id, TIMUserProfile userProfile) {

    }

    @Override
    public void onNewOtherMsg(TIMMessage message) {
        if (message.getConversation() != null && message.getConversation().getPeer() != null){
            if (message.getConversation().getType()== TIMConversationType.Group
                    && !ILiveRoomManager.getInstance().getIMGroupId().equals(message.getConversation().getPeer())) {
                return;
            }
        }

        for (int j = 0; j < message.getElementCount(); j++) {
            if (message.getElement(j) == null)
                continue;
            TIMElem elem = message.getElement(j);
            TIMElemType type = elem.getType();

            //系统消息
            if (type == TIMElemType.GroupSystem) {  // 群组解散消息
                if (TIMGroupSystemElemType.TIM_GROUP_SYSTEM_DELETE_GROUP_TYPE == ((TIMGroupSystemElem) elem).getSubtype()) {
                    DlgMgr.showMsg(getContenxt(), getString(R.string.str_tips_discuss)).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            finish();
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onForceOffline(int error, String message) {
        finish();
    }

    private Context getContenxt(){
        return this;
    }

    // 添加消息
    private void addMessage(String sender, String msg){
        strMsg += "["+sender+"]  "+msg+"\n";
        tvMsg.setText(strMsg);
        svScroll.fullScroll(View.FOCUS_DOWN);
    }

    // 加入房间
    private void joinRoom(){
        int roomId = DemoFunc.getIntValue(etRoom.getText().toString(), -1);
        if (-1 == roomId){
            DlgMgr.showMsg(getContenxt(), getString(R.string.str_tip_num_error));
            return;
        }
        ILVLiveRoomOption option = new ILVLiveRoomOption(ILiveLoginManager.getInstance().getMyUserId())
                .controlRole(Constants.ROLE_LIVEGUEST)
                .videoMode(ILiveConstants.VIDEOMODE_NORMAL)
                .autoFocus(true);
        ILVLiveManager.getInstance().joinRoom(roomId,
                option, new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        afterCreate();
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        DlgMgr.showMsg(getContenxt(), "create failed:"+module+"|"+errCode+"|"+errMsg);
                    }
                });
    }

    private void afterCreate(){
        UserInfo.getInstance().setRoom(ILiveRoomManager.getInstance().getRoomId());
        UserInfo.getInstance().writeToCache(this);
        etRoom.setEnabled(false);
        findViewById(R.id.tv_join).setVisibility(View.INVISIBLE);
        findViewById(R.id.ll_controller).setVisibility(View.VISIBLE);
    }

    private void toggleFlash(){
        if (ILiveConstants.BACK_CAMERA != ILiveRoomManager.getInstance().getActiveCameraId()){
            return;
        }
        AVVideoCtrl videoCtrl = ILiveSDK.getInstance().getAvVideoCtrl();
        if (null == videoCtrl) {
            return;
        }

        final Object cam = videoCtrl.getCamera();
        if ((cam == null) || (!(cam instanceof Camera))) {
            return;
        }
        final Camera.Parameters camParam = ((Camera) cam).getParameters();
        if (null == camParam) {
            return;
        }

        Object camHandler = videoCtrl.getCameraHandler();
        if ((camHandler == null) || (!(camHandler instanceof Handler))) {
            return;
        }

        //对摄像头的操作放在摄像头线程
        if (isFlashOn == false) {
            ((Handler) camHandler).post(new Runnable() {
                public void run() {
                    try {
                        camParam.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        ((Camera) cam).setParameters(camParam);
                        isFlashOn = true;
                    } catch (RuntimeException e) {
                        Log.d(TAG, "setParameters->RuntimeException");
                    }
                }
            });
        } else {
            ((Handler) camHandler).post(new Runnable() {
                public void run() {
                    try {
                        camParam.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        ((Camera) cam).setParameters(camParam);
                        isFlashOn = false;
                    } catch (RuntimeException e) {
                        Log.d(TAG, "setParameters->RuntimeException");
                    }
                }
            });
        }
    }
}
