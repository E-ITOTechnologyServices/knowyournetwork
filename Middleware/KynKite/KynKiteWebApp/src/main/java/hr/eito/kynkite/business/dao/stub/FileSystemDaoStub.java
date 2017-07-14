
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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import hr.eito.kynkite.business.dao.FileSystemDao;

/**
 * Stub class for FileSystemDao
 * 
 * @author Hrvoje
 */
@Component
@Profile("test")
public class FileSystemDaoStub implements FileSystemDao {
	
	private static final Logger logger = LoggerFactory.getLogger(FileSystemDaoStub.class);
	
	@Override
	public File getFile(String url) {
		URL pdfUrl = this.getClass().getClassLoader().getResource("data/reports/report_20170408_20170415.pdf");
		try {
			File file = new File(pdfUrl.toURI());
			return file;
		} catch (URISyntaxException e) {
			logger.error("URISyntaxException!", e);
		}
		return null;
	}
	
	@Override
	public File getDirectory(String url) {
		URL directoryUrl = this.getClass().getClassLoader().getResource("data/reports");
		try {
			File file = new File(directoryUrl.toURI());
			return file;
		} catch (URISyntaxException e) {
			logger.error("URISyntaxException!", e);
		}
		return null;
	}

}
