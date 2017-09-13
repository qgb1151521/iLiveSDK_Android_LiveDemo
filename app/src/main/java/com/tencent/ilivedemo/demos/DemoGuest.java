package com.tencent.ilivedemo.demos;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tencent.TIMConversationType;
import com.tencent.TIMElem;
import com.tencent.TIMElemType;
import com.tencent.TIMGroupSystemElem;
import com.tencent.TIMGroupSystemElemType;
import com.tencent.TIMMessage;
import com.tencent.TIMUserProfile;
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
public class DemoGuest extends Activity implements View.OnClickListener, ILVLiveConfig.ILVLiveMsgListener, ILiveLoginManager.TILVBStatusListener{
    private final String TAG = "DemoGuest";
    private DemoEditText etRoom, etMsg;
    private AVRootView arvRoot;
    private TextView tvMsg;
    private ScrollView svScroll;

    private String strMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_guest);

        UserInfo.getInstance().getCache(getApplicationContext());

        arvRoot = (AVRootView)findViewById(R.id.arv_root);
        etRoom = (DemoEditText)findViewById(R.id.et_room);
//        etRoom.setText(""+UserInfo.getInstance().getRoom());
        etMsg = (DemoEditText)findViewById(R.id.et_msg);
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
            case R.id.tv_send:
                sendMsg();
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
        ILVLiveRoomOption option = new ILVLiveRoomOption("")
                .controlRole(Constants.ROLE_GUEST)
                .videoMode(ILiveConstants.VIDEOMODE_NORMAL)
                .autoCamera(false)
                .autoMic(false);
        ILVLiveManager.getInstance().joinRoom(roomId,
                option, new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        afterJoin();
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        DlgMgr.showMsg(getContenxt(), "create failed:"+module+"|"+errCode+"|"+errMsg);
                    }
                });
    }

    private void afterJoin(){
        UserInfo.getInstance().setRoom(ILiveRoomManager.getInstance().getRoomId());
        UserInfo.getInstance().writeToCache(this);
        etRoom.setEnabled(false);
        findViewById(R.id.tv_join).setVisibility(View.INVISIBLE);
        findViewById(R.id.ll_msg).setVisibility(View.VISIBLE);
    }

    // 发送消息
    private void sendMsg(){
        final String strMsg = etMsg.getText().toString();
        if (TextUtils.isEmpty(strMsg)){
            DlgMgr.showMsg(this, getString(R.string.msg_send_empty));
            return;
        }else if (strMsg.length() > Constants.MAX_SIZE){
            DlgMgr.showMsg(this, getString(R.string.str_send_limit));
            return;
        }

        ILVText ilvText = new ILVText(ILVText.ILVTextType.eGroupMsg,
                ILiveRoomManager.getInstance().getIMGroupId(),
                strMsg);
        ILVLiveManager.getInstance().sendText(ilvText, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                addMessage(ILiveLoginManager.getInstance().getMyUserId(), strMsg);
                etMsg.setText("");
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etMsg.getWindowToken(), 0);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                DlgMgr.showMsg(getContenxt(), "sendText failed:"+module+"|"+errCode+"|"+errMsg);
            }
        });
    }
}
