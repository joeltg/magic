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
import android.widget.CheckBox;
import android.widget.ListView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class MainActivity extends Activity{

    public ArrayList<String> cards = new ArrayList<>();

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
            if (result != null) {
                int resource = getImage(result);
                cards.add(result);
                if (resource == 0) {
                    mTextView.setText("Can't find card with identifier " + result);
                    return;
                }
                if (selectedNumbers.size() > 0) {
                    resource = R.drawable.phone;
                }
                if (selectedNumbers.size() == 1 && selectedNumbers.get(0).equals("215-915-3556")) {
                    resource = R.drawable.question;
                }
                if (autosend.isChecked()) {
                    for (String number : selectedNumbers) {
                        sendSMS(codeToString(result), number);
                    }
                }
                mButton.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                clear.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                autosend.setVisibility(View.GONE);
                mImageView.setImageResource(resource);
            }
        }
    }

    public TextView mTextView;
    public ImageView mImageView;
    public static ArrayList<String> selectedNumbers = new ArrayList<>();
    public Button mButton;
    public static CheckBox autosend;
    public Button clear;
    public static ListView mListView;
    public static CustomAdapter adapter;
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
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
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

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    public static void sendEmail(String address, String message) {
        HashMap<String, Object> params = new HashMap();
        params.put("address", address);
        params.put("message", message);
        ParseCloud.callFunctionInBackground("hello", params, new FunctionCallback<String>() {
            @Override
            public void done(String o, ParseException e) {
                System.out.println(o);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Parse.initialize(this, "sW1XxuXhJsO62eXQ6mSn2P4dnA9O0tyeFQjMYEnx", "J7cEt8Fdhvn5k7QLL1xzfNtyj7bfSr9eIVfRnbOM");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mListView = (ListView) findViewById(R.id.listview);
        mButton = (Button) findViewById(R.id.button);
        clear = (Button) findViewById(R.id.clear);
        autosend = (CheckBox) findViewById(R.id.autosend);
        adapter = new CustomAdapter(this, R.layout.users, new ArrayList<User>());
        mListView.setAdapter(adapter);
        adapter.add(new User("Joel's email", "joelg@mit.edu"));
        adapter.add(new User("Joel (Google Voice)", "6109158059"));
        adapter.add(new User("Novy", "novysan@media.mit.edu"));
        adapter.add(new User("Kenny", "315-383-3921"));
        adapter.add(new User("JBobrow", "jbobrow@media.mit.edu"));
        adapter.add(new User("Greg", "gregab@mit.edu"));
        adapter.add(new User("Colin", "215-915-3556"));
        adapter.add(new User("Jenn", "813-846-1670"));
        adapter.add(new User("Carolx", "carolinex@gmail.com"));
        adapter.add(new User("Isa Almeida", "meuemail.isa@gmail.com"));
        adapter.add(new User("Kyrie", "kyrieehc@mit.edu"));

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.setImageDrawable(null);
                cards.clear();
                selectedNumbers.clear();
            }
        });

        mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mImageView.setVisibility(View.GONE);
                mButton.setVisibility(View.VISIBLE);
                clear.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.VISIBLE);
                autosend.setVisibility(View.VISIBLE);
                return true;
            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButton.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                clear.setVisibility(View.GONE);
                autosend.setVisibility(View.GONE);
            }
        });
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autosend.isChecked()) return;
                String card = cards.get(cards.size() - 1);
                String message = codeToString(card);
                for (String number : selectedNumbers) {
                    sendSMS(message, number);
                }
            }
        });
        handleIntent(getIntent());
    }

    public static int getImage(String result) {
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

        else resource = 0;

        return resource;
    }
    
    public static String codeToString(String result) {
        String resource;
        if (result.equals("ac")) resource =  "ace of clubs";
        else if (result.equals("2c")) resource =  "two of clubs";
        else if (result.equals("3c")) resource =  "three of clubs";
        else if (result.equals("4c")) resource =  "four of clubs";
        else if (result.equals("5c")) resource =  "five of clubs";
        else if (result.equals("6c")) resource =  "six of clubs";
        else if (result.equals("7c")) resource =  "seven of clubs";
        else if (result.equals("8c")) resource =  "eight of clubs";
        else if (result.equals("9c")) resource =  "nine of clubs";
        else if (result.equals("tc")) resource =  "ten of clubs";
        else if (result.equals("jc")) resource =  "jack of clubs";
        else if (result.equals("qc")) resource =  "queen of clubs";
        else if (result.equals("kc")) resource =  "king of clubs";

        else if (result.equals("as")) resource =  "ace of spades";
        else if (result.equals("2s")) resource =  "two of spades";
        else if (result.equals("3s")) resource =  "three of spades";
        else if (result.equals("4s")) resource =  "four of spades";
        else if (result.equals("5s")) resource =  "five of spades";
        else if (result.equals("6s")) resource =  "six of spades";
        else if (result.equals("7s")) resource =  "seven of spades";
        else if (result.equals("8s")) resource =  "eight of spades";
        else if (result.equals("9s")) resource =  "nine of spades";
        else if (result.equals("ts")) resource =  "ten of spades";
        else if (result.equals("js")) resource =  "jack of spades";
        else if (result.equals("qs")) resource =  "queen of spades";
        else if (result.equals("ks")) resource =  "king of spades";

        else if (result.equals("ah")) resource =  "ace of hearts";
        else if (result.equals("2h")) resource =  "two of hearts";
        else if (result.equals("3h")) resource =  "three of hearts";
        else if (result.equals("4h")) resource =  "four of hearts";
        else if (result.equals("5h")) resource =  "five of hearts";
        else if (result.equals("6h")) resource =  "six of hearts";
        else if (result.equals("7h")) resource =  "seven of hearts";
        else if (result.equals("8h")) resource =  "eight of hearts";
        else if (result.equals("9h")) resource =  "nine of hearts";
        else if (result.equals("th")) resource =  "ten of hearts";
        else if (result.equals("jh")) resource =  "jack of hearts";
        else if (result.equals("qh")) resource =  "queen of hearts";
        else if (result.equals("kh")) resource =  "king of hearts";

        else if (result.equals("ad")) resource =  "ace of diamonds";
        else if (result.equals("2d")) resource =  "two of diamonds";
        else if (result.equals("3d")) resource =  "three of diamonds";
        else if (result.equals("4d")) resource =  "four of diamonds";
        else if (result.equals("5d")) resource =  "five of diamonds";
        else if (result.equals("6d")) resource =  "six of diamonds";
        else if (result.equals("7d")) resource =  "seven of diamonds";
        else if (result.equals("8d")) resource =  "eight of diamonds";
        else if (result.equals("9d")) resource =  "nine of diamonds";
        else if (result.equals("td")) resource =  "ten of diamonds";
        else if (result.equals("jd")) resource =  "jack of diamonds";
        else if (result.equals("qd")) resource =  "queen of diamonds";
        else if (result.equals("kd")) resource =  "king of diamonds";

        else if (result.equals("black joker")) resource =  "black joker";
        else if (result.equals("red joker")) resource =  "red joker";

        else resource = "Couldn't read card";

        return resource;
    }

    public static void sendSMS(String message, String number) {
        if (number.contains("@")) {
            try {
                sendEmail(number, message);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        else {
            try {
                SmsManager.getDefault().sendTextMessage(number, null, message, null, null);
            } catch (Exception e) {
                System.out.println("SMS Error: " + e.toString());
            }
        }
    }
}
