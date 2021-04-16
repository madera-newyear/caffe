import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class t
{
	private static HttpClient createHttpClient ()
	{
		HttpClient httpClient = null;
		Integer timeout = 600;
		final RequestConfig config = RequestConfig.custom ().setConnectTimeout (timeout).setSocketTimeout (timeout).build ();

		httpClient = HttpClientBuilder.create ().setDefaultRequestConfig (config).build ();
		return httpClient;
	}

	private static String getDateInP4RequestedFormat(Date inputDate ){
		Date date = inputDate;
		String ret;
		String formatted = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ssZ").format (date);
		System.out.println("formatted: " + formatted);
		ret = formatted.substring (0, 22) + ":" + formatted.substring (22);
		System.out.println("getDateInP4RequestedFormat " + ret);
		return ret;
	}

	public static void main(String[] args) {

		String asynchRequestId = "112233";
		String accountNo = "134358";
		String imsi = "260980000177847";
		// String msisdn = "48790177847";
		String msisdn = "48790185659";
		String subscrNo = "134124";
		String subscrNoResets = "0";
		String submitBy = "DB";
		String priority =  "0";

		// <RTBEvent type= "workflowNotification" processModel="KSI2" msisdn="48790174572" Action="P4DowngradeQoS" BalanceName="DATA" AdditionalValue="SMS-0" COSOrig="12045" />
		// String RTBEventString = "<RTBEvent type = \"workflow =  Notification\" processModel  =   \"KSI2\" msisdn=\"48790174572\" Action=\"P4DowngradeQoS\" BalanceName=\"DATA\" AdditionalValue=\"SMS-0 us\u0142ugami Missing = bad\" COSOrig=\"12045\" />";
		String RTBEventString = "<RTBEvent type = \"workflowNotification\" processModel  =   \"KSI2\" msisdn=\"48790174572\" Action=\"P4DowngradeQoS\" BalanceName=\"DATA\" AdditionalValue=\"SMS-0 usugami\" COSOrig=\"12045\" />";

		JSONObject RTBEventObject = new JSONObject();
		// RTBEventObject.put("type","workflowNotification");
		// RTBEventObject.put("processModel", "KSI2");
		// RTBEventObject.put("msisdn", "48790174572");
		// RTBEventObject.put("Action", "P4DowngradeQoS");
		// RTBEventObject.put("BalanceName", "DATA");
		// RTBEventObject.put("AdditionalValue", "SMS-0 MISSING");
		// RTBEventObject.put("COSOrig", "12045");
			
        	Date date = new Date();
		String effectiveDt = getDateInP4RequestedFormat(date);
		System.out.println("effectiveDt = " + effectiveDt);

		JSONObject requestParams = new JSONObject();
		requestParams.put("imsi", imsi);
                requestParams.put("msisdn", msisdn);
                requestParams.put("subscrNo", subscrNo);
                requestParams.put("subscrNoResets", subscrNoResets);
                requestParams.put("accountNo", accountNo);
                requestParams.put("submitBy", submitBy);
                requestParams.put("priority", priority);
                requestParams.put("effectiveDate", effectiveDt);


		String tmpString = RTBEventString.replace("<RTBEvent", "").replace("/>", "");
		System.out.println("tmpString = " + tmpString);

		String regex = "\\s*[^=\\s]+\\s*=\\s*\"[^\"]*\"";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(tmpString);
			
		while(matcher.find()) {
			String tagValue = tmpString.substring(matcher.start(), matcher.end());
			System.out.println("found: " +  matcher.start() + " - " + matcher.end() + " " + tagValue);

			String[] tmpString3 = tagValue.split("=");
			int len = tmpString3.length;
			if (len < 2) {
				System.out.println("ignoring " + tagValue);
			} else if (len == 2) {
				String tmpString30 = tmpString3[0].replaceAll("^\\s+", "").replaceAll("\\s+$", ""); // trim leading and trailing spaces
				String tmpString31 = tmpString3[1].replaceAll("^\\s*\"", "").replaceAll("\"\\s*$", "");
				System.out.println("(1) adding " + "|" + tmpString30 + "| + |" + tmpString31 + "|");
				RTBEventObject.put(tmpString30, tmpString31);
			} else {
				String tmpString30 = tmpString3[0].replaceAll("^\\s+", "").replaceAll("\\s+$", ""); // trim leading and trailing spaces
				String tmpString3rest = tmpString3[1];
				for (int i = 2; i < len; i++)
					tmpString3rest = tmpString3rest + "=" + tmpString3[i];
				String tmpString31 = tmpString3rest.replaceAll("^\\s*\"", "").replaceAll("\"\\s*$", "");
				System.out.println("(2) adding " + "|" + tmpString30 + "| + |" + tmpString31 + "|");
				RTBEventObject.put(tmpString30, tmpString31);
			}
		}
		requestParams.put("rtbevent", RTBEventObject);

		StringBuffer url = null;
		HttpPost postUrl = null;
		String protocolUsed = null;
		String hostValue = "10.11.32.48:18004";
		protocolUsed = "http";

		url = new StringBuffer (protocolUsed).append ("://").append (hostValue).append ("/order-management/notifications/")
			.append(asynchRequestId).append ("/job");
		System.out.println("url = " + url);

        	postUrl = new HttpPost(url.toString());
        	HttpClient httpClient = createHttpClient ();

		postUrl.addHeader ("Content-Type", "application/json;charset=utf8");
		postUrl.addHeader ("Accept", "application/json");

		try {
			StringEntity se = new StringEntity(requestParams.toString());
			se.setContentEncoding ("UTF-8");
			se.setContentType ("application/json");
			postUrl.setEntity(se);
			String t1 = EntityUtils.toString(postUrl.getEntity(), "UTF-8");
			System.out.println("postUrl.getEntity " + t1);
			HttpResponse response = httpClient.execute (postUrl);
			System.out.println("response = " + response);
			int statusCode = response.getStatusLine ().getStatusCode ();	
			System.out.println("status code = " + statusCode);
			String t2 = EntityUtils.toString(response.getEntity(), "UTF-8");
			System.out.println("response.getEntity = " + t2);
		} catch(Exception e){
			System.out.println("StringEntity exception");
		}

	}
}


