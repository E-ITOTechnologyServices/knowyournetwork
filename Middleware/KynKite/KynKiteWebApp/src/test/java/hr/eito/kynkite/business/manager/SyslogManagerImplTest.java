
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import hr.eito.kynkite.utils.CustomError;
import hr.eito.model.DateTimeRange;
import hr.eito.model.JsonReturnData;
import hr.eito.model.syslog.SyslogASAReturnResult;
import hr.eito.model.syslog.SyslogDHCPReturnResult;
import hr.eito.model.syslog.SyslogIPSReturnResult;
import hr.eito.model.syslog.SyslogParams;
import hr.eito.model.syslog.SyslogProxyQueryResultField;
import hr.eito.model.syslog.SyslogProxyReturnResultData;
import hr.eito.model.syslog.SyslogReturnResult;
import hr.eito.model.syslog.SyslogRouterReturnResult;
import hr.eito.model.syslog.SyslogVoiceReturnResult;

/**
 * Tests the SyslogManagerImpl.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class SyslogManagerImplTest {
	
	@Autowired
	private SyslogManager manager_;

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
	 * Check we have a manager.
	 */
	@Test
	public void checkManager() {
		Assert.assertNotNull(manager_);
	}

	/**
	 * Test the parameterized Syslog Router query.
	 */
	@Test
	public void testSyslogRouter() {
		// Case when params is null. Should return record as if there no filter - all records
		SyslogParams params_null = null;	
		JsonReturnData<SyslogRouterReturnResult> result_null = manager_.syslogRouter(params_null);
		assertSyslogRouterExpectedResult(result_null, 10, 2200, "2016-06-30T06:48:00.769Z", 
				"Jun 30 06:47:25.703 UTC: %SEC-6-IPACCESSLOGRP: list Voice denied pim 10.134.21.2 -> 224.0.0.13, 1 packet", "10.127.248.4");
		
		// Case when not any of the parameters is set. Should return record as if there no filter - all records
		SyslogParams params_empty = new SyslogParams();
		params_empty.setQuery(null);
		params_empty.setDateTimeRange(null);
		JsonReturnData<SyslogRouterReturnResult> result_empty = manager_.syslogRouter(params_empty);
		assertSyslogRouterExpectedResult(result_empty, 10, 2200, "2016-06-30T06:48:00.769Z", 
				"Jun 30 06:47:25.703 UTC: %SEC-6-IPACCESSLOGRP: list Voice denied pim 10.134.21.2 -> 224.0.0.13, 1 packet", "10.127.248.4");
		
		// Case when not any of the parameters is set - query is set to empty String.
		// Should return record as if there are no filter - all records
		SyslogParams params_emptyQuery = new SyslogParams();
		params_emptyQuery.setQuery("");
		params_emptyQuery.setDateTimeRange(null);
		JsonReturnData<SyslogRouterReturnResult> result_emptyQuery = manager_.syslogRouter(params_emptyQuery);
		assertSyslogRouterExpectedResult(result_emptyQuery, 10, 2200, "2016-06-30T06:48:00.769Z", 
				"Jun 30 06:47:25.703 UTC: %SEC-6-IPACCESSLOGRP: list Voice denied pim 10.134.21.2 -> 224.0.0.13, 1 packet", "10.127.248.4");
		
		// Case when datetimerange is set, but broken
		SyslogParams params_brokenDateTimeRange = new SyslogParams();
		params_brokenDateTimeRange.setQuery(null);
		DateTimeRange dateTimeRange = new DateTimeRange();
		dateTimeRange.setDateTimeFrom(new Date());
		dateTimeRange.setDateTimeTo(null);
		params_brokenDateTimeRange.setDateTimeRange(dateTimeRange);
		JsonReturnData<SyslogRouterReturnResult> result_brokenDateTimeRange = manager_.syslogRouter(params_brokenDateTimeRange);
		Assert.assertFalse(result_brokenDateTimeRange.isOK());
		Assert.assertEquals(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage(), result_brokenDateTimeRange.getErrorMessage());
		
		// Case when query expression is valid IP. Should return filtered result set
		SyslogParams params_validIP = new SyslogParams();
		params_validIP.setQuery("10.127.47.180");
		params_validIP.setDateTimeRange(null);
		JsonReturnData<SyslogRouterReturnResult> result_validIP = manager_.syslogRouter(params_validIP);
		assertSyslogRouterExpectedResult(result_validIP, 4, 4, "2016-06-30T06:48:00.768Z", 
				"Jun 30 06:47:54.788 UTC: %LINEPROTO-5-UPDOWN: Line protocol on Interface GigabitEthernet1/0/9, changed state to up", "10.127.47.180");
		
		// Case when query expression is invalid IP or undiscovered piece of message. Should return valid but empty result set
		SyslogParams params_invalidIP = new SyslogParams();
		params_invalidIP.setQuery("invalid_ip");
		params_invalidIP.setDateTimeRange(null);
		JsonReturnData<SyslogRouterReturnResult> result_invalidIP = manager_.syslogRouter(params_invalidIP);		
		Assert.assertTrue(result_invalidIP.isOK());
		Assert.assertNotNull(result_invalidIP.getContent());
		Assert.assertEquals(0, result_invalidIP.getContent().getRecordsFiltered());
		Assert.assertEquals(0, result_invalidIP.getContent().getRecordsTotal());
		
		// Case when query result ends invalid
		SyslogParams params_invalidResult = new SyslogParams();
		params_invalidResult.setQuery("missing_data");
		JsonReturnData<SyslogRouterReturnResult> result_invalidResult = manager_.syslogRouter(params_invalidResult);
		Assert.assertFalse(result_invalidResult.isOK());
		
		// Case when query expression is null and dateTimeRange exists. Should return filtered result set
		SyslogParams params_noQueryYesDTR = new SyslogParams();
		params_noQueryYesDTR.setQuery(null);
		DateTimeRange dateTimeRange2 = new DateTimeRange();
		dateTimeRange2.setDateTimeFrom(generateDateTime("2016-06-30 06:48:00.000"));
		dateTimeRange2.setDateTimeTo(generateDateTime("2016-06-30 06:48:01.000"));
		params_noQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogRouterReturnResult> result_noQueryYesDTR = manager_.syslogRouter(params_noQueryYesDTR);
		assertSyslogRouterExpectedResult(result_noQueryYesDTR, 3, 63, "2016-06-30T06:48:00.769Z", 
				"Jun 30 06:47:25.703 UTC: %SEC-6-IPACCESSLOGRP: list Voice denied pim 10.134.21.2 -> 224.0.0.13, 1 packet", "10.127.248.4");
		
		// Case when query expression is empty and dateTimeRange exists. Should return filtered result set
		SyslogParams params_noQueryYesDTR2 = new SyslogParams();
		params_noQueryYesDTR2.setQuery("");
		params_noQueryYesDTR2.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogRouterReturnResult> result_noQueryYesDTR2 = manager_.syslogRouter(params_noQueryYesDTR);
		assertSyslogRouterExpectedResult(result_noQueryYesDTR2, 3, 63, "2016-06-30T06:48:00.769Z", 
				"Jun 30 06:47:25.703 UTC: %SEC-6-IPACCESSLOGRP: list Voice denied pim 10.134.21.2 -> 224.0.0.13, 1 packet", "10.127.248.4");
		
		// Case when query expression is valid IP and dateTimeRange exists. Should return filtered result set
		SyslogParams params_yesQueryYesDTR = new SyslogParams();
		params_yesQueryYesDTR.setQuery("10.127.47.180");
		params_yesQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogRouterReturnResult> result_yesQueryYesDTR = manager_.syslogRouter(params_yesQueryYesDTR);
		assertSyslogRouterExpectedResult(result_yesQueryYesDTR, 1, 1, "2016-06-30T06:48:00.768Z", 
				"Jun 30 06:47:54.788 UTC: %LINEPROTO-5-UPDOWN: Line protocol on Interface GigabitEthernet1/0/9, changed state to up", "10.127.47.180");
	}
	
	/**
	 * Helper method for comparing Syslog Router result set with expected values
	 */
	private void assertSyslogRouterExpectedResult(final JsonReturnData<SyslogRouterReturnResult> result, final int recordsFilteredExpected,
			final int recordsTotalExpected, final String timestampExpected, final String ciscoMessageExpected, final String routerExpected) {
		Assert.assertTrue(result.isOK());
		final SyslogRouterReturnResult content = result.getContent();
		Assert.assertNotNull(content);

		Assert.assertEquals(recordsFilteredExpected, content.getRecordsFiltered());
		Assert.assertEquals(recordsTotalExpected, content.getRecordsTotal());
		Assert.assertEquals(timestampExpected, content.getData().get(0).getTimestamp());
		Assert.assertEquals(ciscoMessageExpected, content.getData().get(0).getCisco_message());
		Assert.assertEquals(routerExpected, content.getData().get(0).getRouter());
	}

	/**
	 * Test the parameterized Syslog Voice query.
	 */
	@Test
	public void testSyslogVoice() {
		// Case when params is null. Should return record as if there no filter - all records
		SyslogParams params_null = null;	
		JsonReturnData<SyslogVoiceReturnResult> result_null = manager_.syslogVoice(params_null);
		assertSyslogVoiceExpectedResult(result_null, 15, 2800, "2016-07-14T00:25:54.461Z", "10.45.3.3");
		
		// Case when not any of the parameters is set. Should return record as if there no filter - all records
		SyslogParams params_empty = new SyslogParams();
		params_empty.setQuery(null);
		params_empty.setDateTimeRange(null);
		JsonReturnData<SyslogVoiceReturnResult> result_empty = manager_.syslogVoice(params_empty);
		assertSyslogVoiceExpectedResult(result_empty, 15, 2800, "2016-07-14T00:25:54.461Z", "10.45.3.3");
		
		// Case when not any of the parameters is set - query is set to empty String.
		// Should return record as if there are no filter - all records
		SyslogParams params_emptyQuery = new SyslogParams();
		params_emptyQuery.setQuery("");
		params_emptyQuery.setDateTimeRange(null);
		JsonReturnData<SyslogVoiceReturnResult> result_emptyQuery = manager_.syslogVoice(params_emptyQuery);
		assertSyslogVoiceExpectedResult(result_emptyQuery, 15, 2800, "2016-07-14T00:25:54.461Z", "10.45.3.3");
		
		// Case when datetimerange is set, but broken
		SyslogParams params_brokenDateTimeRange = new SyslogParams();
		params_brokenDateTimeRange.setQuery(null);
		DateTimeRange dateTimeRange = new DateTimeRange();
		dateTimeRange.setDateTimeFrom(new Date());
		dateTimeRange.setDateTimeTo(null);
		params_brokenDateTimeRange.setDateTimeRange(dateTimeRange);
		JsonReturnData<SyslogVoiceReturnResult> result_brokenDateTimeRange = manager_.syslogVoice(params_brokenDateTimeRange);
		Assert.assertFalse(result_brokenDateTimeRange.isOK());
		Assert.assertEquals(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage(), result_brokenDateTimeRange.getErrorMessage());
				
		// Case when query expression is valid IP. Should return filtered result set
		SyslogParams params_validIP = new SyslogParams();
		params_validIP.setQuery("10.45.3.3");
		params_validIP.setDateTimeRange(null);
		JsonReturnData<SyslogVoiceReturnResult> result_validIP = manager_.syslogVoice(params_validIP);
		assertSyslogVoiceExpectedResult(result_validIP, 20, 1214, "2016-07-14T00:25:54.414Z", "10.45.3.3");
		
		// Case when query expression is invalid IP or undiscovered piece of message. Should return valid but empty result set
		SyslogParams params_invalidIP = new SyslogParams();
		params_invalidIP.setQuery("invalid_ip");
		params_invalidIP.setDateTimeRange(null);
		JsonReturnData<SyslogVoiceReturnResult> result_invalidIP = manager_.syslogVoice(params_invalidIP);		
		Assert.assertTrue(result_invalidIP.isOK());
		Assert.assertNotNull(result_invalidIP.getContent());
		Assert.assertEquals(0, result_invalidIP.getContent().getRecordsFiltered());
		Assert.assertEquals(0, result_invalidIP.getContent().getRecordsTotal());
		
		// Case when query result ends invalid
		SyslogParams params_invalidResult = new SyslogParams();
		params_invalidResult.setQuery("missing_data");
		JsonReturnData<SyslogVoiceReturnResult> result_invalidResult = manager_.syslogVoice(params_invalidResult);
		Assert.assertFalse(result_invalidResult.isOK());
		
		// Case when query expression is null and dateTimeRange exists. Should return filtered result set
		SyslogParams params_noQueryYesDTR = new SyslogParams();
		params_noQueryYesDTR.setQuery(null);
		DateTimeRange dateTimeRange2 = new DateTimeRange();
		dateTimeRange2.setDateTimeFrom(generateDateTime("2016-07-14 00:25:54.000"));
		dateTimeRange2.setDateTimeTo(generateDateTime("2016-07-14 00:25:54.999"));
		params_noQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogVoiceReturnResult> result_noQueryYesDTR = manager_.syslogVoice(params_noQueryYesDTR);
		assertSyslogVoiceExpectedResult(result_noQueryYesDTR, 3, 35, "2016-07-14T00:25:54.461Z", "10.45.3.3");
		
		// Case when query expression is empty and dateTimeRange exists. Should return filtered result set
		SyslogParams params_noQueryYesDTR2 = new SyslogParams();
		params_noQueryYesDTR2.setQuery("");
		params_noQueryYesDTR2.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogVoiceReturnResult> result_noQueryYesDTR2 = manager_.syslogVoice(params_noQueryYesDTR);
		assertSyslogVoiceExpectedResult(result_noQueryYesDTR2, 3, 35, "2016-07-14T00:25:54.461Z", "10.45.3.3");
		
		// Case when query expression is valid IP and dateTimeRange exists. Should return filtered result set
		SyslogParams params_yesQueryYesDTR = new SyslogParams();
		params_yesQueryYesDTR.setQuery("10.45.3.3");
		params_yesQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogVoiceReturnResult> result_yesQueryYesDTR = manager_.syslogVoice(params_yesQueryYesDTR);
		assertSyslogVoiceExpectedResult(result_yesQueryYesDTR, 2, 14, "2016-07-14T00:25:54.414Z", "10.45.3.3");
	}
	
	/**
	 * Helper method for comparing Syslog Voice result set with expected values
	 */
	private void assertSyslogVoiceExpectedResult(final JsonReturnData<SyslogVoiceReturnResult> result, final int recordsFilteredExpected,
			final int recordsTotalExpected, final String timestampExpected, final String voicegwExpected) {
		Assert.assertTrue(result.isOK());
		final SyslogVoiceReturnResult content = result.getContent();
		Assert.assertNotNull(content);

		Assert.assertEquals(recordsFilteredExpected, content.getRecordsFiltered());
		Assert.assertEquals(recordsTotalExpected, content.getRecordsTotal());
		Assert.assertEquals(timestampExpected, content.getData().get(0).getTimestamp());
		Assert.assertEquals(voicegwExpected, content.getData().get(0).getVoicegw());
	}

	/**
	 * Test the parameterized Syslog ASA query.
	 */
	@Test
	public void testSyslogASA() {
		// Case when params is null. Should return record as if there no filter - all records
		SyslogParams params_null = null;	
		JsonReturnData<SyslogASAReturnResult> result_null = manager_.syslogASA(params_null);
		assertSyslogASAExpectedResult(result_null, 10, 2200, "2016-06-30T06:45:11.515Z", "10.133.13.249");
		
		// Case when not any of the parameters is set. Should return record as if there no filter - all records
		SyslogParams params_empty = new SyslogParams();
		params_empty.setQuery(null);
		params_empty.setDateTimeRange(null);
		JsonReturnData<SyslogASAReturnResult> result_empty = manager_.syslogASA(params_empty);
		assertSyslogASAExpectedResult(result_empty, 10, 2200, "2016-06-30T06:45:11.515Z", "10.133.13.249");
		
		// Case when not any of the parameters is set - query is set to empty String.
		// Should return record as if there are no filter - all records
		SyslogParams params_emptyQuery = new SyslogParams();
		params_emptyQuery.setQuery("");
		params_emptyQuery.setDateTimeRange(null);
		JsonReturnData<SyslogASAReturnResult> result_emptyQuery = manager_.syslogASA(params_emptyQuery);
		assertSyslogASAExpectedResult(result_emptyQuery, 10, 2200, "2016-06-30T06:45:11.515Z", "10.133.13.249");
		
		// Case when datetimerange is set, but broken
		SyslogParams params_brokenDateTimeRange = new SyslogParams();
		params_brokenDateTimeRange.setQuery(null);
		DateTimeRange dateTimeRange = new DateTimeRange();
		dateTimeRange.setDateTimeFrom(new Date());
		dateTimeRange.setDateTimeTo(null);
		params_brokenDateTimeRange.setDateTimeRange(dateTimeRange);
		JsonReturnData<SyslogASAReturnResult> result_brokenDateTimeRange = manager_.syslogASA(params_brokenDateTimeRange);
		Assert.assertFalse(result_brokenDateTimeRange.isOK());
		Assert.assertEquals(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage(), result_brokenDateTimeRange.getErrorMessage());
		
		// Case when query expression is valid IP and dateTimeRange not exists. Should return filtered result set
		SyslogParams params_validIP = new SyslogParams();
		params_validIP.setQuery("10.254.229.162");
		params_validIP.setDateTimeRange(null);
		JsonReturnData<SyslogASAReturnResult> result_validIP = manager_.syslogASA(params_validIP);
		assertSyslogASAExpectedResult(result_validIP, 10, 1044, "2016-06-30T06:45:11.515Z", "10.133.13.249");
		
		// Case when query expression is invalid IP or undiscovered piece of message. Should return valid but empty result set
		SyslogParams params_invalidIP = new SyslogParams();
		params_invalidIP.setQuery("invalid_ip");
		params_invalidIP.setDateTimeRange(null);
		JsonReturnData<SyslogASAReturnResult> result_invalidIP = manager_.syslogASA(params_invalidIP);		
		Assert.assertTrue(result_invalidIP.isOK());
		Assert.assertNotNull(result_invalidIP.getContent());
		Assert.assertEquals(0, result_invalidIP.getContent().getRecordsFiltered());
		Assert.assertEquals(0, result_invalidIP.getContent().getRecordsTotal());
		
		// Case when query result ends invalid
		SyslogParams params_invalidResult = new SyslogParams();
		params_invalidResult.setQuery("missing_data");
		JsonReturnData<SyslogASAReturnResult> result_invalidResult = manager_.syslogASA(params_invalidResult);
		Assert.assertFalse(result_invalidResult.isOK());
		
		// Case when query expression is null and dateTimeRange exists. Should return filtered result set
		SyslogParams params_noQueryYesDTR = new SyslogParams();
		params_noQueryYesDTR.setQuery(null);
		DateTimeRange dateTimeRange2 = new DateTimeRange();
		dateTimeRange2.setDateTimeFrom(generateDateTime("2016-06-30 06:45:11.000"));
		dateTimeRange2.setDateTimeTo(generateDateTime("2016-06-30 06:45:12.000"));
		params_noQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogASAReturnResult> result_noQueryYesDTR = manager_.syslogASA(params_noQueryYesDTR);
		assertSyslogASAExpectedResult(result_noQueryYesDTR, 3, 206, "2016-06-30T06:45:11.515Z", "10.133.13.249");
		
		// Case when query expression is empty and dateTimeRange exists. Should return filtered result set
		SyslogParams params_noQueryYesDTR2 = new SyslogParams();
		params_noQueryYesDTR2.setQuery("");
		params_noQueryYesDTR2.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogASAReturnResult> result_noQueryYesDTR2 = manager_.syslogASA(params_noQueryYesDTR);
		assertSyslogASAExpectedResult(result_noQueryYesDTR2, 3, 206, "2016-06-30T06:45:11.515Z", "10.133.13.249");
		
		// Case when query expression is valid IP and dateTimeRange exists. Should return filtered result set
		SyslogParams params_yesQueryYesDTR = new SyslogParams();
		params_yesQueryYesDTR.setQuery("10.254.229.162");
		params_yesQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogASAReturnResult> result_yesQueryYesDTR = manager_.syslogASA(params_yesQueryYesDTR);
		assertSyslogASAExpectedResult(result_yesQueryYesDTR, 2, 139, "2016-06-30T06:45:11.515Z", "10.133.13.249");
	}
	
	/**
	 * Helper method for comparing Syslog ASA result set with expected values
	 */
	private void assertSyslogASAExpectedResult(final JsonReturnData<SyslogASAReturnResult> result, final int recordsFilteredExpected,
			final int recordsTotalExpected, final String timestampExpected, final String asafwExpected) {
		Assert.assertTrue(result.isOK());
		final SyslogASAReturnResult content = result.getContent();
		Assert.assertNotNull(content);

		Assert.assertEquals(recordsFilteredExpected, content.getRecordsFiltered());
		Assert.assertEquals(recordsTotalExpected, content.getRecordsTotal());
		Assert.assertEquals(timestampExpected, content.getData().get(0).getTimestamp());
		Assert.assertEquals(asafwExpected, content.getData().get(0).getAsa_fw());
	}

	/**
	 * Test the parameterized Syslog IPS.
	 */
	@Test
	public void testSyslogIPS() {
		// Case when params is null. Should return record as if there no filter - all records
		SyslogParams params_null = null;	
		JsonReturnData<SyslogIPSReturnResult> result_null = manager_.syslogIPS(params_null);
		assertSyslogIPSExpectedResult(result_null, 10, 2200, "2016-06-30T07:31:44.360Z", "194.127.8.11", "92.246.34.12", "56451", "10.254.128.17", "HTTP: RFC 2397 Data URL Scheme Policy 3232 tcp ");
		
		// Case when not any of the parameters is set. Should return record as if there no filter - all records
		SyslogParams params_empty = new SyslogParams();
		params_empty.setQuery(null);
		params_empty.setDateTimeRange(null);
		JsonReturnData<SyslogIPSReturnResult> result_empty = manager_.syslogIPS(params_empty);
		assertSyslogIPSExpectedResult(result_empty, 10, 2200, "2016-06-30T07:31:44.360Z", "194.127.8.11", "92.246.34.12", "56451", "10.254.128.17", "HTTP: RFC 2397 Data URL Scheme Policy 3232 tcp ");
		
		// Case when not any of the parameters is set - query is set to empty String.
		// Should return record as if there are no filter - all records
		SyslogParams params_emptyQuery = new SyslogParams();
		params_emptyQuery.setQuery("");
		params_emptyQuery.setDateTimeRange(null);
		JsonReturnData<SyslogIPSReturnResult> result_emptyQuery = manager_.syslogIPS(params_emptyQuery);
		assertSyslogIPSExpectedResult(result_emptyQuery, 10, 2200, "2016-06-30T07:31:44.360Z", "194.127.8.11", "92.246.34.12", "56451", "10.254.128.17", "HTTP: RFC 2397 Data URL Scheme Policy 3232 tcp ");
		
		// Case when datetimerange is set, but broken
		SyslogParams params_brokenDateTimeRange = new SyslogParams();
		params_brokenDateTimeRange.setQuery(null);
		DateTimeRange dateTimeRange = new DateTimeRange();
		dateTimeRange.setDateTimeFrom(new Date());
		dateTimeRange.setDateTimeTo(null);
		params_brokenDateTimeRange.setDateTimeRange(dateTimeRange);
		JsonReturnData<SyslogIPSReturnResult> result_brokenDateTimeRange = manager_.syslogIPS(params_brokenDateTimeRange);
		Assert.assertFalse(result_brokenDateTimeRange.isOK());
		Assert.assertEquals(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage(), result_brokenDateTimeRange.getErrorMessage());
				
		// Case when query expression is valid IP and dateTimeRange not exists. Should return filtered result set
		SyslogParams params_validIP = new SyslogParams();
		params_validIP.setQuery("10.254.128.17");
		params_validIP.setDateTimeRange(null);
		JsonReturnData<SyslogIPSReturnResult> result_validIP = manager_.syslogIPS(params_validIP);
		assertSyslogIPSExpectedResult(result_validIP, 20, 2200, "2016-06-30T07:31:44.360Z", "194.127.8.11", "92.246.34.12", "56451", "10.254.128.17", "HTTP: RFC 2397 Data URL Scheme Policy 3232 tcp ");
		
		// Case when query expression is invalid IP or undiscovered piece of message. Should return valid but empty result set
		SyslogParams params_invalidIP = new SyslogParams();
		params_invalidIP.setQuery("invalid_ip");
		params_invalidIP.setDateTimeRange(null);
		JsonReturnData<SyslogIPSReturnResult> result_invalidIP = manager_.syslogIPS(params_invalidIP);		
		Assert.assertTrue(result_invalidIP.isOK());
		Assert.assertNotNull(result_invalidIP.getContent());
		Assert.assertEquals(0, result_invalidIP.getContent().getRecordsFiltered());
		Assert.assertEquals(0, result_invalidIP.getContent().getRecordsTotal());
		
		// Case when query result ends invalid
		SyslogParams params_invalidResult = new SyslogParams();
		params_invalidResult.setQuery("missing_data");
		JsonReturnData<SyslogIPSReturnResult> result_invalidResult = manager_.syslogIPS(params_invalidResult);
		Assert.assertFalse(result_invalidResult.isOK());	
		
		// Case when query expression is null and dateTimeRange exists. Should return filtered result set
		SyslogParams params_noQueryYesDTR = new SyslogParams();
		params_noQueryYesDTR.setQuery(null);
		DateTimeRange dateTimeRange2 = new DateTimeRange();
		dateTimeRange2.setDateTimeFrom(generateDateTime("2016-06-30 07:31:44.000"));
		dateTimeRange2.setDateTimeTo(generateDateTime("2016-06-30 07:31:44.999"));
		params_noQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogIPSReturnResult> result_noQueryYesDTR = manager_.syslogIPS(params_noQueryYesDTR);
		assertSyslogIPSExpectedResult(result_noQueryYesDTR, 3, 11, "2016-06-30T07:31:44.360Z", "194.127.8.11", 
				"92.246.34.12", "56451", "10.254.128.17", "HTTP: RFC 2397 Data URL Scheme Policy 3232 tcp ");
		
		// Case when query expression is empty and dateTimeRange exists. Should return filtered result set
		SyslogParams params_noQueryYesDTR2 = new SyslogParams();
		params_noQueryYesDTR2.setQuery("");
		params_noQueryYesDTR2.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogIPSReturnResult> result_noQueryYesDTR2 = manager_.syslogIPS(params_noQueryYesDTR);
		assertSyslogIPSExpectedResult(result_noQueryYesDTR2, 3, 11, "2016-06-30T07:31:44.360Z", "194.127.8.11", 
				"92.246.34.12", "56451", "10.254.128.17", "HTTP: RFC 2397 Data URL Scheme Policy 3232 tcp ");
		
		// Case when query expression is valid IP and dateTimeRange exists. Should return filtered result set
		SyslogParams params_yesQueryYesDTR = new SyslogParams();
		params_yesQueryYesDTR.setQuery("10.254.128.17");
		params_yesQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogIPSReturnResult> result_yesQueryYesDTR = manager_.syslogIPS(params_yesQueryYesDTR);
		assertSyslogIPSExpectedResult(result_yesQueryYesDTR, 2, 11, "2016-06-30T07:31:44.360Z", "194.127.8.11", 
				"92.246.34.12", "56451", "10.254.128.17", "HTTP: RFC 2397 Data URL Scheme Policy 3232 tcp ");
	}
	
	/**
	 * Helper method for comparing Syslog IPS result set with expected values
	 */
	private void assertSyslogIPSExpectedResult(final JsonReturnData<SyslogIPSReturnResult> result, final int recordsFilteredExpected,
			final int recordsTotalExpected, final String timestampExpected, final String destinationExpected, final String sourceExpected, final String destinationPortExpected,
			final String ipsSystemExpected, final String logExpected) {
		Assert.assertTrue(result.isOK());
		final SyslogIPSReturnResult content = result.getContent();
		Assert.assertNotNull(content);

		Assert.assertEquals(recordsFilteredExpected, content.getRecordsFiltered());
		Assert.assertEquals(recordsTotalExpected, content.getRecordsTotal());
		Assert.assertEquals(timestampExpected, content.getData().get(0).getTimestamp());
		Assert.assertEquals(destinationExpected, content.getData().get(0).getDestination());
		Assert.assertEquals(sourceExpected, content.getData().get(0).getSource());		
		Assert.assertEquals(destinationPortExpected, content.getData().get(0).getDestinationport());
		Assert.assertEquals(ipsSystemExpected, content.getData().get(0).getIps_system());
		Assert.assertEquals(logExpected, content.getData().get(0).getLog());		
	}
	
	/**
	 * Test the parameterized Syslog DHCP.
	 */
	@Test
	public void testSyslogDHCP() {
		// Case when params is null. Should return record as if there no filter - all records
		SyslogParams params_null = null;	
		JsonReturnData<SyslogDHCPReturnResult> result_null = manager_.syslogDHCP(params_null);
		assertSyslogDHCPExpectedResult(result_null, 10, 2200, "2016-06-30T06:46:01.970Z", "10.254.253.10", 
				"2016-06-30T06:46:01+02:00 10.254.253.10 dhcpd 17620 - - DHCPACK to 10.135.77.144 (2c:44:fd:0f:36:46) via eth0\n");
		
		// Case when not any of the parameters is set. Should return record as if there no filter - all records
		SyslogParams params_empty = new SyslogParams();
		params_empty.setQuery(null);
		params_empty.setDateTimeRange(null);
		JsonReturnData<SyslogDHCPReturnResult> result_empty = manager_.syslogDHCP(params_empty);
		assertSyslogDHCPExpectedResult(result_empty, 10, 2200, "2016-06-30T06:46:01.970Z", "10.254.253.10", 
				"2016-06-30T06:46:01+02:00 10.254.253.10 dhcpd 17620 - - DHCPACK to 10.135.77.144 (2c:44:fd:0f:36:46) via eth0\n");		
		
		// Case when not any of the parameters is set - query is set to empty String.
		// Should return record as if there are no filter - all records
		SyslogParams params_emptyQuery = new SyslogParams();
		params_emptyQuery.setQuery("");
		params_empty.setDateTimeRange(null);
		JsonReturnData<SyslogDHCPReturnResult> result_emptyQuery = manager_.syslogDHCP(params_emptyQuery);
		assertSyslogDHCPExpectedResult(result_emptyQuery, 10, 2200, "2016-06-30T06:46:01.970Z", "10.254.253.10", 
				"2016-06-30T06:46:01+02:00 10.254.253.10 dhcpd 17620 - - DHCPACK to 10.135.77.144 (2c:44:fd:0f:36:46) via eth0\n");	
		
		// Case when datetimerange is set, but broken
		SyslogParams params_brokenDateTimeRange = new SyslogParams();
		params_brokenDateTimeRange.setQuery(null);
		DateTimeRange dateTimeRange = new DateTimeRange();
		dateTimeRange.setDateTimeFrom(new Date());
		dateTimeRange.setDateTimeTo(null);
		params_brokenDateTimeRange.setDateTimeRange(dateTimeRange);
		JsonReturnData<SyslogDHCPReturnResult> result_brokenDateTimeRange = manager_.syslogDHCP(params_brokenDateTimeRange);
		Assert.assertFalse(result_brokenDateTimeRange.isOK());
		Assert.assertEquals(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage(), result_brokenDateTimeRange.getErrorMessage());
		
		// Case when query expression is valid IP and dateTimeRange not exists. Should return filtered result set
		SyslogParams params_validIP = new SyslogParams();
		params_validIP.setQuery("10.254.245.8");
		params_validIP.setDateTimeRange(null);
		JsonReturnData<SyslogDHCPReturnResult> result_validIP = manager_.syslogDHCP(params_validIP);
		assertSyslogDHCPExpectedResult(result_validIP, 20, 912, "2016-06-30T06:46:01.970Z", "10.254.245.8", 
				"2016-06-30T06:46:01+02:00 10.254.245.8 dhcpd 6218 - - DHCPACK to 10.135.77.144 (2c:44:fd:0f:36:46) via eth0\n");		
		
		// Case when query expression is invalid IP or undiscovered piece of message. Should return valid but empty result set
		SyslogParams params_invalidIP = new SyslogParams();
		params_invalidIP.setQuery("invalid_ip");
		params_invalidIP.setDateTimeRange(null);
		JsonReturnData<SyslogDHCPReturnResult> result_invalidIP = manager_.syslogDHCP(params_invalidIP);		
		Assert.assertTrue(result_invalidIP.isOK());
		Assert.assertNotNull(result_invalidIP.getContent());
		Assert.assertEquals(0, result_invalidIP.getContent().getRecordsFiltered());
		Assert.assertEquals(0, result_invalidIP.getContent().getRecordsTotal());
		
		// Case when query result ends invalid
		SyslogParams params_invalidResult = new SyslogParams();
		params_invalidResult.setQuery("missing_data");
		JsonReturnData<SyslogDHCPReturnResult> result_invalidResult = manager_.syslogDHCP(params_invalidResult);
		Assert.assertFalse(result_invalidResult.isOK());
		
		// Case when query expression is null and dateTimeRange exists. Should return filtered result set
		SyslogParams params_noQueryYesDTR = new SyslogParams();
		params_noQueryYesDTR.setQuery(null);
		DateTimeRange dateTimeRange2 = new DateTimeRange();
		dateTimeRange2.setDateTimeFrom(generateDateTime("2016-06-30 06:46:01.000"));
		dateTimeRange2.setDateTimeTo(generateDateTime("2016-06-30 06:46:01.999"));
		params_noQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogDHCPReturnResult> result_noQueryYesDTR = manager_.syslogDHCP(params_noQueryYesDTR);
		assertSyslogDHCPExpectedResult(result_noQueryYesDTR, 2, 101, "2016-06-30T06:46:01.970Z", "10.254.253.10", 
				"2016-06-30T06:46:01+02:00 10.254.253.10 dhcpd 17620 - - DHCPACK to 10.135.77.144 (2c:44:fd:0f:36:46) via eth0\n");
		
		// Case when query expression is empty and dateTimeRange exists. Should return filtered result set
		SyslogParams params_noQueryYesDTR2 = new SyslogParams();
		params_noQueryYesDTR2.setQuery("");
		params_noQueryYesDTR2.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogDHCPReturnResult> result_noQueryYesDTR2 = manager_.syslogDHCP(params_noQueryYesDTR2);
		assertSyslogDHCPExpectedResult(result_noQueryYesDTR2, 2, 101, "2016-06-30T06:46:01.970Z", "10.254.253.10", 
				"2016-06-30T06:46:01+02:00 10.254.253.10 dhcpd 17620 - - DHCPACK to 10.135.77.144 (2c:44:fd:0f:36:46) via eth0\n");
		
		// Case when query expression is valid IP and dateTimeRange exists. Should return filtered result set
		SyslogParams params_yesQueryYesDTR = new SyslogParams();
		params_yesQueryYesDTR.setQuery("10.254.245.8");
		params_yesQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogDHCPReturnResult> result_YesQueryYesDTR2 = manager_.syslogDHCP(params_yesQueryYesDTR);
		assertSyslogDHCPExpectedResult(result_YesQueryYesDTR2, 2, 35, "2016-06-30T06:46:01.970Z", "10.254.245.8", 
				"2016-06-30T06:46:01+02:00 10.254.245.8 dhcpd 6218 - - DHCPACK to 10.135.77.144 (2c:44:fd:0f:36:46) via eth0\n");
	}
	
	/**
	 * Helper method for comparing Syslog DHCP result set with expected values
	 */
	private void assertSyslogDHCPExpectedResult(final JsonReturnData<SyslogDHCPReturnResult> result, final int recordsFilteredExpected,
			final int recordsTotalExpected, final String timestampExpected, final String adServerExpected, final String messageExpected) {
		Assert.assertTrue(result.isOK());
		final SyslogDHCPReturnResult content = result.getContent();
		Assert.assertNotNull(content);
		
		Assert.assertEquals(recordsFilteredExpected, content.getRecordsFiltered());
		Assert.assertEquals(recordsTotalExpected, content.getRecordsTotal());
		Assert.assertEquals(timestampExpected, content.getData().get(0).getTimestamp());
		Assert.assertEquals(adServerExpected, content.getData().get(0).getAd_Server());
		Assert.assertEquals(messageExpected, content.getData().get(0).getMessage());
	}	
	
	/**
	 * Test the parameterized Syslog Proxy.
	 */
	@Test
	public void testSyslogProxy() {
		// Case when params is null. Should return record as if there no filter - all records
		SyslogParams params_null = null;	
		JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> result_null = manager_.syslogProxy(params_null);
		assertSyslogProxyExpectedResult(result_null, 10, 1000, "2016-12-02T14:32:45.000Z", "173.194.32.183", "10.110.149.227", "www.google.de", "443", "/ads/user-lists/1072409767/");
		
		// Case when not any of the parameters is set. Should return record as if there are no filter - all records
		SyslogParams params_empty = new SyslogParams();
		params_empty.setQuery(null);
		params_empty.setDateTimeRange(null);
		JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> result_empty = manager_.syslogProxy(params_empty);
		assertSyslogProxyExpectedResult(result_empty, 10, 1000, "2016-12-02T14:32:45.000Z", "173.194.32.183", "10.110.149.227", "www.google.de", "443", "/ads/user-lists/1072409767/");
		
		// Case when not any of the parameters is set - query is set to empty String.
		// Should return record as if there are no filter - all records
		SyslogParams params_empty2 = new SyslogParams();
		params_empty2.setQuery("");
		params_empty2.setDateTimeRange(null);
		JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> result_empty2 = manager_.syslogProxy(params_empty);
		assertSyslogProxyExpectedResult(result_empty2, 10, 1000, "2016-12-02T14:32:45.000Z", "173.194.32.183", "10.110.149.227", "www.google.de", "443", "/ads/user-lists/1072409767/");
		
		// Case when datetimerange is set, but broken
		SyslogParams params_brokenDateTimeRange = new SyslogParams();
		params_brokenDateTimeRange.setQuery(null);
		DateTimeRange dateTimeRange = new DateTimeRange();
		dateTimeRange.setDateTimeFrom(new Date());
		dateTimeRange.setDateTimeTo(null);
		params_brokenDateTimeRange.setDateTimeRange(dateTimeRange);
		JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> result_brokenDateTimeRange = manager_.syslogProxy(params_brokenDateTimeRange);
		Assert.assertFalse(result_brokenDateTimeRange.isOK());
		Assert.assertEquals(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage(), result_brokenDateTimeRange.getErrorMessage());
		
		// Case when query expression is valid IP and dateTimeRange not exists. Should return filtered result set
		SyslogParams params_validIP = new SyslogParams();
		params_validIP.setQuery("10.110.149.");
		params_validIP.setDateTimeRange(null);
		JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> result_validIP = manager_.syslogProxy(params_validIP);
		assertSyslogProxyExpectedResult(result_validIP, 10, 961, "2016-12-02T14:32:45.000Z", "173.194.32.183", "10.110.149.227", "www.google.de", "443", "/ads/user-lists/1072409767/");
		
		// Case when query expression is invalid IP. Should return valid but empty result set
		SyslogParams params_invalidIP = new SyslogParams();
		params_invalidIP.setQuery("invalid_ip");
		params_invalidIP.setDateTimeRange(null);
		JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> result_invalidIP = manager_.syslogProxy(params_invalidIP);		
		Assert.assertTrue(result_invalidIP.isOK());
		final SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField> content = result_invalidIP.getContent();
		Assert.assertNotNull(content);
		Assert.assertTrue(result_invalidIP.isOK());
		Assert.assertEquals(0, result_invalidIP.getContent().getRecordsFiltered());
		Assert.assertEquals(0, result_invalidIP.getContent().getRecordsTotal());
		
		// Case when query result ends invalid
		SyslogParams params_invalidResult = new SyslogParams();
		params_invalidResult.setQuery("missing_data");
		params_invalidResult.setDateTimeRange(null);
		JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> result_invalidResult = manager_.syslogProxy(params_invalidResult);
		Assert.assertFalse(result_invalidResult.isOK());		
		
		// Case when query expression is null and dateTimeRange exists. Should return filtered result set
		SyslogParams params_noQueryYesDTR = new SyslogParams();
		params_noQueryYesDTR.setQuery(null);
		DateTimeRange dateTimeRange2 = new DateTimeRange();
		dateTimeRange2.setDateTimeFrom(generateDateTime("2017-02-02 14:37:42.123"));
		dateTimeRange2.setDateTimeTo(generateDateTime("2017-02-02 14:37:42.321"));
		params_noQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> result_noQueryYesDTR = manager_.syslogProxy(params_noQueryYesDTR);
		assertSyslogProxyExpectedResult(result_noQueryYesDTR, 2, 9, "2016-12-02T14:32:45.000Z", "173.194.32.183", "10.110.149.227", "www.google.de", "443", "/ads/user-lists/1072409767/");
		
		// Case when query expression is empty and dateTimeRange exists. Should return filtered result set
		SyslogParams params_noQueryYesDTR2 = new SyslogParams();
		params_noQueryYesDTR2.setQuery("");
		params_noQueryYesDTR2.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> result_noQueryYesDTR2 = manager_.syslogProxy(params_noQueryYesDTR2);
		assertSyslogProxyExpectedResult(result_noQueryYesDTR2, 2, 9, "2016-12-02T14:32:45.000Z", "173.194.32.183", "10.110.149.227", "www.google.de", "443", "/ads/user-lists/1072409767/");
		
		// Case when query expression is valid IP and dateTimeRange exists. Should return filtered result set
		SyslogParams params_yesQueryYesDTR = new SyslogParams();
		params_yesQueryYesDTR.setQuery("10.110.149.");
		params_yesQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> result_yesQueryYesDTR = manager_.syslogProxy(params_yesQueryYesDTR);
		assertSyslogProxyExpectedResult(result_yesQueryYesDTR, 2, 9, "2016-12-02T14:32:45.000Z", "173.194.32.183", "10.110.149.227", "www.google.de", "443", "/ads/user-lists/1072409767/");
	}
	
	/**
	 * Helper method for comparing Syslog Proxy result set with expected values
	 */
	private void assertSyslogProxyExpectedResult(final JsonReturnData<SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField>> result, final int recordsFilteredExpected,
			final int recordsTotalExpected, final String timestampExpected, final String destinationExpected, final String sourceExpected, final String hostExpected,
			final String portExpected, final String uriExpected) {
		Assert.assertTrue(result.isOK());
		final SyslogReturnResult<SyslogProxyReturnResultData, SyslogProxyQueryResultField> content = result.getContent();
		Assert.assertNotNull(content);
		
		Assert.assertEquals(recordsFilteredExpected, content.getRecordsFiltered());
		Assert.assertEquals(recordsTotalExpected, content.getRecordsTotal());
		Assert.assertEquals(timestampExpected, content.getData().get(0).getTimestamp());
		Assert.assertEquals(destinationExpected, content.getData().get(0).getDestination());
		Assert.assertEquals(sourceExpected, content.getData().get(0).getSource());
		Assert.assertEquals(hostExpected, content.getData().get(0).getHost());
		Assert.assertEquals(portExpected, content.getData().get(0).getPort());
		Assert.assertEquals(uriExpected, content.getData().get(0).getUri());
	}	
	
	/**
	 * Helper method for returning exact date time
	 */
	private Date generateDateTime(final String source) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		try {
			return formatter.parse(source);
		} catch (ParseException e) {
			return null;
		}
	}	
	
}
