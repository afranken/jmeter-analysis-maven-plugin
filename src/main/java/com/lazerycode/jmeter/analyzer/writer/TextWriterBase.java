package com.lazerycode.jmeter.analyzer.writer;

import com.google.common.annotations.VisibleForTesting;
import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.util.TemplateUtil;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Abstract implementation providing a convenience method for Text writers.
 */
public abstract class TextWriterBase extends WriterBase {

  /**
   * Render results as text to a file
   *
   * @param testResults Map to generate output from
   * @throws java.io.IOException
   * @throws freemarker.template.TemplateException
   *
   */
  @Override
  public void write(Map<String, AggregatedResponses> testResults) throws IOException, TemplateException {
    java.io.Writer out = getWriter(getFile(getFileName()));

    renderText(testResults, getRootTemplate(), out);

    out.flush();
    out.close();
  }

  //--------------------------------------------------------------------------------------------------------------------

  @VisibleForTesting
  protected java.io.Writer getWriter(File file) throws IOException {
    return new FileWriter(file);
  }

  /**
   * @return the relative path to the root Freemarker template
   */
  protected abstract String getRootTemplate();

  /**
   * Render given {@link com.lazerycode.jmeter.analyzer.parser.AggregatedResponses testResults} as text
   *
   * @param testResults results to render
   * @param rootTemplate the template that Freemarker starts rendering with
   * @param out         output to write to
   * @throws java.io.IOException
   * @throws freemarker.template.TemplateException
   *
   */
  protected void renderText(Map<String, ?> testResults, String rootTemplate,
                            java.io.Writer out) throws IOException, TemplateException {

    Map<String, Object> rootMap = TemplateUtil.getRootMap(testResults);
    rootMap.put("SUMMARY_FILE_NAME", fileName);

    Template root = TemplateUtil.getTemplate(rootTemplate);

    // Merge data-model with template
    root.process(rootMap, out);
  }

}
