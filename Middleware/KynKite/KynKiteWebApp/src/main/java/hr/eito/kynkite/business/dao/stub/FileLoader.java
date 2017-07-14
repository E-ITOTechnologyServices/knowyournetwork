
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


/**
 * 
 */
package hr.eito.kynkite.business.dao.stub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Danijel Soltic
 *
 */
public class FileLoader {
	
	public static String loadFileContent(String pathToTemplate){
		StringBuffer content = new StringBuffer();
		
		URL url = FileLoader.class.getResource(pathToTemplate);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(url.toURI())));
			
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				content.append(sCurrentLine);
			}
			
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return content.toString();
		
	}
}
