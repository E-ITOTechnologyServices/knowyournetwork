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

// ---------------
// Query total number of sessions within start and end date
// ----------------------------------------------------------------


$json = '{
  "query": {
    "range": {
      "@timestamp": {
        "gte": "' . $startdate . '",
        "lte": "' . $enddate . '"
      }
    }
  },
  "size": 0
}';


$params = array(
    'index' => 'kyn-netflow-*',
     'body' => $json
);

$response = $elastic->search($params);

$total_sessions = $response['hits']['total'];


// ----------------------------------------
// Query number of sessions with reset flag set
// ----------------------------------------


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
  }
}';


$params = array(
    'index' => 'kyn-netflow-*',
     'body' => $json
);

$response = $elastic->search($params);

$total_resets = $response['hits']['total'];

if($total_sessions == 0)
	{
		$percentage=0;
	}
else
	{
		$percentage=$total_resets/$total_sessions*100;
	}

$file = fopen("csv/300_Total_TCP_Sessions_Terminated_with_RST_a.csv", "w");
$txt = 'Sessions,Percentage' . PHP_EOL;
 $txt .= number_format($total_resets,0,'','.') . ',{' . number_format($percentage,2,',','.') . '}' . PHP_EOL;

fwrite($file, $txt);
fclose($file);

?>
