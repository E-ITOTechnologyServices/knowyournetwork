#!/bin/sh

# Copyright (C) 2017 e-ito Technology Services GmbH
# e-mail: info@e-ito.de

# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

#
# Mysql backup databases script
#
# Usage: db_backup.sh
#
####################################################################################################
#
# send databases compressed to backup folder
#
/usr/bin/mysqldump --password=kyn_RO0T_password --databases user_management_db aql_db | gzip > /backup/backup_`date +%Y%m%d%H%M`.sql.gz
#
# remove all .sql.gz files which are older than 20 days from backup
#
find /backup -name "*.sql.gz" -mtime +20 -exec /bin/rm {} \;
####################################################################################################
