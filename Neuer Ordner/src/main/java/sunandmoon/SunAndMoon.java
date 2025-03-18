package sunandmoon;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

/**
 * This is the Main Class of your Game. It should boot up your game and do initial initialisation
 * Move your Logic into AppStates or Controls or other java classes
 */
public class SunAndMoon extends SimpleApplication {

    // da kann man globale Variablen hinzufüfgen
    private float angle = 0;
    private Node earthOrbitNode;
    private Node nextPlanetOrbitNode;
    private Node moonOrbitNode;

    // da startet unseres Programm
    public static void main(String[] args) {
        SunAndMoon app = new SunAndMoon();

        AppSettings settings = new AppSettings(true);
        settings.setResolution(1920, 1080);
        app.setSettings(settings);

        app.start();
    }

    @Override
    public void simpleInitApp() {

        // Kamera
        cam.setLocation(new Vector3f(100, 100, -40));
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(25);

        // Sonne
        Sphere s = new Sphere(100, 100, 5);
        Geometry sun = new Geometry("Sun", s);
        Material sunMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sunMat.setColor("Color", ColorRGBA.Yellow);
        sun.setMaterial(sunMat);
        sun.setLocalTranslation(0, 0, 0);
        rootNode.attachChild(sun);
        earthOrbitNode = new Node("EarthOrbit");

        rootNode.attachChild(earthOrbitNode);
        Node earthNode = new Node("EarthNode");

        // Erde
        Sphere e = new Sphere(100, 100, 1);
        Geometry earthGeo = new Geometry("Earth", e);
        Material earthMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        earthMat.setColor("Color", ColorRGBA.Blue);
        earthGeo.setMaterial(earthMat);
        earthNode.attachChild(earthGeo);
        earthNode.setLocalTranslation(20, 0, 0);
        earthOrbitNode.attachChild(earthNode);

        moonOrbitNode = new Node("MoonOrbit");
        earthNode.attachChild(moonOrbitNode);

        // Mond
        Sphere m = new Sphere(100, 100, 0.5f);
        Geometry moonGeo = new Geometry("Moon", m);
        Material moonMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        moonMat.setColor("Color", ColorRGBA.White);
        moonGeo.setMaterial(moonMat);
        moonGeo.setLocalTranslation(5, 0, 0);
        moonOrbitNode.attachChild(moonGeo);


        // nextplanet
        nextPlanetOrbitNode = new Node("NextPlanetOrbit");
        rootNode.attachChild(nextPlanetOrbitNode);
        Sphere np = new Sphere(100,100,3);
        Geometry nextPlanet = new Geometry("NextPlanet", np);
        Material nextPlanetMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        nextPlanetMat.setColor("Color", ColorRGBA.Green);
        nextPlanet.setMaterial(nextPlanetMat);
        nextPlanet.setLocalTranslation(40, 0, 0);
        nextPlanetOrbitNode.attachChild(nextPlanet);

        initUI();
    }

    // da geht's um die Bewegungen und Animationen
    @Override
    public void simpleUpdate(float tpf) {
        angle += tpf; // time pro frame (simple update)
        // die Bewegung unserer Erde
        Quaternion rotation = new Quaternion(); // um die Objekte zu bewegen
        rotation.fromAngleAxis(angle, Vector3f.UNIT_Y); //angle ist die Geschwindigkeit und dreht sich um Y Axis
        earthOrbitNode.setLocalRotation(rotation);

        // die Bewegung naechster Planet
        Quaternion rotationNextPlanet = new Quaternion();
        rotationNextPlanet.fromAngleAxis(angle * 0.5f, Vector3f.UNIT_Y);
        nextPlanetOrbitNode.setLocalRotation(rotationNextPlanet);

        // Mondbewegung um die Erde
        Quaternion rotationMoon = new Quaternion();
        rotationMoon.fromAngleAxis(angle * 0.3f, Vector3f.UNIT_Y); // Mond rotiert schneller als Erde

        // Mond bewegt sich relativ zur Erde auf einer Umlaufbahn
        //Vector3f moonPosition = rotationMoon.mult(new Vector3f(10, 5, 0)); // Abstand beibehalten
        //moonOrbitNode.setLocalTranslation(moonPosition);
    }

    // keine Ahnung, irgendwelche Scheiders Grafiks und so
    @Override
    public void simpleRender(RenderManager rm) {

    }

    private void initUI() {
        // Lade das Standard-Font
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        // ----- Buttons (als BitmapText, nur zur Anzeige) -----
        // Button "Start"
        BitmapText btnStart = new BitmapText(guiFont, false);
        btnStart.setText("[ Start ]");
        btnStart.setLocalTranslation(10, cam.getHeight() - 50, 0);
        guiNode.attachChild(btnStart);

        // Button "Stop"
        BitmapText btnStop = new BitmapText(guiFont, false);
        btnStop.setText("[ Stop ]");
        btnStop.setLocalTranslation(10, cam.getHeight() - 80, 0);
        guiNode.attachChild(btnStop);

        // Button "Reset"
        BitmapText btnReset = new BitmapText(guiFont, false);
        btnReset.setText("[ Reset ]");
        btnReset.setLocalTranslation(10, cam.getHeight() - 110, 0);
        guiNode.attachChild(btnReset);

        // ----- Kalender oben rechts -----
        // Erzeuge einen String mit dem aktuellen Datum
        String currentDate = java.time.LocalDate.now().toString(); // z.B. "2025-03-18"
        BitmapText calendar = new BitmapText(guiFont, false);
        calendar.setText("Kalender: " + currentDate);
        // Positioniere den Kalender oben rechts; berechne die X-Position anhand der Textbreite
        calendar.setLocalTranslation(cam.getWidth() - calendar.getLineWidth() - 10, cam.getHeight() - 10, 0);
        guiNode.attachChild(calendar);
    }
}
