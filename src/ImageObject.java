import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

public class ImageObject {
	public String name;
	public BufferedImage image;
	
	public ImageObject(String name, BufferedImage image) {
		this.name = name;
		this.image = image;
	}
	
	public String toBase64() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "data:image/png;base64," + DatatypeConverter.printBase64Binary(output.toByteArray());
	}
}
