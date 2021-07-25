package com.lyl.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private ViewPager mVp;
    private TextView mTvIndex, mTv1, mTv2, mTv3, mTvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        mTvIndex = findViewById(R.id.tv_index);
        mVp = findViewById(R.id.vp_picture);
        mTv1 = findViewById(R.id.tv_1);
        mTv2 = findViewById(R.id.tv_2);
        mTv3 = findViewById(R.id.tv_3);
        mTvResult = findViewById(R.id.tv_result);
        mTv1.setOnClickListener(this);
        mTv2.setOnClickListener(this);
        mTv3.setOnClickListener(this);
    }

    private void initData() {
        mVp.setAdapter(mAdapter);
        mVp.setOffscreenPageLimit(2);
        mVp.setCurrentItem(1,false); //false:不显示跳转过程的动画
        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected: " + position);
                if  ( position < 1) { //首位之前，跳转到末尾（N）
                    position = Const.TAB_CNT;
                } else if ( position > Const.TAB_CNT) { //末位之后，跳转到首位（1）
                    position = 1;
                }
                mTvIndex.setText("第"+(position)+"组");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                int currentItem = mVp.getCurrentItem();
                Log.d(TAG, "onPageScrollStateChanged: currentItem = " + currentItem);
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:
//                    LogUtils.i(TAG, "---->onPageScrollStateChanged 无动作");

                        if (currentItem == 0) {
                            mVp.setCurrentItem(Const.TAB_CNT, false);
                        } else if (currentItem > Const.TAB_CNT) {
                            mVp.setCurrentItem(1, false);
                        }
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
//                    LogUtils.i(TAG, "---->onPageScrollStateChanged 点击、滑屏");
//                        mHandler.removeCallbacksAndMessages(null);
//                        mHandler.sendEmptyMessageDelayed(MSG_BANNER, DELAY);
                        //https://blog.csdn.net/oweixiao123/article/details/23459041
                        //图     D A B C D A
                        //index  0 1 2 3 4 5
                        // 图D划向图A的时候，currentItem为4,当手指抬起，动作释放，划动完成后，
                        // 划动状态会变为SCROLL_STATE_IDLE，此时currentItem为5，进入第二个if
                        // 设置当前Item为1，即显示图片A
                        // setCurrentItem参数2为false:不显示跳转过程的动画
                        // 若在onPageSelected中执行setCurrentItem，从图D到图A切换时，会突然没有动画，很突兀。

                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
//                    LogUtils.i(TAG, "---->onPageScrollStateChanged 释放");
                        break;
                }
            }
        });
    }

    private SparseArray<PictureFragment> mFmList = new SparseArray<>();

    private FragmentPagerAdapter mAdapter = new FragmentPagerAdapter(getSupportFragmentManager(),
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        @NonNull
        @Override
        public Fragment getItem(int position) {
            PictureFragment f = new PictureFragment();
            mFmList.put(position, f);
            return f;
        }

        @Override
        public int getCount() {
            return Const.TAB_CNT+2;
        }
    };

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int curGroupIndex = mVp.getCurrentItem();
        switch (view.getId()){
            case R.id.tv_1:
                setXArr(curGroupIndex, 0);
                break;
            case R.id.tv_2:
                setXArr(curGroupIndex, 1);
                break;
            case R.id.tv_3:
                setXArr(curGroupIndex, 2);
                break;
            default:
                break;
        }
    }

    private float[] xArr = new float[3];
    @SuppressLint("SetTextI18n")
    private void setXArr(int currentItem, int wayIndex){
        Log.d(TAG, "setXArr: currentItem = " + currentItem + ", wayIndex = " + wayIndex);
        PictureFragment fragment = mFmList.get(currentItem);
        HashMap<Integer, Float> grayMap = fragment.getGrayMap();
        Set<Integer> integers = grayMap.keySet();
        Log.d(TAG, "setXArr: integers = " + integers);

        boolean imperfect = true;
        for (int i : integers){
            Float value = grayMap.get(i);
            Log.d(TAG, "setXArr: value = " + value);
            if (null == value || value <= 0f){
                imperfect = false;
                break;
            }
        }
        Log.d(TAG, "setXArr: imperfect = " + imperfect);

        mTvResult.setText("灰度值");
        if (!imperfect) {
            Toast.makeText(this, "请拍完三张照片！", Toast.LENGTH_SHORT).show();
            return;
        }

        float sumVal = 0f;
        float x1 = grayMap.get(0);
        float x2 = grayMap.get(1);
        float x3 = grayMap.get(2);
        Log.d(TAG, "setXArr: x1 = " + x1 + " x2 = " + x2 + ", x3 = " + x3);

        switch (wayIndex){
            case 0:
                sumVal = 23.071208f-0.044822f*x1-0.081604f*x2-0.028755f*x3;
                break;
            case 1:
                sumVal = 14.271050f+0.025443f*x1-0.071180f*x2+0.012054f*x3;
                break;
            case 2:
                sumVal = 17.037723f-0.006135f*x1-0.12029f*x2+0.011254f*x3;
                break;
        }
        Log.d(TAG, "setXArr: sumVal = " + sumVal);

        mTvResult.setText("灰度值：" + sumVal);
    }
}