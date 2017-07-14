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

// Function is used to sort multicolumn arrays
function array_orderby()
{
    $args = func_get_args();
    $data = array_shift($args);
    foreach ($args as $n => $field) {
        if (is_string($field)) {
            $tmp = array();
            foreach ($data as $key => $row)
                $tmp[$key] = $row[$field];
            $args[$n] = $tmp;
            }
    }
    $args[] = &$data;
    call_user_func_array('array_multisort', $args);
    return array_pop($args);
}

function get_protocol_text($protocol_id)
{
	$m_protocol="";
    switch($protocol_id){
       case 1:
         $m_protocol = "ICMP";
         break;
       case 2:
         $m_protocol = "IGMP";
         break;
       case 6:
         $m_protocol = "TCP";
         break;
       case 8:
         $m_protocol = "EGP";
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
    return $m_protocol;	
}
?>
