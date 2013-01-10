package com.lazerycode.jmeter.analyzer.util;

import com.lazerycode.jmeter.analyzer.AnalyzeMojo;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;

/**
 * Utility for rendering Freemarker Templates
 *
 * @author Arne Franken, Peter Kaul
 */
public class TemplateUtil {

  /**
   * Quantiles resolution is 1000 so that we can get 99.9 percent
   */
  private static final int Q_QUANTILES = 1000;
  /**
   * Use this value to get the Quantile for 99.9 percent
   */
  private static final int K_99_PONT_9_PERCENT = 999;
  /**
   * Use this value to get the Quantile for 99 percent
   */
  private static final int K_99_PERCENT = 990;

  private static final int PERCENT_100 = 100;

  /**
   * Create rootMap with all necessary parameters/objects for Freemarker rendering
   *
   * @param self main object that will be accessed from Freemarker template
   *
   * @return populated map
   */
  public static Map<String,Object> getRootMap(Object self) {

    Map<String,Object> rootMap = new HashMap<String,Object>();
    rootMap.put("self", self);
    rootMap.put("Q_QUANTILES", Q_QUANTILES);
    rootMap.put("K_99_PERCENT", K_99_PERCENT);
    rootMap.put("K_99_PONT_9_PERCENT", K_99_PONT_9_PERCENT);
    rootMap.put("PERCENT_100", PERCENT_100);
    rootMap.put("DETAILS", ENVIRONMENT.isGenerateDetails());
    rootMap.put("CHARTS", ENVIRONMENT.isGenerateCharts());

    return rootMap;

  }

  /**
   * Try to load template from custom location.
   * Load bundled template from classpath in case no custom template is available or an error occurs
   *
   * @param templateName name of the template
   *
   * @return the template
   *
   * @throws IOException
   */
  public static Template getTemplate(String templateName) throws IOException {

    Template template = null;
    File templateDirectory = ENVIRONMENT.getTemplateDirectory();
    Configuration configuration = ENVIRONMENT.getConfiguration();

    if(templateDirectory != null && templateDirectory.isDirectory()) {
      if(new File(templateDirectory,templateName).exists()) {
        //load template from custom location
        configuration.setDirectoryForTemplateLoading(templateDirectory);
        template = configuration.getTemplate(templateName);
      }
    }

    if(template == null) {
      //custom location not configured. Load from classpath.
      configuration.setClassForTemplateLoading(AnalyzeMojo.class, "templates");
      template = configuration.getTemplate(templateName);
    }

    return template;
  }

}
