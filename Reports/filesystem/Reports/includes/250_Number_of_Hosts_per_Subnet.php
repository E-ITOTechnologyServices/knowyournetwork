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
                "bool": {
                    "must_not": [
                    {
                        "term": {
                            "Bytes": {
                                "value": 0
                            }
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
    "aggs": {
        "networks": {
            "histogram": {
                "field": "SourceAddress",
                "min_doc_count": 100,
                "interval": 256,
                "order": {
                    "uniqueips": "desc"
                }
            },
            "aggs": {
                "uniqueips": {
                    "cardinality": {
                        "field": "SourceAddress",
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


$file = fopen("250_Number_of_Hosts_per_Subnet.csv", "w");

$txt = 'Number of Hosts,Class C Network' . PHP_EOL;
$i=0;

foreach ($response['aggregations']['networks']['buckets'] as $data) {
    $txt .= number_format($data['uniqueips']['value'],0,'','.') . ',' . $data['key_as_string'] . PHP_EOL;
    $i += 1;
    if ($i>=20) break 1;
}
fwrite($file, $txt);
fclose($file);

?>
