package io.muller.orange;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
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
	private static Typeface oswaldBold, oswaldRegular, oswaldLight, digital;
	
	public enum Font{OSWALD_BOLD, OSWALD_REGULAR, OSWALD_LIGHT, DIGITAL};

	public OrangeApp getInstance() {
		return singleton;
	}

	public Track getTrack() {
		return track;
	}

	public void setTrack(Track track) {
		this.track = track;
	}

	public void onCreate() {
		super.onCreate();
		singleton = this;
		initGPS();
		initFonts();
	}

	private void initFonts() {
		oswaldBold = Typeface.createFromAsset(getAssets(), "Oswald-Bold.ttf");
		oswaldRegular = Typeface.createFromAsset(getAssets(), "Oswald-Regular.ttf");
		oswaldLight = Typeface.createFromAsset(getAssets(), "Oswald-Light.ttf");
		digital = Typeface.createFromAsset(getAssets(), "BebasNeue.ttf");
	}
	
	public Typeface getFont(Font f){
		switch(f){
		case OSWALD_BOLD: return oswaldBold;
		case OSWALD_REGULAR: return oswaldRegular;
		case OSWALD_LIGHT: return oswaldRegular;
		case DIGITAL: return digital;
		}
		return oswaldRegular;
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

	public Track createNewTrack() {
		track.destroy();
		track = new Track(locationManager);
		return track;
	}

	private void notifyLocationUpdateListeners(Location location) {
		for (LocationUpdateListener l : locationListeners) {
			l.locationUpdated(location);
		}
	}

	public void addLocationUpdateListener(LocationUpdateListener l) {
		locationListeners.add(l);
	}

	private void startGPS() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				updateInterval, 0, locationListener);
	}

}
