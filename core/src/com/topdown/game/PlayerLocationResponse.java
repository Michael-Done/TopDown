package com.topdown.game;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

public class PlayerLocationResponse implements Serializable{
	private static final long serialVersionUID = 4227913164072876896L;
	public Vector2 location;
	public PlayerLocationResponse(Vector2 location) {
		this.location = location;
	}

}
