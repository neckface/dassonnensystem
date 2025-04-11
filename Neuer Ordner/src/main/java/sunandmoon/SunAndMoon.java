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
import java.util.LinkedHashMap;

public class SunAndMoon extends SimpleApplication {

    private float angleEarth = 0;
    private float angleMoon = 0;
    private boolean cameraFollowEarth = false;

    private Node earthOrbitNode;
    private Geometry earth, moon;

    private LinkedHashMap<String, Node> planetOrbits = new LinkedHashMap<>();
    private LinkedHashMap<String, Geometry> planets = new LinkedHashMap<>();
    private LinkedHashMap<String, Float> planetDistances = new LinkedHashMap<>();
    private LinkedHashMap<String, Float> planetSpeeds = new LinkedHashMap<>();
    private LinkedHashMap<String, Float> planetAngles = new LinkedHashMap<>();

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
        planetDistances.put("Mercury", 15f + sunSphere.radius + 0.04f );
        planetDistances.put("Venus", 22f + sunSphere.radius + 0.10f);
        planetDistances.put("Earth", 30f + sunSphere.radius + 0.11f);
        planetDistances.put("Mars", 40f + sunSphere.radius + 0.06f);
        planetDistances.put("Jupiter", 70f + sunSphere.radius + 1.20f);
        planetDistances.put("Saturn", 90f + sunSphere.radius + 1.00f);
        planetDistances.put("Uranus", 110f + sunSphere.radius + 0.44f);
        planetDistances.put("Neptune", 130f + sunSphere.radius + 0.42f);


        planetSpeeds.put("Mercury", FastMath.TWO_PI / (88f * SECONDS_PER_SIM_DAY));
        planetSpeeds.put("Venus", FastMath.TWO_PI / (225f * SECONDS_PER_SIM_DAY));
       // planetSpeeds.put("Earth", FastMath.TWO_PI / ((1f / 1440) * SECONDS_PER_SIM_DAY));
        planetSpeeds.put("Earth", FastMath.TWO_PI / (365f * SECONDS_PER_SIM_DAY));
        planetSpeeds.put("Mars", FastMath.TWO_PI / (687f * SECONDS_PER_SIM_DAY));
        planetSpeeds.put("Jupiter", FastMath.TWO_PI / (4333f * SECONDS_PER_SIM_DAY));
        planetSpeeds.put("Saturn", FastMath.TWO_PI / (10759f * SECONDS_PER_SIM_DAY));
        planetSpeeds.put("Uranus", FastMath.TWO_PI / (30687f * SECONDS_PER_SIM_DAY));
        planetSpeeds.put("Neptune", FastMath.TWO_PI / (60190f * SECONDS_PER_SIM_DAY));

        // Додаємо початкові кути обертання для кожної планети
        planetAngles.put("Mercury", 0f);
        planetAngles.put("Venus", FastMath.PI / 4);  // 45 градусів
        planetAngles.put("Earth", FastMath.PI / 2);  // 90 градусів
        planetAngles.put("Mars", 3 * FastMath.PI / 4); // 135 градусів
        planetAngles.put("Jupiter", FastMath.PI);    // 180 градусів
        planetAngles.put("Saturn", 5 * FastMath.PI / 4); // 225 градусів
        planetAngles.put("Uranus", 3 * FastMath.PI / 2); // 270 градусів
        planetAngles.put("Neptune", 7 * FastMath.PI / 4); // 315 градусів

        ColorRGBA[] planetColors = {
            ColorRGBA.Gray,   // Mercury
            ColorRGBA.Orange, // Venus
            ColorRGBA.Blue,   // Earth
            ColorRGBA.Red,    // Mars
            ColorRGBA.Brown,  // Jupiter
            ColorRGBA.Orange, // Saturn
            ColorRGBA.Cyan,   // Uranus
            ColorRGBA.Magenta // Neptune
        };

