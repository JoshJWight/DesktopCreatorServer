import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.imageio.ImageIO;

public class DBSession {

	public static DBSession session;
	
	private final String db_name = "objects.db";
	private final String OBJECTS_PATH = "obj";
	
	Statement s;
	Connection c;
	
	public static void start(){
		
		session = new DBSession();
	}
	
	/**
	 * 
	 */
	private DBSession() {

	    try {
	    	Class.forName("org.sqlite.JDBC");
	    	
			c = DriverManager.getConnection("jdbc:sqlite:"+db_name);
			s = c.createStatement();
		    s.setQueryTimeout(30);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    
	    setupTables();
	}
	
	private void setupTables(){
		executeUpdate("CREATE TABLE IF NOT EXISTS objects(name STRING)");
	}
	
	public synchronized void storeObject(BufferedImage object) {
		String objName = object.hashCode() + "";
		executeUpdate("INSERT INTO objects VALUES(\"" + objName + "\")");
		try {
			ImageIO.write(object, "png", new File(OBJECTS_PATH + "/" + objName + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized BufferedImage retrieveObject() {
		ResultSet rs = executeQuery("SELECT * FROM objects LIMIT 1");
		try {
			if(rs.next())
			{
				String objName = rs.getString("name");
				File img = new File(OBJECTS_PATH + "/" + objName + ".png");
				BufferedImage obj = ImageIO.read(img);
				rs.close();
				executeUpdate("DELETE FROM objects WHERE name=\"" + objName + "\"");
				img.delete();
				return obj;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void executeUpdate(String sql)
	{
		try {
			s.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private ResultSet executeQuery(String sql)
	{
		try {
			return s.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void close()
	{
		try
	    {
	        if(c != null)
	          c.close();
	    }
	    catch(SQLException e)
	    {
	        System.err.println(e);
	    }
	}

}
