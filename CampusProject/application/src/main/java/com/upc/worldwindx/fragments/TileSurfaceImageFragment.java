package com.upc.worldwindx.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.upc.R;
import com.upc.worldwindx.activities.CodeActivity;

import gov.nasa.worldwind.BasicWorldWindowController;
import gov.nasa.worldwind.NavigatorEvent;
import gov.nasa.worldwind.NavigatorListener;
import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.PickedObjectList;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globe.BasicElevationCoverage;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.NASA_SRTM30_TiledElevationCoverage;
import gov.nasa.worldwind.layer.TiandituCia_cLayer;
import gov.nasa.worldwind.layer.TiandituCva_cLayer;
import gov.nasa.worldwind.layer.TiandituCva_wLayer;
import gov.nasa.worldwind.layer.TiandituImg_cLayer;
import gov.nasa.worldwind.layer.TiandituVec_cLayer;
import gov.nasa.worldwind.shape.Highlightable;

/**
 * Created by Lenovo on 2018/3/31.
 */

public class TileSurfaceImageFragment extends BasicGlobeFragment {
    @Override
    public WorldWindow createWorldWindow() {
        WorldWindow wwd = super.createWorldWindow();

        // Override the World Window's built-in navigation behavior by adding picking support.
        wwd.setWorldWindowController(new PickNavigateController());
        /* getWorldWindow().getLayers().addLayer(new TiandituVec_cLayer());
        getWorldWindow().getLayers().addLayer(new TiandituCva_cLayer());*/
        getWorldWindow().getLayers().addLayer(new TiandituImg_cLayer());
        getWorldWindow().getLayers().addLayer(new TiandituCia_cLayer());
        super.getstartLocation(2);
        return  wwd;
    }


    //Navigator Event
    // UI elements
    protected TextView latView;

    protected TextView lonView;

    protected TextView altView;

    protected ImageView crosshairs;

    protected ViewGroup overlay;

    // Use pre-allocated navigator state objects to avoid per-event memory allocations
    private LookAt lookAt = new LookAt();

    private Camera camera = new Camera();

    // Track the navigation event time so the overlay refresh rate can be throttled
    private long lastEventTime;

    // Animation object used to fade the overlays
    private AnimatorSet animatorSet;

    private boolean crosshairsActive;


    //pick Event  UI



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Let the super class (BasicGlobeFragment) create the view and the WorldWindow
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        //Initialize the UI element that we will add webview


        // Initialize the UI elements that we'll update upon the navigation events
        this.crosshairs = (ImageView) rootView.findViewById(R.id.globe_crosshairs);
        this.overlay = (ViewGroup) rootView.findViewById(R.id.globe_status);
        this.crosshairs.setVisibility(View.VISIBLE);
        this.overlay.setVisibility(View.VISIBLE);
        this.latView = (TextView) rootView.findViewById(R.id.lat_value);
        this.lonView = (TextView) rootView.findViewById(R.id.lon_value);
        this.altView = (TextView) rootView.findViewById(R.id.alt_value);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(this.crosshairs, "alpha", 0f).setDuration(1500);
        fadeOut.setStartDelay((long) 500);
        this.animatorSet = new AnimatorSet();
        this.animatorSet.play(fadeOut);

