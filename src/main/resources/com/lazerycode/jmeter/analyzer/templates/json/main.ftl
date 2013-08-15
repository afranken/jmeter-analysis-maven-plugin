<#ftl/>
<#-- @ftlvariable name="self" type="java.util.Map<java.lang.String, com.lazerycode.jmeter.analyzer.parser.AggregatedResponses>" -->
<#-- @ftlvariable name="bytes" type="com.lazerycode.jmeter.analyzer.statistics.Samples" -->
<#-- @ftlvariable name="requests" type="com.lazerycode.jmeter.analyzer.statistics.Samples" -->
{
  "performanceData":
<#if !self?keys?has_content>
  []
<#else>
  [
  <#list self?keys as key>
    <#assign aggregatedResponses=self(key)/>
    <#assign bytes=aggregatedResponses.size/>
    <#assign requests=aggregatedResponses.duration/>
    <#if (requests.successCount > 0) >
    <#include "aggregatedResponse.ftl" />
    </#if>
  </#list>

  ]
</#if>
}