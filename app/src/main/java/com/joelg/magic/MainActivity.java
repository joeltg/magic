package com.joelg.magic;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;


public class MainActivity extends Activity{
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.cancel();
            if (result != null) {
                int resource;
                if (result.equals("ac")) resource = R.drawable.ace_of_clubs;
                else if (result.equals("2c")) resource = R.drawable.two_of_clubs;
                else if (result.equals("3c")) resource = R.drawable.three_of_clubs;
                else if (result.equals("4c")) resource = R.drawable.four_of_clubs;
                else if (result.equals("5c")) resource = R.drawable.five_of_clubs;
                else if (result.equals("6c")) resource = R.drawable.six_of_clubs;
                else if (result.equals("7c")) resource = R.drawable.seven_of_clubs;
                else if (result.equals("8c")) resource = R.drawable.eight_of_clubs;
                else if (result.equals("9c")) resource = R.drawable.nine_of_clubs;
                else if (result.equals("tc")) resource = R.drawable.ten_of_clubs;
                else if (result.equals("jc")) resource = R.drawable.jack_of_clubs;
                else if (result.equals("qc")) resource = R.drawable.queen_of_clubs;
                else if (result.equals("kc")) resource = R.drawable.king_of_clubs;
                
                else if (result.equals("as")) resource = R.drawable.ace_of_spades;
                else if (result.equals("2s")) resource = R.drawable.two_of_spades;
                else if (result.equals("3s")) resource = R.drawable.three_of_spades;
                else if (result.equals("4s")) resource = R.drawable.four_of_spades;
                else if (result.equals("5s")) resource = R.drawable.five_of_spades;
                else if (result.equals("6s")) resource = R.drawable.six_of_spades;
                else if (result.equals("7s")) resource = R.drawable.seven_of_spades;
                else if (result.equals("8s")) resource = R.drawable.eight_of_spades;
                else if (result.equals("9s")) resource = R.drawable.nine_of_spades;
                else if (result.equals("ts")) resource = R.drawable.ten_of_spades;
                else if (result.equals("js")) resource = R.drawable.jack_of_spades;
                else if (result.equals("qs")) resource = R.drawable.queen_of_spades;
                else if (result.equals("ks")) resource = R.drawable.king_of_spades;

                else if (result.equals("ah")) resource = R.drawable.ace_of_hearts;
                else if (result.equals("2h")) resource = R.drawable.two_of_hearts;
                else if (result.equals("3h")) resource = R.drawable.three_of_hearts;
                else if (result.equals("4h")) resource = R.drawable.four_of_hearts;
                else if (result.equals("5h")) resource = R.drawable.five_of_hearts;
                else if (result.equals("6h")) resource = R.drawable.six_of_hearts;
                else if (result.equals("7h")) resource = R.drawable.seven_of_hearts;
                else if (result.equals("8h")) resource = R.drawable.eight_of_hearts;
                else if (result.equals("9h")) resource = R.drawable.nine_of_hearts;
                else if (result.equals("th")) resource = R.drawable.ten_of_hearts;
                else if (result.equals("jh")) resource = R.drawable.jack_of_hearts;
                else if (result.equals("qh")) resource = R.drawable.queen_of_hearts;
                else if (result.equals("kh")) resource = R.drawable.king_of_hearts;

                else if (result.equals("ad")) resource = R.drawable.ace_of_diamonds;
                else if (result.equals("2d")) resource = R.drawable.two_of_diamonds;
                else if (result.equals("3d")) resource = R.drawable.three_of_diamonds;
                else if (result.equals("4d")) resource = R.drawable.four_of_diamonds;
                else if (result.equals("5d")) resource = R.drawable.five_of_diamonds;
                else if (result.equals("6d")) resource = R.drawable.six_of_diamonds;
                else if (result.equals("7d")) resource = R.drawable.seven_of_diamonds;
                else if (result.equals("8d")) resource = R.drawable.eight_of_diamonds;
                else if (result.equals("9d")) resource = R.drawable.nine_of_diamonds;
                else if (result.equals("td")) resource = R.drawable.ten_of_diamonds;
                else if (result.equals("jd")) resource = R.drawable.jack_of_diamonds;
                else if (result.equals("qd")) resource = R.drawable.queen_of_diamonds;
                else if (result.equals("kd")) resource = R.drawable.king_of_diamonds;

                else if (result.equals("black joker")) resource = R.drawable.black_joker;
                else if (result.equals("red joker")) resource = R.drawable.red_joker;

                else {
                    mTextView.setText("Can't find card with identifier " + result);
                    return;
                }
                mImageView.setImageResource(resource);
            }
        }
    }
    public TextView mTextView;
    public ImageView mImageView;
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.cancel();
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.cancel();
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // TODO: handle Intent
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.cancel();
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link BaseActivity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.cancel();
        mTextView = (TextView) findViewById(R.id.textView);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        handleIntent(getIntent());
    }

}
