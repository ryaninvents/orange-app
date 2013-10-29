package io.muller.orange;

import android.location.Location;

public class TrkPt {
	private long timestamp;
	private Location location;
	private double heartRate;
	private double cadence;
	private String currentSong;
	private Track.Mode mode;
	private double distance;
	private double duration;
	
	public TrkPt(long timestamp, Location location){
		this.timestamp = timestamp;
		this.location = location;
	}
	
	public double getLatitude(){
		return location.getLatitude();
	}
	
	public double getLongitude(){
		return location.getLongitude();
	}
	
	public double getElevation(){
		return location.getAltitude();
	}
	
	public double distanceTo(TrkPt that){
		return this.location.distanceTo(that.location);
	}
	
	public long timeBetween(TrkPt that){
		return that.timestamp - this.timestamp;
	}

	public double getHeartRate() {
		return heartRate;
	}

	public void setHeartRate(double heartRate) {
		this.heartRate = heartRate;
	}

	public double getCadence() {
		return cadence;
	}

	public void setCadence(double cadence) {
		this.cadence = cadence;
	}

	public String getCurrentSong() {
		return currentSong;
	}

	public void setCurrentSong(String currentSong) {
		this.currentSong = currentSong;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Location getLocation() {
		return location;
	}

	public Track.Mode getMode() {
		return mode;
	}

	public void setMode(Track.Mode mode) {
		this.mode = mode;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public float getSigma() {
		return location.getAccuracy();
	}
}
