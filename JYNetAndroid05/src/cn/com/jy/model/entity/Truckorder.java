package cn.com.jy.model.entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Truckorder implements Serializable {
	private String tid,
					tkind,
					leadnumber,
					stime;
	private int pertcount,tcount;
	private double pertweight;
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	public String getTkind() {
		return tkind;
	}
	public void setTkind(String tkind) {
		this.tkind = tkind;
	}
	public String getLeadnumber() {
		return leadnumber;
	}
	public void setLeadnumber(String leadnumber) {
		this.leadnumber = leadnumber;
	}
	public String getStime() {
		return stime;
	}
	public void setStime(String stime) {
		this.stime = stime;
	}
	public int getPertcount() {
		return pertcount;
	}
	public void setPertcount(int pertcount) {
		this.pertcount = pertcount;
	}
	public int getTcount() {
		return tcount;
	}
	public void setTcount(int tcount) {
		this.tcount = tcount;
	}
	public double getPertweight() {
		return pertweight;
	}
	public void setPertweight(double pertweight) {
		this.pertweight = pertweight;
	}
	
}
