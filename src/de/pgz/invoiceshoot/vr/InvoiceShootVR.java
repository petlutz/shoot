package de.pgz.invoiceshoot.vr;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import com.jme3.app.SimpleApplication;
import com.jme3.app.VRAppState;
import com.jme3.app.VRConstants;
import com.jme3.app.VREnvironment;
import com.jme3.app.state.AppState;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.audio.Environment;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.CartoonSSAO;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.util.SkyFactory;
import com.jme3.util.VRGUIPositioningMode;

public class InvoiceShootVR extends SimpleApplication implements GameContext {

	private static final Logger logger = Logger.getLogger(InvoiceShootVR.class.getName());

	private Spatial observer;
	private boolean moveForward, moveBackwards, rotateLeft, rotateRight;
	private Material matFloor;
	private Node mainScene;
	
	private Random random = new Random();
	
	private Gun gun;

	private static final String MAT_LIGHTING = "Common/MatDefs/Light/Lighting.j3md";

	private VRAppState vrAppState = null;
	
	private BulletAppState bulletAppState = null;
	
	private long lastDropTime = 0;
	
	private long lastTextUpdateTime = 0;

	private final static long DROP_IDLE = 100;
	
	private final static long TEXTUPDATE_IDLE = 100;

	private final static float GRAVITY = -0.3f;

	private List<Target> targets = new ArrayList<>() ;

	private List<Updateable> updateables =  new ArrayList<>() ;
	
	private BitmapText infoText;
	
	private long score;
	
	private long startTime;

	public InvoiceShootVR(AppState... initialStates) {
		super(initialStates);
		vrAppState = getStateManager().getState(VRAppState.class);
	}

	@Override
	public void simpleInitApp() {
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);

		bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0.0f, GRAVITY, 0.0f));

		logger.info("Updating asset manager with " + System.getProperty("user.dir"));
		getAssetManager().registerLocator(System.getProperty("user.dir") + File.separator + "assets",
				FileLocator.class);

		mainScene = new Node("scene");
		observer = new Node("observer");

		getAudioRenderer().setEnvironment(new Environment(Environment.Garage
				));
		
		Spatial sky = SkyFactory.createSky(getAssetManager(), "Textures/Sky/sky.jpg",
				SkyFactory.EnvMapType.EquirectMap);
		rootNode.attachChild(sky);
		matFloor = new Material(getAssetManager(), MAT_LIGHTING);
		Texture texTeppich = getAssetManager().loadTexture("Textures/marbel.jpg");
		texTeppich.setMagFilter(MagFilter.Nearest);
		texTeppich.setMinFilter(MinFilter.Trilinear);
		texTeppich.setAnisotropicFilter(16);
		texTeppich.setWrap(Texture.WrapMode.Repeat);
		matFloor.setTexture("DiffuseMap", texTeppich);

		// make the floor according to the size of our play area
		Box boxFloor = new Box(5f, 0.5f, 5f);
		boxFloor.scaleTextureCoordinates(new Vector2f(10f, 10f));
		Geometry floor = new Geometry("floor", boxFloor);
		floor.move(0f, Constants.FLOOR_Y, 0f);

		floor.setMaterial(matFloor);
		floor.setShadowMode(RenderQueue.ShadowMode.Receive);

		rootNode.attachChild(floor);
		
		
		RigidBodyControl floorPhy = new RigidBodyControl(0.0f);
		floor.addControl(floorPhy);
		bulletAppState.getPhysicsSpace().add(floorPhy);

		rootNode.setShadowMode(RenderQueue.ShadowMode.Off);

		gun = new Gun(this);
		updateables.add(gun);

		// test any positioning mode here (defaults to AUTO_CAM_ALL)
		vrAppState.getVRGUIManager().setPositioningMode(VRGUIPositioningMode.AUTO_OBSERVER_ALL);
		vrAppState.getVRGUIManager().setGuiScale(0.4f);

		observer.setLocalTranslation(new Vector3f(0.0f, 0.0f, 0.0f));

		vrAppState.setObserver(observer);
		mainScene.attachChild(observer);
		rootNode.attachChild(mainScene);

		initInputs();

		// use magic VR mouse cusor (same usage as non-VR mouse cursor)
		getInputManager().setCursorVisible(true);

		DirectionalLight sun = new DirectionalLight(); sun.setDirection((new
		Vector3f(0.0f, -20.0f, 20.0f)).normalizeLocal());
		sun.setColor(new ColorRGBA(1.0f, 232f / 255f, 151f / 255f, 1f));
		rootNode.addLight(sun);
		
		
		AmbientLight ambient = new AmbientLight(new ColorRGBA(0.3f, 0.3f, 0.3f, 1f));
		rootNode.addLight(ambient);
	
		createInfoBox();
		
		startTime = new Date().getTime();
		
		AudioNode backgroundAudio = new AudioNode(assetManager, "Sound/nature.wav", DataType.Stream);
		backgroundAudio.setLooping(true);  // activate continuous playing
		backgroundAudio.setPositional(false);
		backgroundAudio.setVolume(0.3f);
		backgroundAudio.setDirectional(false);
		backgroundAudio.play();
	    rootNode.attachChild(backgroundAudio);
	}

	private void initInputs() {
		
		InputManager inputManager = getInputManager();
		inputManager.addMapping("toggle", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addMapping("incShift", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("decShift", new KeyTrigger(KeyInput.KEY_E));
		inputManager.addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("back", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("left", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("right", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("filter", new KeyTrigger(KeyInput.KEY_F));
		inputManager.addMapping("dumpImages", new KeyTrigger(KeyInput.KEY_I));
		inputManager.addMapping("exit", new KeyTrigger(KeyInput.KEY_ESCAPE));

		ActionListener acl = new ActionListener() {

			public void onAction(String name, boolean keyPressed, float tpf) {
				if (name.equals("incShift") && keyPressed) {
					vrAppState.getVRGUIManager().adjustGuiDistance(-0.1f);
				} else if (name.equals("decShift") && keyPressed) {
					vrAppState.getVRGUIManager().adjustGuiDistance(0.1f);
				} else if (name.equals("filter") && keyPressed) {
					// adding filters in realtime
					CartoonSSAO cartfilt = new CartoonSSAO(vrAppState.isInstanceRendering());
					FilterPostProcessor fpp = new FilterPostProcessor(getAssetManager());
					fpp.addFilter(cartfilt);
					getViewPort().addProcessor(fpp);
					// filters added to main viewport during runtime,
					// move them into VR processing
					// (won't do anything if not in VR mode)
					vrAppState.moveScreenProcessingToVR();
				}
				if (name.equals("toggle")) {
					vrAppState.getVRGUIManager().positionGui();
				}
				if (name.equals("forward")) {
					if (keyPressed) {
						moveForward = true;
					} else {
						moveForward = false;
					}
				} else if (name.equals("back")) {
					if (keyPressed) {
						moveBackwards = true;
					} else {
						moveBackwards = false;
					}
				} 
//				else if (name.equals("dumpImages")) {
//					((OpenVR) vrAppState.getVRHardware()).getCompositor().CompositorDumpImages.apply();
//				} 
				else if (name.equals("left")) {
					if (keyPressed) {
						rotateLeft = true;
					} else {
						rotateLeft = false;
					}
				} else if (name.equals("right")) {
					if (keyPressed) {
						rotateRight = true;
					} else {
						rotateRight = false;
					}
				} else if (name.equals("exit")) {
					stop(true);
					System.exit(0);
				}

			}
		};
		inputManager.addListener(acl, "forward");
		inputManager.addListener(acl, "back");
		inputManager.addListener(acl, "left");
		inputManager.addListener(acl, "right");
		inputManager.addListener(acl, "toggle");
		inputManager.addListener(acl, "incShift");
		inputManager.addListener(acl, "decShift");
		inputManager.addListener(acl, "filter");
		inputManager.addListener(acl, "dumpImages");
		inputManager.addListener(acl, "exit");
	}

	@Override
	public void simpleUpdate(float tpf) {

		// new Date().getTime();
		// FPS test
		/*
		 * tpfAdder += tpf; tpfCount++; if( tpfCount == 60 ) {
		 * System.out.println("FPS: " + Float.toString(1f / (tpfAdder / tpfCount)));
		 * tpfCount = 0; tpfAdder = 0f; }
		 */
		//
		// prod+=tpf;
		// distance = 100f * FastMath.sin(prod);
		// boxes.setLocalTranslation(0, 0, 200f+ distance);

		if (moveForward) {
			observer.move(vrAppState.getFinalObserverRotation().getRotationColumn(2).mult(tpf * 8f));
		}
		if (moveBackwards) {
			observer.move(vrAppState.getFinalObserverRotation().getRotationColumn(2).mult(-tpf * 8f));
		}
		if (rotateLeft) {
			observer.rotate(0, 0.75f * tpf, 0);
		}
		if (rotateRight) {
			observer.rotate(0, -0.75f * tpf, 0);
		}

		long time = new Date().getTime();
		if (time - lastDropTime > DROP_IDLE) {
			lastDropTime = time;
			updateTargetList();
			dropInvoice();
			if (random.nextInt(20) == 1) 
				dropBomb();
		}

		if (time - lastTextUpdateTime > TEXTUPDATE_IDLE) {
			lastTextUpdateTime = time;
			updateInfoText();
		}
		
		// Audio-"Hoerer" auf Kameraposition setzen
		getListener().setLocation(getCamera().getLocation());
		getListener().setRotation(getCamera().getRotation());
		
		updateUpdateables();

	}
	
	private void updateUpdateables() {
		new ArrayList<>(updateables).forEach(u -> u.update());
		
		Iterator<Updateable> itrUpdateables = updateables.iterator();
		while (itrUpdateables.hasNext()) {
			if (itrUpdateables.next().isDead())
				itrUpdateables.remove();
		}
	}

	private void updateTargetList() {
		Iterator<Target> itr = targets.iterator();
		while (itr.hasNext()) {
			Target target = itr.next();
			if (target.isDead())
				itr.remove();
		}
	}

	private void dropInvoice() {
		Invoice invoice = new Invoice(this);
		targets.add(invoice);
		updateables.add(invoice);
	}
	
	private void dropBomb() {
		Bomb bomb = new Bomb(this);
		targets.add(bomb);
		updateables.add(bomb);
	}

	@Override
	public VRAppState getVRAppState() {
		return vrAppState;
	}

	@Override
	public BulletAppState getBulletAppState() {
		return bulletAppState;
	}

	@Override
	public SimpleApplication getApp() {
		return this;
	}

	@Override
	public List<Target> targets() {
		return targets;
	}

	@Override
	public List<Updateable> updateables() {
		return updateables;
	}

	@Override
	public void addScore(long score) {
		this.score += score;
	}

	public static void main(String[] args) {

		// Prepare settings for VR rendering.
		// It is recommended to share same settings between the VR app state and the
		// application.
		AppSettings settings = new AppSettings(true);

		settings.put(VRConstants.SETTING_VRAPI, Constants.VRAPI); // The VR api to use (need to
																							// be present on the system)
//		settings.put(VRConstants.SETTING_DISABLE_VR, false); // Enable VR
//		settings.put(VRConstants.SETTING_ENABLE_MIRROR_WINDOW, false); // Enable Mirror rendering oh the screen (disable
																		// to be faster)
//		settings.put(VRConstants.SETTING_VR_FORCE, false); // Not forcing VR rendering if no VR system is found.
//		settings.put(VRConstants.SETTING_GUI_CURVED_SURFACE, true); // Curve the mesh that is displaying the GUI
//		settings.put(VRConstants.SETTING_FLIP_EYES, false); // Is the HMD eyes have to be inverted.
//		settings.put(VRConstants.SETTING_NO_GUI, false); // enable gui.
//		settings.put(VRConstants.SETTING_GUI_OVERDRAW, false); // show gui even if it is behind things.

//		settings.put(VRConstants.SETTING_DEFAULT_FOV,108f); // The default ield Of View (FOV)
//		settings.put(VRConstants.SETTING_DEFAULT_ASPECT_RATIO, 1f); // The default aspect ratio.
		
		settings.put(VRConstants.SETTING_SEATED_EXPERIENCE, false);
//		
//		settings.setRenderer(AppSettings.LWJGL_OPENGL3); // Setting the renderer. OpenGL 3 is needed if you're using
															// Instance Rendering.
	
//		settings.setResolution(800, 600);
//		settings.setFullscreen(false);
	
		// The VR Environment.
		// This object is the interface between the JMonkey world (Application,
		// AppState, ...) and the VR specific stuff.
		VREnvironment environment = new VREnvironment(settings);
		environment.initialize();

		// Checking if the VR environment is well initialized
		// (access to the underlying VR system is effective, VR devices are detected).
		if (environment.isInitialized()) {

			// Initialise VR AppState with the VR environment.
			VRAppState vrAppState = new VRAppState(settings, environment);

			// Create the sample application with the VRAppState attached.
			// There is no constraint on the Application type.
			SimpleApplication app = new InvoiceShootVR(vrAppState);
			
//			test.setShowSettings(false); // <- funktioniert nicht bei Oculus, da Auflösung zu hoch

			// Starting the application.
			app.start();
			
		} else {
			logger.severe("Cannot start VR sample application as VR system is not initialized (see log for details)");
		}
	}

	private void createInfoBox() {
		
			BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
			
			infoText = new BitmapText(guiFont, false);
			infoText.setSize(guiFont.getCharSet().getRenderedSize());
		
			updateInfoText(); 
			
			infoText.setColor(new ColorRGBA(1f,0.8f,0.3f,0.8f));
			
			
			infoText.scale(0.02f);
			infoText.rotate(0.0f, 3.14f * 0.65f, 0.0f);
			infoText.move(-3f, 2.0f, 4f);
			
			rootNode.attachChild(infoText);
				
			Box a = new Box(new Vector3f( -0.1f, 0.1f, -0.1f ), new Vector3f( 3.1f, -1.7f, -0.2f ));
			
			Geometry geom = new Geometry("infobox", a);
		
			Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			
			mat.setColor("Color", ColorRGBA.Black);
			
			geom.setMaterial(mat);
			
			geom.rotate(0.0f, 3.14f * 0.65f, 0.0f);
			geom.move(-3.1f, 2.0f, 4.1f);
			
			rootNode.attachChild(geom);
			
	}

	private void updateInfoText() {
		
		long millis =  new Date().getTime() - startTime;
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long secondsOfMinute = seconds - (minutes * 60);
		
		String str = String.format("Invoice Shootout 1.0\ntime: %d:%02d\namount: %d,00$\n%s", minutes, secondsOfMinute, score, updateables.size() + "/" + targets.size());
	
		infoText.setText(str);
	}

}
