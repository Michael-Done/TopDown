package com.topdown.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

public class Player {

	private SerializableSprite sprite;
	//private int index;
	public Body body;
	private Vector2 targetVelocity;

	public Player(String tex, World world) {
		// Sprite
		sprite = new SerializableSprite(tex);
		if(tex.equals("Player_green_pistol.png")){
			sprite.origin.set(8, 7);
		}
		// Body
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set((8), (7));

		body = world.createBody(bodyDef);

		CircleShape shape = new CircleShape();
		shape.setRadius(7);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 0.1f;

		body.createFixture(fixtureDef);
		shape.dispose();

		body.setAngularDamping(100);

		targetVelocity = new Vector2();
	}

	public Player(String tex, int x, int y, World world) {
		this(tex, new Vector2(x, y), world);
	}

	public Player(String tex, Vector2 loc, World world) {
		this(tex, world);
		body.setTransform(loc, body.getAngle());
	}

	public void setVelocity(Vector2 delta) {
		targetVelocity.set(delta);
	}

//	public int getIndex() {
//		return index;
//	}
//
//	public void setIndex(int index) {
//		this.index = index;
//	}

	public Vector2 getLocation() {
		return sprite.location;
	}

	public void updateLocation() {
		if (Math.abs(body.getLinearVelocity().x - targetVelocity.x) > 0.001
				|| Math.abs(body.getLinearVelocity().y - targetVelocity.y) > 0.001) {
			body.setLinearVelocity(targetVelocity);
		}
		sprite.setLocation(body.getPosition().x - sprite.getOrigin().x, body.getPosition().y - sprite.getOrigin().y);
	}
	
	public void onMouseMove(int unprojectedX, int unprojectedY){
		Vector2 mouse = new Vector2(unprojectedX, unprojectedY);
		Vector2 playerPos = new Vector2(sprite.location);
		mouse.sub(playerPos);
		sprite.setAngle(mouse.angle() - 90);
	}
	
	public void update(){
		updateLocation();
	}
	
	public String toString() {
		return ("Player at location " + sprite.location);
	}
	/**
	 * @return the sprite
	 */
	public SerializableSprite getSprite() {
		return sprite;
	}

	/**
	 * @param sprite the sprite to set
	 */
	public void setSprite(SerializableSprite sprite) {
		this.sprite = sprite;
	}
	
	class BulletRayCallback implements RayCastCallback {
		public Vector2 start, end, intersect, normal;

		public BulletRayCallback() {
			start = new Vector2(body.getPosition());
			end = new Vector2(
					camera.unproject(new Vector3(Gdx.input.getX() + ((float) (Math.random() * 32) - 16),
							Gdx.input.getY() + ((float) (Math.random() * 32) - 16), 0)).x,
					camera.unproject(new Vector3(Gdx.input.getX() + ((float) (Math.random() * 32) - 16),
							Gdx.input.getY() + ((float) (Math.random() * 32) - 16), 0)).y);
			normal = new Vector2();
			end.sub(start).nor().scl(10000).add(start);
			intersect = new Vector2(end);
		}

		public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			// if (playerBody.getFixtureList().contains(fixture, true)) {
			// return -1;
			// } else {
			System.out.println(fixture.getBody().getUserData());
			if (point.dst(start) < intersect.dst(start)) {
				intersect.set(point);
				this.normal.set(normal).scl(5);
			}
			// intersect.set(point);

			return -1;
			// }
		}
	}
}
