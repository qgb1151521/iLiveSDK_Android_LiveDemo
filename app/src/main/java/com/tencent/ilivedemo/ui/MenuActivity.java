package com.tencent.ilivedemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.tencent.ilivedemo.R;
import com.tencent.ilivedemo.demos.DemoBtu;
import com.tencent.ilivedemo.demos.DemoCross;
import com.tencent.ilivedemo.demos.DemoGuest;
import com.tencent.ilivedemo.demos.DemoHost;
import com.tencent.ilivedemo.demos.DemoLiveGuest;

/**
 * 示例菜单
 */
public class MenuActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mainmenu);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.lmv_host:
                enterDemo(DemoHost.class);
                break;
            case R.id.lmv_guest:
                enterDemo(DemoGuest.class);
                break;
            case R.id.lmv_live_guest:
                enterDemo(DemoLiveGuest.class);
                break;
            case R.id.lmv_cross:
                enterDemo(DemoCross.class);
                break;
            case R.id.lmv_beauty:
                enterDemo(DemoBtu.class);
                break;
        }
    }


    private void enterDemo(Class clsActivity){
        startActivity(new Intent(this, clsActivity));
    }
}
