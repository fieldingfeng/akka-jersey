package com.example;

import java.io.Serializable;

public class Work implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int from;
	int to;
	
	public Work(int from, int to) {
		this.from = from;
		this.to = to;
	}
}
