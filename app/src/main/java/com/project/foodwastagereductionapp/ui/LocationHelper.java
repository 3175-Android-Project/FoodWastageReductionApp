package com.project.foodwastagereductionapp.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.util.Consumer;

import java.util.List;
import java.util.Locale;

/**
 * Phone location + address <-> map position, for distance-based "Nearby".
 * geocode() and cityFrom() block (network), so call them on a worker thread.
 */
public final class LocationHelper {

    private LocationHelper() {
    }

    public static boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Current phone location on the main thread, or null (no permission, location off, timeout). */
    @SuppressWarnings("MissingPermission") // checked by hasPermission()
    public static void getCurrentLocation(Context context, Consumer<Location> done) {
        if (!hasPermission(context)) {
            done.accept(null);
            return;
        }
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null || !LocationManagerCompat.isLocationEnabled(lm)) {
            done.accept(null);
            return;
        }
        String provider = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                ? LocationManager.NETWORK_PROVIDER : LocationManager.GPS_PROVIDER;

        // A recent saved fix (under 10 min) is good enough and instant
        Location last = lm.getLastKnownLocation(provider);
        if (last != null && System.currentTimeMillis() - last.getTime() < 10 * 60 * 1000) {
            done.accept(last);
            return;
        }
        LocationManagerCompat.getCurrentLocation(lm, provider, (android.os.CancellationSignal) null,
                ContextCompat.getMainExecutor(context), location -> done.accept(location != null ? location : last));
    }

    /** "V5H 2E2, Burnaby, BC, Canada" -> {lat, lng}, or null if not found / no network. */
    public static double[] geocode(Context context, String address) {
        if (address == null || address.trim().isEmpty() || !Geocoder.isPresent()) return null;
        try {
            List<Address> results = new Geocoder(context, Locale.CANADA).getFromLocationName(address, 1);
            if (results == null || results.isEmpty()) return null;
            Address a = results.get(0);
            return new double[]{a.getLatitude(), a.getLongitude()};
        } catch (Exception e) { // IOException (no network) or bad input
            return null;
        }
    }

    /** City name for a position (e.g. "Burnaby"), or null. */
    public static String cityFrom(Context context, double latitude, double longitude) {
        if (!Geocoder.isPresent()) return null;
        try {
            List<Address> results = new Geocoder(context, Locale.CANADA).getFromLocation(latitude, longitude, 1);
            if (results == null || results.isEmpty()) return null;
            Address a = results.get(0);
            return a.getLocality() != null ? a.getLocality() : a.getSubAdminArea();
        } catch (Exception e) {
            return null;
        }
    }

    /** Straight-line distance in km. */
    public static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        float[] result = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, result);
        return result[0] / 1000.0;
    }
}