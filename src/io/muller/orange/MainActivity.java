package io.muller.orange;

import io.muller.orange.Track.Mode;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements TrackUpdateListener, TrackStatusListener{
	private LocationManager locationManager;
	private LocationListener locationListener;
	private TextView accuracyView;
	private TextView timeView;
	private TextView distView;
	private TextView statusView;
	private Button startButton;
	private int updateInterval = 3000;

	private Track track;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initGPS();
		initUI();
	}

	private void initUI(){
		accuracyView = (TextView) findViewById(R.id.accuracy_view);
		timeView = (TextView) findViewById(R.id.time_view);
		distView = (TextView) findViewById(R.id.distance);
		startButton = (Button) findViewById(R.id.start_btn);
		statusView = (TextView) findViewById(R.id.status);
		startButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(getMode()==Track.Mode.RUNNING){
					pause();
				}else{
					start();
				}
			}
		});
	}
	
	public Track.Mode getMode(){
		return track.getMode();
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

		track = new Track(locationManager);
		track.addTrackStatusListener(this);
		track.addTrackUpdateListener(this);
		startGPS();
	}

	private void startGPS(){
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateInterval, 0, locationListener);
    }
	
	protected void start(){
		track.start();
		
	}
	
	protected void pause(){
		track.pause();
	}

	public void locationUpdated(Location loc) {
		accuracyView.setText("Accurate to "+Math.round(loc.getAccuracy()/12*39)+" feet");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void trackUpdated(TrkPt pt) {
		timeView.setText(String.valueOf(pt.getDuration()/1000));
		distView.setText(String.valueOf(pt.getDistance()/1609));
	}

	@Override
	public void statusChanged(Mode mode) {
		statusView.setText(mode.name());
	}

}
