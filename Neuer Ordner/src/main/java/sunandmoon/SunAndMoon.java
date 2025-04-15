package sunandmoon;

import com.jme3.asset.AssetNotFoundException;
import com.jme3.font.BitmapText;



import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

import java.util.LinkedHashMap;

public class SunAndMoon extends SimpleApplication {
    private BitmapText dayCounterText;
    boolean isPaused = false;
    // x y 0, x 0 z
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
        sunMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Sun.jpg"));
        sun.setMaterial(sunMat);
        rootNode.attachChild(sun);


        guiNode.detachAllChildren();  // Bestehende GUI-Elemente entfernen, falls notwendig.
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        dayCounterText = new BitmapText(guiFont, false);
        dayCounterText.setSize(guiFont.getCharSet().getRenderedSize());  // Standardgröße
        dayCounterText.setColor(ColorRGBA.White);
        // Positioniere den Text (10, Höhe - 10 vom oberen Rand)
        dayCounterText.setLocalTranslation(10, settings.getHeight() - 10, 0);
        guiNode.attachChild(dayCounterText);

        // Planeten-Informationen (Abstand von der Sonne & Umlaufzeit)
        planetDistances.put("Mercury", 15f + sunSphere.radius + 0.04f);
        planetDistances.put("Venus", 22f + sunSphere.radius + 0.10f);
        planetDistances.put("Earth", 30f + sunSphere.radius + 0.11f);
        planetDistances.put("Mars", 40f + sunSphere.radius + 0.06f);
        planetDistances.put("Jupiter", 70f + sunSphere.radius + 1.20f);
        planetDistances.put("Saturn", 90f + sunSphere.radius + 1.00f);
        planetDistances.put("Uranus", 110f + sunSphere.radius + 0.44f);
        planetDistances.put("Neptune", 130f + sunSphere.radius + 0.42f);


        // intialisiere planeten mit timescale => 1jahr Simu = 1Minute real life
        planetSpeeds.put("Mercury", FastMath.TWO_PI / (88f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Venus", FastMath.TWO_PI / (225f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Earth", FastMath.TWO_PI / (365f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Mars", FastMath.TWO_PI / (687f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Jupiter", FastMath.TWO_PI / (4333f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Saturn", FastMath.TWO_PI / (10759f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Uranus", FastMath.TWO_PI / (30687f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Neptune", FastMath.TWO_PI / (60190f * eineMinuteGleich1JahrSimu));

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
        sun.rotate(90, 250, 0);



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

            try {
                Texture texture = assetManager.loadTexture("Textures/" + name + ".jpg");
                mat.setTexture("ColorMap", texture);
            } catch (AssetNotFoundException e) {
                mat.setColor("Color", planetColors[index]);
            }

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
        moonMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Moon.jpg"));
        moon.setMaterial(moonMat);
        rootNode.attachChild(moon);

        // Kamera-Follow aktivieren
        inputManager.addMapping("ToggleCameraFollow", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addListener(actionListener, "ToggleCameraFollow");

        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(actionListener, "Pause");

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
            if (name.equals("Pause") && isPressed) {
                isPaused = !isPaused;
                System.out.println(isPaused ? "Simulation pausiert" : "Simulation läuft weiter");
            }
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


    //globale variable um die tage zu counten
    long Tag = 0;
    int monat = 0; // Januar = 0
    int jahr = 2025;

    String[] monatNamen = {
            "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"
    };

    int[] tageProMonat = {
            31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };
    // Globale Variable zum Akkumulieren der Simulationszeit (in Sekunden):
    float simTimeAccumulator = 0f;



    // falls ich dummbatz vergesse
    // 86400f = sekunden tag = 24*60*60; 1440f = 24*60; 365 Tage im Jahr
    // float SECONDS_PER_SIM_DAY = 60f/365f; //Ein Erdenumlauf in einer Minute


    // Nutze diese Zeiten um simu zu verschnelleren bzw. zu verlangsamen. Kannst beliebig hinzufügen oder entferen,denke die reichen
    float eineMinuteGleich1JahrSimu = (86400f / 1440f) * (1f/365f); //Ein Erdenumlauf um die Sonne in einer Minute
    float einJahrSimuGelich30Sekunden = 30f/365f; //(86400f / (1440f * 2f)) * (1f/365f);
    float einTagGleich1JahrSimu =  86400f/365f; // 1 echtzeit tag = 1 jahr simu
    float echtZeit =   86400f;//Echtzeitt


    // intialisiere timescale => 1jahr Simu = 1Minute real life || verändere diese Variable um Timescale zu ändern, damit tage richtig gezählt werdne
    float currentTimeScale = eineMinuteGleich1JahrSimu;





    // Kp wie du das brauchst fürs interface bro.
    public void changeTimeScaleSimu( float neuerTimescale) {
                // setze currentTimeScale auf den neuen Wert .z.B. echtZeit, damit tage vernünftig getracked werden
                currentTimeScale = neuerTimescale;

                // konnte nicht testen ob dieser Part richtig funktioniert, viel glück
                planetSpeeds.put("Mercury", FastMath.TWO_PI / (88f * neuerTimescale));
                planetSpeeds.put("Venus", FastMath.TWO_PI / (225f * neuerTimescale));
                planetSpeeds.put("Earth", FastMath.TWO_PI / (365f * neuerTimescale));
                planetSpeeds.put("Mars", FastMath.TWO_PI / (687f * neuerTimescale));
                planetSpeeds.put("Jupiter", FastMath.TWO_PI / (4333f * neuerTimescale));
                planetSpeeds.put("Saturn", FastMath.TWO_PI / (10759f * neuerTimescale));
                planetSpeeds.put("Uranus", FastMath.TWO_PI / (30687f * neuerTimescale));
                planetSpeeds.put("Neptune", FastMath.TWO_PI / (60190f * neuerTimescale));

    }



    @Override
    public void simpleUpdate(float tpf) {
        if (!isPaused) {
// Berücksichtige den Zeitfaktor in der Zeitsimulation:
            // zum tage zählen
        simTimeAccumulator += tpf;

        // Überprüfen, ob ein ganzer Simulations-Tag vergangen ist
        while (simTimeAccumulator >= currentTimeScale) {
            Tag++;
            simTimeAccumulator -= currentTimeScale;
            tageProMonat[1] = (jahr % 4 == 0 && (jahr % 100 != 0 || jahr % 400 == 0)) ? 29 : 28; // Schaltjahr berücksichtigen

            if (Tag > tageProMonat[monat]) {
                Tag = 1;
                monat++;
                if (monat >= 12) {
                    monat = 0;
                    jahr++;
                }
            }

            String datumText = Tag + ". " + monatNamen[monat] + " " + jahr;
            System.out.println("Datum: " + datumText);
            dayCounterText.setText(datumText);
        }
        // tage zählen ende Kalendar

// Aktualisiere den GUI-Text, damit immer der aktuelle Simulations-Tag angezeigt wird


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
    }

    @Override
    public void simpleRender(RenderManager rm) {}
}
