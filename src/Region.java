import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

public class Region {
    public Rect bounding;
    public Point centroid;
    public int size;
        
    public Region(Rect bounding, Point centroid, int size) {
        this.bounding = bounding;
        this.centroid = centroid;
        this.size = size;
    }
}