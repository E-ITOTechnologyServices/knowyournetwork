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



# List all kyn images with the "latest" tag, leaving out the build containers
IMAGES=$(docker images --format "{{.Repository}} {{.Tag}}" | grep "kyn/" | grep "latest" | grep -v "build" | cut -d " " -f 1)


for img in $IMAGES
do
	OUT=`echo "$img.tar.gz" | sed -e "s/\//_/g"`
	echo saving $img to $OUT
	#docker save $img | gzip > $OUT
done
