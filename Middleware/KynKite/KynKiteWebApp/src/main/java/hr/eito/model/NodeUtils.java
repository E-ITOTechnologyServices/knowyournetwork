
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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Contains some utilities that are useful when working with the Jackson JsonNode tree.
 *
 * @author Steve Chaplin
 */
public class NodeUtils {

	/**
	 * Package scope ctor for complete coverage. Not used.
	 */
	NodeUtils() {
	}

	/**
	 * Get a string value for the child node from a JsonNode parent as a string. If the value does
	 * not exist, then return an empty string.
	 * <p>
	 * It is debatable whether this [default] method should return a null rather than an empty
	 * string.
	 *
	 * @param parent the node of which to get the child
	 * @param key the key of the required child
	 *
	 * @return the value requested or an empty string
	 */
	public static String safeStringValue(final JsonNode parent, final String key) {
		return safeStringValue(parent, key, "");
	}

	/**
	 * Get a string value for the child node from a JsonNode parent as a string. If the value does
	 * not exist, then return the specified default value.
	 *
	 * @param parent the node of which to get the child
	 * @param key the key of the required child
	 * @param defaultVal the value to return if the child does not exist
	 *
	 * @return the value requested or the default value
	 */
	public static String safeStringValue(final JsonNode parent,
										 final String key,
										 final String defaultVal) {
		final JsonNode node = parent.get(key);
		if (null == node) return defaultVal;
		return node.asText();
	}

	/**
	 * Get an int value for the child node from a JsonNode parent as a string. If the value does
	 * not exist, then return zero.
	 *
	 * @param parent the node of which to get the child
	 * @param key the key of the required child
	 *
	 * @return the value requested or zero
	 */
	public static int safeIntValue(final JsonNode parent, final String key) {
		return safeIntValue(parent, key, 0);
	}

	/**
	 * Get an int value for the child node from a JsonNode parent as a string. If the value does
	 * not exist, then return the specified default value.
	 *
	 * @param parent the node of which to get the child
	 * @param key the key of the required child
	 * @param defaultVal the value to return if the child does not exist
	 *
	 * @return the value requested or the default value
	 */
	public static int safeIntValue(final JsonNode parent,
								   final String key,
								   final int defaultVal) {
		final JsonNode node = parent.get(key);
		if (null == node) return defaultVal;
		return node.asInt(defaultVal);
	}

}
