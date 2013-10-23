package io.muller.orange;

import java.io.IOException;

import io.muller.orange.Track.Mode;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements TrackUpdateListener,
		TrackStatusListener {
	private LocationManager locationManager;
	private LocationListener locationListener;
	private TextView accuracyView;
	private TextView timeView;
	private TextView distView;
	private TextView statusView;
	private Button startButton;
	private Button saveButton;
	private int updateInterval = 3000;
	private int clockUpdateInterval = 100;

	private Track track;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initGPS();
		initUI();
		initTimer();
	}

	private void initUI() {
		accuracyView = (TextView) findViewById(R.id.accuracy_view);
		timeView = (TextView) findViewById(R.id.time_view);
		distView = (TextView) findViewById(R.id.distance);
		startButton = (Button) findViewById(R.id.start_btn);
		saveButton = (Button) findViewById(R.id.save_btn);
		statusView = (TextView) findViewById(R.id.status);
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (getMode() == Track.Mode.RUNNING) {
					pause();
				} else {
					start();
				}
			}
		});
		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveTrack();
			}
		});
	}

	public Track.Mode getMode() {
		return track.getMode();
	}

	protected void saveTrack() {
		try {
			final MainActivity a = this;
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Select filename");
			alert.setMessage("Filename:");

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			alert.setView(input);

			alert.setPositiveButton("Save",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();

							try {
								track.writeToXml(a, value);

								Toast.makeText(getApplicationContext(), "Trace saved as "+value+".gpx",
										Toast.LENGTH_SHORT).show();
							} catch (Exception e) {
								Toast.makeText(getApplicationContext(), "Could not save--"+e.getMessage(),
										Toast.LENGTH_LONG).show();
								e.printStackTrace();
							}
						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							Toast.makeText(getApplicationContext(), "Save cancelled",
									Toast.LENGTH_SHORT).show();
						}
					});

			alert.show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Could not save",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
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

	private void startGPS() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				updateInterval, 0, locationListener);
	}

	protected void start() {
		startButton.setText(getString(R.string.pause));
		track.start();

	}

	protected void pause() {
		startButton.setText(getString(R.string.start));
		track.pause();
	}

	public void locationUpdated(Location loc) {
		accuracyView.setText(getString(R.string.accurate_to_within) + " "
				+ Math.round(loc.getAccuracy() / 12 * 39) + " "
				+ getString(R.string.feet));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private String pad(int n) {
		return n < 10 ? "0" + n : "" + n;
	}

	private String toTimeString(long dur) {
		int millis = (int) (dur % 1000) / 100;
		dur /= 1000;
		int secs = (int) (dur % 60);
		dur /= 60;
		int mins = (int) (dur % 60);
		dur /= 60;
		int hours = (int) dur;
		if (hours > 0) {
			return hours + ":" + pad(mins) + ":" + pad(secs) + "." + millis;
		} else if (mins > 0) {
			return mins + ":" + pad(secs) + "." + millis;
		} else {
			return secs + "." + millis;
		}
	}

	private void initTimer() {
		final Handler handler = new Handler();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				updateTimer();
				handler.postDelayed(this, clockUpdateInterval);
			}
		};
		handler.postDelayed(runnable, clockUpdateInterval);
	}

	protected void updateTimer() {
		timeView.setText(toTimeString((long) track.getDuration()));
	}

	@Override
	public void trackUpdated(TrkPt pt) {
		double distance = pt.getDistance() / 1609;
		// distance = Math.round(distance*100)/100;
		timeView.setText(toTimeString((long) pt.getDuration()));
		distView.setText(String.format("%.2f mi", distance));
	}

	@Override
	public void statusChanged(Mode mode) {
		String status;
		switch (mode) {
		case RUNNING:
			status = getString(R.string.running);
			break;
		case PAUSED:
			status = getString(R.string.paused);
			break;
		case STOPPED:
			status = getString(R.string.stopped);
			break;
		default:
			status = "Error";
		}
		statusView.setText(status);
	}

}
