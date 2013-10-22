package io.muller.orange;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Xml;

public class Track implements LocationListener{
	private ArrayList<TrkPt> pts;
	private TrkPt lastPt = null;
	private long duration = 0;
	private long started = 0;
	
	private Mode mode;
	
	public enum Mode {STOPPED, RUNNING, PAUSED};
	
	public Track(){
		pts = new ArrayList<TrkPt>();
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
		pts.add(pt);
	}
	
	public double getDistance(){
		return lastPt.getDistance();
	}
	
	public double getDuration(){
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
		mode = Mode.RUNNING;
	}
	
	public void pause(){
		if(mode == Mode.PAUSED || mode == Mode.STOPPED){
			throw new RuntimeException("Track not running but was paused");
		}
		duration = System.currentTimeMillis() - started;
		mode = Mode.PAUSED;
	}
	
	public void stop(){
		if(mode == Mode.PAUSED || mode == Mode.STOPPED){
			throw new RuntimeException("Track not running but was stopped");
		}
		duration = System.currentTimeMillis() - started;
		mode = Mode.STOPPED;
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
		return writer.toString();
	}
	
	public void writeToXml(Activity a) throws IllegalArgumentException, IllegalStateException, IOException{
		String FILENAME = "activity_"+System.currentTimeMillis()+".xml";
		String string = toGPX();

		FileOutputStream fos = a.openFileOutput(FILENAME, Context.MODE_PRIVATE);
		fos.write(string.getBytes());
		fos.close();
	}

	@Override
	public void onLocationChanged(Location loc) {
		addPt(new TrkPt(System.currentTimeMillis(), loc));
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
}
