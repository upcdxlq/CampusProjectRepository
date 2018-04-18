package com.upc.worldwindx.fragments;

import android.util.Log;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.LayerFactory;

public class WmsLayerFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow (GLSurfaceView) object with a WMS Layer
     *
     * @return The WorldWindow object containing the globe.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Create a layer factory, World Wind's general component for creating layers
        // from complex data sources.
        LayerFactory layerFactory = new LayerFactory();

        // Create an OGC Web Map Service (WMS) layer to display the
        // surface temperature layer from NASA's Near Earth Observations WMS.
        layerFactory.createFromWms(
            "http://gisserver.tianditu.com/TDTService/region/wms", // WMS server URL
            "030100",                         // WMS layer name
            new LayerFactory.Callback() {
                @Override
                public void creationSucceeded(LayerFactory factory, Layer layer) {
                    // Add the finished WMS layer to the World Window.
                    getWorldWindow().getLayers().addLayer(layer);
                    Log.i("gov.nasa.worldwind", "WMS layer creation succeeded");
                }

                @Override
                public void creationFailed(LayerFactory factory, Layer layer, Throwable ex) {
                    // Something went wrong connecting to the WMS server.
                    Log.e("gov.nasa.worldwind", "WMS layer creation failed", ex);
                }
            }
        );

        return wwd;
    }
}