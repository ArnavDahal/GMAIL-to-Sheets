import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.Gmail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

// Start code taken from: https://developers.google.com/gmail/api/quickstart/java
public class GmailAPI {
	/** Application name. */
	private static final String APPLICATION_NAME = "Gmail API Java Quickstart";

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
			".credentials/gmail-java-quickstart");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/**
	 * Global instance of the scopes required by this quickstart.
	 *
	 * If modifying these scopes, delete your previously saved credentials at
	 * ~/.credentials/gmail-java-quickstart
	 */
	private static final List<String> SCOPES = Arrays.asList(GmailScopes.MAIL_GOOGLE_COM);

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	public static Credential authorize() throws IOException {
		// Load client secrets.
		InputStream in = GmailAPI.class.getResourceAsStream("/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
		return credential;
	}

	/**
	 * Build and return an authorized Gmail client service.
	 * 
	 * @return an authorized Gmail client service
	 * @throws IOException
	 */
	public static Gmail getGmailService() throws IOException {
		Credential credential = authorize();
		return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
	}

	// Taken from: https://developers.google.com/gmail/api/v1/reference/users/messages/get
	public static MimeMessage getMimeMessage(Gmail service, String userId, String messageId)
			throws IOException, MessagingException {
		Message message = service.users().messages().get(userId, messageId).setFormat("raw").execute();

		byte[] emailBytes = Base64.decodeBase64(message.getRaw());

		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

		return email;
	}

	// Taken from: https://developers.google.com/gmail/api/v1/reference/users/messages/list
	public static List<Message> listMessagesMatchingQuery(Gmail service, String userId, String query)
			throws IOException {
		ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();

		List<Message> messages = new ArrayList<Message>();
		while (response.getMessages() != null) {
			messages.addAll(response.getMessages());
			if (response.getNextPageToken() != null) {
				String pageToken = response.getNextPageToken();
				response = service.users().messages().list(userId).setQ(query).setPageToken(pageToken).execute();
			} else {
				break;
			}
		}

		return messages;
	}

	public static void getEmails() throws IOException {

		// To change the sheet name to a date
		boolean firstNameChange = true;
		// Row counter init
		Main.rowIndex = 0;

		String emailSubject;
		String messageID;
		Date emailRec;
		MimeMessage curEmail;

		// To convert date the DDMMYY
		Format formatter = new SimpleDateFormat("dd/MM/yy");
		String dateConv;

		// Build a new authorized API client service.
		Gmail service = getGmailService();

		// Print the labels in the user's account.
		String user = "me";

		List<Message> unreadEmails = listMessagesMatchingQuery(service, user, "is:unread");
		// For loop to go through the inbox
		for (Message message : unreadEmails) {
			messageID = message.getId();
			try {
				curEmail = getMimeMessage(service, user, messageID);
				emailSubject = curEmail.getSubject();
				emailRec = curEmail.getSentDate();

				if (firstNameChange) {
					dateConv = formatter.format(emailRec);
					SheetsAPI.createSheet(dateConv.toString());
					firstNameChange = false;
				}

				SheetsAPI.addToSheets(emailSubject, emailRec.toString());
			} catch (MessagingException e) {

			}
		}

	}

}