
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


package hr.eito.kynkite.business.dao;

import java.io.File;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Implementation of file system data access object
 * 
 * @author Hrvoje
 */
@Component
@Profile({"dev","prod"})
public class FileSystemDaoRepository implements FileSystemDao {
	
	/**
	 * Getting the file
	 * 
	 * @param url location of the file
	 * 
	 * @return file
	 */
	public File getFile(String url) {
		File file = new File(url);
		return file;
	}
	
	/**
	 * Getting the directory from filesystem
	 * 
	 * @param url location of the directory
	 * 
	 * @return directory as File
	 */
	public File getDirectory(String url) {
		File file = new File(url);
		return file;
	}

}
