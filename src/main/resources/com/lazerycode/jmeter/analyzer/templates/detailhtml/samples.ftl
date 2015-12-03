<#ftl/>
<#-- @ftlvariable name="key" type="java.lang.String" -->
<#-- @ftlvariable name="samples" type="com.lazerycode.jmeter.analyzer.statistics.Samples" -->
<td>"${key}"</td><td>${samples.successCount + samples.errorsCount}</td><td>${samples.total}</td><td>${samples.min}</td><td>${samples.average}</td><td>${samples.max}</td><td>${samples.standardDeviation}</td><td>${samples.successPerSecond}</td><td>${samples.successCount}</td><td>${samples.errorsCount}</td>
