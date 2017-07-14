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
          "match": {
            "Protocol": {
              "query": 6,
              "type": "phrase"
            }
          }
        },
        {
          "term": {
            "DestinationPort": {
              "value": "21"
            }
          }
        },
        {
          "range": {
            "InputPackets": {
              "gte": "21"
            }
          }
        },
        {
          "range": {
            "@timestamp": {
              "gte": "' . $startdate . '",
              "lte": "' . $enddate . '"
            }
          }
        },
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
    "totalbytes": {
      "date_histogram": {
        "field": "@timestamp",
        "interval": "' . $dteBucket2 . 'h",
        "time_zone": "Europe/Berlin",
        "min_doc_count": 0,
        "extended_bounds": {
          "min": "' . $startdate . '",
          "max": "' . $enddate . '"
        }
      },
      "aggs": {
        "bytes": {
          "sum": {
            "field": "Bytes"
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

$params = array(
    'index' => 'kyn-netflow-*',
     'body' => $json
);

$response = $elastic->search($params);
$file = fopen("csv/237_FTP_Transferred_Bytes.csv", "w");

    $txt = 'Date,Bytes' . PHP_EOL;
    foreach ($response['aggregations']['totalbytes']['buckets'] as $data) {
         $txt .= date_format(date_create($data['key_as_string']), 'Y-m-d H:i') . ',' . $data['bytes']['value'] . PHP_EOL;
    }


fwrite($file, $txt);
fclose($file);

 ?>
