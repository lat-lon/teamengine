/**
 * **************************************************************************
 *
 * Contributor(s): 
 *	C. Heazel (WiSC): Added Fortify adjudication changes
 *
 ***************************************************************************
 */
package com.occamlab.te;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Provides static configuration settings. The {@code TE_BASE} system property
 * or environment variable specifies the location of the main configuration
 * directory that contains several essential sub-directories.
 *
 * <p>
 *
 * <pre>
 * TE_BASE
 *  |-- config.xml
 *  |-- resources/
 *  |-- scripts/
 *  |-- work/
 *  +-- users/
 *      |-- {username1}/
 *      +-- {usernameN}/
 * </pre>
 *
 * </p>
 *
 */
public class SetupOptions {
  public static final String TE_BASE = "TE_BASE";
  private static File teBaseDir = getBaseConfigDirectory();
  boolean validate = true;
  boolean preload = false;
  File workDir = null;
  String sourcesName = "default";
  ArrayList<File> sources = new ArrayList<File>();

  /**
   * Default constructor. Creates the TE_BASE/scripts directory if it does not
   * exist.
   */
  public SetupOptions() {
    File scriptsDir = new File(teBaseDir, "scripts");
    if (!scriptsDir.exists() && !scriptsDir.mkdirs()) {
      throw new RuntimeException("Failed to create directory at "
              + scriptsDir.getAbsolutePath());
    }
  }

  /**
     * Determines the location of the TE_BASE directory by looking for either 1)
     * a system property or 2) an environment variable named {@value #TE_BASE}.
     * Finally, if neither is set then the "teamengine" subdirectory is created
     * in the user home directory (${user.home}/teamengine).
   *
   * @return A File denoting the location of the base configuration directory.
   */
  public static File getBaseConfigDirectory() {
    if (null != teBaseDir) {
      return teBaseDir;
    }
    String basePath = System.getProperty(TE_BASE);
    if (null == basePath) {
      basePath = System.getenv(TE_BASE);
    }
    if (null == basePath) {
      basePath = System.getProperty("user.home")
              + System.getProperty("file.separator") + "teamengine";
    }
    File baseDir = new File(basePath);
    if (!baseDir.isDirectory()) {
      baseDir.mkdirs();
    }
    Logger.getLogger(SetupOptions.class.getName()).log(Level.CONFIG,
            "Using TE_BASE at " + baseDir);
    return baseDir;
  }

  /**
   * Determine the test recording is on or off.
   * @param testName
   * @return 
   * @throws javax.xml.parsers.ParserConfigurationException
   * @throws org.xml.sax.SAXException
   * @throws java.io.IOException
   */
  public static boolean recordingInfo(String testName) throws ParserConfigurationException, SAXException, IOException {
    TECore.rootTestName.clear();
    String path = getBaseConfigDirectory() + "/config.xml";
    if (new File(path).exists()) {
        // Fortify Mod: prevent external entity injection
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setExpandEntityReferences(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        //DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(path);
        NodeList nodeListForStandardTag = doc.getElementsByTagName("standard");
        if (null!=nodeListForStandardTag && nodeListForStandardTag.getLength() > 0) {
          for (int i = 0; i < nodeListForStandardTag.getLength(); i++) {
            Element elementStandard = (Element) nodeListForStandardTag.item(i);
            if (testName.equals(elementStandard.getElementsByTagName("local-name").item(0).getTextContent())) {
              if (null!=elementStandard.getElementsByTagName("record").item(0)) {
                System.setProperty("Record", "True");
                NodeList rootTestNameArray=elementStandard.getElementsByTagName("test-name");
                if (null!=rootTestNameArray && rootTestNameArray.getLength() > 0) {
                  for (int counter = 0; counter < rootTestNameArray.getLength(); counter++) {
                    Element rootTestName = (Element) rootTestNameArray.item(counter);
                    TECore.rootTestName.add(rootTestName.getTextContent());
                  }
                }
                return true;
              }
            }
    
          }
        }
    }
    System.setProperty("Record", "False");
    return false;
  }

  public String getSourcesName() {
    return sourcesName;
  }

  public void setSourcesName(String sourcesName) {
    this.sourcesName = sourcesName;
  }

  /**
   * Returns the location of the work directory (TE_BASE/work).
   *
     * @return A File denoting a directory location; it is created if it does
     *         not exist.
   */
  public File getWorkDir() {
    if (null == this.workDir) {
      File dir = new File(teBaseDir, "work");
      if (!dir.exists() && !dir.mkdir()) {
        throw new RuntimeException("Failed to create directory at "
                + dir.getAbsolutePath());
      }
      this.workDir = dir;
    }
    return workDir;
  }

  /**
     * Returns a list of file system resources (directories and files)
     * containing CTL test scripts.
   *
   * @return A List containing one or more File references (TE_BASE/scripts is
     *         the default location).
   */
  public List<File> getSources() {
    return sources;
  }

  /**
   * Adds a file system resource to the collection of known scripts.
   *
     * @param source
     *            A File object representing a file or directory.
   */
  public void addSource(File source) {
    this.sources.add(source);
  }

  public Element getParamsElement() {
    return null;
  }

  public boolean isValidate() {
    return validate;
  }

  public void setValidate(boolean validate) {
    this.validate = validate;
  }

  public boolean isPreload() {
    return preload;
  }

  public void setPreload(boolean preload) {
    this.preload = preload;
  }
}
