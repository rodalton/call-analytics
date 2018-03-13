package com.ibm.callanalytics;

public class CallData {
	String time; 
	String date; 
	int duration; 
	
	public CallData(String time, String date, int duration){
		this.time = time; 
		this.date = date; 
		this.duration = duration; 
	}
	
	public int addCall() {
		ManageDB dbManager = new ManageDB();
    	dbManager.connect();
    	int call_id = dbManager.addCall(time, date, duration);
    	return call_id;
	}

}
