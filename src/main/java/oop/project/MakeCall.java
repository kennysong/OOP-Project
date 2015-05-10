package oop.project;

import java.util.ArrayList;
import java.util.List;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Call;
import com.twilio.sdk.resource.factory.CallFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class MakeCall {
    // Some authentication constants for Twilio
    public static final String ACCOUNT_SID = "ACfa82c38392db01c0349ce2682ad9134d";
    public static final String AUTH_TOKEN = "f34a0c90160c7392ee50d7632259fc90";
    public static final String TWILIO_NUM = "+12015523660";
    public static final String RESPONSE_XML = "http://twimlbin.com/external/0f3bc87dea4f28d6";

    /* Main method that will send a test call. */
    public static void main(String[] args) {
        makeCall("+12016321315");
    }

    /* Static method that will call a number. */
    public static void makeCall(String toNumber) {
        // Set up Twilio object and authentication
        TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
        Account mainAccount = client.getAccount();

        // Set up call object
        CallFactory callFactory = mainAccount.getCallFactory();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("To", toNumber));
        params.add(new BasicNameValuePair("From", TWILIO_NUM));
        params.add(new BasicNameValuePair("Url", RESPONSE_XML));

        // Make the call!
        try { Call call = callFactory.create(params); }
        catch (TwilioRestException e) { System.out.println(e); }

        // Print in console
        System.out.println("Called " + toNumber + ".");
    }
}
