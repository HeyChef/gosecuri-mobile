package epsi.thomas.gosecuri.api.ocr;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class OcrCni extends AsyncTask<Void, Void, Void> {
    private Context mContext;
    private File file;
    private JSONObject jsonExtracted;
    private static String _url = "https://api.microblink.com/recognize/execute";
    private static String _Bearerauth = "Bearer MTBkYTlkYzBmMTNjNDNjZWI4NjlhYWJlY2MwMDYxNjU6YzEzYTZkOWYtMTViYy00NjI1LThkYWMtMmQ0ZWRlMzkxZjQx";
    private static String body = "{"
            + "\"recognizers\": [\"MRTD\"],"
            + "\"imageBase64\": \"replaceBase64\"" + "}";

    public OcrCni(Context Context, String path) {
        mContext = Context;
        file = new File(path);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            if (file.exists()) {
                callAPi();
            } else {
                Log.i("FileNotFound", "File " + file.getName() + " not found.");
            }
        } catch (
                Exception e)

        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid){
        try {
            Toast toast = Toast.makeText(mContext, "Impossible de scanner votre image", Toast.LENGTH_LONG);
            if (jsonExtracted.has("code")) {
                if (jsonExtracted.getString("code").equals("OK")) {
                    JSONObject data = jsonExtracted.getJSONArray("data").getJSONObject(0);
                    if(!data.isNull("result")){
                        JSONObject result = data.getJSONObject("result");
                            String nom = result.getString("primaryID");
                            toast = Toast.makeText(mContext, nom, Toast.LENGTH_LONG);
                    }
                }
                toast.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void callAPi() {
        try {
            URL obj = new URL(_url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            String encodedBase64 = new String(Base64.encodeBase64(bytes));

            body = body.replace("replaceBase64", encodedBase64);

            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", _Bearerauth);
            con.setRequestProperty("Content-Type", "application/json");
            Log.i("encodedbase64",encodedBase64);

            OutputStream os = con.getOutputStream();
            os.write(body.getBytes("UTF-8"));
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line, response = new String();
            while ((line = in.readLine()) != null) {
                response += line;
            }
            in.close();

            jsonExtracted = new JSONObject(response);

            System.out.println(jsonExtracted);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
