package sunandmoon;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

import java.util.ArrayList;
import java.util.Iterator;

public class Trail {
    private final Node rootNode;
    private final AssetManager assetManager;
    private final ArrayList<Geometry> trailPoints = new ArrayList<>();

    private final ColorRGBA trailColor;
    private final float trailInterval;
    private float trailTimer = 0f;
    private final int maxTrailPoints;
    private final float dropSpeed;

    public Trail(Node rootNode, AssetManager assetManager, ColorRGBA color, float interval, int maxPoints, float dropSpeed) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.trailColor = color;
        this.trailInterval = interval;
        this.maxTrailPoints = maxPoints;
        this.dropSpeed = dropSpeed;
    }


    public void update(float tpf, Vector3f sourcePosition) {
        trailTimer += tpf*1000;

        if (trailTimer >= trailInterval) {
            trailTimer = 0f;

            Box sphere = new Box(0.1f, 0.1f, 0.1f);
            Geometry point = new Geometry("TrailPoint", sphere);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", trailColor.mult(0.8f));
            point.setMaterial(mat);

            Vector3f pos = sourcePosition.clone();
            pos.y -= 0.1f;
            point.setLocalTranslation(pos);
            rootNode.attachChild(point);
            trailPoints.add(point);


            if (trailPoints.size() > maxTrailPoints) {
                Geometry oldest = trailPoints.remove(0);
                oldest.removeFromParent();
            }
        }


        Iterator<Geometry> iter = trailPoints.iterator(); // so gehen die Punkte nach unten
        while (iter.hasNext()) {
            Geometry p = iter.next();
            Vector3f pos = p.getLocalTranslation();
            pos.y -= tpf * dropSpeed;
            p.setLocalTranslation(pos);
        }

    }
    public void clear() {
        for (Geometry pt : trailPoints) {
            pt.removeFromParent();
        }
        trailPoints.clear();
    }
}
