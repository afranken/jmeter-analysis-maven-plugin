<#ftl/>
<#-- @ftlvariable name="self" type="java.util.Map<java.lang.String, com.lazerycode.jmeter.analyzer.statistics.Samples>" -->
<html>
<head>
  <title>Detailed Results</title>
  <style type="text/css">
    body {
      font: normal 68% verdana, arial, helvetica;
      color: #000000;
    }

    table tr td, table tr th {
      font-size: 68%;
    }

    table tr th {
      font-weight: bold;
      text-align: left;
      background: #a6caf0;
      white-space: nowrap;
    }

    table tr td {
      background: #eeeee0;
      white-space: nowrap;
    }

    h1 {
      margin: 0 0 5px;
      font: 165% verdana, arial, helvetica
    }

    h2 {
      margin-top: 1em;
      margin-bottom: 0.5em;
      font: bold 125% verdana, arial, helvetica
    }

    h3 {
      margin-bottom: 0.5em;
      font: bold 115% verdana, arial, helvetica
    }

    img {
      border-width: 0;
    }

    div {
      margin-bottom: 20px;
    }

    div.details ul li {
      list-style: none;
    }

  </style>
</head>
<body>
  <h1>JMeter Summary</h1>

  <table cellpadding="5" cellspacing="2">
    <tr>
      <th>uri</th>
      <th>count</th>
      <th>total</th>
      <th>min</th>
      <th>average</th>
      <th>max</th>
      <th>standarddeviation</th>
      <th>persecond</th>
      <th>success</th>
      <th>errors</th>
    </tr>
    <#list self?keys as key>
    <tr>
      <#assign samples=self(key)/>
      <#include "samples.ftl" />
    </tr>
    </#list>
  </table>
</body>
</html>