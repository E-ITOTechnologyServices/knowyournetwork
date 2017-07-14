<?php

/*
    Copyright (C) 2017 e-ito Technology Services GmbH
    e-mail: info@e-ito.de

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

$json = '{
  "query": {
        "bool": {
          "must": [
            {
              "range": {
                "@timestamp": {
                  "gte": "' . $startdate . '",
                  "lte": "' . $enddate . '"
                }
              }
            }
          ]
        }
  },
  "size": 0,
  "aggs": {
    "totalsession": {
      "date_histogram": {
        "field": "@timestamp",
        "interval": "' . $dteBucket . 'h",
        "time_zone": "Europe/Berlin",
        "min_doc_count": 1,
        "extended_bounds": {
          "min": "' . $startdate . '",
          "max": "' . $enddate . '"
        }
      },
      "aggs": {
        "protocol": {
          "terms": {
            "field": "Protocol",
            "size": 5,
            "order": {
              "_count": "desc"
            }
          }
        }
      }
    }
  }
}';

$params = array(
    'index' => 'kyn-netflow-*',
     'body' => $json
);

$response = $elastic->search($params);


$protocols_tmp = array();
$timestamps_tmp = array();
$output = array();

$data = $response['aggregations']['totalsession']['buckets'];

foreach ($data as $key1 => $value1) {
    $timestamps_tmp[] = $value1['key_as_string'];
    $protocoldict = $value1['protocol']['buckets'];
    foreach ($protocoldict as $key2 => $value2) {
        $protocols_tmp[] = $value2['key'];
    }
}

$protocols = array_unique($protocols_tmp, SORT_NUMERIC);
$timestamps = array_unique($timestamps_tmp, SORT_STRING);

foreach ($timestamps as $key1 => $value1) {
    $output[$value1] = array();

    foreach ($protocols as $key2 => $value2) {
        $output[$value1][$value2] = 0;
    }
}
foreach ($data as $key1 => $value1) {
    $timestamp = $value1['key_as_string'];
    $protocoldict = $value1['protocol']['buckets'];
    foreach ($protocoldict as $key2 => $value2) {
        $protocol = $value2['key'];
        $output[$timestamp][$protocol] = $value2['doc_count'];
    }
}


$file = fopen("pages/270_Protocol_Distribution.tex", "w");
$txt = '\chapter[Protocol Distribution]{\underline{Protocol Distribution}}' . PHP_EOL . 
'\begin{flushleft}'  . PHP_EOL;
//
// Add graph description here
//
$txt .= 'The next figure shows the distribution of protcol usage over time. A significant change in the distribution may indicate problems in the network or application behaviour.'  . PHP_EOL . 
'\end{flushleft}' . PHP_EOL . PHP_EOL;
fwrite($file, $txt);

$txt = '\begin{filecontents}{csv/270_Protocol_Distribution.csv}' . PHP_EOL;
$txt .= 'Zeitstempel';
foreach ($protocols as $key => $value) {
    $txt .= ',' . get_protocol_text($value);
}
$txt .= PHP_EOL;
fwrite($file, $txt);
$txt = "";
foreach ($output as $key1 => $value1) {
    $txt .= date_format(date_create($key1), 'Y-m-d H:i');
    foreach ($output[$key1] as $key2 => $value2) {
        $txt .= ',' . $value2;
    }
    $txt .= PHP_EOL;
}
fwrite($file, $txt);

$txt = '\end{filecontents}' . PHP_EOL . PHP_EOL . 
'\pgfplotstableread[col sep=comma]{csv/270_Protocol_Distribution.csv}\protocoldataA %' . PHP_EOL . 
'\pgfplotsset{compat=newest}' . PHP_EOL . PHP_EOL;

$txt .= '\begin{figure}[h]' . PHP_EOL . 
'\vspace{-0.5cm}' . PHP_EOL . 
'\begin{tikzpicture}[trim left=-3cm]' . PHP_EOL . 
'\begin{axis}[' . PHP_EOL . 
'   ybar stacked,' . PHP_EOL . 
'   scaled ticks=false,' . PHP_EOL . 
'   enlarge x limits=0.02,' . PHP_EOL . 
'   ymajorgrids=true,' . PHP_EOL .
'   y axis line style={opacity = 0},' . PHP_EOL . 
'   height=11cm, width=20cm,' . PHP_EOL . 
'   date coordinates in=x,' . PHP_EOL . 
'   title={},' . PHP_EOL;
fwrite($file, $txt);

$txt = '     legend style=' . PHP_EOL . 
'     {' . PHP_EOL . 
'       at={(1,1)},' . PHP_EOL . 
'       draw=none,' . PHP_EOL . 
'       xshift=0.2cm,' . PHP_EOL . 
'       anchor=north west,' . PHP_EOL . 
'       nodes=right,' . PHP_EOL . 
'       font=\tiny' . PHP_EOL . 
'     },' . PHP_EOL . 
'     ylabel={\small{Protocol Distribution}},' . PHP_EOL . 
'     xticklabel style= {rotate=20,anchor=north east,font=\tiny},' . PHP_EOL . 
'     xticklabel={\day.\month.\year\ \hour:\minute},' . PHP_EOL . 
'     y tick label style={/pgf/number format/fixed, /pgf/number format/precision=5, /pgf/number format/1000 sep=., font=\tiny },' . PHP_EOL . 
'     bar width=6pt]' . PHP_EOL;
fwrite($file, $txt);

$i = 1;
$txt = "";
foreach ($protocols as $key => $value) {
   $m_protocol=get_protocol_text($value);
   $txt .= '\addlegendentry{' . $m_protocol . '}' . PHP_EOL;
   $txt .= '\addplot [fill=RStack' . $i . '] table[x={Zeitstempel}, y=' . $m_protocol . '] from {\protocoldataA};' . PHP_EOL;
   $i = $i + 1;
}
fwrite($file, $txt);

$txt = '\end{axis}' . PHP_EOL . 
'\end{tikzpicture}' . PHP_EOL . 
'\caption[\normalsize{Protocol Distribution by Connection Count}]{\small{Protocol Distribution by Connection Count}}' . PHP_EOL . 
'\end{figure}' . PHP_EOL;
fwrite($file, $txt);

fclose($file);

?>
