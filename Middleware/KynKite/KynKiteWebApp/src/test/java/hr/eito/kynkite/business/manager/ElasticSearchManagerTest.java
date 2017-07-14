
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
import hr.eito.model.cmdb.CMDBParams;
import hr.eito.model.cmdb.CMDBReturnResult;
import hr.eito.model.cmdb.CMDBReturnResultData;
import hr.eito.model.hostreport.HostDetailsReportParams;
import hr.eito.model.hostreport.HostDetailsReportReturnResult;
import hr.eito.model.hostreport.HostReportParams;
import hr.eito.model.hostreport.HostReportReturnResult;
import hr.eito.model.netflow.NetflowParams;
import hr.eito.model.newsticker.NewsTickerParams;
import hr.eito.model.newsticker.NewsTickerReturnResult;
import hr.eito.model.ntv.NetflowReturnResult;

/**
 * Tests the ElasticSearchManager.
 *
 * @author Steve Chaplin
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class ElasticSearchManagerTest {

	@Autowired
	private ElasticSearchManager manager_;

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
	 * Test the loadDataByIP mechanism.
	 * <p>
	 * Invalid date time range
	 */
	@Test
	public void testLoadDataByIp_invalidDateTimeRange() {
		NetflowParams params = new NetflowParams();
		DateTimeRange dateTimeRange = new DateTimeRange();
		dateTimeRange.setDateTimeFrom(generateDateTime("2016-01-01 00:00:00.000"));
		dateTimeRange.setDateTimeTo(null);
		params.setDateTimeRange(dateTimeRange);
		JsonReturnData<NetflowReturnResult> result = manager_.loadDataByIP(params);
		Assert.assertEquals(false, result.isOK());
		Assert.assertEquals(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage(), result.getErrorMessage());
	}

	/**
	 * Test the loadDataByIP mechanism.
	 * <p>
	 * Missing IP
	 */
	@Test
	public void testLoadDataByIp_missingIP() {
		NetflowParams params = new NetflowParams();
		params.setIpAddress("");
		JsonReturnData<NetflowReturnResult> result = manager_.loadDataByIP(params);
		Assert.assertEquals(false, result.isOK());
		Assert.assertEquals(CustomError.IP_MISSING.getErrorMessage(), result.getErrorMessage());
	}

	/**
	 * Test the loadDataByIP mechanism.
	 * <p>
	 * Invalid IP
	 */
	@Test
	public void testLoadDataByIp_invalidIP() {
		NetflowParams params = new NetflowParams();
		params.setIpAddress("invalid IP");
		JsonReturnData<NetflowReturnResult> result = manager_.loadDataByIP(params);
		Assert.assertEquals(false, result.isOK());
		Assert.assertEquals(CustomError.IP_INVALID.getErrorMessage(), result.getErrorMessage());
	}

	/**
	 * Test the loadDataByIP mechanism.
	 * <p>
	 * OK - With IP, without DateTimeRange
	 */
	@Test
	public void testLoadDataByIp_OK1() {
		NetflowParams params = new NetflowParams();
		params.setIpAddress("10.201.2.70");
		JsonReturnData<NetflowReturnResult> result = manager_.loadDataByIP(params);
		Assert.assertEquals(true, result.isOK());
		Assert.assertEquals(2, result.getContent().getCounts().get("2016-W29")[0]);
		Assert.assertEquals(4, result.getContent().getCounts().get("2016-W30")[0]);
		Assert.assertEquals(5, result.getContent().getCounts().get("2016-W30")[1]);
		Assert.assertEquals(2, result.getContent().getCounts().get("2016-W30")[2]);
		Assert.assertEquals(2, result.getContent().getInfo().getSize());
		Assert.assertEquals(5, result.getContent().getInfo().getMaximum());
	}

	/**
	 * Test the loadDataByIP mechanism.
	 * <p>
	 * OK - With IP, with DateTimeRange
	 */
	@Test
	public void testLoadDataByIp_OK2() {
		NetflowParams params = new NetflowParams();
		params.setIpAddress("10.201.2.70");
		DateTimeRange dateTimeRange = new DateTimeRange();
		dateTimeRange.setDateTimeFrom(generateDateTime("2016-01-01 00:00:00.000"));
		dateTimeRange.setDateTimeTo(generateDateTime("2017-01-01 07:00:00.000"));
		params.setDateTimeRange(dateTimeRange);
		JsonReturnData<NetflowReturnResult> result = manager_.loadDataByIP(params);
		Assert.assertEquals(true, result.isOK());
		Assert.assertEquals(2, result.getContent().getCounts().get("2016-W29")[0]);
		Assert.assertEquals(4, result.getContent().getCounts().get("2016-W30")[0]);
		Assert.assertEquals(5, result.getContent().getCounts().get("2016-W30")[1]);
		Assert.assertEquals(2, result.getContent().getCounts().get("2016-W30")[2]);
		Assert.assertEquals(2, result.getContent().getInfo().getSize());
		Assert.assertEquals(5, result.getContent().getInfo().getMaximum());
	}

	/**
	 * Test the CMDB report.
	 * <p>
	 * Case with null params instance or params instance with no parameter set
	 */
	@Test
	public void testCMDBReport_01() {
		CMDBParams params_nullParameter = null;
		JsonReturnData<CMDBReturnResult> result_nullParameter = manager_.cmdbReport(params_nullParameter);
		Assert.assertFalse(result_nullParameter.isOK());

		CMDBParams params_noParameters = new CMDBParams();
		JsonReturnData<CMDBReturnResult> result_noParameters = manager_.cmdbReport(params_noParameters);
		Assert.assertFalse(result_noParameters.isOK());
	}

	/**
	 * Test the empty CMDB report.
	 * <p>
	 * Case with valid IP address which returns no results
	 */
	@Test
	public void testCMDBReport_02() {
		CMDBParams params = new CMDBParams();
		params.setIpAddress("1.1.1.1");

		JsonReturnData<CMDBReturnResult> result = manager_.cmdbReport(params);
		Assert.assertTrue(result.isOK());

		CMDBReturnResult content = result.getContent();
		Assert.assertEquals(0, content.getData().size());
	}

	/**
	 * Test the empty CMDB report.
	 * <p>
	 * Case with invalid IP which returns empty result set
	 */
	@Test
	public void testCMDBReport_03() {
		CMDBParams params = new CMDBParams();
		params.setIpAddress("invalid_ip");

		JsonReturnData<CMDBReturnResult> result = manager_.cmdbReport(params);
		Assert.assertTrue(result.isOK());

		CMDBReturnResult content = result.getContent();
		Assert.assertEquals(0, content.getData().size());
	}

	/**
	 * Test the empty CMDB report.
	 * <p>
	 * Case with empty IP which return empty result set
	 */
	@Test
	public void testCMDBReport_04() {
		CMDBParams params = new CMDBParams();
		params.setIpAddress("");

		JsonReturnData<CMDBReturnResult> result = manager_.cmdbReport(params);
		Assert.assertTrue(result.isOK());

		CMDBReturnResult content = result.getContent();
		Assert.assertEquals(0, content.getData().size());
	}

	/**
	 * Test the CMDB report.
	 * <p>
	 * Case with valid IP address which returns some results
	 */
	@Test
	public void testCMDBReport_05() {
		CMDBParams params = new CMDBParams();
		params.setIpAddress("10.0.0.2");

		JsonReturnData<CMDBReturnResult> result = manager_.cmdbReport(params);
		Assert.assertTrue(result.isOK());

		CMDBReturnResult content = result.getContent();
		Assert.assertEquals(4, content.getData().size());

		CMDBReturnResultData entry = content.getData().get(0);
		checkCMDBEntry(entry, "10.0.0.2", "255.255.255.255", 32, "testing.com. (active PTR), testing.net.", "IP-Admin", "Bochum", "DE");
		entry = content.getData().get(1);
	}

	/**
	 * Test the CMDB report.
	 * <p>
	 * Case with invalid IP which returns error result set
	 */
	@Test
	public void testCMDBReport_06() {
		CMDBParams params = new CMDBParams();
		params.setIpAddress("missing");

		JsonReturnData<CMDBReturnResult> result = manager_.cmdbReport(params);
		Assert.assertFalse(result.isOK());
	}

	/**
	 * Check a CMDB entry has the expected contents.
	 *
	 * @param ip the ip address to check the entry against
	 * @param mask the mask to check the entry against
	 * @param maskCidr the cidr to check the entry against
	 * @param name the name to check the entry against
	 * @param source the source to check the entry against
	 * @param city the city to check the entry against
	 * @param country the country to check the entry against
	 */
	private void checkCMDBEntry(final CMDBReturnResultData entry,
							final String ip,
							final String mask,
							final int maskCidr,
							final String name,
							final String source,
							final String city,
							final String country) {

		// Mandatory.
		Assert.assertEquals(ip, entry.getIp());
		Assert.assertEquals(mask, entry.getMask());
		Assert.assertEquals(maskCidr, entry.getMaskCidr());
		Assert.assertEquals(name, entry.getName());
		Assert.assertEquals(source, entry.getSource());

		// Optional.
		if (null == city) {
			Assert.assertNull(entry.getCity());
		}
		else {
			Assert.assertEquals(city, entry.getCity());
		}
		if (null == country) {
			Assert.assertNull(entry.getCountry());
		}
		else {
			Assert.assertEquals(country, entry.getCountry());
		}
	}

	/**
	 * Test the HostReport part
	 * <p>
	 * Testing HostReport part with different states of HostReportParams instance
	 */
	@Test
	public void testHostReport() {
		// Case when datetimerange is set, but broken
		HostReportParams params_brokenDateTimeRange = new HostReportParams();
		params_brokenDateTimeRange.setIpAddress(null);
		DateTimeRange dateTimeRange_broken = new DateTimeRange();
		dateTimeRange_broken.setDateTimeFrom(new Date());
		dateTimeRange_broken.setDateTimeTo(null);
		params_brokenDateTimeRange.setDateTimeRange(dateTimeRange_broken);
		JsonReturnData<HostReportReturnResult> result_brokenDateTimeRange = manager_.hostReport(params_brokenDateTimeRange);
		Assert.assertFalse(result_brokenDateTimeRange.isOK());
		Assert.assertEquals(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage(), result_brokenDateTimeRange.getErrorMessage());

		// HostReportParams instance is null
		HostReportParams params1 = null;
		JsonReturnData<HostReportReturnResult> result1 = manager_.hostReport(params1);
		assertHostReportExpectedResult(result1, 19, "10.47.9.10", 16, "many_connections, scanner, many_destinations", "2016-06-18T15:45:00.000Z", "2016-06-19T02:45:00.000Z");

		// parameters are null
		HostReportParams params2 = new HostReportParams();
		params2.setIpAddress(null);
		params2.setDateTimeRange(null);
		JsonReturnData<HostReportReturnResult> result2 = manager_.hostReport(params2);
		assertHostReportExpectedResult(result2, 19, "10.47.9.10", 16, "many_connections, scanner, many_destinations", "2016-06-18T15:45:00.000Z", "2016-06-19T02:45:00.000Z");

		// parameter ipAddress is empty string
		HostReportParams params3 = new HostReportParams();
		params3.setIpAddress("");
		params3.setDateTimeRange(null);
		JsonReturnData<HostReportReturnResult> result3 = manager_.hostReport(params3);
		assertHostReportExpectedResult(result3, 19, "10.47.9.10", 16, "many_connections, scanner, many_destinations", "2016-06-18T15:45:00.000Z", "2016-06-19T02:45:00.000Z");

		// Case when query expression is invalid IP or undiscovered piece of message. Should return valid but empty result set
		HostReportParams params4 = new HostReportParams();
		params4.setIpAddress("invalid_ip");
		JsonReturnData<HostReportReturnResult> result_invalidIP = manager_.hostReport(params4);
		Assert.assertTrue(result_invalidIP.isOK());
		Assert.assertNotNull(result_invalidIP.getContent());
		Assert.assertEquals(0, result_invalidIP.getContent().getData().size());

		// Case when query result ends invalid
		HostReportParams params_invalidData = new HostReportParams();
		params_invalidData.setIpAddress("missing_data");
		JsonReturnData<HostReportReturnResult> result_invalidData = manager_.hostReport(params_invalidData);
		Assert.assertFalse(result_invalidData.isOK());

		// parameter ipAddress is valid IP
		HostReportParams params5 = new HostReportParams();
		params5.setIpAddress("10.210.2.72");
		params5.setDateTimeRange(null);
		JsonReturnData<HostReportReturnResult> result5 = manager_.hostReport(params5);
		assertHostReportExpectedResult(result5, 1, params5.getIpAddress(), 10, "scanner", "2016-06-15T07:43:30.055Z", "2016-06-15T10:43:30.055Z");

		// Case when query expression is null and dateTimeRange exists. Should return filtered result set
		HostReportParams params_noQueryYesDTR = new HostReportParams();
		params_noQueryYesDTR.setIpAddress(null);
		DateTimeRange dateTimeRange2 = new DateTimeRange();
		dateTimeRange2.setDateTimeFrom(generateDateTime("2016-06-15 07:43:30.000"));
		dateTimeRange2.setDateTimeTo(generateDateTime("2016-06-15 07:43:31.000"));
		params_noQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<HostReportReturnResult> result_noQueryYesDTR = manager_.hostReport(params_noQueryYesDTR);
		assertHostReportExpectedResult(result_noQueryYesDTR, 1, "10.210.2.72", 10, "scanner", "2016-06-15T07:43:30.055Z", "2016-06-15T07:43:30.055Z");

		// Case when query expression is empty and dateTimeRange exists. Should return filtered result set
		HostReportParams params_noQueryYesDTR2 = new HostReportParams();
		params_noQueryYesDTR2.setIpAddress("");
		params_noQueryYesDTR2.setDateTimeRange(dateTimeRange2);
		JsonReturnData<HostReportReturnResult> result_noQueryYesDTR2 = manager_.hostReport(params_noQueryYesDTR);
		assertHostReportExpectedResult(result_noQueryYesDTR2, 1, "10.210.2.72", 10, "scanner", "2016-06-15T07:43:30.055Z", "2016-06-15T07:43:30.055Z");

		// Case when query expression is valid IP and dateTimeRange exists. Should return filtered result set
		HostReportParams params_yesQueryYesDTR = new HostReportParams();
		params_yesQueryYesDTR.setIpAddress("10.210.2.72");
		params_yesQueryYesDTR.setDateTimeRange(dateTimeRange2);
		JsonReturnData<HostReportReturnResult> result_yesQueryYesDTR = manager_.hostReport(params_yesQueryYesDTR);
		assertHostReportExpectedResult(result_yesQueryYesDTR, 1, "10.210.2.72", 10, "scanner", "2016-06-15T07:43:30.055Z", "2016-06-15T07:43:30.055Z");
	}

	/**
	 * Helper method for comparing Host Report result set with expected values
	 */
	private void assertHostReportExpectedResult(final JsonReturnData<HostReportReturnResult> result, final int sizeExpected, final String hostExpected,
			final int severityExpected, final String eventsExpected, final String firstDetectionExpected, final String lastDetectionExpected) {
		Assert.assertTrue(result.isOK());
		final HostReportReturnResult content = result.getContent();
		Assert.assertNotNull(content);

		Assert.assertEquals(sizeExpected, content.getData().size());
		Assert.assertEquals(hostExpected, content.getData().get(0).getHost());
		Assert.assertEquals(severityExpected, content.getData().get(0).getSeverity());
		Assert.assertEquals(eventsExpected, content.getData().get(0).getEvents());
		// Note here, we are not serializing so the date conversion is not active.
		Assert.assertEquals(firstDetectionExpected, content.getData().get(0).getFirstDetection());
		Assert.assertEquals(lastDetectionExpected, content.getData().get(0).getLastDetection());
	}

	/**
	 * Test the host details report.
	 */
	@Test
	public void testHostDetailsReport() {
		// Case when datetimerange is set, but broken
		HostDetailsReportParams params_brokenDateTimeRange = new HostDetailsReportParams();
		params_brokenDateTimeRange.setIpAddress(null);
		DateTimeRange dateTimeRange_broken = new DateTimeRange();
		dateTimeRange_broken.setDateTimeFrom(new Date());
		dateTimeRange_broken.setDateTimeTo(null);
		params_brokenDateTimeRange.setDateTimeRange(dateTimeRange_broken);
		JsonReturnData<HostDetailsReportReturnResult> result_brokenDateTimeRange = manager_.hostDetailsReport(params_brokenDateTimeRange);
		Assert.assertFalse(result_brokenDateTimeRange.isOK());
		Assert.assertEquals(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage(), result_brokenDateTimeRange.getErrorMessage());

		// HostDetailsReportParams instance is null
		final HostDetailsReportParams params1 = null;
		JsonReturnData<HostDetailsReportReturnResult> result1 = manager_.hostDetailsReport(params1);
		Assert.assertFalse(result1.isOK());

		// ipAddress is null
		final HostDetailsReportParams params2 = new HostDetailsReportParams();
		params2.setIpAddress(null);
		JsonReturnData<HostDetailsReportReturnResult> result2 = manager_.hostDetailsReport(params2);
		Assert.assertFalse(result2.isOK());
		Assert.assertEquals(CustomError.IP_MISSING.getErrorMessage(), result2.getErrorMessage());

		// ipAddress is empty
		final HostDetailsReportParams params3 = new HostDetailsReportParams();
		params3.setIpAddress("");
		JsonReturnData<HostDetailsReportReturnResult> result3 = manager_.hostDetailsReport(params3);
		Assert.assertFalse(result3.isOK());
		Assert.assertEquals(CustomError.IP_MISSING.getErrorMessage(), result3.getErrorMessage());

		// ipAddress is set, no datetime range
		final HostDetailsReportParams params4 = new HostDetailsReportParams();
		params4.setIpAddress("10.47.9.10");
		JsonReturnData<HostDetailsReportReturnResult> result4 = manager_.hostDetailsReport(params4);
		assertHostDetailsReportExpectedResult(result4, 3, "many_connections", 11, "2016-06-18T15:45:00.000Z", "2016-06-19T02:45:00.000Z");

		// ipAddress is set, set datetime range
		final HostDetailsReportParams params5 = new HostDetailsReportParams();
		params5.setIpAddress("10.47.9.10");
		DateTimeRange dateTimeRange2 = new DateTimeRange();
		dateTimeRange2.setDateTimeFrom(generateDateTime("2016-06-18 15:44:00.000"));
		dateTimeRange2.setDateTimeTo(generateDateTime("2016-06-18 15:46:00.000"));
		params5.setDateTimeRange(dateTimeRange2);
		JsonReturnData<HostDetailsReportReturnResult> result5 = manager_.hostDetailsReport(params5);
		assertHostDetailsReportExpectedResult(result5, 1, "many_connections", 1, "2016-06-18T15:45:00.000Z", "2016-06-18T15:45:00.000Z");

		// Check with invalid IP
		final HostDetailsReportParams params6 = new HostDetailsReportParams();
		params6.setIpAddress("invalid_ip");
		JsonReturnData<HostDetailsReportReturnResult> result6 = manager_.hostDetailsReport(params6);
		Assert.assertFalse(result6.isOK());
		Assert.assertEquals(CustomError.IP_INVALID.getErrorMessage(), result6.getErrorMessage());

		// Check with IP that brings no data
		final HostDetailsReportParams params7 = new HostDetailsReportParams();
		params7.setIpAddress("10.47.9.9");
		JsonReturnData<HostDetailsReportReturnResult> result7 = manager_.hostDetailsReport(params7);
		Assert.assertTrue(result7.isOK());
		Assert.assertEquals(0, result7.getContent().getData().size());
	}

	/**
	 * Helper method for comparing Host Details Report result set with expected values
	 */
	private void assertHostDetailsReportExpectedResult(final JsonReturnData<HostDetailsReportReturnResult> result, final int sizeExpected, final String eventTypeExpected,
			final int countExpected, final String firstDetectionExpected, final String lastDetectionExpected) {
		Assert.assertTrue(result.isOK());
		final HostDetailsReportReturnResult content = result.getContent();
		Assert.assertNotNull(content);

		Assert.assertEquals(sizeExpected, content.getData().size());
		Assert.assertEquals(eventTypeExpected, content.getData().get(0).getEventType());
		Assert.assertEquals(countExpected, content.getData().get(0).getNumber());
		// Note here, we are not serializing so the date conversion is not active.
		Assert.assertEquals(firstDetectionExpected, content.getData().get(0).getFirstDetection());
		Assert.assertEquals(lastDetectionExpected, content.getData().get(0).getLastDetection());
	}

	/**
	 * Test the newsticker methods.
	 */
	@Test
	public void testNewsTicker() {
		DateTimeRange dateTimeRange = new DateTimeRange();
		dateTimeRange.setDateTimeFrom(generateDateTime("2016-11-29 23:59:02.000"));
		dateTimeRange.setDateTimeTo(generateDateTime("2016-11-29 23:59:03.000"));

		// Case when datetimerange is set, but broken
		NewsTickerParams params_brokenDateTimeRange = new NewsTickerParams();
		params_brokenDateTimeRange.setQuery(null);
		DateTimeRange dateTimeRange_broken = new DateTimeRange();
		dateTimeRange_broken.setDateTimeFrom(new Date());
		dateTimeRange_broken.setDateTimeTo(null);
		params_brokenDateTimeRange.setDateTimeRange(dateTimeRange_broken);
		JsonReturnData<NewsTickerReturnResult> result_brokenDateTimeRange = manager_.newsticker(params_brokenDateTimeRange);
		Assert.assertFalse(result_brokenDateTimeRange.isOK());
		Assert.assertEquals(CustomError.INVALID_DATE_TIME_RANGE.getErrorMessage(), result_brokenDateTimeRange.getErrorMessage());

		// NewsTickerParams instance is null
		NewsTickerParams params_null = null;
		JsonReturnData<NewsTickerReturnResult> result1 = manager_.newsticker(params_null);
		assertNewstickerExpectedResult(result1, 5, 18);

		// parameters are null
		NewsTickerParams params2 = new NewsTickerParams();
		JsonReturnData<NewsTickerReturnResult> result2 = manager_.newsticker(params2);
		assertNewstickerExpectedResult(result2, 5, 18);

		// parameter ipAddress is empty string
		NewsTickerParams params3 = new NewsTickerParams();
		params3.setQuery("");
		JsonReturnData<NewsTickerReturnResult> result3 = manager_.newsticker(params3);
		assertNewstickerExpectedResult(result3, 5, 18);

		// size is 0
		NewsTickerParams params4 = new NewsTickerParams();
		params4.setSize(0);
		JsonReturnData<NewsTickerReturnResult> result4 = manager_.newsticker(params4);
		assertNewstickerExpectedResult(result4, 0, 0);

		// size is 0, ipAddress is set
		NewsTickerParams params5 = new NewsTickerParams();
		params5.setSize(0);
		params5.setQuery("10.11.12.60");
		JsonReturnData<NewsTickerReturnResult> result5 = manager_.newsticker(params5);
		assertNewstickerExpectedResult(result5, 0, 0);

		// size is 1, ipAddress not set, datetimerange not set
		NewsTickerParams params6 = new NewsTickerParams();
		params6.setSize(1);
		params6.setQuery(null);
		params6.setDateTimeRange(null);
		JsonReturnData<NewsTickerReturnResult> result6 = manager_.newsticker(params6);
		assertNewstickerExpectedResult(result6, 1, 1);

		// size is 1, ipAddress set, datetimerange not set
		NewsTickerParams params7 = new NewsTickerParams();
		params7.setSize(1);
		params7.setQuery("10.11.12.60");
		params7.setDateTimeRange(null);
		JsonReturnData<NewsTickerReturnResult> result7 = manager_.newsticker(params7);
		assertNewstickerExpectedResult(result7, 1, 1);

		// size is 1, ipAddress not set, datetimerange set
		NewsTickerParams params8 = new NewsTickerParams();
		params8.setSize(1);
		params8.setQuery(null);
		params8.setDateTimeRange(dateTimeRange);
		JsonReturnData<NewsTickerReturnResult> result8 = manager_.newsticker(params8);
		assertNewstickerExpectedResult(result8, 1, 1);

		// size is 1, ipAddress not set, datetimerange set
		NewsTickerParams params9 = new NewsTickerParams();
		params9.setSize(1);
		params9.setQuery("10.11.12.60");
		params9.setDateTimeRange(dateTimeRange);
		JsonReturnData<NewsTickerReturnResult> result9 = manager_.newsticker(params9);
		assertNewstickerExpectedResult(result9, 1, 1);

		// Case when query expression is invalid IP or undiscovered piece of message. Should return valid but empty result set
		NewsTickerParams params10 = new NewsTickerParams();
		params10.setQuery("invalid_ip");
		JsonReturnData<NewsTickerReturnResult> result_invalidIP = manager_.newsticker(params10);
		assertNewstickerExpectedResult(result_invalidIP, 0, 0);
	}

	/**
	 * Helper method for comparing Newsticker result set with expected values
	 */
	private void assertNewstickerExpectedResult(final JsonReturnData<NewsTickerReturnResult> result, final int recordsFilteredExpected,	final int recordsTotalExpected) {
		Assert.assertTrue(result.isOK());
		final NewsTickerReturnResult content = result.getContent();
		Assert.assertNotNull(content);

		Assert.assertEquals(recordsFilteredExpected, content.getRecordsFiltered());
		Assert.assertEquals(recordsTotalExpected, content.getRecordsTotal());
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
