<#ftl/>
<#-- @ftlvariable name="requests" type="com.lazerycode.jmeter.analyzer.statistics.Samples" -->
<#-- @ftlvariable name="Q_QUANTILES" type="java.lang.Integer" -->
<#-- @ftlvariable name="K_99_PERCENT" type="java.lang.Integer" -->
<#-- @ftlvariable name="K_99_PONT_9_PERCENT" type="java.lang.Integer" -->
    quantiles (ms)
    <#assign quantiles=requests.getQuantiles(Q_QUANTILES)/>
    <#assign x=90/>
    <#list 1..x as i>
      <#if i % 10 == 0>
         ${i}%  ${quantiles.getQuantile(i*10)?string?left_pad(7)}
      </#if>
    </#list>
         99%  ${quantiles.getQuantile(K_99_PERCENT)?string?left_pad(7)}
       99.9%  ${quantiles.getQuantile(K_99_PONT_9_PERCENT)?string?left_pad(7)}
      100.0%  ${quantiles.getQuantile(Q_QUANTILES)?string?left_pad(7)} (max. value)
