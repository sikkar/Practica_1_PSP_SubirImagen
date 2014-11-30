package com.izv.angel.subirimagen;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class Principal extends Activity {

    private EditText etUrl, etGuardar;
    private ImageView ivImagen;
    private HiloBajarImagen hbj =null;
    private String extension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);
        etUrl=(EditText)findViewById(R.id.etUrl);
        etGuardar=(EditText)findViewById(R.id.etGuardar);
        ivImagen = (ImageView)findViewById(R.id.ivImagen);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void descargar(View view){
        String url="";
        extension="";
        if(!etUrl.getText().toString().trim().isEmpty() && !etGuardar.getText().toString().trim().isEmpty()) {
            url = etUrl.getText().toString();
            extension = url.substring(url.lastIndexOf("."), url.length());
            Log.v("extension",MimeTypeMap.getFileExtensionFromUrl(url));
            if(extension.contains(".jpg") || extension.contains(".jpeg") || extension.contains(".png")){
                hbj = new HiloBajarImagen();
                hbj.execute(etUrl.getText().toString());
            }else{
                Toast.makeText(this, getString(R.string.formato), Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, getString(R.string.urlVacia), Toast.LENGTH_SHORT).show();
        }

    }


    public void guardarImg() {
        String direccion=etUrl.getText().toString();
        File archivo=null;
        RadioGroup rg=(RadioGroup)findViewById(R.id.radioGroup);
        RadioButton rb = (RadioButton)findViewById(rg.getCheckedRadioButtonId());
        if(rb.getText().toString().equals(getString(R.string.extPrivada))){
            archivo = new File (getExternalFilesDir(Environment.DIRECTORY_DCIM),etGuardar.getText().toString()+extension);
        }else{
            archivo = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),etGuardar.getText().toString()+extension);
        }
        URL url;
        try {
            url = new URL(direccion);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(archivo);
            byte[] b = new byte[1024];
            int length;
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }
            is.close();
            os.close();

        } catch (MalformedURLException ex) {
            Log.v("guardando",ex.toString());
        } catch (IOException ex) {
            Log.v("guardando",ex.toString());
        }

    }

    class HiloBajarImagen extends AsyncTask<String, Void, Bitmap> {

        private ProgressDialog pDialog;
        private boolean error=false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Principal.this);
            pDialog.setMessage(getString(R.string.cargando));
            pDialog.setCancelable(true);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.show();
        }

        protected Bitmap doInBackground(String... urls) {
            URL imageUrl = null;
            String urlImagen = urls[0];
            Bitmap icono = null;

            try {
                imageUrl = new URL(urlImagen);
                HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                conn.connect();
                icono = BitmapFactory.decodeStream(conn.getInputStream());
                guardarImg();
            } catch (Exception e) {
                Log.v("Error", e.getMessage());
                e.printStackTrace();
                error=true;
            }
            return icono;
        }

        protected void onPostExecute(Bitmap result) {
            ivImagen.setImageBitmap(result);
            pDialog.dismiss();
            if(error){
                error();
            }else {
                Toast.makeText(getApplicationContext(), R.string.imgGuardada, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void error(){
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/drawable/notfound");
        ivImagen.setImageURI(uri);
    }

}
