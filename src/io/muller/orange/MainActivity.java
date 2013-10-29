package io.muller.orange;

import io.muller.orange.Track.Mode;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements TrackUpdateListener,
		TrackStatusListener, LocationUpdateListener, TrackCreationListener {
	
	
	private TextView accuracyView;
	private TextView timeView;
	private TextView distView;
	private TextView paceView;
	private TextView locationIcon;
	private Button startButton;
	private Button saveButton;
	private int clockUpdateInterval = 100;
	private OrangeApp app;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		app = (OrangeApp) getApplicationContext();
		app.addLocationUpdateListener(this);
		app.addTrackCreationListener(this);
		

		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		initUI();
		initTimer();
		getTrack().addTrackStatusListener(this);
		getTrack().addTrackUpdateListener(this);
	}

	private void initUI() {
		accuracyView = (TextView) findViewById(R.id.accuracy_view);
		timeView = (TextView) findViewById(R.id.time_view);
		distView = (TextView) findViewById(R.id.distance);
		startButton = (Button) findViewById(R.id.start_btn);
		saveButton = (Button) findViewById(R.id.save_btn);
		Button settingsButton = (Button) findViewById(R.id.settings_btn);

		accuracyView.setTypeface(app.getFont(OrangeApp.Font.OSWALD_REGULAR));
		timeView.setTypeface(app.getFont(OrangeApp.Font.DIGITAL));
		distView.setTypeface(app.getFont(OrangeApp.Font.DIGITAL));
		
		saveButton.setTypeface(app.getFont(OrangeApp.Font.ICONS));

		startButton.setTypeface(app.getFont(OrangeApp.Font.ICONS));
		
		settingsButton.setTypeface(app.getFont(OrangeApp.Font.ICONS));
		
		locationIcon = ((TextView)findViewById(R.id.location_icon));
		paceView = ((TextView) findViewById(R.id.pace));
		
		TextView miles_label = ((TextView) findViewById(R.id.miles_label));
		miles_label.setTypeface(app.getFont(OrangeApp.Font.OSWALD_REGULAR));

		boolean metric = prefs.getString("units_system", "0").equals("0");
		if(metric){
			miles_label.setText(R.string.kilometers);
		}
		((TextView) findViewById(R.id.pace_label)).setTypeface(app.getFont(OrangeApp.Font.OSWALD_REGULAR));
		((TextView) findViewById(R.id.pace)).setTypeface(app.getFont(OrangeApp.Font.OSWALD_REGULAR));
		((TextView) findViewById(R.id.road_icon)).setTypeface(app.getFont(OrangeApp.Font.ICONS));
		((TextView) findViewById(R.id.clock_icon)).setTypeface(app.getFont(OrangeApp.Font.ICONS));
		locationIcon.setTypeface(app.getFont(OrangeApp.Font.ICONS));
	
		statusChanged(getMode());
		trackUpdated(getTrack().getLastPoint());
		updateTimer();
		
		
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
//				saveTrack();
				saveActivity();
			}
		});
		settingsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				viewSettings();
			}
		});
		
	}
	
	protected Track getTrack(){
		return app.getTrack();
	}

	public Track.Mode getMode() {
		return getTrack().getMode();
	}


	protected void start() {
		getTrack().start();

	}

	protected void pause() {
		getTrack().pause();
	}

	public void locationUpdated(Location loc) {
		boolean metric = prefs.getString("units_system", "0").equals("0");
		accuracyView.setText(getString(R.string.accurate_to_within) + " "
				+ Math.round(loc.getAccuracy() * (metric?1.0:39.0/12.0)) + " "
				+ (metric? getString(R.string.meters):getString(R.string.feet)));
		locationIcon.setText(getString(R.string.icon_gps_locked));
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
			return hours + ":" + pad(mins) + ":" + pad(secs);// + "." + millis;
		} else if (mins > 0) {
			return mins + ":" + pad(secs);// + "." + millis;
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
		timeView.setText(toTimeString((long) getTrack().getDuration()));
	}

	@Override
	public void trackUpdated(TrkPt pt) {
		if(pt==null) return;
		boolean metric = prefs.getString("units_system", "0").equals("0");
		double distance = pt.getDistance() / (metric? 1000:1609);
		// distance = Math.round(distance*100)/100;
		timeView.setText(toTimeString((long) pt.getDuration()));
		distView.setText(String.format("%.2f", distance));
		if(pt.getDistance()>100){
			// only calculate pace if we've traveled more than 100 meters
			float pace = (float) pt.getDuration() / (float) distance;
			if(pace<3600000){
				paceView.setText(toTimeString((long) pace));
				paceView.setTypeface(app.getFont(OrangeApp.Font.DIGITAL));
			}else {
				paceView.setText(R.string.infinity);
				paceView.setTypeface(app.getFont(OrangeApp.Font.OSWALD_REGULAR));
			}
		}
	}

	@Override
	public void statusChanged(Mode mode) {
		switch (mode) {
		case RUNNING:
			startButton.setText(getString(R.string.icon_pause));
			startButton.setBackgroundResource(R.drawable.orange_button);
			saveButton.setVisibility(View.GONE);
			break;
		case PAUSED:
		case STOPPED:
			startButton.setText(getString(R.string.icon_play));
			startButton.setBackgroundResource(R.drawable.green_button);
			saveButton.setVisibility(View.VISIBLE);
			break;
		}
		
	}
	
	public void saveActivity(){
		Intent i = new Intent(getApplicationContext(), SaveActivity.class);
		startActivity(i);
	}
	
	public void viewSettings(){

		Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
		startActivity(i);
	}

	@Override
	public void trackCreated(Track track) {
		getTrack().addTrackStatusListener(this);
		getTrack().addTrackUpdateListener(this);
	}

}
