//BB
//https://www.codejava.net/coding/upload-files-to-database-servlet-jsp-mysql

import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import com.microsoft.sqlserver.jdbc.*;

//import org.apache.commons.io.comparator.LastModifiedFileComparator;
//import org.apache.commons.io.filefilter.FileFileFilter;


import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;



import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;


import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.InputStream;

public class App {
	
	static Connection con = null;
	
	private static Connection GetCon() {
		String message = null; 
        try {            
            SQLServerDataSource ds = new SQLServerDataSource();
			ds.setServerName("APOLO");
			ds.setPortNumber(1433); 
			ds.setDatabaseName("DB_Docs");
			ds.setPassword("Panam2014");
			ds.setUser("sa");
			con = ds.getConnection();
			return con;
        } catch (SQLException ex) {
            message = "ERROR: " + ex.getMessage();
            ex.printStackTrace();
        }
		return con;
        
	}
	private static void Insert_Data(String TAG_Nombre,String Tag_pdf,String Tag_xml,String Tag_RazSoc) throws FileNotFoundException {
		
		File initialFile = new File(Tag_xml);
        InputStream inputStream = new FileInputStream(initialFile);
         
        InputStream inputStreamPDF = null;
        File initialFilePDF = null;
        
        while(inputStreamPDF==null) {
        	try {
       		Thread.sleep(2000);
			initialFilePDF = new File(Tag_pdf);
			inputStreamPDF = new FileInputStream(initialFilePDF);
			
        	} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(" pdf no encontrado");
			}
        	finally {
        		System.out.println("--------");
        	}
        }         
        String[] Doc_arr = TAG_Nombre.split("-");
		
		try {
			// constructs SQL statement
	        String sql = "INSERT INTO Docs (doc_tipdoc, doc_serdoc, doc_ruc,doc_nro,doc_xml,doc_pdf,[Doc_EmiRazSoc]) values (?, ?, ?, ?,?,?,?)";
	        PreparedStatement statement = con.prepareStatement(sql);
	
	        
	        statement.setInt(1,Integer.parseInt(Doc_arr[1]) );//(1, Doc_arr[1].toString());
	        statement.setString(2, Doc_arr[2].toString());
	        statement.setString(3, Doc_arr[0].toString());
	        statement.setInt(4, Integer.parseInt(Doc_arr[3]));
	        statement.setString(7, Tag_RazSoc);
	
	        
			if (inputStream != null) {      	
	            statement.setBlob(5, inputStream, (int) initialFile.length());
	        }
	        
	        if (inputStreamPDF != null) {
	            statement.setBlob(6, inputStreamPDF, (int) initialFilePDF.length());
	        }
	
	        // sends the statement to the database server
	        int row = statement.executeUpdate();
	        System.out.println(" Documento Ingresado Correctamente");
	
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	    } finally {
	        if (con != null) {
	            // closes the database connection
	            try {
	                con.close();
	            } catch (SQLException ex) {
	                ex.printStackTrace();
	            }
	        }
	    }
	}
	
	
	public static void watchDirectoryPath(Path path,Path pathPDF) {
		
        try {
            Boolean isFolder = (Boolean) Files.getAttribute(path,
                    "basic:isDirectory", NOFOLLOW_LINKS);
            if (!isFolder) {
                throw new IllegalArgumentException("Path: " + path
                        + " is not a folder");
            }
        } catch (IOException ioe) {
            // Folder does not exists
            ioe.printStackTrace();
        }

        System.out.println("Escaneando Ruta XML: " + path);
        System.out.println("Escaneando Ruta PDF: " + pathPDF);

        // We obtain the file system of the Path
        FileSystem fs = path.getFileSystem();

        // We create the new WatchService using the new try() block
        try (WatchService service = fs.newWatchService()) {

            // We register the path to the service
            // We watch for creation events
        	
        	path.register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE); 
         
            // Start the infinite polling loop
            WatchKey key = null;
            while (true) {
                key = service.take();

                // Dequeueing events
                Kind<?> kind = null;
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    // Get the type of the event
                    kind = watchEvent.kind();
                    if (OVERFLOW == kind) {
                        continue; // loop
                    } else if (ENTRY_CREATE == kind) {
                        // A new Path was created
                        Path newPath = ((WatchEvent<Path>) watchEvent)
                                .context();                        
                        // Output
                        System.out.println("Archivo Creado: " + path);
                        if(newPath.toString().endsWith(".xml")) {
                    	String[] Doc_arr = null;
                    	String Doc_inf = null;
                    	String doc_Raz_Soc=null;
                        try {

                        	File fXmlFile = new File(path.toString()+newPath.toString());
                        	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        	Document doc = dBuilder.parse(fXmlFile);
                        			
                        	//optional, but recommended
                        	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
                        	doc.getDocumentElement().normalize();

                        	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                        			
                        	NodeList nList = doc.getElementsByTagName("Documento");
                        	doc_Raz_Soc=	 doc.getElementsByTagName("cbc:Name").item(0).getTextContent();		
                        	
                        	System.out.println("----------------------------");

                        	for (int temp = 0; temp < nList.getLength(); temp++) {

                        		Node nNode = nList.item(temp);
                        				
                        		System.out.println("\nElemento Actual :" + nNode.getNodeName());
                        				
                        		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        			Element eElement = (Element) nNode;
                        			Doc_arr = eElement.getElementsByTagName("Nombre").item(0).getTextContent().split("-");
                        			Doc_inf = eElement.getElementsByTagName("Nombre").item(0).getTextContent();
                        			
                        			System.out.println("RUC	: " + Doc_arr[0]);
                        			System.out.println("TipDoc 	: " + Doc_arr[1]);
                        			System.out.println("Serie	: " + Doc_arr[2]);
                        			System.out.println("NroDoc	: " + Doc_arr[3]);                        			
                        			System.out.println("RazSoc	: " + doc_Raz_Soc);

                        		}
                        	}
                            } catch (Exception e) {
                        	e.printStackTrace();
                            }
                        GetCon();
                        Insert_Data(Doc_inf,pathPDF+"//"+Doc_inf+".pdf",path+newPath.toString(),doc_Raz_Soc);

                        }
                        
                        

                    } else if (ENTRY_MODIFY == kind) {
                        // modified
                        Path newPath = ((WatchEvent<Path>) watchEvent)
                                .context();
                        // Output
                        System.out.println("Archivo Modificado: " + newPath);
                    } else if (ENTRY_DELETE == kind) {
	                    // modified
	                    Path newPath = ((WatchEvent<Path>) watchEvent)
	                            .context();
	                    // Output
	                    System.out.println("Archivo Borrado: " + newPath);
	                }
                }

                if (!key.reset()) {
                    break; // loop
                }
            }
            

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

    }

	public static File[] finder( String dirName){
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() { 
                 public boolean accept(File dir, String filename)
                      { return filename.endsWith(".pdf"); }
        } );

    }

	public static void main ( String[] args ) throws InterruptedException, IOException
	{
		///*** ARGS ****
	//	String Test_Path ="Y:\\";
	//	File x;
		
		readConfig fConfig=new readConfig();
		String[]PROPS=fConfig.getPropValues();
		System.out.println(PROPS[0]);
		
		File dirXML = new File(PROPS[1]);
		File dirPDF = new File(PROPS[2]);
		
        watchDirectoryPath(dirXML.toPath(),dirPDF.toPath());
		
	
		 
	}
}


