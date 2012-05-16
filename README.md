JMeter Result Analysis Plugin
======================

A Maven plugin that parses JMeter result XML files and generates detailed reports with charts

Features
--------
* Text and HTML output of certain statistics (minimum, maximum, average, standard deviation, quantiles) for response duration and response size
* Output is rendered with Freemarker and can be customized
* Chart containing request duration and average of all requests
* CSV file containing durations of all response (by URL)
* CSV file containing sizes of all responses (by URL)
* Statistics and charts can be generated per request group. Request groups are defined by URL patterns.
* Download of resources from remote systems for the JMeter execution interval

Usage Example
-------------

    <plugin>
      <groupId>com.lazerycode.jmeter</groupId>
      <artifactId>jmeter-analyze-maven-plugin</artifactId>
      <executions>
        <execution>
          <goals>
            <goal>analyze</goal>
          </goals>
          <configuration>
            <!--
            source file that contains jmeter result data. Must be XML format or a GZIPed XML format
            -->
            <source>path/to/jmeter.jtl</source>

            <!--
            directory where to store analysis result files. At least the files "summary.txt" and "summary.html" will be stored here.
            -->
            <target>${project.build.directory}/results</target>

            <!--
            Defines groups of requests by URL patterns,
            e.g. URIs starting with /page are associated with group "pages". All analysis results are
            If there is no such mapping then the threadgroups from the jmeter.xml are used.
            -->
            <requestGroups>
              <pages>/page/**</pages>
              <binaries>/binary/**</binaries>
            </requestGroups>

            <!--
            If set to true, additional files "<category>-sizes.csv" and "<category>-durations.csv" will be stored.
            These files contain detailed information for response size and response durations for every URI.
            -->
            <details>true</details>

            <!--
            If set to true, additional chart files "<category>-durations.png" will be created.
            -->
            <charts>true</charts>

            <!--
            Template directory where custom freemarker templates are stored.
            Freemarker templates are used for all generated output. (CSV files, HTML files, console output)
            Templates must be stored in one of the following three subfolders of the templateDirectory:

            csv
            html
            text

            The entry template must be called "main.ftl".

            For example,
            templateDirectory/text/main.ftl will be used for generating the console output.
            -->
            <templateDirectory>${project.basedir}/src/main/resources/</templateDirectory>

            <!--
            Mapping from resource URL to file name. Every resource will be downloaded and stored in 'targetDirectory'
            with the given filename. Tokens "_FROM_" and "_TO_" can be used as placeholders. These placeholders will
            be replaced by timestamps of execution interval (formatted as ISO8601, e.g. '20111216T145509+0100').
            -->
            <remoteResources>
              <property>
                <name>http://yourhost/path?from=_FROM_&amp;to=_TO_</name>
                <value>my_resource.txt</value>
              </property>
            </remoteResources>

          </configuration>
        </execution>
      </executions>
    </plugin>

Example Output
--------------
An analysis summary text output looks like this:


    time: 20111216T145509+0100 - 20111216T145539+0100
    requests:             36049
    requests per second:  1201
    total duration:       30
    response duration (ms)
      min:                0
      average:            0
      max:                1352
      standard deviation: 7
      quantiles (ms)
           10%        0
           20%        0
           30%        0
           40%        0
           50%        1
           60%        1
           70%        1
           80%        1
           90%        1
           99%        6
         99.9%       19
        100.0%     1352 (max. value)
    response size (bytes)
      total:              750210890
      min:                20480
      average:            20810
      max:                53890
      standard deviation: 3308
    response status codes
      200:               36049 (100%)