        int index = 0;
        for (String name : planetDistances.keySet()) {
            Node orbitNode = new Node(name + "Orbit");
            rootNode.attachChild(orbitNode);
            addOrbit(planetDistances.get(name), planetColors[index], rootNode);

            float radius;
            switch (name) {
                case "Mercury": radius = 0.04f; break;
                case "Venus":   radius = 0.10f; break;
                case "Earth":   radius = 0.11f; break;
                case "Mars":    radius = 0.06f; break;
                case "Jupiter": radius = 1.20f; break;
                case "Saturn":  radius = 1.00f; break;
                case "Uranus":  radius = 0.44f; break;
                case "Neptune": radius = 0.42f; break;
                default:        radius = 0.11f; break; // За замовчуванням
            }

            Sphere sphere = new Sphere(32, 32, radius);
            Geometry planet = new Geometry(name, sphere);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", planetColors[index]);
            planet.setMaterial(mat);
            planets.put(name, planet);
            planetOrbits.put(name, orbitNode);
            orbitNode.attachChild(planet);

            index++;
        }

        int keyCode = KeyInput.KEY_1;
        for (String name : planets.keySet()) {
            String actionName = "Follow_" + name;
            inputManager.addMapping(actionName, new KeyTrigger(keyCode));
            inputManager.addListener(actionListener, actionName);
            keyCode++;
        }

        // Erde & Mond separat behandeln
        earthOrbitNode = planetOrbits.get("Earth");
        earth = planets.get("Earth");

        Sphere moonSphere = new Sphere(32, 32, 0.03f);
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
        Torus orbitShape = new Torus(100, 2, 0.02f, radius);
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
            if (isPressed) {
                // Перевіряємо, чи це дія "ToggleCameraFollow"
                if (name.startsWith("Follow_")) {
                    String planetName = name.replace("Follow_", "");
                    Geometry planet = planets.get(planetName);

                    if (planet != null) {
                        cameraFollowEarth = !cameraFollowEarth;
                        if (cameraFollowEarth) {
                            cam.setLocation(new Vector3f(planet.getLocalTranslation().x, 10, planet.getLocalTranslation().z - 10));
                            cam.lookAt(planet.getLocalTranslation(), Vector3f.UNIT_Y);
                            planet.setLocalScale(4f);
                        } else {
                            cam.setLocation(new Vector3f(150, 70, -200));
                            cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
                            planet.setLocalScale(1f);
                        }
                    }
                }
            }
        }
    };


    /*
        TEST

        sekunden = tag 86400
         */


    float zeit;
    float simulationTime = 0f;
    // 86400f = sekunden tag = 24*60*60; 1440f = 24*60; 365 Tage im Jahr
    //float SECONDS_PER_SIM_DAY = (86400f / 1440f) * (1f/365f);
    float SECONDS_PER_SIM_DAY = 60f/365f; //Ein Erdenumlauf in einer Minute
    float ECHTZEIT_UMLAUF = 86400; //Das ergibt das ein Simulationsumlauf genau so lange dauert wie in echt also ein Jahr


    /*
     *
     * */


    public float zeitVerstellen(){




        return 1;
    }



    @Override
    public void simpleUpdate(float tpf) {


        simulationTime += tpf;
        float simulationDays = simulationTime / SECONDS_PER_SIM_DAY;

        for (String name : planets.keySet()) {
            float speed = planetSpeeds.get(name);
            float distance = planetDistances.get(name);

            // Оновлюємо кут планети (окремо для кожної)
            float currentAngle = planetAngles.get(name);
            currentAngle += speed * tpf;
            planetAngles.put(name, currentAngle);

            // Позиція планети по орбіті навколо Сонця
            float x = distance * FastMath.cos(currentAngle);
            float z = distance * FastMath.sin(currentAngle);

            // Оновлюємо позицію планети
            Geometry planet = planets.get(name);
            planet.setLocalTranslation(x, 0, z);
        }

        // Рух Місяця навколо Землі
        angleMoon += FastMath.TWO_PI / 27.32f * tpf;  // 27.32 дні - орбітальний період Місяця
        float moonX = earth.getLocalTranslation().x + 0.8f * FastMath.cos(angleMoon);
        float moonZ = earth.getLocalTranslation().z + 0.8f * FastMath.sin(angleMoon);
        moon.setLocalTranslation(moonX, 0, moonZ);
    }

    @Override
    public void simpleRender(RenderManager rm) {}
}
