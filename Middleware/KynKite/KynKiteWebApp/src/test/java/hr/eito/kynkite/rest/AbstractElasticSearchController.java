
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


package hr.eito.kynkite.rest;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import hr.eito.model.JsonReturnData;
import hr.eito.model.hostreport.HostReportParams;
import hr.eito.model.hostreport.HostReportReturnResult;
import hr.eito.model.netflow.NetflowParams;

/**
 * @author Danijel Soltic
 *
 */
public abstract class AbstractElasticSearchController {
	@Autowired
	ElasticSearchController rest;
	
	private static String newsTickerURI = "rest/newsTicker";
	private static String resetsURI = "rest/resets";
	private static String hostReportURI = "rest/hostReport";
	private static String syslogASAURI = "rest/syslogASA";
	private static String laraCiscoACLURI = "rest/laraCiscoACL";
	private static String laraDetailsTableURI = "rest/laraDetailsTable";
	private static String laraRulesetTableURI = "rest/laraRulesetTable";
	private static String syslogRouterURI = "rest/syslogRouter";
	private static String syslogVoiceURI = "rest/syslogVoice";
	private static String syslogDHCPURI = "rest/syslogDHCP";
	private static String syslogIPSURI = "rest/syslogIPS";
	private static String getJsonFileURI = "rest/getJsonFile";
	private static String getTextFileURI = "rest/getTextFile";
	private static String getPdfFileURI = "rest/getPdfFile";

	abstract URI getServerUri();
	
	private String callRest(String url) throws HttpException, IOException{
		HttpClient httpClient = new HttpClient();
		
		GetMethod request = new GetMethod( url );
		int statusCode = httpClient.executeMethod( request );

		Assert.assertEquals(statusCode, HttpStatus.SC_OK);

		String json = new String(request.getResponseBodyAsString());
		Assert.assertNotNull(json);
		
		return json;
	}

	/**
	 * Test the HostReport rest endpoint.
	 * <p>
	 * Example where params equals to null. Should return non null value
	 * 
	 */
	@Test
	public void hostReportDirectTest_NullParams(){
		JsonReturnData<HostReportReturnResult> test = this.rest.hostReport(null);
		Assert.assertNotNull(test);
	}
	
	/**
	 * Test the HostReport rest endpoint.
	 * <p>
	 * Example where params ipAddress is empty. Should return non null value
	 * 
	 */
	@Test
	public void hostReportDirectTest_EmptyIpAddress(){
		HostReportParams params = new HostReportParams();
		params.setIpAddress("");
		JsonReturnData<HostReportReturnResult> test = this.rest.hostReport(params);
		Assert.assertNotNull(test);
	}
	
	/**
	 * Test the HostReport rest endpoint.
	 * <p>
	 * Example where params ipAddress is invalid. Should return non null value
	 * 
	 */
	@Test
	public void hostReportDirectTest_InvalidIpAddress(){
		HostReportParams params = new HostReportParams();
		params.setIpAddress("invalid_ip");
		JsonReturnData<HostReportReturnResult> test = this.rest.hostReport(params);
		Assert.assertNotNull(test);
	}
	
	/**
	 * Test the HostReport rest endpoint.
	 * <p>
	 * Example where params ipAddress is valid. Should return non null value
	 * 
	 */
	@Test
	public void hostReportDirectTest_ValidIpAddress(){
		HostReportParams params = new HostReportParams();
		params.setIpAddress("1.1.1.1");
		JsonReturnData<HostReportReturnResult> test = this.rest.hostReport(params);
		Assert.assertNotNull(test);
	}

	@Test
	public void hostReportRestTest() throws HttpException, IOException{
		
		callRest(getServerUri().toURL() + hostReportURI);
	}

	@Test
	public void syslogASARestTest() throws HttpException, IOException{
		
		callRest(getServerUri().toURL() + syslogASAURI);
		callRest(getServerUri().toURL() + syslogASAURI +"/*");
		
	}
	
	@Test
	public void resetsTest() throws HttpException, IOException{

		callRest(getServerUri().toURL() + resetsURI + "/10.79.73.15");

	}
	
	@Test
	public void laraCiscoRestTest() throws HttpException, IOException{
		
		callRest(getServerUri().toURL() + laraCiscoACLURI);

	}
	
	@Test
	public void laraDetailsRestTest() throws HttpException, IOException{

		callRest(getServerUri().toURL() + laraDetailsTableURI);

	}
	
	@Test
	public void laraRulesetRestTest() throws HttpException, IOException{

		callRest( getServerUri().toURL() + laraRulesetTableURI);

	}
	
	@Test
	public void syslogRouterRestTest() throws HttpException, IOException{
		
		callRest(getServerUri().toURL() + syslogRouterURI);
		callRest(getServerUri().toURL() + syslogRouterURI +"/*");
	}

	@Test
	public void syslogVoiceRestTest() throws HttpException, IOException{
		
		callRest(getServerUri().toURL() + syslogVoiceURI);
		callRest(getServerUri().toURL() + syslogVoiceURI +"/*");
	}
	
	@Test
	public void syslogDHCPRestTest() throws HttpException, IOException{
		
		callRest(getServerUri().toURL() + syslogDHCPURI);
		callRest(getServerUri().toURL() + syslogDHCPURI +"/*");
	}

	@Test
	public void syslogIPSRestTest() throws HttpException, IOException{
		
		callRest(getServerUri().toURL() + syslogIPSURI);
		callRest(getServerUri().toURL() + syslogIPSURI +"/*");
	}


	@Test
	public void getJsonFileRestTest() throws HttpException, IOException{

		callRest(getServerUri().toURL() + getJsonFileURI);

	}
	
	
	@Test
	public void getTextFileRestTest() throws HttpException, IOException{
		
		callRest(getServerUri().toURL() + getTextFileURI);
		
	}
	
	
	@Test
	public void getPdfFileRestTest() throws HttpException, IOException{
		
		callRest(getServerUri().toURL() + getPdfFileURI);
		
	}
	
}
