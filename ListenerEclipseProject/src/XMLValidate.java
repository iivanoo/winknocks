/*
 * XMLValidate.java
 *
 */

import java.io.*;

import javax.xml.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.*;
import javax.xml.validation.*;
import org.w3c.dom.*;

/**
 *
 * @author Robert Eckstein
 */
public class XMLValidate {
    
	public static String file = "";
    /** Creates a new instance of Main */
    private static void validate(String fileName, String xSchema) throws Exception {
    		try {
	            // parse an XML document into a DOM tree
	            DocumentBuilder parser =
	                DocumentBuilderFactory.newInstance().newDocumentBuilder();
	            Document document = parser.parse(new File(fileName));
	
	            // create a SchemaFactory capable of understanding WXS schemas
	            SchemaFactory factory =
	                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	
	            // load a WXS schema, represented by a Schema instance
	            Source schemaFile = new StreamSource(new File(xSchema));
	            Schema schema = factory.newSchema(schemaFile);
	
	            // create a Validator object, which can be used to validate
	            // an instance document
	            Validator validator = schema.newValidator();
	
	            // validate the DOM tree
	
	            validator.validate(new DOMSource(document));
    		} catch(Exception e) {
    			XMLValidate.file = fileName.substring(fileName.lastIndexOf("/") + 1).replaceAll(".xml", "");
    			throw e;
    		}
    
    }
    
    /**
     * @param args the command line arguments
     */
    public static void validateKnockSequences() throws Exception {
    	File directory = new File("knockSequences/");
		String[] fileList = directory.list();
		for (int i=0; i<fileList.length; i++) {
			if (fileList[i].endsWith(".xml")) {
				XMLValidate.validate(directory.getAbsolutePath() + "/" + fileList[i], "xSchemas/KnockSequence.xsd");
			} 
		}
    }
    
    public static void validateOptions() throws Exception {
		XMLValidate.validate("options.xml", "xSchemas/options.xsd");
    }
    
}
