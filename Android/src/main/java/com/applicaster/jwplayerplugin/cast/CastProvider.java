package com.applicaster.jwplayerplugin.cast;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.mediarouter.app.MediaRouteButton;

import com.applicaster.jwplayerplugin.JWPlayerContainer;
import com.applicaster.jwplayerplugin.R;
import com.applicaster.jwplayerplugin.analytics.AnalyticsAdapter;
import com.applicaster.jwplayerplugin.analytics.AnalyticsData;
import com.applicaster.jwplayerplugin.analytics.AnalyticsTypes;
import com.applicaster.plugin_manager.playersmanager.Playable;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.longtailvideo.jwplayer.JWPlayerView;

public class CastProvider {

    //play services availability
    private static final String GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD = "com.google.market";
    private static final String GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW = "com.android.vending";

    private Activity context;
    private JWPlayerView playerView;

    private AnalyticsData analyticsData;

    private CastContext castContext;
    private MediaRouteButton mediaRouteButton;
    private CastListenerOperator castListenerOperator;
    private String castBtnPreviousState = AnalyticsTypes.CastBtnPreviousState.OFF;
    private String screenAnalyticsState = AnalyticsTypes.PlayerView.FULLSCREEN;


    public CastProvider(Activity context, JWPlayerContainer container) {
        this.context = context;
        this.playerView = container.getJWPlayerView();
    }

    public CastContext getCastContext() {
        return castContext;
    }

    public MediaRouteButton getMediaRouteButton() {
        return mediaRouteButton;
    }

    public CastListenerOperator getCastListenerOperator() {
        return castListenerOperator;
    }

    public void init(Playable playable, AnalyticsData analyticsData, String screenAnalyticsState) {
        //play services availability check and Chromecast init
        this.analyticsData = analyticsData;
        this.screenAnalyticsState = screenAnalyticsState;
        if (isGoogleApiAvailable(context)) {
            initMediaRouteButton();
            castContext = CastContext.getSharedInstance(context);
            collectCastAnalyticsData(playable);
            castListenerOperator = new CastListenerOperator(playerView, analyticsData);
        }
    }

    public void addSessionManagerListener() {
        if (castContext.getSessionManager() != null)
            castContext.getSessionManager().addSessionManagerListener(castListenerOperator, CastSession.class);
    }

    public void removeSessionManagerListener() {
        if (castContext.getSessionManager() != null) {
            castContext.getSessionManager().removeSessionManagerListener(castListenerOperator, CastSession.class);
        }
    }

    public void setScreenAnalyticsState(String screenAnalyticsState) {
        this.screenAnalyticsState = screenAnalyticsState;
    }

    public void release() {
        context = null;
        playerView = null;
        castContext = null;
        castListenerOperator = null;
    }

    private void initMediaRouteButton() {
        if (mediaRouteButton == null) {
            attachCustomMediaRouteButtonView();
        }
        setMediaRouteButtonCustomListener();
        CastButtonFactory.setUpMediaRouteButton(context.getApplicationContext(), mediaRouteButton);
    }

    private void setMediaRouteButtonCustomListener() {
        mediaRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analyticsData.setTimeCode(playerView.getPosition());
                if (castBtnPreviousState.equals(AnalyticsTypes.CastBtnPreviousState.OFF))
                    castBtnPreviousState = AnalyticsTypes.CastBtnPreviousState.ON;
                else
                    castBtnPreviousState = AnalyticsTypes.CastBtnPreviousState.OFF;
                AnalyticsAdapter.logTapCast(analyticsData);
            }
        });
    }

    private void attachCustomMediaRouteButtonView() {
        ViewGroup.LayoutParams lp = getMediaRouteButtonLayoutParams();
        playerView.addView(mediaRouteButton, lp);
        ViewGroup.LayoutParams mediaRouteBtnLp = mediaRouteButton.getLayoutParams();
        if (mediaRouteBtnLp instanceof FrameLayout.LayoutParams) {
            ((FrameLayout.LayoutParams) mediaRouteBtnLp).gravity = Gravity.END;
            mediaRouteButton.setLayoutParams(mediaRouteBtnLp);
        }
    }

    private  ViewGroup.LayoutParams getMediaRouteButtonLayoutParams() {
        mediaRouteButton = new MediaRouteButton(
                new ContextThemeWrapper(context, R.style.MediaRouteButton)
        );
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ViewGroup.MarginLayoutParams marginLayoutParams =
                new ViewGroup.MarginLayoutParams(layoutParams);
        int mediaRouteBtnMargins = context.getResources().getDimensionPixelSize(R.dimen.media_route_btn_margins);
        marginLayoutParams.setMargins(0, mediaRouteBtnMargins, mediaRouteBtnMargins, 0);
        return marginLayoutParams;
    }


    private void collectCastAnalyticsData(Playable playable) {
        analyticsData.setView(this.screenAnalyticsState);
        analyticsData.setPreviousState(castBtnPreviousState);
    }

    private boolean doesPackageExist(String targetPackage) {
        try {
            context.getPackageManager().getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    // Without the Google API's Chromecast won't work
    private boolean isGoogleApiAvailable(Context context) {
        boolean isOldPlayStoreInstalled = doesPackageExist(GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD);
        boolean isNewPlayStoreInstalled = doesPackageExist(GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW);

        boolean isPlaystoreInstalled = isNewPlayStoreInstalled || isOldPlayStoreInstalled;

        boolean isGoogleApiAvailable = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
        return isPlaystoreInstalled && isGoogleApiAvailable;
    }
}
