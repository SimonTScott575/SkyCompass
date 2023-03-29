package com.icarus1.map;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class SelectionMapView extends MapView {

    private boolean setUp;
    private boolean drawnOnce;
    private Marker setLocationMarker;

    public SelectionMapView(Context context) {
        super(context);
    }

    public SelectionMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectionMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (setUp && !drawnOnce) {
            getController().setZoom(getScreenRect(new Rect()).right / 256d / 2d);
            drawnOnce = true;
        }
    }

    public void setUp() {

        setTileSource(TileSourceFactory.MAPNIK);
        getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        getController().setCenter(new GeoPoint(0d, 0d));
        setMultiTouchControls(true);

        setLocationMarker = new Marker(this);
        getSetLocationMarker().setDraggable(true);
        getSetLocationMarker().setDragOffset(7);
        getOverlays().add(getSetLocationMarker());

        setUp = true;

    }

    public Marker getSetLocationMarker() {
        return setLocationMarker;
    }

}
