package com.example.cristhianc.atacamobile;


import com.google.gson.Gson;
import android.Manifest;
import android.app.PendingIntent;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private NfcAdapter mNfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] nfcIntentFilter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sp.edit();

        if(!(sp.getBoolean("carrinhoEscrito", false))){
           lerProduto();
            editor.putBoolean("carrinhoEscrito", true);
            editor.commit();
        }
          


        Button bt = (Button) findViewById(R.id.btn_read);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.NFC}, 1);
            }
        }

        /*bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, DetalhesProduto.class);
                startActivity(intent);
            }
        });*/


        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);


        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        nfcIntentFilter = new IntentFilter[]{techDetected,tagDetected,ndefDetected};
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);


    }

    @Override
    public void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mNfcAdapter!= null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);
        }else{
            CharSequence text = "NFC não detectado!";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.carrinho) {

            Intent intent = new Intent(MainActivity.this, CarrinhoProdutos.class);
            startActivity(intent);

            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public void lerProduto(){

        Gson gson = new Gson();
        Carrinho car = new Carrinho();
        Produto pd = new Produto(203, "teste", "21/01/2018", "produto1", 25.00);
        car.addCarinho(pd);

        String json = gson.toJson(car);
        try {
            editor.putString("carrinhoObj", json);
            editor.commit();
        }catch(Exception e){
            String msg = e.getMessage();
        }

    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        CharSequence text = "LEU NFC!!!!";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        toast.show();


        if(intent != null && (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))){



            Parcelable[] rawMsg =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if(rawMsg != null){
                NdefMessage[] mensagens = new NdefMessage[rawMsg.length];
                for(int i = 0; i < mensagens.length; i++){
                    mensagens[i] = (NdefMessage) rawMsg[i];
                }
            }
        }
    }
}
