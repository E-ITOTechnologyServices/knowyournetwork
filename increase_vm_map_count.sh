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


MAX_MAP_COUNT=262144

echo -n "setting vm.max_map_count to $MAX_MAP_COUNT..."

sudo sysctl -w vm.max_map_count=$MAX_MAP_COUNT
if [ $? -eq 0 ]; then
    echo "done."
else
    echo "FAIL!"
fi
