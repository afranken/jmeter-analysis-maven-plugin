<#ftl/>
<#-- @ftlvariable name="requests" type="com.lazerycode.jmeter.analyzer.statistics.Samples" -->
<#-- @ftlvariable name="Q_QUANTILES" type="java.lang.Integer" -->
<#-- @ftlvariable name="K_99_PERCENT" type="java.lang.Integer" -->
<#-- @ftlvariable name="K_99_PONT_9_PERCENT" type="java.lang.Integer" -->
        <tr>
          <th colspan="2">Response duration quantiles (ms)</th>
        </tr>
        <#assign quantiles=requests.getQuantiles(Q_QUANTILES)/>
        <#assign x=90/>
        <#list 1..x as i>
          <#if i % 10 == 0>
        <tr>
          <td> ${i}%</td>
          <td>${quantiles.getQuantile(i*10)?string?left_pad(7)}</td>
        </tr>
          </#if>
        </#list>
        <tr>
          <td> 99%</td>
          <td>${quantiles.getQuantile(K_99_PERCENT)?string?left_pad(7)}</td>
        </tr>
        <tr>
          <td>99.9%</td>
          <td>${quantiles.getQuantile(K_99_PONT_9_PERCENT)?string?left_pad(7)}</td>
        </tr>
        <tr>
          <td>100% (max. value)</td>
          <td>${quantiles.getQuantile(Q_QUANTILES)?string?left_pad(7)}</td>
        </tr>
