package sunandmoon;

import com.jme3.app.SimpleApplication;
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

    // da startet unseres Programm
    public static void main(String[] args) {
        SunAndMoon app = new SunAndMoon();

        AppSettings settings = new AppSettings(true);
        settings.setResolution(1920, 1080);
        app.setSettings(settings);

        app.start();
    }

    // da schreibt man alle Objekte und Eigenschaften dazu
    @Override
    public void simpleInitApp() {

        //cameraman
        cam.setLocation(new Vector3f(100, 100, -40));
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(25);

        // sun
        Sphere s = new Sphere(100,100,5);
        Geometry sun = new Geometry("Sun", s);
        Material sunMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sunMat.setColor("Color", ColorRGBA.Yellow);
        sun.setMaterial(sunMat);
        sun.setLocalTranslation(0,0,0);
        rootNode.attachChild(sun);

        // earth
        earthOrbitNode = new Node("EarthOrbit");
        rootNode.attachChild(earthOrbitNode);
        Sphere e = new Sphere(100,100,1);
        Geometry eth = new Geometry("Earth", e);
        Material earthMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        earthMat.setColor("Color", ColorRGBA.Blue);
        eth.setMaterial(earthMat);
        eth.setLocalTranslation(20, 0, 0);
        earthOrbitNode.attachChild(eth);


        // nextplanet
        nextPlanetOrbitNode = new Node("NextPlanetOrbit");
        rootNode.attachChild(nextPlanetOrbitNode);
        Sphere np = new Sphere(100,100,2);
        Geometry nextPlanet = new Geometry("NextPlanet", np);
        Material nextPlanetMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        nextPlanetMat.setColor("Color", ColorRGBA.Green);
        nextPlanet.setMaterial(nextPlanetMat);
        nextPlanet.setLocalTranslation(40, 0, 0);
        nextPlanetOrbitNode.attachChild(nextPlanet);


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
    }

    // keine Ahnung, irgendwelche Scheiders Grafiks und so
    @Override
    public void simpleRender(RenderManager rm) {

    }
}
