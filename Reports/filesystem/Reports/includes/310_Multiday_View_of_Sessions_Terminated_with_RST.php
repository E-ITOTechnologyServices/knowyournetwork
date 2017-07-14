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
            },
            {
              "wildcard": {
                "TCPFlags": {
                  "value": "*R*"
                }
              }
            }
          ]
        }
  },
  "size": 0,
  "aggs": {
    "resets": {
      "date_histogram": {
        "field": "@timestamp",
        "interval": "1h",
        "min_doc_count": 0,
        "extended_bounds": {
          "min": "' . $startdate . '",
          "max": "' . $enddate . '"
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

$file = fopen("csv/310_Multiday_View_of_Sessions_Terminated_with_RST.csv", "w");
//    $txt = 'hour,day,count' . PHP_EOL;
    $txt = '';
    foreach ($response['aggregations']['resets']['buckets'] as $data) {
         $txt .= date_format(date_create($data['key_as_string']), 'G'). ' ' . date_format(date_create($data['key_as_string']), 'Y-m-d') . ' ' . $data['doc_count'] . PHP_EOL;
         if (date_format(date_create($data['key_as_string']), 'G') >= 23) {
            $txt .= PHP_EOL;
         }
    }
// 3D Plot requires empty line at the end of table. In case time line end prior hour 23 and additional EOL is needed.
    if (date_format(date_create($data['key_as_string']), 'G') < 23) {
            $txt .= PHP_EOL;
         }

fwrite($file, $txt);
fclose($file);


?>
