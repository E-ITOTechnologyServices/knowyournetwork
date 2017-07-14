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
// Start and Enddate of data collection
//

$file = fopen("csv/020_volumetric_c.csv", "w");
$s_date = substr($startdate,0,10) . PHP_EOL;

fwrite($file, $s_date);
fclose($file);

// Startdate
$file = fopen("csv/000_title_startdate.csv", "w");
$st_date = date_create($s_date);
$txt = date_format($st_date, 'd.m.Y') . PHP_EOL;

fwrite($file, $txt);
fclose($file);

$file = fopen("csv/020_volumetric_d.csv", "w");
$e_date = substr($enddate,0,10) . PHP_EOL;

fwrite($file, $e_date);
fclose($file);

//Enddate
$file = fopen("csv/000_title_enddate.csv", "w");
$end_date = date_create($e_date);
$txt = date_format($end_date, 'd.m.Y') . PHP_EOL;

fwrite($file, $txt);
fclose($file);

 ?>
