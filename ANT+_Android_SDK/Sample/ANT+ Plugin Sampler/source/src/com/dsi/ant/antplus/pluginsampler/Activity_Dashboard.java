/*
This software is subject to the license described in the License.txt file
included with this software distribution. You may not use this file except in compliance
with this license.

Copyright (c) Dynastream Innovations Inc. 2013
All rights reserved.
*/

package com.dsi.ant.antplus.pluginsampler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.dsi.ant.antplus.pluginsampler.heartrate.Activity_AsyncScanHeartRateSampler;
import com.dsi.ant.antplus.pluginsampler.heartrate.Activity_SearchUiHeartRateSampler;
import com.dsi.ant.antplus.pluginsampler.multidevicesearch.Activity_MultiDeviceFilter;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.pluginlib.version.PluginLibVersionInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard 'menu' of available sampler activities
 */
public class Activity_Dashboard extends FragmentActivity
{
    protected ListAdapter mAdapter;
    protected ListView mList;
    protected EditText age;
    protected RadioButton man,woman;
    protected Button ok;
    protected TextView ErrorMessage;
    private int get_age=0,sex=0;


    //Initialize the list
    @SuppressWarnings("serial") //Suppress warnings about hash maps not having custom UIDs
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            Log.i("ANT+ Plugin Sampler", "Version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e)
        {
            Log.i("ANT+ Plugin Sampler", "Version: " + e.toString());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        List<Map<String,String>> menuItems = new ArrayList<Map<String,String>>();
        menuItems.add(new HashMap<String,String>(){{put("title","Heart Rate Display");put("desc","Receive from HRM sensors");}});

        SimpleAdapter adapter = new SimpleAdapter(this, menuItems, android.R.layout.simple_list_item_2, new String[]{"title","desc"}, new int[]{android.R.id.text1,android.R.id.text2});
        setListAdapter(adapter);

        try
        {
            ((TextView)findViewById(R.id.textView_PluginSamplerVersion)).setText("Sampler Version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e)
        {
            ((TextView)findViewById(R.id.textView_PluginSamplerVersion)).setText("Sampler Version: ERR");
        }
        ((TextView)findViewById(R.id.textView_PluginLibVersion)).setText("Built w/ PluginLib: " + PluginLibVersionInfo.PLUGINLIB_VERSION_STRING);
        ((TextView)findViewById(R.id.textView_PluginsPkgVersion)).setText("Installed Plugin Version: " + AntPluginPcc.getInstalledPluginsVersionString(this));
    }

    protected Button.OnClickListener btnListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(check()){
                ErrorMessage.setText("設置完成");
            }
        }
    };
    Boolean check(){
        if("".equals(age.getText().toString().trim())){
            ErrorMessage.setText("年齡設置錯誤");
            return false;
        }
        if(age.getText().toString()!=""&&((Integer.parseInt(age.getText().toString())>0)&&(Integer.parseInt(age.getText().toString())<=120))){
            get_age = Integer.parseInt(age.getText().toString());
            if(man.isChecked()){
                sex = 1;
                ErrorMessage.setText("設置完成");
                return true;
            }
            else if(woman.isChecked()){
                sex = 0;
                ErrorMessage.setText("設置完成");
                return true;
            }
            else{
                ErrorMessage.setText("性別錯誤");
                return false;
            }
        }
        else{
            ErrorMessage.setText("年齡設置錯誤");
            return false;
        }
    }

    //Launch the appropriate activity/action when a selection is made
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        int j=0;

        if(position == j++)
        {
            Intent i = new Intent(this, Activity_SearchUiHeartRateSampler.class);
            i.putExtra("sex",sex);
            i.putExtra("age",get_age);
            startActivity(i);
        }
        else if(position == j++)
        {
            Intent i = new Intent(this, Activity_AsyncScanHeartRateSampler.class);
            startActivity(i);
        }
        else if(position == j++)
        {
            Intent i = new Intent(this, Activity_MultiDeviceFilter.class);
            startActivity(i);
        }
        else if(position == j++)
        {
            /**
             * Launches the ANT+ Plugin Manager. The ANT+ Plugin Manager provides access to view and modify devices
             * saved in the plugin device database and control default plugin settings. It is also available as a
             * stand alone application, but the ability to launch it from your own application is useful in situations
             * where a user wants extra convenience or doesn't already have the stand alone launcher installed. For example,
             * you could place this launch command in your application's own settings menu.
             */
            if(!AntPluginPcc.startPluginManagerActivity(this))
            {
                AlertDialog.Builder adlgBldr = new AlertDialog.Builder(this);
                adlgBldr.setTitle("Missing Dependency");
                adlgBldr.setMessage("This application requires the ANT+ Plugins, would you like to install them?");
                adlgBldr.setCancelable(true);
                adlgBldr.setPositiveButton("Go to Store", new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent startStore = null;
                        startStore = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.dsi.ant.plugins.antplus"));
                        startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        Activity_Dashboard.this.startActivity(startStore);
                    }
                });
                adlgBldr.setNegativeButton("Cancel", new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });

                final AlertDialog waitDialog = adlgBldr.create();
                waitDialog.show();
            }
        }
        else
        {
            Toast.makeText(this, "This menu item is not implemented", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets the list display to the give adapter
     * @param adapter Adapter to set list display to
     */
    public void setListAdapter(ListAdapter adapter)
    {
        synchronized (this)
        {
            if (mList != null)
                return;
            age = (EditText)findViewById(R.id.ProfileAge);
            man = (RadioButton)findViewById(R.id.ProfileMan);
            woman = (RadioButton)findViewById(R.id.ProfileWoman);
            ok = (Button)findViewById(R.id.SetProfile);
            ErrorMessage = (TextView)findViewById(R.id.ErrorMessage);
            ok.setOnClickListener(btnListener);
            ErrorMessage.setText("尚未設置");
            ErrorMessage.setTextColor(Color.RED);
            mAdapter = adapter;
            mList = (ListView)findViewById(android.R.id.list);
            mList.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?>  parent, View v, int position, long id)
                {
                    if (check())
                        onListItemClick((ListView)parent, v, position, id);
                }
            });
            mList.setAdapter(adapter);
        }
    }
}
