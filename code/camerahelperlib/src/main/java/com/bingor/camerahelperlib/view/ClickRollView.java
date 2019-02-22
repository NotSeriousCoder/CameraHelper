package com.bingor.camerahelperlib.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bingor.camerahelperlib.R;
import com.bingor.utillib.general.UnitConverter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Bingor on 2019/2/22.
 */
public class ClickRollView extends FrameLayout {
    private TextView tvTop, tvBottom;
    private List<String> datas = new ArrayList<>();
    private int position = 0;
    private int textSize = 0;
    private int textColor = Color.BLACK;
    private float originalYTop = 0;
    private float originalYBottom = 0;
    private OnCheckChangedListener onCheckChangedListener;

    public ClickRollView(@NonNull Context context) {
        this(context, null);
    }

    public ClickRollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClickRollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ClickRollView);
        textColor = ta.getColor(R.styleable.ClickRollView_textColor, Color.BLACK);
        textSize = ta.getDimensionPixelSize(R.styleable.ClickRollView_textSize, UnitConverter.dip2px(getContext(), 14));
        ta.recycle();
        initView();
    }

    private void initView() {

        tvTop = new TextView(getContext());
        tvTop.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tvBottom = new TextView(getContext());
        tvBottom.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tvBottom.setVisibility(View.INVISIBLE);
        tvTop.setTextSize(textSize);
        tvBottom.setTextSize(textSize);
        tvTop.setTextColor(textColor);
        tvBottom.setTextColor(textColor);

        addView(tvBottom);
        addView(tvTop);
        update();

        tvTop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tvTop.setClickable(false);
                ValueAnimator out = ValueAnimator.ofInt(0, 100);
                out.setDuration(500);
                out.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    boolean changed = false;

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = Integer.parseInt(animation.getAnimatedValue().toString());
                        float process = value / 100f;
                        int height = tvTop.getMeasuredHeight();
                        if (value == 0) {
                            originalYTop = tvTop.getY();
                            originalYBottom = tvBottom.getY();
                        } else if (value == 99) {
                            tvTop.setText(tvBottom.getText());
                        } else if (value == 100) {
                            tvTop.setY(originalYTop);
                            tvBottom.setVisibility(View.INVISIBLE);
                            tvBottom.setY(originalYBottom);
                            if (!changed) {
                                changed = true;
                                position++;
                                position = position % datas.size();
                                update();
                                tvTop.setClickable(true);
                                if (onCheckChangedListener != null) {
                                    onCheckChangedListener.onCheckChanged(datas.get(position), position);
                                }
                            }
                        } else {
                            if (value == 1) {
                                tvBottom.setVisibility(View.VISIBLE);
                            }
                            tvTop.setY(originalYTop + height * process);
                            tvBottom.setY(originalYBottom - height + height * process);
                        }
                    }
                });
                out.start();

//                out.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//                        position++;
//                        position = position % datas.size();
//                        update();
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) {
//
//                    }
//                });

//                TranslateAnimation in = new TranslateAnimation(Animation.ABSOLUTE, 0,
//                        Animation.ABSOLUTE, 0,
//                        Animation.RELATIVE_TO_SELF, -1.0f,
//                        Animation.RELATIVE_TO_SELF, 0f);
//                in.setDuration(500);

//                tvTop.startAnimation(out);
//                tvBottom.startAnimation(in);
            }
        });
    }


    private void update() {
        if (position == datas.size() - 1) {
            tvTop.setText(datas.get(position));
            tvBottom.setText(datas.get(0));
        } else if (position < datas.size() - 1) {
            tvTop.setText(datas.get(position));
            tvBottom.setText(datas.get(position + 1));
        }
    }

    public void setDatas(List<String> datas) {
        this.datas = datas;
        position = 0;
        update();
    }

    public OnCheckChangedListener getOnCheckChangedListener() {
        return onCheckChangedListener;
    }

    public void setOnCheckChangedListener(OnCheckChangedListener onCheckChangedListener) {
        this.onCheckChangedListener = onCheckChangedListener;
    }

    public interface OnCheckChangedListener {
        public void onCheckChanged(String item, int position);
    }
}
