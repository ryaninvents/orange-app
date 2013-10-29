package io.muller.orange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Xml;

public class Track implements LocationListener{
	private ArrayList<TrkPt> pts;
	private TrkPt lastPt = null;
	private long duration = 0;
	private long started = 0;
	private long timestamp = 0;
	private LocationManager locationManager;
	private int updateInterval = 3000;
	private int rating = 3;
	private String name = "";
	
	private ArrayList<TrackUpdateListener> pointListeners = new ArrayList<TrackUpdateListener>();
	private ArrayList<TrackStatusListener> statusListeners = new ArrayList<TrackStatusListener>();
	
	private Mode mode = Mode.STOPPED;
	
	public enum Mode {STOPPED, RUNNING, PAUSED};
	
	public Track(LocationManager locationManager){
		pts = new ArrayList<TrkPt>();
		this.locationManager = locationManager;
		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateInterval, 0, this);
		
	}
	
	public void addTrackUpdateListener(TrackUpdateListener listener){
		pointListeners.add(listener);
	}
	
	public void addTrackStatusListener(TrackStatusListener listener){
		statusListeners.add(listener);
	}
	
	public void destroy(){
		locationManager.removeUpdates(this);
	}
	
	public void addPt(TrkPt pt){
		pt.setMode(mode);
		double distance = 0;
		if(lastPt != null){
			distance = lastPt.getDistance();
		
			if(mode==Mode.RUNNING && lastPt.getMode() == Mode.RUNNING){
				distance += lastPt.distanceTo(pt);
			}
		}
		lastPt = pt;
		pt.setDistance(distance);
		pt.setDuration(getDuration());
		pts.add(pt);
		for(TrackUpdateListener l:pointListeners){
			l.trackUpdated(pt);
		}
	}
	
	public double getDistance(){
		if(lastPt==null) return 0;
		return lastPt.getDistance();
	}
	
	public long getDuration(){
		if(mode == Mode.RUNNING){
			return duration + System.currentTimeMillis() - started;
		}else{
			return duration;
		}
	}
	
	public void start(){
		if(mode == Mode.RUNNING){
			throw new RuntimeException("Track already running but was started");
		}
		started = System.currentTimeMillis();
		if(timestamp == 0) timestamp = started;
		mode = Mode.RUNNING;
		notifyStatusListeners();
	}
	
	public void pause(){
		if(mode == Mode.PAUSED || mode == Mode.STOPPED){
			throw new RuntimeException("Track not running but was paused");
		}
		duration += System.currentTimeMillis() - started;
		mode = Mode.PAUSED;
		notifyStatusListeners();
	}
	
	public void stop(){
		if(mode == Mode.PAUSED || mode == Mode.STOPPED){
			throw new RuntimeException("Track not running but was stopped");
		}
		duration = System.currentTimeMillis() - started;
		mode = Mode.STOPPED;
		notifyStatusListeners();
	}
	
	private void notifyStatusListeners(){
		for(TrackStatusListener l:statusListeners){
			l.statusChanged(mode);
		}
	}
	
	public TrkPt getLastPoint(){
		if(pts.size()==0) return null;
		return pts.get(pts.size()-1);
	}
	
	public long getTimestamp(){
		return timestamp;
	}
	
	public String toJSON(){
		JSONObject ob = new JSONObject();
		try {
			ob.put("rating", rating);
			ob.put("activity_name",name);
			ob.put("ts", pts.size()>0?pts.get(0).getTimestamp():0);
			ob.put("distance", getDistance());
			ob.put("duration", getDuration());
			JSONArray pts = new JSONArray();
			for(TrkPt pt:this.pts){
				JSONObject pp = new JSONObject();
				pp.put("ts",pt.getTimestamp());
				pp.put("lat", pt.getLatitude());
				pp.put("lon", pt.getLongitude());
				pp.put("sigma", pt.getSigma());
				pp.put("duration", pt.getDuration());
				pp.put("distance", pt.getDistance());
				pts.put(pp);
			}
			if(this.pts.size()>0) ob.put("points", pts);
		}catch(Exception e){
			e.printStackTrace();
		}
		return ob.toString();
	}
	
	public String toGPX() throws IllegalArgumentException, IllegalStateException, IOException{
		XmlSerializer s = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		s.setOutput(writer);
		s.startDocument("UTF-8",true);
		s.startTag("","gpx");
		s.startTag("","trk");
		s.startTag("","name");
		s.text("Activity "+(System.currentTimeMillis()/1000));
		s.endTag("", "name");
		s.startTag("","trkseg");
		for(TrkPt pt:pts){
			s.startTag("", "trkpt");
			{
				s.attribute("", "lat", String.valueOf(pt.getLatitude()));
				s.attribute("", "lon", String.valueOf(pt.getLongitude()));
				
				s.startTag("","ele");
				{
					s.text(String.valueOf(pt.getElevation()));
				}
				s.endTag("","ele");
				
				s.startTag("","time");
				{
					s.text(String.format("%tFT%<tRZ", new Date(pt.getTimestamp())));
				}
				s.endTag("", "time");
				
			}
			s.endTag("", "trkpt");
		}
		s.endTag("","trkseg");
		s.endTag("","trk");
		s.endTag("","gpx");
		s.endDocument();
		return writer.toString();
	}
	
	public void writeToXml(Activity a, String filename) throws IllegalArgumentException, IllegalStateException, IOException{
		if(pts.size()==0) return;
		
		String string = toGPX();

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
		    File dir = Environment.getExternalStorageDirectory();
//		    dir = new File(dir.getAbsolutePath()+"/orange");
		    File xml = new File(dir, filename+".gpx");
		    FileOutputStream fos = new FileOutputStream(xml);
			fos.write(string.getBytes());
			fos.close();
		}
	}

	@Override
	public void onLocationChanged(Location loc) {
		addPt(new TrkPt(System.currentTimeMillis(), loc));
	}

	@Override
	public void onProviderDisabled(String arg0) {}

	@Override
	public void onProviderEnabled(String arg0) {}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

	public Mode getMode() {
		return mode;
	}

	public void setName(String name) {
		this.name = name; 
	}
	
	public void setRating(int r){
		rating = r;
	}
}
