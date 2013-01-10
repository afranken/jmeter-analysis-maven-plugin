package com.lazerycode.jmeter.analyzer.writer;

import com.lazerycode.jmeter.analyzer.util.TemplateUtil;
import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;

/**
 * Abstract implementation providing a convenience method for Text writers.
 */
public abstract class AbstractTextWriter extends AbstractWriter {

  /**
   * Render given {@link com.lazerycode.jmeter.analyzer.parser.AggregatedResponses testResults} as text
   *
   * @param testResults results to render
   * @param out         output to write to
   * @throws java.io.IOException
   * @throws freemarker.template.TemplateException
   *
   */
  protected void renderText(Map<String, AggregatedResponses> testResults, String template,
                            java.io.Writer out) throws IOException, TemplateException {

    Map<String, Object> rootMap = TemplateUtil.getRootMap(testResults);

    Template root = TemplateUtil.getTemplate(template);

    // Merge data-model with template
    root.process(rootMap, out);
  }

}
