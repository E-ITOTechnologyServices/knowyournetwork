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
  "size": 0,
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
  "aggs": {
    "protocols": {
      "terms": {
        "field": "Protocol",
        "size": 7
      }
    }
  }
}';


$params = array(
    'index' => 'kyn-netflow-*',
     'body' => $json
);

$response = $elastic->search($params);
$file = fopen("csv/260_Total_Sessions_per_protocol.csv", "w");

    $txt = 'Protocol,Sessions' . PHP_EOL;
    foreach ($response['aggregations']['protocols']['buckets'] as $data) {
         $txt .= get_protocol_text($data['key']) . ',' . number_format($data['doc_count'],0,'','.') . PHP_EOL;
    }

fwrite($file, $txt);
fclose($file);


?>
