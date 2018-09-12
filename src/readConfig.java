
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;



//https://www.mkyong.com/java/java-properties-file-examples/
public class readConfig {
	String result = "";
	InputStream inputStream;
 
	public String[] getPropValues() throws IOException {
 
//		String [] res=null;
		String[] res = new String[3]; 
		try {
			Properties prop = new Properties();
			String propFileName = "config.properties";
 
			//inputStream = getClass().getClassLoader().getResourceAsStream(propFileName); //para cargarlo del classpath osea oculo
			inputStream = new FileInputStream("config.properties"); //cargar el archivo
 
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
 
			Date time = new Date(System.currentTimeMillis());
 
			// get the property value and print it out
			String servidor = prop.getProperty("servidor");
			String pdf_path = prop.getProperty("XML_PATH");
			String xml_path = prop.getProperty("PDF_PATH");
			res[0]=servidor;
			res[1]=pdf_path;
			res[2]=xml_path;
 
			result = "Properties List = " + servidor + ", " + pdf_path + ", " + xml_path;
			System.out.println(result + "\nProgram Ran on " + time + " by user=" + servidor);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
		return res;
	}
}
