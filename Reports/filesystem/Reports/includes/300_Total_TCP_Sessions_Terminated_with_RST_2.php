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
        "interval": "' . $dteBucket . 'h",
        "min_doc_count": 1,
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
$file = fopen("csv/300_Total_TCP_Sessions_Terminated_with_RST_b.csv", "w");

    $txt = 'Date,Count' . PHP_EOL;
    foreach ($response['aggregations']['resets']['buckets'] as $data) {
         $txt .= date_format(date_create($data['key_as_string']), 'Y-m-d H:i') . ',' . $data['doc_count'] . PHP_EOL;
    }


fwrite($file, $txt);
fclose($file);


?>
