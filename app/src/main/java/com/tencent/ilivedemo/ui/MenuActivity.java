package com.tencent.ilivedemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.tencent.ilivedemo.R;
import com.tencent.ilivedemo.demos.DemoHost;

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
        }
    }


    private void enterDemo(Class clsActivity){
        startActivity(new Intent(this, clsActivity));
    }
}
