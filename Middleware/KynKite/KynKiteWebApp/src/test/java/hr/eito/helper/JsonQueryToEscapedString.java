
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
package hr.eito.helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Danijel Soltic
 *
 */
public class JsonQueryToEscapedString {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		String escapedString = "";
		
		FileReader fr = new FileReader("d:/kyn/PHP to Java/query.txt");
		BufferedReader br = new BufferedReader(fr);
		
		String line = null;
		
		while((line = br.readLine())!= null){
			String tmp = line.replace("\"", "\\\"");
			System.out.println(tmp);
			escapedString+=tmp;
		}
		br.close();
		escapedString = escapedString.replaceAll(" ", "");
		System.out.println(escapedString);
		
	}

}
