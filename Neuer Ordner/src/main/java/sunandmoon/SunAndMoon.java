package sunandmoon;

import com.jme3.asset.AssetNotFoundException;
import com.jme3.font.BitmapText;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.*;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;

import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.simsilica.lemur.*;
import com.simsilica.lemur.style.BaseStyles;

import java.util.LinkedHashMap;


public class SunAndMoon extends SimpleApplication {

    private BitmapText dayCounterText;
    boolean isPaused = false;
    private boolean showTrails = false;
    // x y 0, x 0 z
    private float angleMoon = 0;
    private Geometry followPlanet = null;
    private Node earthOrbitNode;
    private Geometry earth, moon;
    private Label infoLabel;
    private LinkedHashMap<String, Node> planetOrbits = new LinkedHashMap<>();
    private LinkedHashMap<String, Geometry> planets = new LinkedHashMap<>();
    private LinkedHashMap<String, Float> planetDistances = new LinkedHashMap<>();
    private LinkedHashMap<String, Float> planetSpeeds = new LinkedHashMap<>();
    private LinkedHashMap<String, Float> planetAngles = new LinkedHashMap<>();
    private LinkedHashMap<String, Trail> planetTrails = new LinkedHashMap<>();
    private LinkedHashMap<String, String> planetInfo = new LinkedHashMap<>();



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

