package com.topdown.game;

import java.io.Serializable;

public class RotatePlayerRequest implements Serializable{
	private static final long serialVersionUID = 3210575712573134215L;
	public int index;
	public float angle;
	public RotatePlayerRequest(int index, float angle) {
		this.index = index;
		this.angle = angle;
	}

}
