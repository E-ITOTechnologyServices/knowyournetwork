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
    "protocol": {
      "terms": {
        "field": "Protocol",
        "size": 2,
        "order": {
          "bytes": "desc"
        }
      },
      "aggs": {
        "bytes": {
          "sum": {
            "field": "Bytes"
          }
        },
        "sourceport": {
          "terms": {
            "field": "DestinationPort",
            "size": 10,
            "order": {
              "bytes": "desc"
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
    }
  }
}';


$params = array(
    'index' => 'kyn-netflow-*',
     'body' => $json
);

$response = $elastic->search($params);
$file = fopen("csv/350_Top_20_Destination_TCP_and_UDP_Ports_by_Traffic_Volume_in_Bytes.csv", "w");

    $txt = 'Protocol,Destination Port,Sum (bytes)' . PHP_EOL;
    foreach ($response['aggregations']['protocol']['buckets'] as $data) {
         switch($data['key']){
           case 1:
             $m_protocol = "ICMP";
             break;
           case 2:
             $m_protocol = "IGMP";
             break;
           case 6:
             $m_protocol = "TCP";
             break;
           case 17:
             $m_protocol = "UDP";
             break;
           case 41:
             $m_protocol = "IPv6 Encapsulation";
             break;
           case 47:
             $m_protocol = "GRE";
             break;
           case 50:
             $m_protocol = "ESP";
             break;
           case 88:
             $m_protocol = "EIGRP";
             break;
           case 89:
             $m_protocol = "OSPF";
             break;
           case 103:
             $m_protocol = "PIM";
             break;
           case 112:
             $m_protocol = "VRRP";
             break;
           case 139:
             $m_protocol = "HIP";
             break;
           default:
             $m_protocol = "Others";
         }
         foreach ($data['sourceport']['buckets'] as $data) {
              $txt .= $m_protocol . ',' . $data['key'] . ',' . number_format($data['bytes']['value'],0,'','.') . PHP_EOL;
         }
    }

fwrite($file, $txt);
fclose($file);


?>
