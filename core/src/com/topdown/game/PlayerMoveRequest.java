package com.topdown.game;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

public class PlayerMoveRequest implements Serializable{
	private static final long serialVersionUID = -381360011373311578L;
	
	public Vector2 delta;
	public int index;
	public PlayerMoveRequest(int index, Vector2 delta) {
		this.index = index;
		this.delta = delta;
	}
	public PlayerMoveRequest(int index, int x, int y){
		this(index, new Vector2(x, y));
	}
}
