
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


package hr.eito.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import hr.eito.kynkite.rest.model.ImportModel;
import hr.eito.kynkite.rest.model.ImportModel2;

public class ElasticSearchLoader {
	
	private static ObjectMapper mapper;
	static{
		mapper = new ObjectMapper();
	}

	public static boolean createTemplate(String host, String templateName, String pathToTemplate){
//		curl -XPUT localhost:9200/kyn-netflow-demo -d 
		String statement = "%s -XPUT %s/%s -d' %s'";
		
		String content = loadFileContent(pathToTemplate);
		
		String command = String.format(statement, findAbsolutePath("curl.exe"), host, templateName, content);

		return executeCurlCommand(command);
	}
	
	public static boolean deleteTemplate(String host, String templateName){
		//curl -XDELETE "http://192.168.0.12:9200/kyn-eventdescription"
		String statement = "%s -XDELETE %s/%s";
		
		String command = String.format(statement, findAbsolutePath("curl.exe"), host, templateName);
		
		return executeCurlCommand(command);
	}
	
	public static String getTemplates(String host){
//		curl -XGET "http://192.168.0.12:9200/_cat/indices?v"
		String statement = "%s -XGET %s/_cat/indices?v";
		
		String command = String.format(statement, findAbsolutePath("curl.exe"), host);
		StringBuffer sb = new StringBuffer();
		executeCurlCommand(command, sb);
		
		return sb.toString();
	}
	
	public static boolean loadData(String host, String templateName, String pathToTemplate){
//		curl -XPUT "http://192.168.0.12:9200/kyn-events-demo/event/AVVtRTw1Ypp19M7mWE0s" -d'
//		{"use_case":"many_connections","details":"Actual value: 1.0","@timestamp":"2016-06-18T17:45.000Z","host":"10.47.121.39","script":"mad.rb JUNE2016-13_SCOPE"}'
		String statement = "%s -XPUT \"%s/%s/%s/%s\" -d \" %s\"";
		URL url = ElasticSearchLoader.class.getResource(pathToTemplate);
		List<String> lines = new ArrayList<String>();
		Path curl = findAbsolutePath("curl.exe");
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(url.toURI())));
			
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				
				lines.add(sCurrentLine);

			}
			
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return false;
		}
		boolean success = false;

		
		try {
			for(String s : lines){
				ImportModel queryResult = mapper.readValue(s, ImportModel.class);
				
				String jsonInString = mapper.writeValueAsString(queryResult.get_source());
				
				String command = String.format(statement, curl, host, templateName,queryResult.get_type(), queryResult.get_id(), jsonInString.replace("\"", "\\\""));
				
				executeCurlCommand(command);
				success = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//mozda probati bulk
		//https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html
		if(!success){
			try {
				int i = 0;
				int counter = 1;
				while(true){
					ImportModel2 model = mapper.readValue(lines.get(i), ImportModel2.class);
					
					String json = lines.get(++i).replace("\"", "\\\"");
					json = json.replaceAll("\n", "");
					json = json.replaceAll("\\n", "");
					json = json.replaceAll("\\\\n", "");
					json = json.replace("\\\\\"", "\\\\\\\"");
					
					String command = String.format(statement, curl, host, templateName, model.get_type(), counter, json);
					
					executeCurlCommand(command);
					++counter;
					if(lines.size() == (i+1))
						break;
					++i;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		
		
		return true;

	}
	
	public static String loadFileContent(String pathToTemplate){
		StringBuffer content = new StringBuffer();
		
		URL url = ElasticSearchLoader.class.getResource(pathToTemplate);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(url.toURI())));
			
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				content.append(sCurrentLine.replace(" ", ""));
			}
			
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return content.toString();
		
	}
	
	private static boolean executeCurlCommand(String command){
		boolean retVal = false;
		Process p;
        try {
        	p = Runtime.getRuntime().exec(command);
        	BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            p.waitFor();
            String line = "";
            while ((line = reader.readLine()) != null) {
            	System.out.println(line);
//                if(line.contains("acknowledged")&&line.contains("true")){
//                	retVal=true;
//                	break;
//                }
            }
        }catch(Exception e){
        	e.printStackTrace();
        }
        return retVal;
	}
	private static boolean executeCurlCommand(String command, StringBuffer sb){
		boolean retVal = false;
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			p.waitFor();
			String line = "";
			while ((line = reader.readLine()) != null) {
				sb.append(line);
//                if(line.contains("acknowledged")&&line.contains("true")){
//                	retVal=true;
//                	break;
//                }
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return retVal;
	}
	
	private static Path findAbsolutePath( String curl ){
		String path = System.getenv( "PATH" );
		String[] dirs = path.split( ";" );
		for( String dir: dirs ){
			Path toCurl = Paths.get( dir, curl );
			File curlFile = new File( toCurl.toString() );
			if( curlFile.canExecute() ) 
				return toCurl;
		}
		throw new RuntimeException("curl must be defined in PATH");
	}
	
	
	public static void main(String[] args) {
//		createTemplate("192.168.0.12:9200", "kyn-eventdescription", "/data/kite/kyn-eventdescription-template.json");
		
//		loadData("192.168.0.12:9200", "kyn-eventdescription", "/data/kite/kyn-eventdescription-import-data.json");
		
		getTemplates("192.168.0.12:9200");
		
	}
}
