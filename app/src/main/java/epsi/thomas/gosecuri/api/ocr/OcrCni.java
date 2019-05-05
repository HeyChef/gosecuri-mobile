package epsi.thomas.gosecuri.api.ocr;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OcrCni extends AsyncTask<String, Void, Void> {
    public static String _key1 = "556248739298583",
            _key2 = "QIBHYZTGKENTEZF",
            _url = "https://www.ocrmobile.cloud/public/api/",
            _apiid = "M8J2O-9ZIH3-99RU3-YE9L4-Y06HC",
            _apipasswd = "QHUYAVIHRMUUEBW",
            _apiidPwd = _apiid + ":" + _apipasswd;

    static byte[] _encodedBytes = Base64.encodeBase64(_apiidPwd.getBytes());
    public static String _apiidpwd64 = new String(_encodedBytes),
            _basicAuth = "Basic " + _apiidpwd64,
            processJson = "{\"task\": \"scan\","
                    +"\"type\": \"img\","
                    +"\"taxes\": [2.1,5.5,10,20],"
                    +"\"device\": \"smartphone\","
                    +"\"country\": \"FR\","
                    +"\"fileName\": \"temp_img_file.jpg\","
                    +"\"language\": \"FR\","
                    +"\"useCrop\": false,"
                    +"\"supportId\": 7 }";



    public static File file;

    // this function is used to authenticate and retrieve the token.
    private String authenticate(String addressAuth) throws Exception {

        StringBuilder stringBuilder = new StringBuilder(addressAuth);

        URL obj = new URL(stringBuilder.toString());
        HttpURLConnection con = (HttpURLConnection) obj.openConnection(); // open connection

        System.out.println(stringBuilder.toString());

        con.setRequestMethod("GET"); // request method.
        con.setRequestProperty("Authorization", _basicAuth); // authentication.

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())); // stock the response.

        String line, response = new String();
        while ((line = in.readLine()) != null) {
            // copy the response
            response += line;
        }
        // close the BufferedReader
        in.close();

        // need for the getString. Extraction in JSON format.
        JSONObject jsonExtracted = new JSONObject(response);

        // display the response.
        System.out.println(response);

        // return the token. The token is required for the treat function.
        return jsonExtracted.getString("token");
    }

    // This function is used to send a picture and a JSON format file according to the token. (which is in the url)
    private int process(String addressTreat) throws Exception {
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        HttpClient httpClient = new DefaultHttpClient();

        /*HttpEntity entity = MultipartEntityBuilder.create()
                // allows to send the JSON file.
                .addTextBody("processJson", processJson)
                // allows to send the picture.
                .addBinaryBody("image", file, ContentType.create("application/octet-stream"), file.getName())
                .build();*/

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addTextBody("processJson", processJson);
                builder.addBinaryBody("image", file, ContentType.create("application/octet-stream"), file.getName());

        HttpPost request = new HttpPost(addressTreat);

        HttpEntity entity = builder.build();

        request.setHeader("Authorization", _basicAuth); // authentication.
        request.setEntity(entity);

        // retrieves the response.
        String response = httpClient.execute(request, responseHandler);

        // need for the getString. Extraction in JSON format.
        JSONObject jsonExtracted = new JSONObject(response);
        System.out.println(entity);
        System.out.println(request);
        System.out.println(response);

        // retrieve "processId" to be able to retrieve the result.
        return jsonExtracted.getInt("processId");
    }

    // This function is used to display the result according to the processId and the token.
    private void result(String addressResult) throws Exception {

        StringBuilder stringBuilder = new StringBuilder(addressResult);

        URL obj = new URL(stringBuilder.toString());

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();// open connection
        con.setRequestMethod("GET"); // request method.
        con.setRequestProperty("Authorization", _basicAuth); // authentication.

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())); // stock the response.

        String line, response = new String();
        while ((line = in.readLine()) != null) {
            // copy the response
            response += line;
        }
        // close the BufferedReader
        in.close();

        // need to retrieve treatment status. Extraction in JSON format.
        JSONObject jsonExtracted = new JSONObject(response);
        // retrieve the status and stock it in "status".
        String status = jsonExtracted.getString("status");

        // if the status equals to "todo", then
        if (status.equalsIgnoreCase("todo")) { // the result is not process yet, wait...
            //  /!\ IMPORTANT /!\ allows to let some time to the server to process the data.
            Thread.sleep(1000);

            // retry after 1 second.
            result(addressResult);
        } else
            Log.i("CallApi",response); // display the response.
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            file = new File(strings[0]);
            if (file.exists()) {
                String address = _url + "authenticate/" + _key1 + "::" + _key2;
                String token = null;
                token = authenticate(address);
                Log.i("TokenApi", token);
                address = _url + "process/" + token;
                int processId = process(address);
                address = _url + "getResult/" + processId + "/" + token;
                result(address);

            } else {
                Log.i("FileNotFound","File " + file.getName() + " not found.");
            }
        } catch(
                Exception e)

        {
            e.printStackTrace();
        }
        return null;
    }
}
