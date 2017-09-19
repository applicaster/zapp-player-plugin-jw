package com.applicaster.jwplayerplugin;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;

public class JWPlayerContainer extends FrameLayout {

    private JWPlayerView jwPlayerView;

    public JWPlayerContainer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public JWPlayerContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public JWPlayerContainer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public JWPlayerView getJWPlayerView() {
        return jwPlayerView;
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        // The spinner relies on a measure + layout pass happening after it calls requestLayout().
        // Without this, the widget never actually changes the selection and doesn't call the
        // appropriate listeners. Since we override onLayout in our ViewGroups, a layout pass never
        // happens after a call to requestLayout, so we simulate one here.
        post(new Runnable() {
            @Override
            public void run() {
                measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
                layout(getLeft(), getTop(), getRight(), getBottom());
            }
        });
    }

    private void init(Context context) {
        jwPlayerView = new JWPlayerView(context, new PlayerConfig.Builder().build());
        addView(jwPlayerView);
    }
}
