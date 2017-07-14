<?php

// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Volumetrics
// 020_Volumetrics
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

echo "Start Query Gathering Volumetric Data" . date("Y-m-d H:i:s") . PHP_EOL;


//
// Number of unique source ip addresses
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
                "field": "SourceAddress",
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

$unique_srcips = $response['aggregations']['uniqueips']['value'];

$file = fopen("csv/020_volumetric_a.csv", "w");
$txt = number_format($unique_srcips,0,'','.') . PHP_EOL;

fwrite($file, $txt);
fclose($file);

?>