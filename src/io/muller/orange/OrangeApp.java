package io.muller.orange;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class OrangeApp extends Application {
	private Track track;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private int updateInterval = 3000;
	private ArrayList<LocationUpdateListener> locationListeners = new ArrayList<LocationUpdateListener>();
	private static OrangeApp singleton;
	
	public OrangeApp getInstance(){
		return singleton;
	}

	public Track getTrack() {
		return track;
	}

	public void setTrack(Track track) {
		this.track = track;
	}

	public void onCreate(){
		super.onCreate();
		singleton = this;
		initGPS();
	}
	
	private void initGPS() {

		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				notifyLocationUpdateListeners(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		track = new Track(locationManager);
		
		startGPS();
	}
	
	private void notifyLocationUpdateListeners(Location location){
		for(LocationUpdateListener l:locationListeners){
			l.locationUpdated(location);
		}
	}
	
	public void addLocationUpdateListener(LocationUpdateListener l){
		locationListeners.add(l);
	}
	

	private void startGPS() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				updateInterval, 0, locationListener);
	}

}
