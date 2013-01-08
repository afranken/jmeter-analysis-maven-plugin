JMeter Result Analysis Plugin
======================

A Maven plugin that parses [JMeter][2] result XML files and generates detailed reports with charts

Can be used in combination with the [JMeter Maven Plugin][1] that is developed by the same authors

Features
--------
* Text and HTML output of certain statistics (minimum, maximum, average, standard deviation, quantiles) for response duration and response size
* Output is rendered with [Freemarker][4] and can be customized
* Chart containing request duration and average of all requests
* CSV file containing durations of all response (by URL)
* CSV file containing sizes of all responses (by URL)
* Statistics and charts can be generated per request group. Request groups are defined by URL patterns.
* Download of resources from remote systems for the [JMeter][2] execution interval

Build status
------------
Buildhive CI:
[![Build Status](https://buildhive.cloudbees.com/job/afranken/job/jmeter-analysis-maven-plugin/badge/icon)](https://buildhive.cloudbees.com/job/afranken/job/jmeter-analysis-maven-plugin/)

Help
----
Run the HelpMojo of this plugin:
`mvn jmeter-analysis:help -Ddetail=true -Dgoal=analyze`

Usage Example
-------------

    <plugin>
      <groupId>com.lazerycode.jmeter</groupId>
      <artifactId>jmeter-analysis-maven-plugin</artifactId>
      <executions>
        <execution>
          <goals>
            <goal>analyze</goal>
          </goals>
          <configuration>
            <!--
            An AntPath-Style pattern matching a JMeter XML result file to analyze. Must be a fully qualified path.
            File may be GZiped, must end in .gz then.

            Default: not set.
            Required.
            -->
            <source>${project.build.directory}/**/*.jtl</source>

            <!--
            directory where to store analysis result files.

            Default: ${project.build.directory}
            Required.
            -->
            <targetDirectory>${project.build.directory}/results</targetDirectory>

            <!--
            Request groups as a mapping from "group name" to "ant pattern".
            A request uri that matches an ant pattern will be associated with the group name.
            Request details, charts and CSV files are generated per requestGroup.

            If not set, the threadgroup name of the request will be used.

            Default: not set.
            -->
            <requestGroups>
              <pages>/page/**</pages>
              <binaries>/binary/**</binaries>
            </requestGroups>

            <!--
            If set to true, additional files "<category>-sizes.csv" and "<category>-durations.csv" will be stored.
            These files contain detailed information for response size and response durations for every URI.

            Default: true
            -->
            <generateCSVs>true</generateCSVs>

            <!--
            If set to true, additional chart files "<category>-durations.png" will be created.

            Default: true
            -->
            <generateCharts>true</generateCharts>

            <!--
            If set to true, the directory structure relative to source will be preserved during output.

            Default: false
            -->
            <preserveDirectories>false</preserveDirectories>

            <!--
            Set<String> of sample names that should be processed when analysing a results file.

            Default: sample, httpSample
            -->
            <sampleNames>
              <sampleName>sample</sampleName>
              <sampleName>httpSample</sampleName>
            </sampleNames>

            <!--
            If set to true will process all files found by the pattern defined in <source>.
            If set to false (the default) it will only process the first file found.

            *SETTING TO TRUE IS NOT RECOMMENDED*
            It can substantially impact performance, if you do this you do it at your own risk!

            Default: false
            -->
            <processAllFilesFound>false</processAllFilesFound>

            <!--
            Template directory where custom freemarker templates are stored.
            Freemarker templates are used for all generated output. (CSV files, HTML files, console output)
            Templates must be stored in one of the following three subfolders of the templateDirectory:

            csv, html, text

            The entry template must be called "main.ftl".

            For example,
            templateDirectory/text/main.ftl will be used for generating the console output.

            Default: not set.
            -->
            <templateDirectory>${project.basedir}/src/main/resources/</templateDirectory>

            <!--
            Mapping from resource URL to file name. Every resource will be downloaded and stored in 'targetDirectory'
            with the given filename. Tokens "_FROM_" and "_TO_" can be used as placeholders. These placeholders will
            be replaced by timestamps of execution interval (formatted as ISO8601, e.g. '20111216T145509+0100').

            Default: not set.
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

Credits
--------------

Part of the development of this plugin is sponsored by [CoreMedia][3]

[1]:    http://jmeter.lazerycode.com                                "JMeter Maven Plugin"
[2]:    http://jakarta.apache.org/jmeter/                           "JMeter"
[3]:    http://www.coremedia.com                                    "CoreMedia AG"
[4]:    http://freemarker.sourceforge.net/                          "Freemarker"