        // Create a simple Navigator Listener that logs navigator events emitted by the World Window.
        NavigatorListener listener = new NavigatorListener() {
            @Override
            public void onNavigatorEvent(WorldWindow wwd, NavigatorEvent event) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - lastEventTime;
                int eventAction = event.getAction();
                boolean receivedUserInput = (eventAction == WorldWind.NAVIGATOR_MOVED && event.getLastInputEvent() != null);

                // Update the status overlay views whenever the navigator stops moving,
                // and also it is moving but at an (arbitrary) maximum refresh rate of 20 Hz.
                if (eventAction == WorldWind.NAVIGATOR_STOPPED || elapsedTime > 50) {

                    // Get the current navigator state to apply to the overlays
                    event.getNavigator().getAsLookAt(wwd.getGlobe(), lookAt);
                    event.getNavigator().getAsCamera(wwd.getGlobe(), camera);

                    // Update the overlays
                    updateOverlayContents(lookAt, camera);
                    updateOverlayColor(eventAction);

                    lastEventTime = currentTime;
                }

                // Show the crosshairs while the user is gesturing and fade them out after the user stops
                if (receivedUserInput) {
                    showCrosshairs();
                } else {
                    fadeCrosshairs();
                }
            }
        };

        // Register the Navigator Listener with the activity's World Window.
        this.getWorldWindow().addNavigatorListener(listener);

        return rootView;
    }
    protected void showCrosshairs() {
        if (this.animatorSet.isStarted()) {
            this.animatorSet.cancel();
        }
        this.crosshairs.setAlpha(1.0f);
        this.crosshairsActive = true;
    }

    /**
     * Fades the crosshairs using animation.
     */
    protected void fadeCrosshairs() {
        if (this.crosshairsActive) {
            this.crosshairsActive = false;
            if (!this.animatorSet.isStarted()) {
                this.animatorSet.start();
            }
        }
    }

    /**
     * Displays navigator state information in the status overlay views.
     *
     * @param lookAt Where the navigator is looking
     * @param camera Where the camera is positioned
     */
    protected void updateOverlayContents(LookAt lookAt, Camera camera) {
        latView.setText(String.valueOf(lookAt.latitude).substring(0,String.valueOf(lookAt.latitude).indexOf(".")+7));
        lonView.setText(String.valueOf(lookAt.longitude).substring(0,String.valueOf(lookAt.longitude).indexOf(".")+7));
        altView.setText(formatAltitude(camera.altitude));
    }

    /**
     * Brightens the colors of the overlay views when when user input occurs.
     *
     * @param eventAction The action associated with this navigator event
     */
    protected void updateOverlayColor(@WorldWind.NavigatorAction int eventAction) {
        int color = (eventAction == WorldWind.NAVIGATOR_STOPPED) ? 0xA0FFFF00 /*semi-transparent yellow*/ : Color.YELLOW;
        latView.setTextColor(color);
        lonView.setTextColor(color);
        altView.setTextColor(color);
    }

    protected String formatLatitude(double latitude) {
        int sign = (int) Math.signum(latitude);
        return String.format("%6.3f°%s", (latitude * sign), (sign >= 0.0 ? "N" : "S"));
    }

    protected String formatLongitude(double longitude) {
        int sign = (int) Math.signum(longitude);
        return String.format("%7.3f°%s", (longitude * sign), (sign >= 0.0 ? "E" : "W"));
    }

    protected String formatAltitude(double altitude) {
        return String.format("Eye: %,.0f %s",
                (altitude < 100000 ? altitude : altitude / 1000),
                (altitude < 100000 ? "m" : "km"));
    }



   //----------------------------------------------------------------------------


    /**
     * This inner class is a custom WorldWindController that handles both picking and navigation via a combination of
     * the native World Wind navigation gestures and Android gestures. This class' onTouchEvent method arbitrates
     * between pick events and globe navigation events.
     */
    public class PickNavigateController extends BasicWorldWindowController {

        protected Object pickedObject;          // last picked object from onDown events

        protected Object selectedObject;        // last "selected" object from single tap

        /**
         * Assign a subclassed SimpleOnGestureListener to a GestureDetector to handle the "pick" events.
         */
        protected GestureDetector pickGestureDetector = new GestureDetector(
                getContext().getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent event) {
                pick(event);    // Pick the object(s) at the tap location
                return false;   // By not consuming this event, we allow it to pass on to the navigation gesture handlers
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                toggleSelection();  // Highlight the picked object
                codeView("file:///android_asset/test.html");
                // By not consuming this event, we allow the "up" event to pass on to the navigation gestures,
                // which is required for proper zoom gestures.  Consuming this event will cause the first zoom
                // gesture to be ignored.  As an alternative, you can implement onSingleTapConfirmed and consume
                // event as you would expect, with the trade-off being a slight delay tap response.
                return false;
            }
        });

        /**
         * Delegates events to the pick handler or the native World Wind navigation handlers.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Allow pick listener to process the event first.
            boolean consumed = this.pickGestureDetector.onTouchEvent(event);

            // If event was not consumed by the pick operation, pass it on the globe navigation handlers
            if (!consumed) {

                // The super class performs the pan, tilt, rotate and zoom
                return super.onTouchEvent(event);
            }
            return consumed;
        }

        /**
         * Performs a pick at the tap location.
         */
        public void pick(MotionEvent event) {
            // Forget our last picked object
            this.pickedObject = null;

            // Perform a new pick at the screen x, y
            PickedObjectList pickList = getWorldWindow().pick(event.getX(), event.getY());

            // Get the top-most object for our new picked object
            PickedObject topPickedObject = pickList.topPickedObject();
            if (topPickedObject != null) {
                this.pickedObject = topPickedObject.getUserObject();
            }
        }

        /**
         * Toggles the selected state of a picked object.
         */
        public void toggleSelection() {

            // Display the highlight or normal attributes to indicate the
            // selected or unselected state respectively.
            if (pickedObject instanceof Highlightable) {

                // Determine if we've picked a "new" object so we know to deselect the previous selection
                boolean isNewSelection = pickedObject != this.selectedObject;

                // Only one object can be selected at time, deselect any previously selected object
                if (isNewSelection && this.selectedObject instanceof Highlightable) {
                    ((Highlightable) this.selectedObject).setHighlighted(false);
                }

                // Show the selection by showing its highlight attributes
                ((Highlightable) pickedObject).setHighlighted(isNewSelection);
                this.getWorldWindow().requestRedraw();

                // Track the selected object
                this.selectedObject = isNewSelection ? pickedObject : null;
            }
        }

        public void codeView(String url){
            // Enable JavaScript (which is off by default)
            Context context = getContext();
            Intent intent = new Intent(context, CodeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("url", url);
            intent.putExtra("arguments", bundle);
            context.startActivity(intent);
        }

    }

}
