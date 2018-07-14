package com.latidude99.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Point implements Serializable{
	private static final long serialVersionUID = 2410247765300103983L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	private String lat;
	
	private String lng;
	
	@ManyToOne
	@JoinColumn(name = "enquiry_id")
	private Enquiry enquiry;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public Enquiry getEnquiry() {
		return enquiry;
	}

	public void setEnquiry(Enquiry enquiry) {
		this.enquiry = enquiry;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((enquiry == null) ? 0 : enquiry.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((lat == null) ? 0 : lat.hashCode());
		result = prime * result + ((lng == null) ? 0 : lng.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (enquiry == null) {
			if (other.enquiry != null)
				return false;
		} else if (!enquiry.equals(other.enquiry))
			return false;
		if (id != other.id)
			return false;
		if (lat == null) {
			if (other.lat != null)
				return false;
		} else if (!lat.equals(other.lat))
			return false;
		if (lng == null) {
			if (other.lng != null)
				return false;
		} else if (!lng.equals(other.lng))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Point [id=" + id + ", lat=" + lat + ", lng=" + lng + ", enquiry=" + enquiry + "]";
	}

	public String toLatLng() {
		return lat + ","+ lng;
	}
	
	

}
