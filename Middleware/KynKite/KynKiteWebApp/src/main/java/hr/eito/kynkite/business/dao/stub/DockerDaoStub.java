
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


package hr.eito.kynkite.business.dao.stub;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import hr.eito.kynkite.business.dao.DockerDao;
import hr.eito.kynkite.business.dao.FileSystemDao;

/**
 * Stubbed out implementation of Docker client
 *
 * @author Hrvoje
 */
@Component
@Profile("test")
public class DockerDaoStub implements DockerDao {
	
	@Autowired
	FileSystemDao fileSystemDao;
	
	/**
	 * Stub out the creating of Docker report container
	 * 
	 * @param containerName name of the container
	 * @param mountPoint location in container to mount data
	 * @param imageName name of the image
	 * @param volumeName name of the volume to bind data to
	 * @param entrypointCommand 
	 * @param startDate
	 * @param endDate
	 * @param maxDestSyn
	 * @param maxDestIpPing
	 * 
	 * @throws Exception
	 */
	@Override
	public void createReportContainer(final String containerName, 
			final String mountPoint, 
			final String imageName, 
			final String volumeName,
			final String entrypointCommand,
			final String startDate,
			final String endDate,
			final String maxDestSyn,
			final String maxDestIpPing,
			final String reportName) throws Exception {
		
		if(StringUtils.equals(containerName, "invalid-container")) {
			throw new Exception("Something went wrong during container life cycle");
		}
	}
	
}
