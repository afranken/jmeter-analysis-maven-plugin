<#ftl/>
<#setting locale="en_US">
<#setting url_escaping_charset='UTF-8'>
<#-- @ftlvariable name="key" type="java.lang.String" -->
<#-- @ftlvariable name="aggregatedResponses" type="com.lazerycode.jmeter.analyzer.parser.AggregatedResponses" -->
<#-- @ftlvariable name="bytes" type="com.lazerycode.jmeter.analyzer.statistics.Samples" -->
<#-- @ftlvariable name="requests" type="com.lazerycode.jmeter.analyzer.statistics.Samples" -->
<#-- @ftlvariable name="Q_QUANTILES" type="java.lang.Integer" -->
<#-- @ftlvariable name="K_99_PERCENT" type="java.lang.Integer" -->
<#-- @ftlvariable name="K_99_PONT_9_PERCENT" type="java.lang.Integer" -->
<#-- @ftlvariable name="PERCENT_100" type="java.lang.Integer" -->
<#-- @ftlvariable name="CHARTS" type="java.lang.Boolean" -->
<#-- @ftlvariable name="DETAILS" type="java.lang.Boolean" -->
<#-- @ftlvariable name="SUMMARY_FILE_NAME" type="java.lang.String" -->
    <h2>Group: ${key}</h2>
    <div class="aggregation">
      <h3>Summary</h3>
      <table>
        <tr>
          <th colspan="2">General</th>
        </tr>
        <tr>
          <td>Time</td>
          <td>${aggregatedResponses.startDate?date?string} - ${aggregatedResponses.endDate?date?string}</td>
        </tr>
        <tr>
          <td>Duration in seconds</td>
          <td>${requests.duration}</td>
        </tr>
        <tr>
          <td>Number of requests</td>
          <td>${requests.successCount + requests.errorsCount}</td>
        </tr>
        <tr>
          <td>Requests per second</td>
          <td>${requests.successPerSecond}</td>
        </tr>
        <tr>
          <td>Failed requests</td>
          <td>${requests.errorsCount}</td>
        </tr>

        <#if (requests.successCount > 0) >
        <tr>
          <th colspan="2">Response duration (ms)</th>
        </tr>
        <tr>
          <td>Min</td>
          <td>${requests.min}</td>
        </tr>
        <tr>
          <td>Average</td>
          <td>${requests.average}</td>
        </tr>
        <tr>
          <td>Max</td>
          <td>${requests.max}</td>
        </tr>
        <tr>
          <td>Standard deviation</td>
          <td>${requests.standardDeviation}</td>
        </tr>

        <#include "quantiles.ftl" />

        <tr>
          <th colspan="2">Response size (bytes)</th>
        </tr>
        <tr>
          <td>Total</td>
          <td>${bytes.total}</td>
        </tr>
        <tr>
          <td>Min</td>
          <td>${bytes.min}</td>
        </tr>
        <tr>
          <td>Average</td>
          <td>${bytes.average}</td>
        </tr>
        <tr>
          <td>Max</td>
          <td>${bytes.max}</td>
        </tr>
        <tr>
          <td>Standard deviation</td>
          <td>${bytes.standardDeviation}</td>
        </tr>


        <tr>
          <th colspan="2">Status codes</th>
        </tr>

        <#assign statusCodes=aggregatedResponses.statusCodes.codes/>
        <#assign total=requests.successCount + requests.errorsCount/>
          <#list statusCodes?keys as statusCode>
            <#assign statusCodeCount=statusCodes(statusCode)/>
        <tr>
          <td>${statusCode?string}</td>
          <td>${statusCodeCount?string?left_pad(7)} (${(statusCodeCount/total*PERCENT_100)?string("###.##")}%)</td>
        </tr>
          </#list>
        <#else>
          <tr>
            <td>Errors</td>
            <td>100%</td>
          </tr>
        </#if>
      </table>
    </div>

    <#if CHARTS >
    <div class="center">
      <div class="images">
        <img src="${key?url?url}-throughput-${SUMMARY_FILE_NAME?url?url}.png" tooltip="Throughput and threads count"/>
      </div>
      <div class="images">
        <img src="${key?url?url}-durations-${SUMMARY_FILE_NAME?url?url}.png" tooltip="Durations"/>
      </div>
      <div class="images">
        <img src="${key?url?url}-response_times-${SUMMARY_FILE_NAME?url?url}.png" tooltip="Response times distribution and percentiles"/>
      </div>
    </div>
    </#if>
    <#if DETAILS>
    <div class="details">
      <h3>Detailed response information</h3>
      <ul>
        <li><a href="${key?url?url}-durations-${SUMMARY_FILE_NAME?url?url}.html">Response durations per URL (HTML)</a></li>
        <li><a href="${key?url?url}-sizes-${SUMMARY_FILE_NAME?url?url}.html">Response sizes per URL (HTML)</a></li>
      </ul>
    </div>
    </#if>
