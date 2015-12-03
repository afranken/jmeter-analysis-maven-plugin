<#ftl/>
<#-- @ftlvariable name="self" type="java.util.Map<java.lang.String, com.lazerycode.jmeter.analyzer.statistics.Samples>" -->
uri;count;total;min;average;max;standarddeviation;persecond;success;errors
<#list self?keys as key>
  <#assign samples=self(key)/>
  <#include "samples.ftl" />
</#list>