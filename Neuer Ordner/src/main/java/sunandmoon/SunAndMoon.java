package sunandmoon;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.util.HashMap;

public class SunAndMoon extends SimpleApplication {

    private float angleEarth = 0;
    private float angleMoon = 0;
    private boolean cameraFollowEarth = false;

    private Node earthOrbitNode;
    private Geometry earth, moon;

    private HashMap<String, Node> planetOrbits = new HashMap<>();
    private HashMap<String, Geometry> planets = new HashMap<>();
    private HashMap<String, Float> planetDistances = new HashMap<>();
    private HashMap<String, Float> planetSpeeds = new HashMap<>();

    public static void main(String[] args) {
        SunAndMoon app = new SunAndMoon();

        AppSettings settings = new AppSettings(true);
        settings.setResolution(1440, 940);
        settings.setTitle("Sonnensystem-Simulation");
        app.setSettings(settings);

        app.start();
    }

    @Override
    public void simpleInitApp() {
        cam.setLocation(new Vector3f(150, 70, -200));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(30);

        // Lichtquelle hinzufügen
        DirectionalLight sunLight = new DirectionalLight();
        sunLight.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        sunLight.setColor(ColorRGBA.White.mult(1.5f));
        rootNode.addLight(sunLight);

        // Sonne erstellen
        Sphere sunSphere = new Sphere(32, 32, 12f);
        Geometry sun = new Geometry("Sun", sunSphere);
        Material sunMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sunMat.setColor("Color", ColorRGBA.Yellow);
        sun.setMaterial(sunMat);
        rootNode.attachChild(sun);

        // Planeten-Informationen (Abstand von der Sonne & Umlaufzeit)
        planetDistances.put("Mercury", 15f);
        planetDistances.put("Venus", 22f);
        planetDistances.put("Earth", 30f);
        planetDistances.put("Mars", 40f);
        planetDistances.put("Jupiter", 70f);
        planetDistances.put("Saturn", 90f);
        planetDistances.put("Uranus", 110f);
        planetDistances.put("Neptune", 130f);

        planetSpeeds.put("Mercury", FastMath.TWO_PI / 8f);
        planetSpeeds.put("Venus", FastMath.TWO_PI / 20f);
        planetSpeeds.put("Earth", FastMath.TWO_PI / 36.5f);
        planetSpeeds.put("Mars", FastMath.TWO_PI / 68.7f);
        planetSpeeds.put("Jupiter", FastMath.TWO_PI / 433f);
        planetSpeeds.put("Saturn", FastMath.TWO_PI / 1076f);
        planetSpeeds.put("Uranus", FastMath.TWO_PI / 3067f);
        planetSpeeds.put("Neptune", FastMath.TWO_PI / 6019f);

        ColorRGBA[] planetColors = {
            ColorRGBA.Blue, ColorRGBA.Orange, ColorRGBA.Blue,
            ColorRGBA.Red, ColorRGBA.Brown, ColorRGBA.Yellow,
            ColorRGBA.Cyan, ColorRGBA.Magenta
        };

        int index = 0;
        for (String name : planetDistances.keySet()) {
            Node orbitNode = new Node(name + "Orbit");
            rootNode.attachChild(orbitNode);
            addOrbit(planetDistances.get(name), planetColors[index], rootNode);

            Sphere sphere = new Sphere(32, 32, name.equals("Jupiter") ? 3f : name.equals("Saturn") ? 2.5f : 1.5f);
            Geometry planet = new Geometry(name, sphere);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", planetColors[index]);
            planet.setMaterial(mat);
            planets.put(name, planet);
            planetOrbits.put(name, orbitNode);
            orbitNode.attachChild(planet);

            index++;
        }

        // Erde & Mond separat behandeln
        earthOrbitNode = planetOrbits.get("Earth");
        earth = planets.get("Earth");

        Sphere moonSphere = new Sphere(32, 32, 0.8f);
        moon = new Geometry("Moon", moonSphere);
        Material moonMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        moonMat.setColor("Color", ColorRGBA.White);
        moon.setMaterial(moonMat);
        rootNode.attachChild(moon);

        // Kamera-Follow aktivieren
        inputManager.addMapping("ToggleCameraFollow", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addListener(actionListener, "ToggleCameraFollow");
    }

    private void addOrbit(float radius, ColorRGBA color, Node parentNode) {
        Torus orbitShape = new Torus(100, 2, 0.1f, radius);
        Geometry orbit = new Geometry("Orbit", orbitShape);
        Material orbitMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        orbitMat.setColor("Color", color.mult(0.5f));
        orbit.setMaterial(orbitMat);
        orbit.rotate(FastMath.HALF_PI, 0, 0);
        parentNode.attachChild(orbit);
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("ToggleCameraFollow") && isPressed) {
                cameraFollowEarth = !cameraFollowEarth;
                if (cameraFollowEarth) {
                    cam.setLocation(new Vector3f(earth.getLocalTranslation().x, 10, earth.getLocalTranslation().z - 10));
                    cam.lookAt(earth.getLocalTranslation(), Vector3f.UNIT_Y);
                    earth.setLocalScale(4f);
                } else {
                    cam.setLocation(new Vector3f(150, 70, -200));
                    cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
                    earth.setLocalScale(1f);
                }
            }
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        for (String name : planets.keySet()) {
            float angle = planetSpeeds.get(name) * tpf;
            Node orbitNode = planetOrbits.get(name);
            Quaternion rotation = new Quaternion();
            rotation.fromAngleAxis(angle, Vector3f.UNIT_Y);
            orbitNode.setLocalRotation(rotation);

            Geometry planet = planets.get(name);
            float distance = planetDistances.get(name);
            planet.setLocalTranslation(distance * FastMath.cos(angle), 0, distance * FastMath.sin(angle));
        }

        // Mond bewegt sich um die Erde
        float moonX = earth.getLocalTranslation().x + 4f * FastMath.cos(angleMoon);
        float moonZ = earth.getLocalTranslation().z + 4f * FastMath.sin(angleMoon);
        moon.setLocalTranslation(moonX, 0, moonZ);
    }

    @Override
    public void simpleRender(RenderManager rm) {}
}
