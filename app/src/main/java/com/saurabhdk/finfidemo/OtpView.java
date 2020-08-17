package com.saurabhdk.finfidemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class OtpView extends LinearLayout {
    TypedArray typedArray;
    int mOtpSize;
    int mTextType;
    ArrayList<EditText> inputs = new ArrayList<>();
    Context context;
    OtpListener otpListener;

    LayoutParams center = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    LayoutParams wrapped = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    InputMethodManager imm;

    public OtpView(Context context) {
        super(context);
    }

    public OtpView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER);
        setLayoutParams(center);
        typedArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.OtpView,
                0,
                0);

        try {
            mOtpSize = typedArray.getInteger(R.styleable.OtpView_otpSize, 4);
            mTextType = typedArray.getInteger(R.styleable.OtpView_textType, 0);
        } finally {
            typedArray.recycle();
        }

        init(context);

    }

    public OtpListener getOtpListener() {
        return otpListener;
    }

    public void setOtpListener(OtpListener otpListener) {
        this.otpListener = otpListener;
    }

    public String getOtp() {
        StringBuilder otp = new StringBuilder("");
        for (EditText input : inputs) {
            otp.append(input.getText());
        }
        Log.d("OTPComplete", otp.toString());
        return otp.toString();
    }

    private void init(Context context) {

        if (mOtpSize < 2 || mOtpSize > 8) {
            mOtpSize = 4;
        }

        int margin = convertDpToPixels(6, context);
        int widthPixels = (context.getResources().getDisplayMetrics().widthPixels / mOtpSize)
                - convertDpToPixels(24, context);
        // int width = convertDpToPixels(widthPixels, context);
        wrapped.setMargins(margin, margin, margin, margin);

        for (int i = 0; i < mOtpSize; i++) {
            EditText editText = new EditText(context);
            editText.setLayoutParams(wrapped);
            editText.setWidth(widthPixels);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setGravity(Gravity.CENTER);
            editText.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.rounded_inactive_background,
                    null));
            editText.setTextSize(32f);
            editText.setMaxLines(1);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            addView(editText);
            inputs.add(editText);
        }

        for (int i = 0, inputsSize = inputs.size(); i < inputsSize; i++) {
            final EditText input = inputs.get(i);
            final int index = i;
            final int size = inputsSize;
            if (i == 0) {
                input.requestFocus();
                input.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        input.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.rounded_background, null));
                        showKeyboard(input);
                    }
                }, 300);
            }
            input.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    v.setBackground(ResourcesCompat.getDrawable(getResources(),
                            hasFocus ? R.drawable.rounded_background : R.drawable.rounded_inactive_background,
                            null));
                }
            });
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int num;
                    Log.d("sasasa", s.toString());
                    if (s.toString().trim().length() > 0) {
                        try {
                            num = Integer.parseInt(s.toString().trim());
                            if (num > 9 || num < 0) {
                                throw new NumberFormatException();
                            } else if (index < size - 1) {
                                inputs.get(index + 1).requestFocus();
                            } else {
                                input.clearFocus();
                                hideKeyboard(input);
                                if (OtpView.this.otpListener != null) {
                                    OtpView.this.otpListener.onOtpComplete(getOtp());
                                }
                            }
                        } catch (NumberFormatException nfe) {
                            input.setText("");
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    public OtpView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getOtpSize() {
        return this.mOtpSize;
    }

    public void setOtpSize(int newSize) {
        this.mOtpSize = newSize;
        invalidate();
        requestLayout();
    }

    public static int convertDpToPixels(float px, Context context) {
        return (int) (px * context.getResources().getDisplayMetrics().density);
    }

    public void hideKeyboard(View view) {
        if (view != null) {
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showKeyboard(View view) {
        if (imm != null) imm.showSoftInput(view, 0);
    }
}
