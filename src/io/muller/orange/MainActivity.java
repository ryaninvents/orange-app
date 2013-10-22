package io.muller.orange;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	private LocationManager locationManager;
	private LocationListener locationListener;
	private TextView accuracyView;
	private int updateInterval = 3000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initGPS();
		initUI();
	}

	private void initUI(){
		accuracyView = (TextView) findViewById(R.id.accuracy_view);
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
				locationUpdated(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// track = new Track();
		startGPS();
	}

	private void startGPS(){
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateInterval, 0, locationListener);
    }

	public void locationUpdated(Location loc) {
		accuracyView.setText("Accurate to "+loc.getAccuracy()+" meters");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
