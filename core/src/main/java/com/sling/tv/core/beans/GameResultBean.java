package com.sling.tv.core.beans;

import java.util.Date;

public class GameResultBean {

	String team1;
	String team2;
	long blackout;
	String callsign;
	String channelid;
	String displaydata;
	Date gmt_datetime;
	String location;
	String packageName;
	String searchData;
	String ziplist;
	
	public String getTeam1() {
		return team1;
	}
	public void setTeam1(String team1) {
		this.team1 = team1;
	}
	public String getTeam2() {
		return team2;
	}
	public void setTeam2(String team2) {
		this.team2 = team2;
	}
	public long getBlackout() {
		return blackout;
	}
	public void setBlackout(long blackout) {
		this.blackout = blackout;
	}
	public String getCallsign() {
		return callsign;
	}
	public void setCallsign(String callsign) {
		this.callsign = callsign;
	}
	public String getChannelid() {
		return channelid;
	}
	public void setChannelid(String channelid) {
		this.channelid = channelid;
	}
	public String getDisplaydata() {
		return displaydata;
	}
	public void setDisplaydata(String displaydata) {
		this.displaydata = displaydata;
	}
	public Date getGmt_datetime() {
		return gmt_datetime;
	}
	public void setGmt_datetime(Date gmt_datetime) {
		this.gmt_datetime = gmt_datetime;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	
}
