package com.github.podd.resources;

public class Filter {

	public String field;
	public String value;
	
	public Filter(String field, String value) {
		this.field = field;
		this.value = value;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
