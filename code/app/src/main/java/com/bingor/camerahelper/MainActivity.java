package com.bingor.camerahelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.DatePicker;

import com.bingor.camerahelperlib.callback.OnCameraHelperListener;
import com.bingor.camerahelperlib.helper.CameraBuilder;
import com.bingor.camerahelperlib.helper.CameraHelper;
import com.bingor.camerahelperlib.other.Size;
import com.bingor.camerahelperlib.view.ClickRollView;
import com.bingor.utillib.hardware.ScreenUtil;
import com.bingor.utillib.system.PermissionApplier;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private AutoFitTextureView mTextureView;
    private ClickRollView crvImageRatio;
    private MySurfaceTextureListener mySurfaceTextureListener;

    private CameraHelper cameraHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextureView = findViewById(R.id.texture);
        crvImageRatio = findViewById(R.id.crv_image_ratio);

        cameraHelper = new CameraBuilder(this)
                .setOnCameraHelperListener(new OnCameraHelperListener() {
                    @Override
                    public SurfaceTexture getSurfaceTexture() {
                        if (mTextureView != null) {
                            return mTextureView.getSurfaceTexture();
                        }
                        return null;
                    }

                    @Override
                    public void onPreviewSizeChanged(@NotNull Size previewSize, boolean vertical) {
                        if (mTextureView != null) {
                            if (vertical) {
                                mTextureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
                            } else {
                                mTextureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
                            }
                            configureTransform(cameraHelper.getSurfaceWidth(), cameraHelper.getSurfaceHeight());
                        } else {
                            int i = 0;
                            System.out.println(i);
                        }
                    }
                })
                .create();

        List<String> ratios = new ArrayList<>();
        ratios.add("16:9");
        ratios.add("4:3");
        ratios.add("1:1");
        crvImageRatio.setDatas(ratios);
        crvImageRatio.setOnCheckChangedListener(new ClickRollView.OnCheckChangedListener() {
            @Override
            public void onCheckChanged(String item, int position) {
                float ratio = 0;
                switch (position) {
                    case 0:
                        ratio = CameraHelper.Companion.getIMAGE_RATIO_16_9();
                        break;
                    case 1:
                        ratio = CameraHelper.Companion.getIMAGE_RATIO_4_3();
                        break;
                    case 2:
                        ratio = CameraHelper.Companion.getIMAGE_RATIO_1_1();
                        break;
                }
                cameraHelper.closeCamera();
                cameraHelper = null;
                cameraHelper = new CameraBuilder(MainActivity.this)
                        .setImageRatio(ratio)
                        .setOnCameraHelperListener(new OnCameraHelperListener() {
                            @Override
                            public SurfaceTexture getSurfaceTexture() {
                                if (mTextureView != null) {
                                    return mTextureView.getSurfaceTexture();
                                }
                                return null;
                            }

                            @Override
                            public void onPreviewSizeChanged(@NotNull Size previewSize, boolean vertical) {
                                if (mTextureView != null) {
                                    if (vertical) {
                                        mTextureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
                                    } else {
                                        mTextureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
                                    }
                                    configureTransform(cameraHelper.getSurfaceWidth(), cameraHelper.getSurfaceHeight());
                                } else {
                                    int i = 0;
                                    System.out.println(i);
                                }
                            }
                        })
                        .create();

                cameraHelper.init(mTextureView.getWidth(), mTextureView.getHeight());
                cameraHelper.makeCameraPrepare();
                cameraHelper.openCamera();
            }
        });

        findViewById(R.id.picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHelper.capture();
            }
        });
        findViewById(R.id.info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHelper.setFlash(!cameraHelper.isFlash());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraHelper.init(mTextureView.getWidth(), mTextureView.getHeight());
        cameraHelper.makeCameraPrepare();

        //这里用来应对用户锁屏的情况
        //锁屏再打开的话，其实TextureView是可用的，只需要再打开摄像头即可
        if (mTextureView.isAvailable()) {
            if (!PermissionApplier.checkPermissions(this, 0x1234, Manifest.permission.CAMERA)) {
                cameraHelper.openCamera();
            }
        } else {
            mySurfaceTextureListener = new MySurfaceTextureListener();
            mTextureView.setSurfaceTextureListener(mySurfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraHelper.makeCameraPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0x1234) {
            if (grantResults == null || grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionApplier.checkPermissions(this, 0x1234, Manifest.permission.CAMERA);
            }
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == cameraHelper.getPreviewSize()) {
            return;
        }
        int rotation = ScreenUtil.getScreenRotation(MainActivity.this);
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, cameraHelper.getPreviewSize().getHeight(), cameraHelper.getPreviewSize().getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / cameraHelper.getPreviewSize().getHeight(),
                    (float) viewWidth / cameraHelper.getPreviewSize().getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }


    private class MySurfaceTextureListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            cameraHelper.init(mTextureView.getWidth(), mTextureView.getHeight());
            if (!PermissionApplier.checkPermissions(MainActivity.this, 0x1234, Manifest.permission.CAMERA)) {
                cameraHelper.openCamera();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }


    }
}
