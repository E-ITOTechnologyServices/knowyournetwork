
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


package hr.eito.model;

import hr.eito.kynkite.utils.ByteConverter;

/**
 * Model byte converter
 * <p>Encapsulates the behaviour required by the model when converting bytes
 * from long to String in other units, particularly in the face of parse exceptions.
 *
 * @author Hrvoje Zeljko
 *
 */
public class ModelByteConverter {

	// Whether we are using SI converting (1000B=1kB) or binary (1024B=1kB)
	private final static boolean SI = false;
	private final ByteConverter byteConverter = new ByteConverter();

	/**
	 * Convert bytes from long type to appropriate String value+unit format. 
	 * In case of exceptions set result to ""
	 *
	 * @param bytes the ISO8601 timestamp to convert
	 *
	 * @return the converted timestamp or an empty string.
	 */
	public String humanReadableByteCount(final String bytes) {
		String result = "";
		try {
			result = byteConverter.humanReadableByteCount(Long.parseLong(bytes), SI);
		}
		catch (final Exception e) {
			
		}
		return result;
	}
}
