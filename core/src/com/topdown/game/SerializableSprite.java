package com.topdown.game;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

public class SerializableSprite implements Serializable {
	private static final long serialVersionUID = 757104122458221840L;
	
	public String sprite;
	public float angle;
	public Vector2 location;
	public float scalar;
	public Vector2 origin;
	
	public SerializableSprite(String sprite, float angle, Vector2 location, float scalar, Vector2 origin) {
		this.sprite = sprite;
		this.angle = angle;
		this.location = location;
		this.scalar = scalar;
		this.origin = origin;
	}
	
	public SerializableSprite(String sprite){
		this(sprite, 0, new Vector2(0, 0), 1, new Vector2(0, 0));
	}

	/**
	 * @return the sprite
	 */
	public String getSprite() {
		return sprite;
	}

	/**
	 * @param sprite the sprite to set
	 */
	public void setSprite(String sprite) {
		this.sprite = sprite;
	}

	/**
	 * @return the angle
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * @param angle the angle to set
	 */
	public void setAngle(float angle) {
		this.angle = angle;
	}

	/**
	 * @return the location
	 */
	public Vector2 getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Vector2 location) {
		this.location = location;
	}
	
	public void setLocation(float x, float y){
		setLocation(new Vector2(x, y));
	}

	/**
	 * @return the scalar
	 */
	public float getScalar() {
		return scalar;
	}

	/**
	 * @param scalar the scalar to set
	 */
	public void setScalar(float scalar) {
		this.scalar = scalar;
	}

	/**
	 * @return the origin
	 */
	public Vector2 getOrigin() {
		return origin;
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(Vector2 origin) {
		this.origin = origin;
	}

}
