<#ftl/>
<#-- @ftlvariable name="self" type="java.util.Map<java.lang.String, com.lazerycode.jmeter.analyzer.parser.AggregatedResponses>" -->
<#-- @ftlvariable name="bytes" type="com.lazerycode.jmeter.analyzer.statistics.Samples" -->
<#-- @ftlvariable name="requests" type="com.lazerycode.jmeter.analyzer.statistics.Samples" -->
<#list self?keys as key>
  <#assign aggregatedResponses=self(key)/>
  <#assign bytes=aggregatedResponses.size/>
  <#assign requests=aggregatedResponses.duration/>
  <#include "aggregatedResponse.ftl" />
</#list>