package fr.xml.migration;

import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

public class MigrationLauncher {

    private static final Logger logger = LoggerFactory.getLogger(MigrationLauncher.class);

    private static final String elmFwdDirectoryPath = "D:\\WDM_DATA\\FWD_ELM\\";
    private static final String wdualFwdDirectoryPath = "D:\\WDM_DATA\\RESULT\\";
    private static final String templateXmlPath = "D:\\WDM_DATA\\template.xml";
    private static final String csvFilePath = "D:\\WDM_DATA\\mapping.csv";

    public static void main(final String[] args) throws JDOMException {
        logger.info("Starting migration");
        // Parcours du fichier CSV.
        try {
            logger.debug("loading template file: {}", templateXmlPath);
            final Element root = MigrationLauncher.loadtemplate(new File(templateXmlPath));

            logger.debug("loading file: {}", csvFilePath);
            final CSVReader reader = new CSVReader(new FileReader(new File(csvFilePath)), ';');
            try {
                final List<String[]> myEntries = reader.readAll();

                String elmFwdPath = "";
                String wdualFwdPath = "";
                for (final String[] entry : myEntries) {
                    if (entry != null) {
                        elmFwdPath = elmFwdDirectoryPath + entry[0];
                        logger.debug("memorizing an ELM FWD file path: {}", elmFwdPath);
                        final Map<String, Element> content = MigrationLauncher.getElmFwdContent(new File(elmFwdPath));

                        final Element clonedRoot = root;
                        final Element wireharnessElement = clonedRoot.getChild("WireHarness");
                        wireharnessElement.removeContent();
                        if (wireharnessElement != null) {
                            for (final Entry<String, Element> entryTemp : content.entrySet()) {
                                wireharnessElement.addContent(entryTemp.getValue());
                            }
                        }

                        wdualFwdPath = wdualFwdDirectoryPath + entry[1] + ".xml";
                        logger.debug("memorizing a WDUAL FWD file path: {}", wdualFwdPath);

                        try {
                            final XMLOutputter xmlop = new XMLOutputter(Format.getPrettyFormat());
                            xmlop.output(clonedRoot, new FileOutputStream(new File(wdualFwdPath)));
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

}
