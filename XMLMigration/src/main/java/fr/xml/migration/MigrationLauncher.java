package fr.xml.migration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

public class MigrationLauncher {

	private static final Logger logger = LoggerFactory.getLogger(MigrationLauncher.class);

	private static final String elmFwdDirectoryPath = "F:\\DATA\\FWD_ELM\\";
	private static final String wdualFwdDirectoryPath = "F:\\DATA\\FWD_WDUAL\\";
	private static final String result = "F:\\DATA\\RESULT\\";
	private static final String csvFilePath = "F:\\DATA\\mapping.csv";
	private static final String externalDocumentDirectoryName = "External Document";
	private static final String xmlFilePrefix = "SEE_EDB_WD_";

	public static void main(final String[] args) throws JDOMException, IOException {			
		MigrationLauncher.process();
	}

	public static void process() throws JDOMException {
		logger.info("Starting migration");
		// Parcours du fichier CSV.
		try {        	        

			// Chargement du CSV
			logger.debug("loading file: {}", csvFilePath);
			final CSVReader reader = new CSVReader(new FileReader(new File(csvFilePath)), ';');
			try {
				final List<String[]> myEntries = reader.readAll();

				String elmFwdPath = "";
				String wdualFwdPath = "";

				for (final String[] entry : myEntries) {
					if (entry != null) {

						// Memorisation du nom du FWD ELM
						elmFwdPath = elmFwdDirectoryPath + entry[0];
						logger.debug("memorizing an ELM FWD file path: {}", elmFwdPath);

						// Recuperation des balises de liste electrique
						final Map<String, Element> content = getElmFwdContent(new File(elmFwdPath));

						// Construction du chemin FWD WDUAL
						wdualFwdPath = wdualFwdDirectoryPath + File.separator + entry[1] + File.separator + externalDocumentDirectoryName + File.separator +  xmlFilePrefix + entry[1] + ".xml";
						logger.debug("memorizing a WDUAL FWD file path: {}", wdualFwdPath);

						// Chargement du template XML
						logger.debug("loading wdual xml file: {}", wdualFwdPath);
						final Element root = MigrationLauncher.loadtemplate(new File(wdualFwdPath));

						// Suppression des données de liste electrique existante
						logger.debug("removing exisiting electrical list from: {}", wdualFwdPath);
						removeExisitingElectricList(root);

						// Completion du XML final
						final Element clonedRoot = root;
						final Element wireharnessElement = clonedRoot.getChild("WireHarness");
						wireharnessElement.removeContent();
						if (wireharnessElement != null) {
							for (final Entry<String, Element> entryTemp : content.entrySet()) {
								wireharnessElement.addContent(entryTemp.getValue());
							}
						}

						try {
							File xmlFile = new File(wdualFwdPath);
							if(xmlFile.exists()) {
								logger.debug("Found an exisiting xml file. It will be removed");
								xmlFile.delete();
							}
							final XMLOutputter xmlop = new XMLOutputter(Format.getPrettyFormat());
							xmlop.output(clonedRoot, new FileOutputStream(new File(wdualFwdPath)));
							logger.debug("Writing migrated FWD XML file: {}", wdualFwdPath);
						} catch (final IOException e) {

						}
					}
				}
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		} catch (final IOException ex) {
			logger.error("Error during reading CSV file", ex);
		}
		logger.info("End of migration");
	}

	public static Map<String, Element> getElmFwdContent(final File xmlFile) throws JDOMException, IOException {
		final Map<String, Element> content = new HashMap<String, Element>();

		final String xpGetConnectiveDevicesList = "//ConnectiveDevicesList";
		final String xpGetWiresList = "//WiresList";
		final String xpGetWiresExtremitiesListList = "//WiresExtremitiesList";

		// read the XML into a JDOM2 document.
		final SAXBuilder jdomBuilder = new SAXBuilder();

		final Document jdomDocument = jdomBuilder.build(xmlFile);

		// use the default implementation
		final XPathFactory xFactory = XPathFactory.instance();

		// select all links
		final XPathExpression<Object> connectiveDevicesListExpression = xFactory.compile(xpGetConnectiveDevicesList);
		final List<Object> o1 = connectiveDevicesListExpression.evaluate(jdomDocument);
		final Element connectiveDevicesList = (Element) o1.get(0);
		connectiveDevicesList.detach();

		final XPathExpression<Object> wiresListExpression = xFactory.compile(xpGetWiresList);
		final List<Object> o2 = wiresListExpression.evaluate(jdomDocument);
		final Element wiresList = (Element) o2.get(0);
		wiresList.detach();

		final XPathExpression<Object> wiresExtremitiesListExpression = xFactory.compile(xpGetWiresExtremitiesListList);
		final List<Object> o3 = wiresExtremitiesListExpression.evaluate(jdomDocument);
		final Element wiresExtremitiesList = (Element) o3.get(0);
		wiresExtremitiesList.detach();

		content.put("ConnectiveDevicesList", connectiveDevicesList);
		content.put("WiresList", wiresList);
		content.put("WiresExtremitiesList", wiresExtremitiesList);

		return content;
	}

	public static Element loadtemplate(final File templateFile) {
		Element root = null;
		Document doc = null;
		final SAXBuilder sxb = new SAXBuilder();
		try {
			doc = sxb.build(templateFile);
			root = doc.getRootElement();
		} catch (final Exception e) {

		}
		return root;
	}

	public static void putTagInWireDiagram(final Element rootElement, final String tagValue) {
		Element e1 = (Element)rootElement.getChild("FunctionalGroup");
		Element e2 = (Element)e1.getChild("FunctionalGroup");
		Element wd = (Element)e2.getChild("WireDiagram");
		wd.getAttribute("Tag").setValue(tagValue);
	}

	public static void removeExisitingElectricList(final Element rootElement) {
		Element wh = (Element)rootElement.getChild("WireHarness");
		wh.removeChild("ConnectiveDevicesList");
		wh.removeChild("WiresList");
		wh.removeChild("WiresExtremitiesList");
	}
	
	public static void removeUnusedFwd() throws IOException {
		// Chargement du CSV
		logger.debug("loading file: {}", csvFilePath);
		final CSVReader reader = new CSVReader(new FileReader(new File(csvFilePath)), ';');
		final List<String[]> myEntries = reader.readAll();
		
		File dir = new File(wdualFwdDirectoryPath);
		
		File[] files = dir.listFiles();
		
		for(File f : files) {
			for (final String[] entry : myEntries) {
				String wdualFwdName = entry[1];
				if(f.getName().equalsIgnoreCase(wdualFwdName)) {
					FileUtils.copyDirectoryToDirectory(f, new File(result));
				}			
			}
		}
	}
}
