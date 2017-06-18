import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ObjectExtractor {
	static{System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
	
	public static BufferedImage extract(BufferedImage img){
		return null;
	}
	
	public static Mat detectEdges(Mat I, float gaussSD, int strelSize) {
		int h = I.height();
		int w = I.width();
		Mat blurred = new Mat(h, w, CvType.CV_8U);
		Mat x_grad = new Mat(h, w, CvType.CV_8U);
		Mat y_grad = new Mat(h, w, CvType.CV_8U);
		Mat abs_x_grad = new Mat(h, w, CvType.CV_8U);
		Mat abs_y_grad = new Mat(h, w, CvType.CV_8U); 
		Mat grad = new Mat(h, w, CvType.CV_8U);
		Mat threshold = new Mat(h, w, CvType.CV_8U);
		Mat dilated = new Mat(h, w, CvType.CV_8U);
		Mat closed = new Mat(h, w, CvType.CV_8U);
		Imgproc.GaussianBlur(I, blurred, new Size(5, 5), gaussSD);
		Imgproc.Sobel(blurred, x_grad, CvType.CV_16S, 1, 0);
		Imgproc.Sobel(blurred, y_grad, CvType.CV_16S, 0, 1);
		Core.convertScaleAbs(x_grad, abs_x_grad);
		Core.convertScaleAbs(x_grad, abs_y_grad);
		Core.addWeighted(abs_x_grad, 0.5, abs_y_grad, 0.5, 0, grad);
		Imgproc.adaptiveThreshold(grad, threshold, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, -40);
		Imgproc.dilate(threshold, dilated, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(strelSize, strelSize)));
		Imgproc.erode(dilated, closed, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(strelSize, strelSize)));
		
		return imfill(closed);
		
	}
	
	public static Region largestRegion(Mat img) {
		Mat labeled = new Mat(img.size(), img.type());

        Mat rectComponents = Mat.zeros(new Size(0, 0), 0);
        Mat centComponents = Mat.zeros(new Size(0, 0), 0);
        Imgproc.connectedComponentsWithStats(img, labeled, rectComponents, centComponents);
        int[] rectangleInfo = new int[5];
        double[] centroidInfo = new double[2];
        
        Region largestRegion = null;
        int largestSize = 0;
        for(int i = 1; i < rectComponents.rows(); i++) {

            // Extract bounding box
            rectComponents.row(i).get(0, 0, rectangleInfo);
            int size = rectangleInfo[4];
            if(size > largestSize) {
            	
            	Rect rectangle = new Rect(rectangleInfo[0], rectangleInfo[1], rectangleInfo[2], rectangleInfo[3]);
                
                
                // Extract centroids
                centComponents.row(i).get(0, 0, centroidInfo);
                Point centroid = new Point(centroidInfo[0], centroidInfo[1]);
                largestSize = size;
                largestRegion = new Region(rectangle, centroid, size);
            }
        }

        // Free memory
        rectComponents.release();
        centComponents.release();
        return largestRegion;
	}
	
	public static BufferedImage cutOutObject(BufferedImage img) {
		Mat imgBGR = bufferedImageToMat(img);
		Mat imgHSV = imgBGR.clone();
		Imgproc.cvtColor(imgBGR, imgHSV, Imgproc.COLOR_BGR2HSV);
		List<Mat> bgrChannels = new ArrayList<Mat>(3);
		List<Mat> hsvChannels = new ArrayList<Mat>(3);
		Core.split(imgHSV, hsvChannels);
		Core.split(imgBGR, bgrChannels);
		
		ArrayList<Mat> channels = new ArrayList<Mat>();
		channels.addAll(bgrChannels);
		channels.addAll(hsvChannels);
		
		ArrayList<Mat> edges = new ArrayList<Mat>();
		
		for(Mat channel: channels) {
			edges.add(detectEdges(channel, 0.5f, 10));
			edges.add(detectEdges(channel, 5f, 10));
			edges.add(detectEdges(channel, 10f, 10));
		}
		
		int bestSize = 0;
		Mat bestMask = null;
		Region bestRegion = null;
		
		for(Mat edge: edges) {
			Region r = largestRegion(edge);
			if(r==null) {
				continue;
			}
			
			Mat mask = cutOutRegion(edge, r);
			
			if(r.size > bestSize) {
				bestSize = r.size;
				bestMask = mask;
				bestRegion = r;
			}
		}
		
		Mat bgrMask = imgBGR.clone();
		ArrayList<Mat> maskChannels = new ArrayList<Mat>();
		for(int i=0; i<3; i++) {
			maskChannels.add(bestMask);
		}
		Core.merge(maskChannels, bgrMask);
		
		Mat masked = imgBGR.clone();
		Core.bitwise_and(imgBGR, bgrMask, masked);
		
		BufferedImage outImg = matToBufferedImage(masked.submat(bestRegion.bounding));
		BufferedImage cutImg = new BufferedImage(outImg.getWidth(), outImg.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		//make background transparent
		for(int x = 0; x<outImg.getWidth(); x++) {
			for(int y = 0; y<outImg.getHeight(); y++) {
				int abgr = (outImg.getRGB(x, y) | 0xFF000000);
				if(abgr == 0xFF000000) {
					cutImg.setRGB(x, y, 0x00000000);
				} else {
					cutImg.setRGB(x, y, abgr );
				}
			}
		}
		return cutImg;
	}
	
	public static Mat imfill(Mat img) {
		Mat mask = Mat.zeros(new Size(img.cols()+2, img.rows()+2), CvType.CV_8U);
		Mat floodfilled = img.clone();
		Imgproc.floodFill(floodfilled, mask, new Point(0,0), new Scalar(255));
		Mat inv = floodfilled.clone();
		Core.bitwise_not(floodfilled, inv);
		Mat result = img.clone();
		Core.bitwise_or(img, inv, result);
		return result;
	}
	
	public static Mat bufferedImageToMat(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		return mat;
	}
	
	public static BufferedImage matToBufferedImage(Mat m){
	      int type = BufferedImage.TYPE_BYTE_GRAY;
	      if ( m.channels() > 1 ) {
	          type = BufferedImage.TYPE_3BYTE_BGR;
	      }
	      int bufferSize = m.channels()*m.cols()*m.rows();
	      byte [] b = new byte[bufferSize];
	      m.get(0,0,b); // get all the pixels
	      BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
	      final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	      System.arraycopy(b, 0, targetPixels, 0, b.length);  
	      return image;

	  }
	
	public static Mat cutOutRegion(Mat img, Region r) {
		Mat mask = Mat.zeros(new Size(img.cols()+2, img.rows()+2), CvType.CV_8U);
		Mat inv = img.clone();
		Core.bitwise_not(img, inv);
		Imgproc.floodFill(inv, mask, r.centroid, new Scalar(255));
		Mat result = img.clone();
		Core.bitwise_and(img, inv, result);
		return result;
	}
	
	public static void imshow(Mat m){
		imshow(m, "");
	}
	
	public static void imshow(Mat m, String title) {
		imshow(matToBufferedImage(m), title);
	}
	
	public static void imshow(BufferedImage img, String title) {
		JFrame frame = new JFrame();
		JLabel picLabel = new JLabel(new ImageIcon(img));
		frame.add(picLabel);
		frame.setSize(img.getWidth(), img.getHeight()+20);
		frame.setTitle(title);
		frame.setVisible(true);
	}
}
