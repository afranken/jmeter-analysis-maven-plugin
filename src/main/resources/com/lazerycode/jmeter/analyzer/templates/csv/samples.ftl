<#ftl/>
<#-- @ftlvariable name="key" type="java.lang.String" -->
<#-- @ftlvariable name="samples" type="com.lazerycode.jmeter.analyzer.statistics.Samples" -->
"${key}";${samples.successCount};${samples.total};${samples.min};${samples.average};${samples.max};${samples.standardDeviation};${samples.successPerSecond};${samples.errorsCount}
