//This program is to retrieve a transcript from YouTube. It converts escape characters to actual characters after retrieving a transcript.  

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class HttpClient_Transcript {

    public static final String HEROKU_URL = "https://transcribe-2-braille.herokuapp.com/result";
	

    // one instance, reuse
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public static void main(String[] args) throws Exception {

    	HttpClient_Transcript obj = new HttpClient_Transcript();

    
        System.out.println("Retrieve the transcript.");
        String postResults = obj.sendPost(args[0]);
        String printableResults = extractparagraphsB(postResults);
        System.out.println(printableResults);

    }

    private static void extractparagraphs(String pTag) {
		Pattern pattern = Pattern.compile("<p>(.*?)</p>");
		Matcher matcher = pattern.matcher(pTag);
		while (matcher.find()) {
            String replaceEscChar = matcher.group(1).replaceAll("&#33;", "!")
            							 .replaceAll("&#34;", "\"")
									 .replaceAll("&#35;", "#")
            							 .replaceAll("&#36;", "$")
            							 .replaceAll("&#37;", "%")
									 .replaceAll("&#38;", "&")
            							 .replaceAll("&#39;", "'")
            							 .replaceAll("&#40;", "(")
									 .replaceAll("&#41;", ")")              
									 .replaceAll("&#60;", "<")
									 .replaceAll("&#62;", ">") 
									 .replaceAll("&#63;", "?")
									 .replaceAll("&#64;", "@");
                                     System.out.println(replaceEscChar); 
		}
	}

    private static String extractparagraphsB(String pTag) {
		Pattern pattern = Pattern.compile("<p>(.*?)</p>");
		Matcher matcher = pattern.matcher(pTag);
		StringBuilder builder = new StringBuilder();
		while (matcher.find()) {
            String replaceEscChar = matcher.group(1);
            unescapeHtmlRegex(builder, replaceEscChar);
		}
		return builder.toString();
	}
    
    private static String unescapeHtmlRegex(StringBuilder sb, String htmlString) {
    	// The Map would most likely be passed to the method or made static, not created every time.
    	Map<String, String> abbrevs  = new HashMap<String, String>();
    	abbrevs.put("&quot;", "\"");
    	abbrevs.put("&amp;", "&");
    	abbrevs.put("&lt;", "<");
    	abbrevs.put("&gt;", ">");
    	abbrevs.put("&apos;", "'");
    	

    	// pull out escape pattern, either &#nnn; or @abbrev;  Only the above abbreviations are caught
    	Pattern pat = Pattern.compile("&((#[0-9]+)|([a-z]+));");
    	Matcher matcher = pat.matcher(htmlString);
    	while (matcher.find()) {
    		String sequence = matcher.group();
    		String replacement;
    		// determine whether this is a numeric or abbreviation type escape sequence
    		if (sequence.charAt(1) == '#') {
    			// numeric, so get the ASCII value and convert it to a String
    			char asciiNum = (char)Integer.parseInt(sequence.substring(2, sequence.length()-1));
    			replacement = String.valueOf(asciiNum);
    		} else {
    			// abbreviation, so look it up in the Map; put a question mark if it isn't found
    			replacement = abbrevs.getOrDefault(sequence, "?");
    		}
    		matcher.appendReplacement(sb, replacement);
    	}
    	matcher.appendTail(sb);
    	return sb.toString();
    }

    
    private String sendPost(String urlAddress) throws Exception {

        // form parameters
        Map<Object, Object> data = new HashMap<>();
        data.put("yt_url", urlAddress);
        
        HttpRequest request = HttpRequest.newBuilder()
                .POST(buildFormDataFromMap(data))
                .uri(URI.create(HEROKU_URL))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // print status code
        System.out.println("Status code: " + response.statusCode() + "\n");

        // print response body
        return response.body().toString();
        //extractparagraphs(getParagraph);
    }

    private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
        var builder = new StringBuilder();
         
        for (Map.Entry<Object, Object> entry : data.entrySet()) {                    
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }

        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

}