        //Stars hintegrund
        Sphere skySphere = new Sphere(30, 30, 500f, true, false);
        Geometry skyGeo = new Geometry("SkySphere", skySphere);
        Material skyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        skyMat.setTexture("ColorMap", assetManager.loadTexture("Textures/stars.png"));
        skyMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Front);
        skyGeo.setMaterial(skyMat);
        skyGeo.rotate(90f, 0f, 0f);
        rootNode.attachChild(skyGeo);

        //buttons Tastatur
        inputManager.addMapping("Speed_Normal", new KeyTrigger(KeyInput.KEY_F1));
        inputManager.addMapping("Speed_Slow", new KeyTrigger(KeyInput.KEY_F2));
        inputManager.addMapping("Speed_Pause", new KeyTrigger(KeyInput.KEY_F3));
        inputManager.addMapping("No_Clip", new KeyTrigger(KeyInput.KEY_V));
        inputManager.addListener(actionListener, "Speed_Normal", "Speed_Slow", "Speed_Pause", "No_Clip");

        cam.setLocation(new Vector3f(150, 70, -200));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(0);
        flyCam.setRotationSpeed(0f);


        // Lichtquelle hinzufügen
        DirectionalLight sunLight = new DirectionalLight();
        sunLight.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        sunLight.setColor(ColorRGBA.White.mult(1.5f));
        rootNode.addLight(sunLight);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.2f));

        rootNode.addLight(ambient);

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


        planetDistances.put("Mercury", 15f + sunSphere.radius + 0.04f);
        planetDistances.put("Venus", 22f + sunSphere.radius + 0.10f);
        planetDistances.put("Earth", 30f + sunSphere.radius + 0.11f);
        planetDistances.put("Mars", 40f + sunSphere.radius + 0.06f);
        planetDistances.put("Jupiter", 70f + sunSphere.radius + 1.20f);
        planetDistances.put("Saturn", 90f + sunSphere.radius + 1.00f);
        planetDistances.put("Uranus", 110f + sunSphere.radius + 0.44f);
        planetDistances.put("Neptune", 130f + sunSphere.radius + 0.42f);



        planetSpeeds.put("Mercury", FastMath.TWO_PI / (88f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Venus", FastMath.TWO_PI / (225f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Earth", FastMath.TWO_PI / (365f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Mars", FastMath.TWO_PI / (687f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Jupiter", FastMath.TWO_PI / (4333f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Saturn", FastMath.TWO_PI / (10759f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Uranus", FastMath.TWO_PI / (30687f * eineMinuteGleich1JahrSimu));
        planetSpeeds.put("Neptune", FastMath.TWO_PI / (60190f * eineMinuteGleich1JahrSimu));


        planetAngles.put("Mercury", 0f);
        planetAngles.put("Venus", FastMath.PI / 4);
        planetAngles.put("Earth", FastMath.PI / 2);
        planetAngles.put("Mars", 3 * FastMath.PI / 4);
        planetAngles.put("Jupiter", FastMath.PI);
        planetAngles.put("Saturn", 5 * FastMath.PI / 4);
        planetAngles.put("Uranus", 3 * FastMath.PI / 2);
        planetAngles.put("Neptune", 7 * FastMath.PI / 4);

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
        planetInfo.put("Mercury", "Merkur\nDer kleinste Planet und der sonnennächste Bewohner unseres Systems rast in gerade einmal 88 Erdtagen um die Sonne! Ohne nennenswerte Atmosphäre schwankt seine Oberflächentemperatur extrem: tagsüber können es fast 430 °C sein, nachts sinkt sie auf −180 °C. Die zerklüftete Oberfläche ist übersät mit Einschlagskratern und tiefen Klüften – Zeugen eines turbulenten Jugend-Acts aus Asteroiden- und Kometenbombardements.");
        planetInfo.put("Venus", "Venus\nSchwesterplanet der Erde – aber unter einer dichten, ätzenden Wolkendecke herrscht ein Höllenklima: Oberflächendrücke zwanzig Mal so hoch wie auf der Erde und Temperaturen um 465 °C. Vulkanismus könnte die glühende Hülle regelmäßig neu speisen. In den Wolken verbergen sich außerdem noch rätselhafte Schwefelsäuretröpfchen und womöglich Spuren vergangener mikrobieller Lebensformen.");
        planetInfo.put("Earth", "Erde\nEin blauer Juwel im All: 70 % ihrer Oberfläche sind von Wasser bedeckt, einzig bekanntes Lebensträger im Universum. Dank des Magnetfelds und der schützenden Atmosphäre bewahrt sie uns vor schädlicher Strahlung. Kontinente wandern, Berge wachsen und Ozeane verändern ihr Gesicht – unser Heimatplanet ist ein dynamisches Gleichgewicht.");
        planetInfo.put("Mars", "Mars\nDer Rote Planet fasziniert mit rostfarbenem Staub und gigantischen Vulkanen wie dem Olympus Mons (dreimal so hoch wie der Mount Everest!). Ehemals wasserreiche Täler und ausgetrocknete Flussbetten deuten auf flüssiges Wasser in der Vergangenheit hin – heute lauert es verborgen als Eispol und Salzwasser-Lösungen in Untergrundreservoirs. Vielleicht ist er unser nächstes Ziel für bewohnbare Außenposten?\n" +
                "\n");
        planetInfo.put("Jupiter", "Jupiter\nDer König der Planeten: mit über 300 Erdmassen beherrscht er die äußeren Regionen. Sein wildes Wolkensystem formt den berühmten Großen Roten Fleck – ein Sturmtief seit Jahrhunderten. Aus Gestein, Eis und Gas bestehend, erzeugt Jupiter ein starkes Magnetfeld, das Asteroiden ablenkt und so als Schutzschild für die inneren Planeten dient.");
        planetInfo.put("Saturn", "Saturn\nUnübersehbar sind seine atemberaubenden Ringe – Milliarden von Partikeln, von winzigen Staubkörnern bis zu Felsbrocken, eingekeilt in Bahnen aus Eis und Gestein. Darunter verbirgt sich ein Gasriese, dessen Windgeschwindigkeiten über 1.800 km/h erreichen können. Saturns größte Monde, besonders Titan mit seinem dichten Stickstoff-Meer, bieten spannende Einblicke in fremde Welten.");
        planetInfo.put("Uranus", "Uranus\nDer „seitliche“ Planet rollt auf seiner Seite um die Sonne – seine Rotationsachse liegt fast in seiner Umlaufebene. Dieses ungewöhnliche Gefälle führt zu extremen Jahreszeiten: 21 Jahre lang strahlt ein Pol im steten Sonnenlicht, während der andere in 21 Jahre Nacht gehüllt ist. Das kalte Gasgemisch aus Wasser, Ammoniak und Methan verleiht ihm seine markant blassblaue Farbe.");
        planetInfo.put("Neptune", "Neptun\nDer fernste Gasriese imponiert mit den kräftigsten Winden im Sonnensystem: bis zu 2.100 km/h fegen Stürme über seine eisigen Wolken. Seine tiefblaue Farbe entsteht durch Methan in der Atmosphäre. Neptun birgt außerdem eine geheime Wärmequelle, die ihn von innen heraus aufheizt – ein faszinierendes Rätsel für Planetologen.");



        int index = 0;
        for (String name : planetDistances.keySet()) {
            Node orbitNode = new Node(name + "Orbit");
            rootNode.attachChild(orbitNode);
            addOrbit(planetDistances.get(name), planetColors[index], rootNode);

            float radius;
            float multipl = 3;
            switch (name) {
                case "Mercury": radius = 0.04f * multipl; break;
                case "Venus":   radius = 0.10f * multipl; break;
                case "Earth":   radius = 0.11f * multipl; break;
                case "Mars":    radius = 0.06f* multipl; break;
                case "Jupiter": radius = 1.20f* multipl; break;
                case "Saturn":  radius = 1.00f* multipl; break;
                case "Uranus":  radius = 0.44f* multipl; break;
                case "Neptune": radius = 0.42f* multipl; break;
                default:        radius = 0.11f* multipl; break;
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
            Trail trail = new Trail(rootNode, assetManager, planetColors[index], 0.01f, 10000, (5.5f /currentTimeScale));// (10*currentTimeScale));
            // idee mit Trail
            planetTrails.put(name, trail);
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



        // INTERFACE INTERFACE INTERFACE INTERFACE INTERFACE INTERFACE INTERFACE INTERFACE
        // da initialisieren wir Lemur
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();      // default glass thema
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        Container hudTrials = new Container(); // containers für Buttons
        hudTrials.setLocalTranslation(10, cam.getHeight() - 500, 0); // position des Hud
        guiNode.attachChild(hudTrials);


        Button btnTrails = hudTrials.addChild(new Button("Trails On/Off"), 1, 0);
        btnTrails.addClickCommands((Button src) -> {
            showTrails = !showTrails;
            src.setText("Trails: " + (showTrails ? "On" : "Off"));

            // Устанавливаем активность всех трейлов
            for (Trail trail : planetTrails.values()) {
                trail.setActive(showTrails);
            }
        });






        // container fuer buttons (HUD)
        Container hud = new Container(); // containers für Buttons
        hud.setLocalTranslation(1200, cam.getHeight() - 10, 0); // position des Hud
        guiNode.attachChild(hud);

        //Normal button
        Button btnNormal = hud.addChild(new Button("Normal"), 0, 0);
        btnNormal.addClickCommands((Button source) -> {
            changeTimeScaleSimu(1);
        });

        // Schnell button
        Button btnSlow = hud.addChild(new Button("Schnell"), 1, 0);
        btnSlow.addClickCommands((Button source) -> {
            changeTimeScaleSimu(15);
        });

        // Pause/Resume
        Button btnPause = hud.addChild(new Button("Pause"), 2, 0);
        btnPause.addClickCommands((Button source) -> {
            togglePause();
            source.setText(isPaused ? "Resume" : "Pause"); // da ändern
        });

        btnNormal.setPreferredSize(new Vector3f(200, 50, 0));
        btnNormal.setInsets(new Insets3f(5,5,5,5)); // (top,left,bottom,right)

        btnSlow.setPreferredSize(new Vector3f(200, 50, 0));
        btnSlow.setInsets(new Insets3f(5,5,5,5));

        btnPause.setPreferredSize(new Vector3f(200, 50, 0));
        btnPause.setInsets(new Insets3f(5,5,5,5));
        GuiGlobals.getInstance().getStyles()
                .getSelector("button").set("fontSize", 18);


        Container camHud = new Container();
        float margin = 10;
        float screenW = cam.getWidth();
        float screenH = cam.getHeight();
        camHud.setLocalTranslation(
                screenW - margin - 220,   // height
                screenH - margin - 720,   // width
                0
        );
        guiNode.attachChild(camHud);

        // 2 zeilen 4 buttons
        int col = 0, row = 0;
        for (String name : planets.keySet()) {
            Button b = camHud.addChild(new Button(name), col, row);
            b.setPreferredSize(new Vector3f(100, 40, 0));
            b.setInsets(new Insets3f(3f, 3f, 3f, 3f));
            Container hudText = new Container(); // containers für Buttons
            hudText.setLocalTranslation(1200, cam.getHeight() - 10, 0); // position des Hud
            guiNode.attachChild(hudText);
            final String planetName = name;
            b.addClickCommands((Button src) -> {
                for (Trail trail : planetTrails.values()) {
                    trail.setActive(showTrails);
                    if (!showTrails) {
                        trail.clear();    // alle vorhandenen Punkte entfernen
                    }
                }

                    // follow-Toggle durchführen
                    actionListener.onAction("Follow_" + planetName, true, 0);

                    // je nach dem, ob wir jetzt folgen, Text setzen oder löschen
                    if (followPlanet == planets.get(planetName)) {
                        infoLabel.setText(planetInfo.get(planetName));
                    } else {
                        infoLabel.setText("");
                    }


            });
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
            cam.setLocation(new Vector3f(150, 70, -200));
            cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        }
        infoLabel = new Label("");
        infoLabel.setFontSize(20);
        infoLabel.setPreferredSize(new Vector3f(300, 200, 0));
        infoLabel.setLocalTranslation(settings.getWidth() - 350, settings.getHeight() - 250, 0);
        guiNode.attachChild(infoLabel);
    }


    private Geometry addOrbit(float radius, ColorRGBA color, Node parentNode) {
        Torus orbitShape = new Torus(100, 2, 0.02f, radius);
        Geometry orbit = new Geometry("Orbit", orbitShape);
        Material orbitMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        orbitMat.setColor("Color", color.mult(0.5f));
        orbit.setMaterial(orbitMat);
        orbit.rotate(FastMath.HALF_PI, 0, 0);
        parentNode.attachChild(orbit);
        return orbit;
    }

    private final ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean isPressed, float tpf) {

            if (isPressed){
            if (name.equals("No_Clip")) {
                flyCam.setMoveSpeed(100);
                flyCam.setRotationSpeed(3f);
            }
            }
            if (isPressed) {
                if (name.equals("Speed_Normal")) {
                    changeTimeScaleSimu(1);
                } else if (name.equals("Speed_Slow")) {
                    changeTimeScaleSimu(15);
                }
            }
            if (name.equals("Pause") && isPressed) {
                isPaused = !isPaused;
            }
            if (isPressed) {
                if (name.startsWith("Follow_")) {
                    String planetName = name.replace("Follow_", "");
                    Geometry planet = planets.get(planetName);
                    if (planet != null) {

                        if (followPlanet == planet) {
                            followPlanet = null;

                            cam.setLocation(new Vector3f(150, 70, -200));
                            cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
                        } else {
                            followPlanet = planet;
                        }
                    }
                }

            }

        }

    };
    public void togglePause() {
        isPaused = !isPaused;
    }


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
    final float eineMinuteGleich1JahrSimu = (86400f / 1440f) * (1f/365f); //Ein Erdenumlauf um die Sonne in einer Minute
    final float einJahrSimuGelich30Sekunden = 30f/365f; //(86400f / (1440f * 2f)) * (1f/365f);
    final float einTagGleich1JahrSimu =  86400f/365f; // 1 echtzeit tag = 1 jahr simu
    final float echtZeit =   86400f;//Echtzeitt


    // intialisiere timescale => 1jahr Simu = 1Minute real life || verändere diese Variable um Timescale zu ändern, damit tage richtig gezählt werdne
    float currentTimeScale = eineMinuteGleich1JahrSimu;





    // Kp wie du das brauchst fürs interface bro.
    // keine Sorge bro ich weiss was ich tue
    public void changeTimeScaleSimu(float multiplier) {
        float baseTimeScale = eineMinuteGleich1JahrSimu;
                // setze currentTimeScale auf den neuen Wert .z.B. echtZeit, damit tage vernünftig getracked werden

                currentTimeScale = baseTimeScale / multiplier;

                // konnte nicht testen ob dieser Part richtig funktioniert, viel glück
                planetSpeeds.put("Mercury", FastMath.TWO_PI / (88f * currentTimeScale));
                planetSpeeds.put("Venus", FastMath.TWO_PI / (225f * currentTimeScale));
                planetSpeeds.put("Earth", FastMath.TWO_PI/ (365f * currentTimeScale));
                planetSpeeds.put("Mars", FastMath.TWO_PI / (687f * currentTimeScale));
                planetSpeeds.put("Jupiter", FastMath.TWO_PI / (4333f * currentTimeScale));
                planetSpeeds.put("Saturn", FastMath.TWO_PI / (10759f * currentTimeScale));
                planetSpeeds.put("Uranus", FastMath.TWO_PI / (30687f * currentTimeScale));
                planetSpeeds.put("Neptune", FastMath.TWO_PI / (60190f * currentTimeScale));

    }




    @Override
    public void simpleUpdate(float tpf) {
        System.out.println(5.5f/currentTimeScale);

        if (!isPaused) {
            if (followPlanet != null) {
                Vector3f pos = followPlanet.getLocalTranslation();
                //schieben wir unsere Kamera nach hinten
                cam.setLocation(new Vector3f(pos.x, pos.y + 5, pos.z - 10));
                cam.lookAt(pos, Vector3f.UNIT_Y);
            }

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
            dayCounterText.setText(datumText);
        }
        // tage zählen ende Kalendar

// Aktualisiere den GUI-Text, damit immer der aktuelle Simulations-Tag angezeigt wird


        for (String name : planets.keySet()) {
            float speed = planetSpeeds.get(name);
            float distance = planetDistances.get(name);


            float currentAngle = planetAngles.get(name);
            currentAngle += speed * tpf;
            planetAngles.put(name, currentAngle);


            float x = distance * FastMath.cos(currentAngle);
            float z = distance * FastMath.sin(currentAngle);


            Geometry planet = planets.get(name);
            planet.setLocalTranslation(x, 0, z);
        }


        angleMoon += FastMath.TWO_PI / 27.32f * tpf;
        float moonX = earth.getLocalTranslation().x + 0.8f * FastMath.cos(angleMoon);
        float moonZ = earth.getLocalTranslation().z + 0.8f * FastMath.sin(angleMoon);
        moon.setLocalTranslation(moonX, 0, moonZ);
            if (showTrails) {
                for (String name : planets.keySet()) {
                    Trail trail = planetTrails.get(name);
                    if (trail != null) {
                        trail.update(tpf, planets.get(name).getLocalTranslation());
                    }
                }
            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {}
}
