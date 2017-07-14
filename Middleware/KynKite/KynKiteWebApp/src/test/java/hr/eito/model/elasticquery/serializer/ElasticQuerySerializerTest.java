
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


package hr.eito.model.elasticquery.serializer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import hr.eito.model.elasticquery.Aggs;
import hr.eito.model.elasticquery.AggsDetailsBuilder;
import hr.eito.model.elasticquery.AggsOrder;
import hr.eito.model.elasticquery.AggsSegment;
import hr.eito.model.elasticquery.AggsTerms;
import hr.eito.model.elasticquery.ElasticQuery;
import hr.eito.model.elasticquery.QueryFieldDetailsBuilder;
import hr.eito.model.elasticquery.QueryFieldLongInformation;
import hr.eito.model.elasticquery.SortDetails;
import hr.eito.model.elasticquery.SortSegment;
import hr.eito.model.elasticquery.QueryLeafClause;

/**
 * Tests the ElasticQuerySerializer.
 *
 * @author Hrvoje
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/config/app-config.xml" })
@ActiveProfiles("test")
public class ElasticQuerySerializerTest {

	/**
	 * Runs before the tests start.
	 */
	@BeforeClass
	public static void testStart() {}
	
	/**
	 * Runs after the tests end.
	 */
	@AfterClass
	public static void testEnd() {}

	/**
	 * Test default construction.
	 */
	@Test
	public void testDefaultCtor() {
		ElasticQuerySerializer eqs = new ElasticQuerySerializer();
		Assert.assertNotNull(eqs);
	}

	/**
	 * Aggs construction.
	 */
	@Test
	public void testStringCtor() {
		ElasticQuerySerializer eqs = new ElasticQuerySerializer(ElasticQuery.class);
		Assert.assertNotNull(eqs);
	}

	/**
	 * Test value serialization - null properties.
	 */
	@Test
	public void testValueSerialization_nullProperties() throws IOException, JsonProcessingException {
		ElasticQuery query = new ElasticQuery();
		Writer jsonWriter = new StringWriter();
	    JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
	    jsonGenerator.setCodec(new ObjectMapper());
	    SerializerProvider serializerProvider = new ObjectMapper().getSerializerProvider();
	    new ElasticQuerySerializer().serialize(query, jsonGenerator, serializerProvider);
	    jsonGenerator.flush();
	    Assert.assertEquals(jsonWriter.toString(), "{}");
	}
	
	/**
	 * Test value serialization - properties set.
	 */
	@Test
	public void testValueSerialization() throws IOException, JsonProcessingException {
		// Prepare all objects
		ElasticQuery query = new ElasticQuery();
		query.setSize(10);
		AggsTerms aggsTerms = new AggsTerms("terms", 
				new AggsDetailsBuilder()
				.setField("host")
				.setSize(1000)
				.setAggsOrder(new AggsOrder("desc"))
				.build());
		Aggs aggs = new Aggs();
		aggs.addAggsSegment(new AggsSegment("hosts", aggsTerms));		
		query.setAggs(aggs);
		query.addSort(
				new SortSegment(
						"@timestamp",
						new SortDetails("desc")
				)
		);
		query.addSources("@timestamp", "host");
		QueryLeafClause wildcard = new QueryLeafClause(
				"wildcard",
				new QueryFieldLongInformation(
						"message", 
						new QueryFieldDetailsBuilder()
						.setValue("1.1.1.1")
						.build()
				)
		);
		query.setQuery(wildcard);
		// Prepare surrounding and serialize
		Writer jsonWriter = new StringWriter();
	    JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
	    jsonGenerator.setCodec(new ObjectMapper());
	    SerializerProvider serializerProvider = new ObjectMapper().getSerializerProvider();
	    new ElasticQuerySerializer().serialize(query, jsonGenerator, serializerProvider);
	    jsonGenerator.flush();
	    
	    Assert.assertEquals("{\"size\":10,"
	    		+ "\"_source\":[\"@timestamp\",\"host\"],"
	    		+ "\"query\":{\"wildcard\":{\"message\":{\"value\":\"1.1.1.1\"}}},"
	    		+ "\"aggs\":{\"hosts\":{\"terms\":{\"field\":\"host\",\"size\":1000,\"order\":{\"cardinal\":\"desc\"}}}},"
	    		+ "\"sort\":[{\"@timestamp\":{\"order\":\"desc\"}}]}", 
	    		jsonWriter.toString());
	}
}
