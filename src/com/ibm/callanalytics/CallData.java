package com.ibm.callanalytics;

public class CallData {
	String time; 
	String date; 
	int length; 
	
	public CallData(String time, String date, int length){
		this.time = time; 
		this.date = date; 
		this.length = length; 
	}
	
	public int addCall() {
		ManageDB dbManager = new ManageDB();
    	dbManager.connect();
    	int call_id = dbManager.addCall(time, date, length);
    	return call_id;
	}

}
