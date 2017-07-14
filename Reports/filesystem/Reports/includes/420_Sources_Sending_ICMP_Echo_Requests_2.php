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
                "match": {
                  "Protocol": {
                    "query": 1,
                    "type": "phrase"
                  }
                }
            },
            {
                "match": {
                  "ICMPType": {
                    "query": 8,
                    "type": "phrase"
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
    "ipaddress": {
      "terms": {
        "field": "SourceAddress",
        "size": 10,
        "order": {
          "dest_count": "desc"
        }
      },
      "aggs": {
        "dest_count": {
          "cardinality": {
            "field": "DestinationAddress",
            "precision_threshold": 10000
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
$file = fopen("csv/420_Sources_Sending_ICMP_Echo_Requests_b.csv", "w");
// Check if Elasticsearch was able to provide any result
if (isset($response['aggregations']['ipaddress']['buckets'][0]['key'])) {
    $txt = 'Source IP Address,Destination IP Addresses' . PHP_EOL;
    foreach ($response['aggregations']['ipaddress']['buckets'] as $data) {
         $txt .= $data['key'] . ',' . number_format($data['dest_count']['value'],0,'','.') . PHP_EOL;
    }
} else {
    $txt = 'Source IP Address,Destination IP Addresses' . PHP_EOL;
    $txt .= "n/a,n/a" . PHP_EOL;
}


fwrite($file, $txt);
fclose($file);

?>
