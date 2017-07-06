package com.topdown.game;

import java.io.IOException;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntIntMap;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import box2dLight.PointLight;
import box2dLight.RayHandler;

public class TopDownClient extends InputAdapter implements ApplicationListener {
	// Limits the distance the camera can look from the player
	private final float CAM_DISTANCE = 50;
	// The smoothing of the camera movement
	// Higher number means more smooth
	private final float CAM_SMOOTHING = 10;
	// User input things
	private IntIntMap keys = new IntIntMap();
	// Graphics Stuff
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Skin skin;
	private Stage stage;
	private Label labelDetails;
	private Label labelMessage;
	private TextButton button;
	private TextArea textIPAddress;
	// ObjectMap<String, Sprite> sprites;
	// Networking things
	private Kryo kryoClient;
	private Client client;
	// Player data
	private int thisPlayerIndex;
	// Current scene
	private boolean isConnected = false;
	// Player connection stuff
	AddPlayerRequest connectRequest;
	// Map stuff
	private TiledMap background;
	private OrthogonalTiledMapRenderer tileMapRenderer;
	// Box2d stuff
	World world;
	Box2DDebugRenderer debugRenderer;
	RayHandler handler;
	PointLight point;

	SerializableSprite[] sprites;
	private boolean initializeGraphicsNow = false;
	private String map;
	private Vector2 spawnLocation;
	private Vector2 thisPlayerPosition;
	private Vector2 camTarget;

