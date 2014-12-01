package com.lazerycode.jmeter.analyzer.writer;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;
import static com.lazerycode.jmeter.analyzer.util.FileUtil.initializeFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.google.common.annotations.VisibleForTesting;
import com.lazerycode.jmeter.analyzer.util.TemplateUtil;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Index
 * {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#requestGroups RequestGroups} as a HTML file
 */
public class HtmlIndexWriter {

    private static final String ROOT_TEMPLATE = "html/index.ftl";

    private String fileName = "index.html";

    /**
     * Render results as text to a file
     *
     * @throws java.io.IOException
     * @throws freemarker.template.TemplateException
     *
     */
    public void write(Resource[] resultDataFiles) throws IOException, TemplateException {
        List<String> tests = new ArrayList<String>(resultDataFiles.length);
        for (Resource resource : resultDataFiles) {
            if(!resource.getFilename().isEmpty() && resource.getFilename().lastIndexOf('.') > -1) {
                tests.add(resource.getFilename().substring(0, resource.getFilename().lastIndexOf('.')));
            }
        }
        Map<String, Object> self = new HashMap<String, Object>();
        self.put("tests", tests);

        java.io.Writer out = getWriter(getFile(fileName));

        renderText(self, getRootTemplate(), out);

        out.flush();
        out.close();
    }

    //--------------------------------------------------------------------------------------------------------------------
    protected File getFile(String name) throws IOException {
        return initializeFile(ENVIRONMENT.getTargetDirectory(), name, null);
    }

    /**
     * @return the relative path to the root Freemarker template
     */

    protected String getRootTemplate() {
        return ROOT_TEMPLATE;
    }

    //--------------------------------------------------------------------------------------------------------------------

    @VisibleForTesting
    protected java.io.Writer getWriter(File file) throws IOException {
        return new FileWriter(file);
    }

    /**
     * Render given {@link com.lazerycode.jmeter.analyzer.parser.AggregatedResponses testResults} as text
     *
     * @param rootTemplate the template that Freemarker starts rendering with
     * @param out         output to write to
     * @throws java.io.IOException
     * @throws freemarker.template.TemplateException
     *
     */
    protected void renderText(Map<String, ?> self, String rootTemplate,
            java.io.Writer out) throws IOException, TemplateException {

        Map<String, Object> rootMap = TemplateUtil.getRootMap(self);
        rootMap.put("SUMMARY_FILE_NAME", fileName);

        Template root = TemplateUtil.getTemplate(rootTemplate);

        // Merge data-model with template
        root.process(rootMap, out);
    }
}
