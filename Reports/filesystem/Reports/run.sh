#!/bin/bash

#    Copyright (C) 2017 e-ito Technology Services GmbH
#    e-mail: info@e-ito.de
#
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.

LOG=/out/report.log
STARTDATE_HANDED_OVER=${1:-$(date -dlast-monday-7day --iso-8601=seconds)}
ENDDATE_HANDED_OVER=${2:-$(date -dlast-sunday+23hours+59minutes+59seconds --iso-8601=seconds)}
STARTDATE=$(date --date="$STARTDATE_HANDED_OVER" +%Y-%m-%dT00:00:00+0000 )
ENDDATE=$(date --date="$ENDDATE_HANDED_OVER" +%Y-%m-%dT23:59:59+0000 )

echo "[Report] Started at $(date)" > $LOG
echo "[Report] Arguments:" >> $LOG
echo "[Report] 1: $1" >> $LOG
echo "[Report] 2: $2" >> $LOG
echo "[Report] 3: $3" >> $LOG
echo "[Report] 4: $4" >> $LOG
echo "[Report] 5: $5" >> $LOG


mkdir -v ./csv >> $LOG
if [ -z $? ]; then
    echo "[Report] Error while creating temporary directory ./csv" >> $LOG
    exit $?
fi

MIN_IP_ADD=${3:-5}
MIN_DEST_IP=${4:-6}
report_name=${5:-"no-name-report.pdf"}


#running php-script to create csv- and tex-files
echo "[Report] Calling php actmanrep.php -s $STARTDATE -e $ENDDATE -y $MIN_IP_ADD -p $MIN_DEST_IP" >> $LOG
php actmanrep.php -s $STARTDATE -e $ENDDATE -y $MIN_IP_ADD -p $MIN_DEST_IP > /out/actmanrep.log
if [ ${PIPESTATUS[0]} -ne 0 ]
then
        echo "[Report] Error while gathering data: Non-zero exit code" >> $LOG
        exit ${PIPESTATUS[0]}
fi
echo "[Report] actmanrep.php done." >> $LOG

#running latex to create pdf
xelatex -file-line-error -halt-on-error report.tex | tee /out/xelatex_1.log | egrep ".*:[0-9]*:.*|LaTeX Warning:"
if [ ${PIPESTATUS[0]} -ne 0 ]
then
        echo "[Report] Error during first run of xelatex: Non-zero exit code" >> $LOG
        exit ${PIPESTATUS[0]}
fi
echo "[Report] xelatex stage 1/2 done." >> $LOG

#second run of latex to create table of contents
xelatex -file-line-error -halt-on-error report.tex | tee /out/xelatex_2.log |  egrep ".*:[0-9]*:.*|LaTeX Warning:"
if [ ${PIPESTATUS[0]} -ne 0 ]
then
        echo "[Report] Error during second run of xelatex: Non-zero exit code" >> $LOG
        exit ${PIPESTATUS[0]}
fi
echo "[Report] xelatex stage 2/2 done." >> $LOG

if [ -r ./report.pdf ]
then
        echo "[Report] Report file created" >> $LOG
else
        echo "[Report] ERROR: Report file not created" >> $LOG
fi

#move the report.pdf file
mv ./report.pdf /out/"${report_name}"

echo "[Report] run.sh done. exit." >> $LOG