	@Override
	public void create() {
		spawnLocation = new Vector2(20, 20);
		thisPlayerPosition = new Vector2(spawnLocation);
		camTarget = new Vector2();
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		// Sprite array
		sprites = new SerializableSprite[0];
		// Initialize camera and sprite batch
		camera = new OrthographicCamera(150 * width / height, 150);
		// camera = new OrthographicCamera(width, height);
		batch = new SpriteBatch();
		// Initialize skin and stage
		skin = new Skin(Gdx.files.internal("Holo-dark-hdpi.json"));
		stage = new Stage();
		// Initialize GUI
		labelMessage = new Label("Enter IP to Connect", skin);
		button = new TextButton("Connect", skin);
		textIPAddress = new TextArea("", skin);
		textIPAddress.setAlignment(Align.bottom);
		textIPAddress.setText("127.0.0.1");
		stage.setKeyboardFocus(textIPAddress);
		Table table = new Table();
		table.add(labelDetails);
		table.row();
		table.add(labelMessage);
		table.row();
		table.add(button);
		table.row();
		table.add(textIPAddress).width(275);
		table.row();
		table.pack();
		table.setX(width / 2 - table.getWidth() / 2);
		table.setY(height / 2 - table.getHeight() / 2);
		// Add scene to stage
		stage.addActor(table);
		Gdx.input.setInputProcessor(stage);

		// the client that will connect to the server
		client = new Client();
		client.start();
		// Register the classes with the client's kryo serialization
		kryoClient = client.getKryo();
		Registration.registerClasses(kryoClient);

		// Add a listener to the button
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				connectRequest = new AddPlayerRequest("Player_green_pistol.png", spawnLocation);
				try {
					client.connect(5000, textIPAddress.getText(), 54555, 54777);
					client.sendTCP(connectRequest);
				} catch (IOException e) {
					labelMessage.setText("Connection Failed");
				}
			}
		});

		// Add a listener to the client
		client.addListener(new Listener() {
			public void received(Connection connection, Object object) {
				// System.out.println("Some object recieved");
				if (object instanceof PlayerAddedResponse) {
					PlayerAddedResponse response = (PlayerAddedResponse) object;
					// point.attachToBody(playerBody);
					initializeGraphicsNow = true;
					map = response.map;
					thisPlayerIndex = response.index;
					setInputProcessorToThis();
					System.out.println("Added player response recieved");
				} else if (object instanceof SerializableSprite[]) {
					SerializableSprite[] response = (SerializableSprite[]) object;
					// System.out.println("New sprite set recieved");
					sprites = response;
				} else if (object instanceof PlayerLocationResponse) {
					PlayerLocationResponse response = (PlayerLocationResponse) object;
					thisPlayerPosition.set(response.location);
				}
			}
		});
	}

	public void initializeGraphics(String map) {
		// Initialize debug renderer
		debugRenderer = new Box2DDebugRenderer();

		// Initialize world
		world = new World(new Vector2(0, 0), true);
		// Initialize map
		background = new TmxMapLoader().load(map);
		tileMapRenderer = new OrthogonalTiledMapRenderer(background);

		MapLayer layer = background.getLayers().get("CollisionLayer");
		for (MapObject object : layer.getObjects()) {
			if (object instanceof RectangleMapObject) {
				RectangleMapObject rectangleObject = (RectangleMapObject) object;
				Rectangle rect = rectangleObject.getRectangle();
				System.out.println(rect.x + ", " + rect.y + "| width: " + rect.width + " height: " + rect.height);
				Body body;
				BodyDef bodyDef = new BodyDef();
				bodyDef.type = BodyDef.BodyType.StaticBody;
				bodyDef.position.set((rect.getX() + rect.getWidth() / 2), (rect.getY() + rect.getHeight() / 2));

				body = world.createBody(bodyDef);

				PolygonShape shape = new PolygonShape();
				shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);

				FixtureDef fixtureDef = new FixtureDef();
				fixtureDef.shape = shape;
				fixtureDef.density = 0.1f;
				// fixtureDef.filter.categoryBits = (short) 1;
				fixtureDef.filter.groupIndex = (short) 1;
				// fixtureDef.filter.maskBits = (short) 2;

				body.createFixture(fixtureDef);
				shape.dispose();
			}
		}
		// Initialize player's light
		handler = new RayHandler(world);
		handler.setCombinedMatrix(camera);

		point = new PointLight(handler, 1000, Color.WHITE, 1000, 20, 20);
		point.setSoft(true);
		point.setSoftnessLength(20);
		point.setColor(0f, 0f, 0f, 1f);
		initializeGraphicsNow = false;
		isConnected = true;
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		// Step the physics simulation forward at a rate of 60hz
		// world.step(1 / 60f, 6, 2);

		batch.setProjectionMatrix(camera.combined);

		if (!isConnected) {
			stage.draw();
			if (initializeGraphicsNow) {
				initializeGraphics(map);
			}
		} else if (isConnected) {
			// camera.position.set(sprites[thisPlayerIndex].origin.x,
			// sprites[thisPlayerIndex].origin.y, 0);
			updateCamLocation();
			camera.update();

			tileMapRenderer.setView(camera);
			tileMapRenderer.render();
			for (SerializableSprite s : sprites) {
				Sprite sprite = new Sprite(new Texture("Player_green_pistol.png"));
				sprite.setOrigin(s.origin.x, s.origin.y);
				sprite.setPosition(s.location.x, s.location.y);
				// sprite.scale(s.scalar);
				sprite.setRotation(s.angle);
				batch.begin();
				sprite.draw(batch);
				batch.end();
			}
			point.setPosition(thisPlayerPosition.x, thisPlayerPosition.y);
			handler.setCombinedMatrix(camera);
			handler.updateAndRender();
			// Send a request for the rotation if the player is moving
			if (keys.size != 0) {
				sendRotationRequest();
			}

		}

		// System.out.println(Arrays.toString(sprites));
	}

	private void updateCamLocation() {
		// Determine target location
		Vector2 playerLoc = new Vector2(thisPlayerPosition);
		Vector2 camLoc = new Vector2(camera.position.x, camera.position.y);
		Vector2 mouseLoc = new Vector2(getMousePosInGameWorld().x, getMousePosInGameWorld().y);
		mouseLoc.sub(playerLoc);
		if (mouseLoc.len() / 2 > CAM_DISTANCE) {
			mouseLoc.nor().scl(CAM_DISTANCE);
		} else {
			float len = mouseLoc.len();
			mouseLoc.nor().scl(len / 2);
		}
		mouseLoc.add(playerLoc);
		camTarget.set(mouseLoc);

		// Move camera towards target
		Vector2 newLoc = new Vector2(camTarget);
		newLoc.sub(camLoc);
		float len = newLoc.len();
		newLoc.nor().scl(len / CAM_SMOOTHING);
		camera.position.x += newLoc.x;
		camera.position.y += newLoc.y;
	}

	private void processInput() {
		if (isConnected) {
			float xVelocity = exclusiveOr3Result(keys.containsKey(Keys.D), keys.containsKey(Keys.A)) * 200;
			float yVelocity = exclusiveOr3Result(keys.containsKey(Keys.W), keys.containsKey(Keys.S)) * 200;
			Vector2 velocity = new Vector2(xVelocity, yVelocity);
			System.out.println(velocity);
			client.sendTCP(new PlayerMoveRequest(thisPlayerIndex, velocity));
		}
	}

	/**
	 * @return 0 if a and b are both true or are both false, 1 if a is true and
	 *         b is false, -1 if a is false and b is true
	 * @param a
	 * @param b
	 */
	private int exclusiveOr3Result(boolean a, boolean b) {
		if (a && !b) {
			return 1;
		} else if (!a && b) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public void dispose() {
		batch.dispose();
		client.close();
		skin.dispose();
		stage.dispose();
	}

	public void setInputProcessorToThis() {
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean keyDown(int keycode) {
		keys.put(keycode, keycode);
		processInput();
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		keys.remove(keycode, 0);
		processInput();
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		sendRotationRequest();
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		sendRotationRequest();
		// System.out.println(mouse);
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	private Vector3 getMousePosInGameWorld() {
		return camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
	}

	private void sendRotationRequest() {
		Vector2 mouse = new Vector2(getMousePosInGameWorld().x, getMousePosInGameWorld().y);
		Vector2 playerPos = new Vector2(thisPlayerPosition.x + 8, thisPlayerPosition.y + 7);// new
																							// Vector2(thisPlayerPosition.x
																							// +
																							// 8,
																							// thisPlayerPosition.y
																							// +
																							// 7);
		mouse.sub(playerPos);
		client.sendTCP(new RotatePlayerRequest(thisPlayerIndex, mouse.angle()));
	}
}
