
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
package hr.eito.model;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.junit.Test;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the JsonReturnData.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class JsonReturnDataTest {

	/**
	 * Runs before the tests start.
	 */
	@BeforeClass
	public static void testStart() { }
	
	/**
	 * Runs after the tests end.
	 */
	@AfterClass
	public static void testEnd() { }

	/**
	 * Test default construction.
	 */
	@Test
	public void testDefaultCtor() {
		JsonReturnData<Integer> j = new JsonReturnData<Integer>();
		Assert.assertFalse(j.isOK());
	}

	/**
	 * Test construction with an error message.
	 */
	@Test
	public void testErrorCtor() {

		final String errorMessage = "This is an error";

		JsonReturnData<Integer> j = new JsonReturnData<Integer>(errorMessage);
		Assert.assertFalse(j.isOK());
		Assert.assertEquals(JsonReturnData.STATUS_ERROR, j.getStatus());
		Assert.assertEquals(errorMessage, j.getErrorMessage());
	}

	/**
	 * Test changing status.
	 */
	@Test
	public void testChangeStatus() {
		final String errorMessage = "This is an error";

		JsonReturnData<Integer> j = new JsonReturnData<Integer>(errorMessage);

		// Check we can make the object OK.
		j.setOK();
		checkOK(j);

		// Now change status to an invalid one - it should go to the expected error state.
		j.setStatus(14);
		checkError(j, JsonReturnData.UNKNOWN);
		
		// Now change status to an security forbidden one - it should go to the expected error state.
		j.setStatus(2);
		checkSecurityError(j, JsonReturnData.UNKNOWN_SECURITY);

		// Now set an error message.
		final String anotherError = "More errors";
		j.setErrorMessage(anotherError);
		checkError(j, anotherError);
		
		// Now set an security error message.
		final String securityError = "Security error message";
		j.setSecurityMessage(securityError);
		checkSecurityError(j, securityError);
	}

	/**
	 * Test setting from an exception.
	 */
	@Test
	public void testException() {
		final String message = "This is my exception message";
		final java.lang.Exception e = new java.lang.Exception(message);

		JsonReturnData<Integer> j = new JsonReturnData<Integer>();
		j.setError(e);
		checkError(j, "Exception: java.lang.Exception: " + message);
	}

	/**
	 * Check the object is OK.
	 *
	 * @param data the object to test
	 */
	private void checkOK(final JsonReturnData<?> data) {
		Assert.assertTrue(data.isOK());
		Assert.assertEquals(JsonReturnData.STATUS_OK, data.getStatus());
		Assert.assertEquals(JsonReturnData.OK, data.getErrorMessage());
	}

	/**
	 * Check the object is in error with the expected message.
	 *
	 * @param data the object to test
	 */
	private void checkError(final JsonReturnData<?> data, final String message) {
		Assert.assertFalse(data.isOK());
		Assert.assertEquals(JsonReturnData.STATUS_ERROR, data.getStatus());
		Assert.assertEquals(message, data.getErrorMessage());
	}
	
	/**
	 * Check the object is in security error with the expected message.
	 *
	 * @param data the object to test
	 */
	private void checkSecurityError(final JsonReturnData<?> data, final String message) {
		Assert.assertFalse(data.isOK());
		Assert.assertEquals(JsonReturnData.STATUS_FORBIDDEN, data.getStatus());
		Assert.assertEquals(message, data.getErrorMessage());
	}
}
