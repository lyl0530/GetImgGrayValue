package com.lyl.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lym on 2021/7/22
 * Describe :
 */
public class PictureFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "PictureFragment";

    private View mRootView;
    private Context mContext;
    private ImageView mIv1, mIv2, mIv3;
    private int curIndex;
    private List<ImageView> mImgViewList = new ArrayList<>();
    private File imgFile;
    private Uri imgUri;
    private HashMap<Integer, Float> grayMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_picture, container, false);
        mIv1 = mRootView.findViewById(R.id.iv_1);
        mIv2 = mRootView.findViewById(R.id.iv_2);
        mIv3 = mRootView.findViewById(R.id.iv_3);
        mIv1.setOnClickListener(this);
        mIv2.setOnClickListener(this);
        mIv3.setOnClickListener(this);
        mImgViewList.clear();
        mImgViewList.add(mIv1);
        mImgViewList.add(mIv2);
        mImgViewList.add(mIv3);
        grayMap.put(0, 0f);
        grayMap.put(1, 0f);
        grayMap.put(2, 0f);
        return mRootView;
    }

    private void openCamera(int index) {
        Context context = getContext();
        curIndex = index;
        grayMap.put(curIndex, 0f);
        Log.d(TAG, "openCamera: curIndex = " + curIndex + ", grayMap = " + grayMap);

        if (null == context) return;
        ArrayList<String> permissionList = new ArrayList<>();
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        //判断手机版本,如果低于6.0 则不用申请权限,直接拍照
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //android M 6.0
            if (context.checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissions[0]);
            }
            if (context.checkSelfPermission(permissions[1]) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissions[1]);
            }

            if (!permissionList.isEmpty()) {
                String[] permissions1 = permissionList.toArray(new String[permissionList.size()]);
                requestPermissions(permissions1, Const.REQ_PERMISSION);
            } else {
                startCamera(context);
            }
        } else {
            startCamera(context);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Context context = getContext();
        if (null == context) return;
        switch (requestCode) {
            case Const.REQ_PERMISSION:
                if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    startCamera(context);
                } else {
                    Toast.makeText(context, "获取权限失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void startCamera(Context context) {
        imgFile = new File(context.getExternalCacheDir(), "camera.png");
        //"/storage/emulated/0/Android/data/com.lyl.takephoto/cache/camera.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //android N 7.0
            /*
                Android7.0以后不再使用真实路径的Uri,即从"file:///" -> "content://"
                FileProvider是特殊的内容提供者，可以选择性的将封装过的Uri共享给外部
                FileProvider是ContentProvider的子类，清单文件中要添加provider
            */
            //注意参2和AndroidManifest.xml中的fileprovider路径一致
            imgUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", imgFile);
            //content://com.lyl.takephoto.fileprovider/external_files/Android/data/com.lyl.takephoto/cache/camera.png
        } else {
            //File -> Uri
            imgUri = Uri.fromFile(imgFile);
            //"file:///storage/emulated/0/Android/data/com.lyl.avatar/cache/abc.png"
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(intent, Const.REQ_CAMERA);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode = " + requestCode + ", resultCode = " + ", data = " + data);
        if (Const.REQ_CAMERA == requestCode) {
            assert data != null;
            Context context = getContext();
            if (null == context) return;
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            getPicturePixel(bitmap);
            mImgViewList.get(curIndex).setImageResource(0);
            mImgViewList.get(curIndex).setBackground(new BitmapDrawable(context.getResources(), bitmap));

        }
    }

    /**
     * 获得图片的像素方法
     *
     * @param bitmap
     */
    private void getPicturePixel(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight(); // 保存所有的像素的数组，图片宽×高

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        float grayValueSum = 0f;

        for (int i = 0; i < pixels.length; i++) {
            int clr = pixels[i];
            int red = (clr & 0x00ff0000) >> 16; // 取高两位
            int green = (clr & 0x0000ff00) >> 8; // 取中两位
            int blue = clr & 0x000000ff; // 取低两位 Log.d("tag", "r=" + red + ",g=" + green + ",b=" + blue);
            float grayValue = 0.299f*red + 0.587f*green + 0.114f*blue;
            grayValueSum += grayValue;
        }

        float grayAveValue = grayValueSum/(width*height*1f);
        Log.d(TAG, "getPicturePixel: point cnt = " + pixels.length + ", w = " + width + ", h = " +
                height + ", grayValueSum = " + grayValueSum + ", grayAveValue = " + grayAveValue);

        grayMap.put(curIndex, grayAveValue);
    }

    public HashMap<Integer, Float> getGrayMap(){
        Log.d(TAG, "getGrayMap: " + grayMap);
        return grayMap;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_1:
                openCamera(0);
                break;
            case R.id.iv_2:
                openCamera(1);
                break;
            case R.id.iv_3:
                openCamera(2);
                break;
            default:
                break;
        }
    }
}
