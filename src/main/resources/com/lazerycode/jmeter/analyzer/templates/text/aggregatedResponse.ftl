<#ftl/>
<#setting locale="en_US">
<#-- @ftlvariable name="key" type="java.lang.String" -->
<#-- @ftlvariable name="aggregatedResponses" type="com.lazerycode.jmeter.analyzer.parser.AggregatedResponses" -->
<#-- @ftlvariable name="bytes" type="com.lazerycode.jmeter.analyzer.statistics.Samples" -->
<#-- @ftlvariable name="requests" type="com.lazerycode.jmeter.analyzer.statistics.Samples" -->
<#-- @ftlvariable name="Q_QUANTILES" type="java.lang.Integer" -->
<#-- @ftlvariable name="K_99_PERCENT" type="java.lang.Integer" -->
<#-- @ftlvariable name="K_99_PONT_9_PERCENT" type="java.lang.Integer" -->
<#-- @ftlvariable name="PERCENT_100" type="java.lang.Integer" -->
Group: ${key}
  time: ${aggregatedResponses.startDate?date?string} - ${aggregatedResponses.endDate?date?string}
  total duration:       ${requests.duration}
  requests:             ${requests.successCount + requests.errorsCount}
  requests per second:  ${requests.successPerSecond}
  failed requests:      ${requests.errorsCount}
  <#if (requests.successCount > 0) >
  response duration (ms)
    min:                ${requests.min}
    average:            ${requests.average}
    max:                ${requests.max}
    standard deviation: ${requests.standardDeviation}
    <#include "quantiles.ftl" />
  response size (bytes)
    total:              ${bytes.total}
    min:                ${bytes.min}
    average:            ${bytes.average}
    max:                ${bytes.max}
    standard deviation: ${bytes.standardDeviation}
  <#assign statusCodes=aggregatedResponses.statusCodes.codes/>
  <#assign total=requests.successCount + requests.errorsCount/>
  response status codes
    <#list statusCodes?keys as statusCode>
    <#assign statusCodeCount=statusCodes(statusCode)/>
    ${statusCode?string}:             ${statusCodeCount?string?left_pad(7)} (${(statusCodeCount/total*PERCENT_100)?string("###.##")}%)
    </#list>
  <#else>
  errors:               100%
  </#if>