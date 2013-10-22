package io.muller.orange;

import java.util.ArrayList;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class Track implements LocationListener{
	private ArrayList<TrkPt> pts;
	private TrkPt lastPt = null;
	
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
