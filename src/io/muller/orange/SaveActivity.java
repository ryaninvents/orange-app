package io.muller.orange;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

public class SaveActivity extends Activity {
	private OrangeApp app;
	private Button backButton;
	private Button trashButton;
	private Button uploadButton;
	private EditText activityName;
	private RatingBar ratingBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save);
		app = (OrangeApp) getApplicationContext();
		initUI();
	}

	private void initUI() {
		backButton = (Button) findViewById(R.id.back);
		trashButton = (Button) findViewById(R.id.trash);
		uploadButton = (Button) findViewById(R.id.upload);
		activityName = (EditText) findViewById(R.id.activity_name);
		ratingBar = (RatingBar) findViewById(R.id.ratingBar1);
		Typeface iconFont = app.getFont(OrangeApp.Font.ICONS);
		backButton.setTypeface(iconFont);
		trashButton.setTypeface(iconFont);
		uploadButton.setTypeface(iconFont);

		uploadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				saveTrack();
			}
		});

		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		trashButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				app.createNewTrack();
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.save, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void saveTrack() {
		String value = activityName.getText().toString();
		app.getTrack().setName(value);
		app.getTrack().setRating((int) ratingBar.getRating());
		try {
			final String message = app.getTrack().toJSON();
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			final String server = prefs.getString("server", "orange.muller.io");
			final String username = prefs.getString("user_name", "");
			final String userKey = prefs.getString("user_key", "");
			if (username.length() == 0 || userKey.length() == 0) {
				Toast.makeText(getApplicationContext(),
						"Please set up username and key in Settings",
						Toast.LENGTH_LONG).show();
				return;
			}
			final SaveActivity _this = this;

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					HttpURLConnection conn = null;
					try {

						URL url = new URL("http://" + server + "/upload?user="
								+ username + "&key=" + userKey);

						conn = (HttpURLConnection) url.openConnection();

						conn.setDoInput(true);
						conn.setDoOutput(true);
						//conn.setChunkedStreamingMode(0);
						conn.setRequestMethod("POST");
						conn.setRequestProperty("Content-Type",
								"application/json");

						conn.setFixedLengthStreamingMode(message.getBytes().length);

						conn.connect();
						OutputStream os = new BufferedOutputStream(
								conn.getOutputStream());
						os.write(message.getBytes());
						// clean up
						os.flush();
						os.close();

						/*
						 * BufferedOutputStream out = new BufferedOutputStream(
						 * urlConnection.getOutputStream());
						 * out.write(json.getBytes());
						 * 
						 * out.flush();
						 */
						int code = conn.getResponseCode();
						// check the response code
						if (code == 401) {
							// auth error
							_this.runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(_this,
											"Invalid username or key",
											Toast.LENGTH_SHORT).show();
								}
							});
						} else {
							// success!
							_this.runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(_this, "Saved!",
											Toast.LENGTH_SHORT).show();
								}
							});
						}
					} catch (Exception e) {
						e.printStackTrace();
						_this.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(
										_this,
										"Error uploading to \"" + server + "\"",
										Toast.LENGTH_SHORT).show();
							}
						});
					} finally {
						if (conn != null)
							conn.disconnect();
					}
				}
			};

			Thread thr = new Thread(runnable);
			thr.start();

			Toast.makeText(getApplicationContext(), "Saving...",// "Trace saved as "+value+".gpx",
					Toast.LENGTH_SHORT).show();
			app.createNewTrack();
			finish();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Could not save--" + e.getMessage(), Toast.LENGTH_LONG)
					.show();
			e.printStackTrace();
		}
	}

}
