package epsi.thomas.gosecuri.api;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.SSLException;

import epsi.thomas.gosecuri.entity.Personne;
import epsi.thomas.gosecuri.firebase.FirebaseStorageCloud;
import epsi.thomas.gosecuri.service.SecondCheckService;

public class FaceDetection extends AsyncTask<Bitmap, Void, String>{

    Personne personne;
    SecondCheckService secondCheckService;

    public FaceDetection(Personne personne, SecondCheckService secondCheckService){
        this.personne = personne;
        this.secondCheckService = secondCheckService;
    }

    private FirebaseStorageCloud firebaseStorageCloud = FirebaseStorageCloud.getInstance();

    @Override
    protected String doInBackground(Bitmap... bitmaps) {
        final Bitmap faceBitmap = bitmaps[0];
        final Bitmap idBitmap = bitmaps[1];

        personne = firebaseStorageCloud.uploadFiles(faceBitmap,idBitmap,personne);

        String t = "";
        try {
            t = compareFace(personne.getFaceURL(), personne.getImageURL());
            System.out.println(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    protected void onPostExecute(String s) {
        secondCheckService.faceResult(s,personne);
    }

    public String compareFace( String id, String face ) throws Exception {
        String url = "https://api-us.faceplusplus.com/facepp/v3/compare";
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, byte[]> byteMap = new HashMap<>();
        map.put( "api_key", "OrpakZmvJYZ6AjkhRpkhu-HE6C3tW-2O" );
        map.put( "api_secret", "Aej_oa8xqgCL3PGXn0P7y6ibZ_tRek7F" );
        map.put( "faceset_token", "6ec607d40246a42df796b259cb11fd04" );
        map.put( "image_url1", id );
        map.put( "image_url2", face );
        String str = "";
        try {
            byte[] bacd = post( url, map, byteMap );
            str = new String( bacd );
            System.out.println( str );
            return str;
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return str;
    }

    private final static int CONNECT_TIME_OUT = 90000;
    private final static int READ_OUT_TIME    = 90000;
    private static String    boundaryString   = getBoundary();

    protected static byte[] post(String url, HashMap<String, String> map, HashMap<String, byte[]> fileMap )
            throws Exception {
        HttpURLConnection conne;
        URL url1 = new URL( url );
        conne = (HttpURLConnection) url1.openConnection();
        conne.setDoOutput( true );
        conne.setUseCaches( false );
        conne.setRequestMethod( "POST" );
        conne.setConnectTimeout( CONNECT_TIME_OUT );
        conne.setReadTimeout( READ_OUT_TIME );
        conne.setRequestProperty( "accept", "*/*" );
        conne.setRequestProperty( "Content-Type", "multipart/form-data; boundary=" + boundaryString );
        conne.setRequestProperty( "connection", "Keep-Alive" );
        conne.setRequestProperty( "user-agent", "Mozilla/4.0 (compatible;MSIE 6.0;Windows NT 5.1;SV1)" );
        DataOutputStream obos = new DataOutputStream( conne.getOutputStream() );
        Iterator iter = map.entrySet().iterator();
        while ( iter.hasNext() ) {
            Map.Entry<String, String> entry = (Map.Entry) iter.next();
            String key = entry.getKey();
            String value = entry.getValue();
            obos.writeBytes( "--" + boundaryString + "\r\n" );
            obos.writeBytes( "Content-Disposition: form-data; name=\"" + key
                    + "\"\r\n" );
            obos.writeBytes( "\r\n" );
            obos.writeBytes( value + "\r\n" );
        }
        if ( fileMap != null && fileMap.size() > 0 ) {
            Iterator fileIter = fileMap.entrySet().iterator();
            while ( fileIter.hasNext() ) {
                Map.Entry<String, byte[]> fileEntry = (Map.Entry<String, byte[]>) fileIter.next();
                obos.writeBytes( "--" + boundaryString + "\r\n" );
                obos.writeBytes( "Content-Disposition: form-data; name=\"" + fileEntry.getKey()
                        + "\"; filename=\"" + encode( " " ) + "\"\r\n" );
                obos.writeBytes( "\r\n" );
                obos.write( fileEntry.getValue() );
                obos.writeBytes( "\r\n" );
            }
        }
        obos.writeBytes( "--" + boundaryString + "--" + "\r\n" );
        obos.writeBytes( "\r\n" );
        obos.flush();
        obos.close();
        InputStream ins = null;
        int code = conne.getResponseCode();
        try {
            if ( code == 200 ) {
                ins = conne.getInputStream();
            } else {
                ins = conne.getErrorStream();
            }
        } catch ( SSLException e ) {
            e.printStackTrace();
            return new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[4096];
        int len;
        while ( ( len = ins.read( buff ) ) != -1 ) {
            baos.write( buff, 0, len );
        }
        byte[] bytes = baos.toByteArray();
        ins.close();
        return bytes;
    }

    private static String getBoundary() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for ( int i = 0; i < 32; ++i ) {
            sb.append( "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".charAt(
                    random.nextInt( "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_".length() ) ) );
        }
        return sb.toString();
    }

    private static String encode( String value ) throws Exception {
        return URLEncoder.encode( value, "UTF-8" );
    }
}
