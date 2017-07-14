
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


package hr.eito.model.newsticker;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import hr.eito.model.DateSerializer;

/**
 * Data payload for newsticker report.
 *
 * @author Marko
 */
class NewsTickerReturnResultData {
	
	private String host_;
	private String text_;
	private String timestamp_;
	private int priority_;
	private int type_;
	
	public NewsTickerReturnResultData(final NewsTickerQueryResultField hit){

		if(hit == null) return;

		host_ = hit.getHost();
		text_ = hit.getText();
		timestamp_ = hit.get$timestamp();
		priority_ = hit.getPriority();
		type_ = eventIdToType_(hit.getEventid());
    }

	public String getHost() {
		return host_;
	}

	public String getText() {
		return text_;
	}

	@JsonGetter("@timestamp")
	@JsonSerialize(using = DateSerializer.class)
	public String getTimestamp() {
		return timestamp_;
	}

	public int getPriority() {
		return priority_;
	}

	public int getType() {
		return type_;
	}

	/**
	 * Convert the supplied eventId into a type.
	 * <p>
	 * Historically, type was a numeric value that indicated the type of the newsticker item and
	 * governed [amongst other things] how it was presented on screen. Type was removed and replaced
	 * by eventId. However, most of the information in eventId is of no use to the front end and
	 * the type field was preserved (with a limited range of values) based on the contents of
	 * eventId. The contents are mapped as follows;
	 * <ul>
	 * <li>First digit of eventId 0-4, type = 100
	 * <li>First digit of eventId 5-9, type = 200
	 * <li>Any other value for the first digit, type = 0
	 * </ul>
	 *
	 * @param eventId the eventId we are trying to interpret
	 *
	 * @return The mapped type
	 */
	private static int eventIdToType_(final String eventId) {

		// The type defaults to zero as agreed.
		int type = 0;
		if (eventId.length() == 0) return type;

		final char firstChar = eventId.charAt(0);


		switch (firstChar) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
				type = 100;
				break;
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				type = 200;
				break;
		}

		return type;
	}
}
