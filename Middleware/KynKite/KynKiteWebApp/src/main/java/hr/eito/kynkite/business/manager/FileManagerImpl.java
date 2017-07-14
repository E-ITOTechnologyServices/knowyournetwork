
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


package hr.eito.kynkite.business.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hr.eito.kynkite.business.dao.FileSystemDao;

/**
 * Implementation of File managing component
 * 
 * @author Hrvoje
 *
 */
@Component
public class FileManagerImpl implements FileManager {
	
	@Autowired
	FileSystemDao fileSystemDao;

	/**
	 * Get the file from filesystem
	 * 
	 * @param url location of the file
	 * 
	 * @return file
	 */
	@Override
	public File getFile(String url) {
		File pdfFile = this.fileSystemDao.getFile(url);
		return pdfFile;
	}

	/**
	 * Getting the list of file names located in location
	 * 
	 * @param location where to get file names
	 * 
	 * @return list of file names
	 */
	@Override
	public List<String> getListOfFileNames(String location) {
		File f = this.fileSystemDao.getDirectory(location);
		List<String> names = new ArrayList<String>(Arrays.asList(f.list()));
		return names;
	}

}
