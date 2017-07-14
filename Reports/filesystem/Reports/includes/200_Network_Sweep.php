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
                "bool": {
                  "should": [
                    {
                      "match": {
                        "Protocol": 6
                      }
                    },
                    {
                      "match": {
                        "DestinationPort": 25
                      }
                    },
                    {
                      "match": {
                        "DestinationPort": 53
                      }
                    },
                    {
                      "match": {
                        "DestinationPort": 80
                      }
                    },
                    {
                      "match": {
                        "DestinationPort": 443
                      }
                    },
                    {
                      "match": {
                        "DestinationPort": 445
                      }
                    },
                    {
                      "match": {
                        "DestinationPort": 3389
                      }
                    },
                    {
                      "match": {
                        "TCPFlags": "....S."
                      }
                    }
                  ],
                  "minimum_should_match": 3,
                  "filter": [
                    {
                      "script": {
                        "script": "doc[\'SourcePort\'].value > doc[\'DestinationPort\'].value"
                      }
                    }
                  ]
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
    "sourceip": {
      "terms": {
        "field": "SourceAddress",
        "size": 20,
        "order": {
          "destip": "desc"
        }
      },
      "aggs": {
        "destip": {
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
$file = fopen("csv/200_Network_Sweep.csv", "w");

    $txt = 'Source IP,Number of Connections' . PHP_EOL;
    foreach ($response['aggregations']['sourceip']['buckets'] as $data) {
          $txt .= $data['key'] . ',' . number_format($data['destip']['value'],0,'','.') . PHP_EOL;
    }


fwrite($file, $txt);
fclose($file);
?>
