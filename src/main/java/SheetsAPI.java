
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.api.services.sheets.v4.Sheets;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SheetsAPI {
	/** Application name. */
	private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
			".credentials/sheets.googleapis.com-java-quickstart");

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
	 * ~/.credentials/sheets.googleapis.com-java-quickstart
	 */
	private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS);

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
		InputStream in = SheetsAPI.class.getResourceAsStream("/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		//System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
		return credential;
	}

	/**
	 * Build and return an authorized Sheets API client service.
	 * 
	 * @return an authorized Sheets API client service
	 * @throws IOException
	 */
	public static Sheets getSheetsService() throws IOException {
		Credential credential = authorize();
		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

	// Changes the sheet name to the newest received date
	public static void changeSheetName(String date) throws IOException {
		Sheets service = getSheetsService();

		String spreadsheetId = "1BlPuVJOaTYWeAzmACFD3TDC3OBpC-KTjhoTCIpQDD5s";
		List<Request> requests = new ArrayList<>();

		requests.add(new Request().setUpdateSheetProperties(new UpdateSheetPropertiesRequest()
				.setProperties(new SheetProperties().setSheetId(0).setTitle(date)).setFields("title")));
		BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests);
		service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
		
		
	}

	public static Sheets service;
	public static String spreadsheetId;
	
	// Creates a new spreadsheet
	public static void createSheet(String date) throws IOException {

	service = getSheetsService();
		Spreadsheet s = service.spreadsheets()
				.create(new Spreadsheet().setProperties((new SpreadsheetProperties().setTitle(date)))).execute();
		
		spreadsheetId = s.getSpreadsheetId();
	}



	public static void addToSheets(String subject, String date) throws IOException {
		// Build a new authorized API client service.
		//Sheets service = getSheetsService();
		// SpreadSheet ID
		//String spreadsheetId = "1BlPuVJOaTYWeAzmACFD3TDC3OBpC-KTjhoTCIpQDD5s";
		List<Request> requests = new ArrayList<>();

		// All the values to be added
		List<CellData> values = new ArrayList<>();
		// Email Subject
		values.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(subject)));
		// Date time
		values.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(date)));

		// Combines the values into a request
		requests.add(
				new Request()
						.setUpdateCells(
								new UpdateCellsRequest()
										.setStart(new GridCoordinate().setSheetId(0).setRowIndex(Main.rowIndex)
												.setColumnIndex(0))
								.setRows(Arrays.asList(new RowData().setValues(values)))
								.setFields("userEnteredValue,userEnteredFormat.backgroundColor")));
		// Sends the request to sheets
		BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests);
		service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
		// Increments the row
		Main.rowIndex++;

	}

}