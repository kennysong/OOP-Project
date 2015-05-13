package oop.project.util;

import java.util.ArrayList;
import java.util.List;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;
import com.twilio.sdk.TwilioRestException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class SMSSender {
    // Some authentication constants for Twilio
    public static final String ACCOUNT_SID = "ACfa82c38392db01c0349ce2682ad9134d";
    public static final String AUTH_TOKEN = "f34a0c90160c7392ee50d7632259fc90";
    public static final String TWILIO_NUM = "+12015523660";

    /* Main method that will send a test SMS. */
    public static void main(String[] args) {
        // sendSMS("+12016321315", "Sent from SMSSender.java.");
        sendSMS("+12016321315", "Hello! Your train is arriving in 10 minutes.");
    }

    /* Static method that will send an SMS. */
    public static void sendSMS(String toNumber, String message) {
        // Set up Twilio object and authentication
        TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
        Account account = client.getAccount();

        // Set up SMS object
        MessageFactory messageFactory = account.getMessageFactory();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("To", toNumber));
        params.add(new BasicNameValuePair("From", TWILIO_NUM));
        params.add(new BasicNameValuePair("Body", message));

        // Send SMS!
        try { Message sms = messageFactory.create(params); }
        catch (TwilioRestException e) { System.out.println(e); }

        // Print in console
        System.out.println("\"" + message + "\" sent to " + toNumber + ".");
    }
}
