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
  "aggs": {
    "ipaddress": {
      "terms": {
        "field": "DestinationAddress",
        "size": 20,
        "order": {
          "_count": "desc"
        }
      },
      "aggs": {
        "destport": {
          "terms": {
            "field": "DestinationPort",
            "size": 20,
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
    $myresetarray = array();
    $txt = 'IP Address,Port,Amount of Resets' . PHP_EOL;
    foreach ($response['aggregations']['ipaddress']['buckets'] as $data) {
          $p_dip =  $data['key'];
          foreach ($data['destport']['buckets'] as $data) {
              $myresetarray[] = array("IP_Address"=>$p_dip, "Port"=>$data['key'], "Amount"=>$data['doc_count']);
          }
    }

$myresetarray = array_orderby($myresetarray, 'Amount', SORT_DESC);

$file = fopen("csv/320_Top_10_Ports_with_Session_Resets_aggregated_by_IP_address.csv", "w");
    $txt = 'IP Address,Port,Amount of Resets' . PHP_EOL;
    $i = 0;
    foreach ($myresetarray as $data) {
       $txt .= $data['IP_Address'] . ',' . $data['Port'] . ',' .  number_format($data['Amount'],0,'','.') .  PHP_EOL;
       $i=$i+1;
       if ($i>=20)
         break;
    }
fwrite($file, $txt);
fclose($file);


?>
