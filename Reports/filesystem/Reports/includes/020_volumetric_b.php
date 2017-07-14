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

//
// Number of unique destination ip addresses
//

$json = '{
  "query": {
    "range": {
      "@timestamp": {
        "gte": "' . $startdate . '",
        "lte": "' . $enddate . '"
      }
    }
  },
  "size": 0,
  "aggs": {
    "uniqueips": {
      "cardinality": {
        "field": "DestinationAddress",
        "precision_threshold": 10000
      }
    }
  }
}';

$params = array(
    'index' => 'kyn-netflow-*',
     'body' => $json
);

$response = $elastic->search($params);

$unique_dstips = $response['aggregations']['uniqueips']['value'];

$file = fopen("csv/020_volumetric_b.csv", "w");
$txt = number_format($unique_dstips,0,'','.') . PHP_EOL;

fwrite($file, $txt);
fclose($file);


?>
