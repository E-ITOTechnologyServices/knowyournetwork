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
// Execute required ElasticSearch queries required for the Actionable Management Report
// Script creates per query one dedicated csv File
//

require '/var/kyn/scripts/vendor/autoload.php';

$es_host = ['elasticsearch:9200'];

$elastic_config = \Elasticsearch\ClientBuilder::create();
$elastic_config -> setHosts($es_host);
$elastic = $elastic_config->build();


putenv ( 'http_proxy' );
putenv ( 'https_proxy' );

// Enter Start and End date you want to report
// Start has "gte" as query while end date is "lte"!!!!

$options = getopt("s:e:y:p:");

$startdate = $options["s"];
$enddate = $options["e"];


// define limits of number of destination IP addresses for summary chapter. 
$maxdestsyn = $options["y"]; // Minimum limit IP addresses have been found to communicate or establish connections with more than eg. 300 different destinations
//write value into file for XeLaTex
$file = fopen("csv/100_Summary_of_Security_findings_a1.csv", "w");
$txt = $maxdestsyn . PHP_EOL;

fwrite($file, $txt);
fclose($file);

$maxdestip_ping = $options["p"]; // Minimum limit for number of destination IPs for ping sweeps
//write value into file for XeLaTex
$file = fopen("csv/100_Summary_of_Security_findings_a2.csv", "w");
$txt = $maxdestip_ping . PHP_EOL;

fwrite($file, $txt);
fclose($file);

//Define bucket size for time line queries. LATEX provide 56 stacked lines per graph.
//Therefore interval need to be adapted for periods >= 1 week.
//Calculate hours of report period and device this number by 56.
$dteStart = strtotime($startdate);
$dteEnd   = strtotime($enddate);

if (is_null($dteStart)) {
	echo "Invalid start date, exiting...";
	exit(1);
}
if (is_null($dteEnd)) {
	echo "Invalid end date, exiting...";
	exit(1);
}

$dteDiff = ($dteEnd-$dteStart);
$dteHour = $dteDiff/3600;    // Convert seconds to hours
$dte_Bucket = $dteHour/56;    // Calculate Elastic bucket interval for stacked bars
$dte_Bucket2 = $dteHour/56*2;    // Calculate Elastic bucket interval for parallel bars
$dteBucket = number_format($dte_Bucket,0,'','');
$dteBucket2 = number_format($dte_Bucket2,0,'','');

require "./includes/functions.php";


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Volumetrics
// 020_Volumetrics
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Gathering Volumetric Data" . date("Y-m-d H:i:s") . PHP_EOL;

require "./includes/020_volumetric.php";

require "./includes/020_volumetric_b.php";

require "./includes/020_volumetric_c.php";

require "./includes/020_volumetric_e.php";

require "./includes/020_volumetric_f.php";

echo "End Query Gathering Volumetric Data" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Summary of security findings Network Sweep Restricted Ports (more than destination defined with $maxdestsyn)
// 100_Summary_of_Security_findings_1.csv
// Info: 1-ICMP, 2-IGMP, 6-TCP, 17-UDP, 47-GRE, 89-OSPF, 103-PIM
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Network Sweep Restricted Ports (more than " . $maxdestsyn . " destinations)" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/100_Summary_of_Security_findings_1.php";
echo "End Query Network Sweep Restricted Ports (more than " . $maxdestsyn . " destinations)" . date("Y-m-d H:i:s") . PHP_EOL;




// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Summary of security findings Network Sweep Restricted Ports (more than destination defined with $maxdestsyn)
// 100_Summary_of_Security_findings_2.csv
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query SYN Sweep (more than " . $maxdestsyn . " destinations)" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/100_Summary_of_Security_findings_2.php";
echo "End Query SYN Sweep (more than " . $maxdestsyn . " destinations)" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Summary of security findings Ping Sweep (more than x destinations defined with $maxdestip_ping)
// csv/100_Summary_of_Security_findings_3.csv
// Info: 1-ICMP, 2-IGMP, 6-TCP, 17-UDP, 47-GRE, 89-OSPF, 103-PIM
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Ping Sweeps (more than " . $maxdestip_ping . " destinations)" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/100_Summary_of_Security_findings_3.php";
echo "End Query Ping Sweep (more than " . $maxdestip_ping . " destinations)" . date("Y-m-d H:i:s") . PHP_EOL;



// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Network Sweep Restricted Ports
// 200_Network_Sweep
// Info: 1-ICMP, 2-IGMP, 6-TCP, 17-UDP, 47-GRE, 89-OSPF, 103-PIM
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Network Sweep Restricted Ports" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/200_Network_Sweep.php";
echo "End Query Network Sweep Restricted Ports" . date("Y-m-d H:i:s") . PHP_EOL;



// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Ping Sweeps
// 210_Ping_Sweeps
// Info: 1-ICMP, 2-IGMP, 6-TCP, 17-UDP, 47-GRE, 89-OSPF, 103-PIM
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Ping Sweeps" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/210_Ping_Sweeps.php";
echo "End Query Ping Sweeps" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Top Talker Restricted Ports
// 220_Top_Talker_Restricted_Ports
// Info: 1-ICMP, 2-IGMP, 6-TCP, 17-UDP, 47-GRE, 89-OSPF, 103-PIM
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Top Talker Restricted Ports" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/220_Top_Talker_Restricted_Ports.php";
echo "End Query Top Talker Restricted Ports" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Telnet Server and Unique Hosts 
// 230_Telnet_Server_Unique_Hosts
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Telnet Server and Unique Hosts" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/230_Telnet_Server_Unique_Hosts.php";
echo "End Query Telnet Server and Unique Hosts" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Telnet usage over Time (Graph)
// 232_Telnet_Transferred_Bytes
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Telnet usage over Time" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/232_Telnet_Transferred_Bytes.php";
echo "End Query Telnet usage over Time" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// FTP Server and Unique Hosts 
// 235_FTP_server_unique hosts
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query FTP Server and Unique Hosts" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/235_FTP_server_unique.php";
echo "End Query FTP Server and Unique Hosts" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// FTP usage over Time (Graph)
// 237_FTP_Transferred_Bytes
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query FTP usage over Time" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/237_FTP_Transferred_Bytes.php";
echo "End Query FTP usage over Time" . date("Y-m-d H:i:s") . PHP_EOL;

// --------------------------------------------------------
// Unique count of source IP addresses accessing a single host
// 241_Destination_Accessed_by_Number_of_Sources
// --------------------------------------------------------
echo "Start Query Unique count of IP addresses accessing a single destination " . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/241_Destination_Accessed_by_Number_of_Sources.php";
echo "End Query Unique count of IP addresses accessing a single destination " . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Total number of sessions per protocol
// 260_Total_Sessions_per_protocol
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query total number of sessions per protocol" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/260_Total_Sessions_per_protocol.php";
echo "End Query total number of sessions per protocol" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Protocol Distribution over time (Graph)
// 270_Protocol_Distribution
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Protocol Distribution over time" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/270_Protocol_Distribution.php";
echo "End Query Protocol Distribution over time" . date("Y-m-d H:i:s") . PHP_EOL;



// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Sum of transferred bytes per protocol over time
// 280_Sum_of_Transferred_Bytes_per_Protocol
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Sum of transferred bytes/protocol over time" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/280_Sum_of_Transferred_Bytes_per_Protocol.php";
echo "End Query Query Sum of transferred bytes/protocol over time" . date("Y-m-d H:i:s") . PHP_EOL;


// ----------------------------------------------------------------
// Calculate percentage of resets of the entire sessions seen
// 300_Total_TCP_Sessions_Terminated_with_RST_1
// ----------------------------------------------------------------
echo "Start Calculate percentage of resets of the entire sessions seen " . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/300_Total_TCP_Sessions_Terminated_with_RST_1.php";
echo "End Calculate percentage of resets of the entire sessions seen " . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Total Number of Sessions with Reset over Time
// 300_Total_TCP_Sessions_Terminated_with_RST_2
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Total Session with Reset over Time" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/300_Total_TCP_Sessions_Terminated_with_RST_2.php";
echo "End Query Start Total Session with Reset over Time" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Multiday overview of Sessions Terminated with RST as 3D Graph
// 310_Multiday_View_of_Sessions_Terminated_with_RST.php
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Total Session with Reset over Time 1h steps" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/310_Multiday_View_of_Sessions_Terminated_with_RST.php";
echo "End Query Start Total Session with Reset over Time" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Top 10 Ports with Sessions Resets aggregated by IP address
// 320_Top_10_Ports_with_Session_Resets_aggregated_by_IP_address 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Top 10 Ports with Sessions Reset aggregated by IP address" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/320_Top_10_Ports_with_Session_Resets_aggregated_by_IP_address.php";
echo "End Query Top 10 Ports with Sessions Reset aggregated by IP address" . date("Y-m-d H:i:s") . PHP_EOL;



// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Top 10 Ports with Sessions Reset
// 330_Top_10_Ports_with_Session_Resets
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Top 10 Ports with Sessions Reset" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/330_Top_10_Ports_with_Session_Resets.php";
echo "End  Query Top 10 Ports with Sessions Reset" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Top 10 Destination with Sessions Reset
// 340_Top_10_Destinations_with_Session_Resets
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Top 10 Destinations with Sessions Reset" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/340_Top_10_Destinations_with_Session_Resets.php";
echo "End Query Top 10 Ports with Sessions Reset" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Top 20 Destination TCP and UDP Ports by Traffic Volume in Bytes
// 350_Top_20_Destination_TCP_and_UDP_Ports_by_Traffic_Volume_in_Bytes
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Top 20 Destination TCP and UDP Ports by Traffic Volume in Bytes" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/350_Top_20_Destination_TCP_and_UDP_Ports_by_Traffic_Volume_in_Bytes.php";
echo "End Query Top 10 Destination Ports by Traffic Volume" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Top 20 Source TCP and UDP Ports by Traffic Volume in Bytes
// 350_Top_20_Source_TCP_and_UDP_Ports_by_Traffic_Volume_in_Bytes
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Top 20 Source TCP and UDP Ports by Traffic Volume in Bytes" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/351_Top_20_Source_TCP_and_UDP_Ports_by_Traffic_Volume_in_Bytes.php";
echo "End Query Top 20 Source TCP and UDP Ports by Traffic Volume in Bytes" . date("Y-m-d H:i:s") . PHP_EOL;

// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Top 10 Source by Traffic Volume
// 360_Top_10_Sources_by_Traffic_Volume
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Top 10 Source by Traffic" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/360_Top_10_Sources_by_Traffic_Volume.php";
echo "End Query Top 10 Source by Traffic" . date("Y-m-d H:i:s") . PHP_EOL;

// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Top 10 Source by Number of Connections
// 370_Top_10_Sources_by_Number_of_Connections
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Top 10 Source by Number of Connections" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/370_Top_10_Sources_by_Number_of_Connections.php";
echo "End Query Top 10 Source by Number of Connections" . date("Y-m-d H:i:s") . PHP_EOL;

// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Total Transferred Bytes (Graph)
// 380_Total_Transferred_Bytes
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Total Transferred Bytes" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/380_Total_Transferred_Bytes.php";
echo "End Query Total Transferred Bytes" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Sessions per VLAN (Graph - Stacked lines)
// 390_Sessions_per_VLAN
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Session per VLAN" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/390_Sessions_per_VLAN.php";
echo "End Query Session per VLAN" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Bytes per VLAN (Graph)
// 400_Bytes_per_VLAN
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Bytes per VLAN" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/400_Bytes_per_VLAN.php";
echo "End Query Bytes per VLAN" . date("Y-m-d H:i:s") . PHP_EOL;

// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Source sending SYN only (1)
// 410_Sources_Sending_SYN_Only_1
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Source sending SYN only (1)" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/410_Sources_Sending_SYN_Only_1.php";
echo "End Query Source sending SYN only (1)" . date("Y-m-d H:i:s") . PHP_EOL;

// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Source sending SYN only (2)
// 410_Sources_Sending_SYN_Only_2
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Source sending SYN only (2)" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/410_Sources_Sending_SYN_Only_2.php";
echo "End Query Source sending SYN only (2)" . date("Y-m-d H:i:s") . PHP_EOL;

// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Source sending ICMP Echo Reqeust (1)
// 420_Sources_Sending_ICMP_Echo_Requests_1
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Source sending ICMP Echo Reqeust (1)" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/420_Sources_Sending_ICMP_Echo_Requests_1.php";
echo "End Query Source sending ICMP Echo Reqeust (1)" . date("Y-m-d H:i:s") . PHP_EOL;



// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Source sending ICMP Echo Reqeust (2)
// 420_Sources_Sending_ICMP_Echo_Requests_2
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Source sending ICMP Echo Reqeust (2)" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/420_Sources_Sending_ICMP_Echo_Requests_2.php";
echo "End Query Source sending ICMP Echo Reqeust (2)" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Source sending SNMP get Reqeust (1)
// 430_Source_Sending_SNMP_Get_Requests_1
// Info: 1-ICMP, 2-IGMP, 6-TCP, 17-UDP, 47-GRE, 89-OSPF, 103-PIM
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Source sending SNMP get Request (1)" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/430_Source_Sending_SNMP_Get_Requests_1.php";
echo "End Query Source sending SNMP get Reqeust (1)" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Source sending SNMP get Reqeust (2)
// 430_Source_Sending_SNMP_Get_Requests_2
// Info: 1-ICMP, 2-IGMP, 6-TCP, 17-UDP, 47-GRE, 89-OSPF, 103-PIM
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query Source sending SNMP get Reqeust (2)" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/430_Source_Sending_SNMP_Get_Requests_2.php";
echo "End Query Source sending SNMP get Reqeust (2)" . date("Y-m-d H:i:s") . PHP_EOL;


// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// GRE Tunnel Detection
// 440_GRE_Tunnel_Detection
// Info: 1-ICMP, 2-IGMP, 6-TCP, 17-UDP, 47-GRE, 89-OSPF, 103-PIM
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
echo "Start Query GRE Tunnel Detection" . date("Y-m-d H:i:s") . PHP_EOL;
require "./includes/440_GRE_Tunnel_Detection.php";
echo "End Query GRE Tunnel Detection" . date("Y-m-d H:i:s") . PHP_EOL;
?>

