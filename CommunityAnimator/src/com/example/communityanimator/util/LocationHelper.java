package com.example.communityanimator.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.parse.ParseGeoPoint;

public class LocationHelper {
	// Initial offset for calculating the map bounds
	private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;

	// Accuracy for calculating the map bounds
	private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;
	private final static Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
	// Fields for the map radius in feet
	private static float radius = Application.getSearchDistance();

	/*
	 * Helper method to clean up old markers
	 */
	public static void cleanUpMarkers(Set<String> markersToKeep) {
		Log.d(Application.APPTAG, "cleanUpMarkers!");
		for (String objId : new HashSet<String>(mapMarkers.keySet())) {
			if (!markersToKeep.contains(objId)) {
				Marker marker = mapMarkers.get(objId);
				marker.remove();
				mapMarkers.get(objId).remove();
				mapMarkers.remove(objId);
			}
		}
	}

	/*
	 * Helper method to get the Parse GEO point representation of a location
	 */
	public static ParseGeoPoint geoPointFromLocation(Location loc) {
		return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
	}

	/*
	 * Helper method to calculate the offset for the bounds used in map zooming
	 */
	public static double calculateLatLngOffset(LatLng myLatLng,
			boolean bLatOffset) {
		Log.d(Application.APPTAG, "calculateLatLngOffset!");
		// The return offset, initialized to the default difference
		double latLngOffset = OFFSET_CALCULATION_INIT_DIFF;
		// Set up the desired offset distance in meters
		float desiredOffsetInMeters = radius;
		// Variables for the distance calculation
		float[] distance = new float[1];
		boolean foundMax = false;
		double foundMinDiff = 0;
		// Loop through and get the offset
		do {
			// Calculate the distance between the point of interest
			// and the current offset in the latitude or longitude direction
			if (bLatOffset) {
				Location.distanceBetween(myLatLng.latitude, myLatLng.longitude,
						myLatLng.latitude + latLngOffset, myLatLng.longitude,
						distance);
			} else {
				Location.distanceBetween(myLatLng.latitude, myLatLng.longitude,
						myLatLng.latitude, myLatLng.longitude + latLngOffset,
						distance);
			}
			// Compare the current difference with the desired one
			float distanceDiff = distance[0] - desiredOffsetInMeters;
			if (distanceDiff < 0) {
				// Need to catch up to the desired distance
				if (!foundMax) {
					foundMinDiff = latLngOffset;
					// Increase the calculated offset
					latLngOffset *= 2;
				} else {
					double tmp = latLngOffset;
					// Increase the calculated offset, at a slower pace
					latLngOffset += (latLngOffset - foundMinDiff) / 2;
					foundMinDiff = tmp;
				}
			} else {
				// Overshot the desired distance
				// Decrease the calculated offset
				latLngOffset -= (latLngOffset - foundMinDiff) / 2;
				foundMax = true;
			}
		} while (Math.abs(distance[0] - desiredOffsetInMeters) > OFFSET_CALCULATION_ACCURACY);
		return latLngOffset;
	}

	/*
	 * Helper method to calculate the bounds for map zooming
	 */
	public static LatLngBounds calculateBoundsWithCenter(LatLng myLatLng) {
		Log.d(Application.APPTAG, "calculateBoundsWithCenter!");
		// Create a bounds
		LatLngBounds.Builder builder = LatLngBounds.builder();

		// Calculate east/west points that should to be included
		// in the bounds
		double lngDifference = calculateLatLngOffset(myLatLng, false);
		LatLng east = new LatLng(myLatLng.latitude, myLatLng.longitude
				+ lngDifference);
		builder.include(east);
		LatLng west = new LatLng(myLatLng.latitude, myLatLng.longitude
				- lngDifference);
		builder.include(west);

		// Calculate north/south points that should to be included
		// in the bounds
		double latDifference = calculateLatLngOffset(myLatLng, true);
		LatLng north = new LatLng(myLatLng.latitude + latDifference,
				myLatLng.longitude);
		builder.include(north);
		LatLng south = new LatLng(myLatLng.latitude - latDifference,
				myLatLng.longitude);
		builder.include(south);

		return builder.build();
	}

}
