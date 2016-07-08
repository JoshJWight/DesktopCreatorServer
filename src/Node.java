/**
 * Node.java 
 */

/**
 * For use in migratefilter's heap
 * 
 * @author Josh Wight
 *
 */
public class Node{
		public int x;
		public int y;
		public double f;//fitness
		public int rgb;
		
		public Node(int x, int y, int rgb, double fitness)
		{
			this.x = x;
			this.y = y;
			this.f = fitness;
			this.rgb = rgb;
		}
	}