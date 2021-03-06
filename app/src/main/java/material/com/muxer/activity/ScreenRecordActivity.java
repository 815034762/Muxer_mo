package material.com.muxer.activity;

import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import cangwang.com.base.modulebus.ELPublicApi;
import cangwang.com.base.modulebus.ELPublicHelper;
import cangwang.com.base.service.ScreenRecorderService;
import material.com.muxer.R;
import material.com.muxer.activity.presenter.ScreenRecordPresenter;
import material.com.muxer.activity.view.IScreenRecordView;
import material.com.muxer.config.PageConfig;
import material.com.muxer.adapter.RecordPagerAdapter;
import material.com.muxer.receiver.MyBroadcastReceiver;

import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public final class ScreenRecordActivity extends AppCompatActivity implements IScreenRecordView{
    private static final boolean DEBUG = false;
    public static final int REQUEST_CODE_SCREEN_CAPTURE = 1;
    private static final String TAG = "ScreenRecordActivity";

    private MyBroadcastReceiver mReceiver;

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private List<String> pageTitles = new ArrayList<String>();
    private List<Fragment> pageFagments = new ArrayList<Fragment>();

    private RecordPagerAdapter recordPagerAdapter;
    private ScreenRecordPresenter presenter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.v(TAG, "onCreate:");
        setContentView(R.layout.activity_main);
        presenter = new ScreenRecordPresenter(this,this);
        initView();

        if (mReceiver == null)
            mReceiver = new MyBroadcastReceiver(this);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ScreenRecorderService.ACTION_QUERY_STATUS_RESULT);
        registerReceiver(mReceiver, intentFilter);

    }

    public void initView(){
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(onMenuItemClick);

        mTabLayout = (TabLayout) findViewById(R.id.record_tab_layout);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);

        pageTitles = PageConfig.getPageTitles(this);

        for(String title:pageTitles){
            mTabLayout.addTab(mTabLayout.newTab().setText(title));
        }

        try {
            //遍历Fragment地址
            for(String address:PageConfig.fragmentNames){
                //反射获得Class
                Class clazz = Class.forName(address);
                //创建类
                Fragment tab = (Fragment) clazz.newInstance();
                //添加到viewPagerAdapter的资源
                pageFagments.add(tab);
            }

        }catch (ClassNotFoundException e){

        }catch (IllegalAccessException e){

        }catch (InstantiationException e){

        }

        mViewPager = (ViewPager) findViewById(R.id.record_view_pager);
        recordPagerAdapter = new RecordPagerAdapter(getSupportFragmentManager(),pageFagments,pageTitles);
        mViewPager.setAdapter(recordPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int currentPage = mViewPager.getCurrentItem();
                mViewPager.setCurrentItem(currentPage);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //全部预加载
        mViewPager.setOffscreenPageLimit(pageFagments.size());

        mTabLayout.setupWithViewPager(mViewPager);
    }



    public void updateRecording(boolean isRecording, boolean isPausing){
//        for (Fragment fragment:pageFagments){
//            if(fragment instanceof RecordFragment){
//                ((RecordFragment) fragment).updateRecording(isRecording,isPausing);
//            }
//        }
//        EventBus.getDefault().post(new UpdateRecordViewEvent(isRecording,isPausing));
//        ((RecordFragment)pageFagments.get(0)).getPresenter().updateRecording(isRecording,isPausing);
        ELPublicHelper.getInstance().getModuleApi(ELPublicApi.RecordApi.class).updateRecordView(isRecording,isPausing);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.action_record:
                    mViewPager.setCurrentItem(0);
                    break;
                case R.id.action_read:
                    mViewPager.setCurrentItem(1);
                    break;
                case R.id.action_settings:
                    mViewPager.setCurrentItem(2);
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.v(TAG, "onResume:");

    }

    @Override
    protected void onPause() {
        if (DEBUG) Log.v(TAG, "onPause:");

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"requsetCode = "+requestCode+",resultCode = " + resultCode+", data = "+data);
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_SCREEN_CAPTURE == requestCode) {
//            recordPresenter.startScreenRecorder(resultCode, data);
            if (resultCode != Activity.RESULT_OK) {
                // when no permission
                Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                return;
            }

            final Intent intent = new Intent(this, ScreenRecorderService.class);
            intent.setAction(ScreenRecorderService.ACTION_START);
            intent.putExtra(ScreenRecorderService.EXTRA_RESULT_CODE, resultCode);
            intent.putExtras(data);
            startService(intent);
        }
    }
    ArrayMap<String,String> map = new ArrayMap<>();


}
