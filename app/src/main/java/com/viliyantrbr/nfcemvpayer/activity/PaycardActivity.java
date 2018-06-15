package com.viliyantrbr.nfcemvpayer.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.viliyantrbr.nfcemvpayer.R;
import com.viliyantrbr.nfcemvpayer.object.PaycardObject;
import com.viliyantrbr.nfcemvpayer.util.AidUtil;
import com.viliyantrbr.nfcemvpayer.util.HexUtil;
import com.viliyantrbr.nfcemvpayer.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

public class PaycardActivity extends AppCompatActivity {
    private static final String TAG = PaycardActivity.class.getSimpleName();

    private Realm mRealm = null;

    private PaycardObject mPaycardObject = null;

    private TextView mPaycardTypeTextView = null;
    private ImageView mPaycardTypeImageView = null;
    private TextView mPaycardAidTextView = null;
    private TextView mPaycardExpDateTextView = null;
    private TextView mPaycardAddDateTextView = null;

    private TextView mPaycardApplicationLabelTextView = null;
    private TextView mPaycardCardholderNameTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, "\"" + TAG + "\": Activity create");

        byte[] applicationPan = getIntent().getByteArrayExtra("Pan");

        String applicationPanHexadecimal = HexUtil.bytesToHexadecimal(applicationPan);

        setTitle(applicationPanHexadecimal);

        setContentView(R.layout.activity_paycard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        try {
            mRealm = Realm.getDefaultInstance();
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
            LogUtil.e(TAG, e.toString());

            e.printStackTrace();
        }

        if (mRealm != null) {
            try {
                mPaycardObject = mRealm.where(PaycardObject.class).equalTo("mApplicationPan", applicationPan).findFirst();
            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage());
                LogUtil.e(TAG, e.toString());

                e.printStackTrace();
            }
        }

        mPaycardTypeTextView = findViewById(R.id.content_paycard_type);
        mPaycardTypeImageView = findViewById(R.id.content_paycard_type_image);
        mPaycardAidTextView = findViewById(R.id.content_paycard_aid);
        mPaycardExpDateTextView = findViewById(R.id.content_paycard_exp_date);
        mPaycardAddDateTextView = findViewById(R.id.content_paycard_add_date);

        mPaycardApplicationLabelTextView = findViewById(R.id.content_paycard_application_label);
        mPaycardCardholderNameTextView = findViewById(R.id.content_paycard_cardholder_name);

        // Type (text)
        if (Arrays.equals(mPaycardObject.getAid(), AidUtil.A0000000041010)) {
            mPaycardTypeTextView.setText("Mastercard (PayPass)");
        } else if (Arrays.equals(mPaycardObject.getAid(), AidUtil.A0000000043060)) {
            mPaycardTypeTextView.setText("Maestro (PayPass)");
        } else if (Arrays.equals(mPaycardObject.getAid(), AidUtil.A0000000031010)) {
            mPaycardTypeTextView.setText("Visa (PayWave)");
        } else if (Arrays.equals(mPaycardObject.getAid(), AidUtil.A0000000032010)) {
            mPaycardTypeTextView.setText("Visa Electron (PayWave)");
        } else {
            mPaycardTypeTextView.setText("Type: N/A");
        }
        // - Type (text)

        // Type (image)
        if (Arrays.equals(mPaycardObject.getAid(), AidUtil.A0000000041010)) {
            mPaycardTypeImageView.setImageResource(R.drawable.card_mastercard);
        } else if (Arrays.equals(mPaycardObject.getAid(), AidUtil.A0000000043060)) {
            mPaycardTypeImageView.setImageResource(R.drawable.card_maestro);
        } else if (Arrays.equals(mPaycardObject.getAid(), AidUtil.A0000000031010)) {
            mPaycardTypeImageView.setImageResource(R.drawable.card_visa);
        } else if (Arrays.equals(mPaycardObject.getAid(), AidUtil.A0000000032010)) {
            mPaycardTypeImageView.setImageResource(R.drawable.card_visa_electron);
        } else {
            // TODO: Default paycard image
        }
        // - Type (image)

        // AID
        String aidString = HexUtil.bytesToHexadecimal(mPaycardObject.getAid());

        mPaycardAidTextView.setText("AID: " + (aidString != null ? aidString : "N/A"));
        // - AID

        // Exp Date
        String expDateString = null;

        Date expDate = null;
        try {
            expDate = new SimpleDateFormat("yyMMdd", Locale.getDefault()).parse(HexUtil.bytesToHexadecimal(mPaycardObject.getApplicationExpirationDate()));
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
            LogUtil.e(TAG, e.toString());

            e.printStackTrace();
        }

        if (expDate != null) {
            try {
                expDateString = new SimpleDateFormat("MM/yy", Locale.getDefault()).format(expDate);
            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage());
                LogUtil.e(TAG, e.toString());

                e.printStackTrace();
            }
        }

        mPaycardExpDateTextView.setText("Exp. Date: " + (expDateString != null ? expDateString : "N/A"));
        // - Exp Date

        // Add Date
        String addDateString = null;

        Date addDate = null;
        try {
            addDate = mPaycardObject.getAddDate();
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
            LogUtil.e(TAG, e.toString());

            e.printStackTrace();
        }

        if (addDate != null) {
            try {
                addDateString = new SimpleDateFormat("HH:mm:ss dd/MM/yy", Locale.getDefault()).format(addDate);
            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage());
                LogUtil.e(TAG, e.toString());

                e.printStackTrace();
            }

            mPaycardAddDateTextView.setText(getString(R.string.added) + " / " + getString(R.string.updated) + ": " + (addDateString != null ? addDateString : "N/A"));
        }
        // - Add Date

        // Application Label (ASCII)
        if (mPaycardObject.getApplicationLabelHasAscii()) {
            String applicationLabelAscii = HexUtil.bytesToAscii(mPaycardObject.getApplicationLabel());

            if (applicationLabelAscii != null) {
                mPaycardApplicationLabelTextView.setVisibility(View.VISIBLE);
                mPaycardApplicationLabelTextView.setText(getString(R.string.application_label) + ": " + applicationLabelAscii);
            }
        }
        // - Application Label (ASCII)

        // Cardholder name (ASCII)
        if (mPaycardObject.getCardholderNameHasAscii()) {
            String cardholderNameAscii = HexUtil.bytesToAscii(mPaycardObject.getCardholderName());

            if (cardholderNameAscii != null) {
                mPaycardCardholderNameTextView.setVisibility(View.VISIBLE);
                mPaycardCardholderNameTextView.setText(getString(R.string.cardholder_name) + ": " + cardholderNameAscii);
            }
        }
        // - Cardholder name (ASCII)

        StringBuilder logContent = new StringBuilder("---- Tech Data - ISO 14443-3A, ISO 14443-3B\n");

        logContent.append("\n");

        if (mPaycardObject.getCPse() != null && mPaycardObject.getRPse() != null) {
            logContent.append(getString(R.string.pse)).append(" C-APDU -> \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getCPse())).append("\n");
            logContent.append(getString(R.string.pse)).append(" R-APDU <- \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getRPse())).append("\n");

            logContent.append("\n");
        }

        if (mPaycardObject.getCPpse() != null && mPaycardObject.getRPpse() != null) {
            logContent.append(getString(R.string.ppse)).append(" C-APDU -> \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getCPpse())).append("\n");
            logContent.append(getString(R.string.ppse)).append(" R-APDU <- \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getRPpse())).append("\n");

            logContent.append("\n");
        }

        if (mPaycardObject.getCFci() != null && mPaycardObject.getRFci() != null) {
            logContent.append(getString(R.string.fci)).append(" C-APDU -> \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getCFci())).append("\n");
            logContent.append(getString(R.string.fci)).append(" R-APDU <- \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getRFci())).append("\n");

            logContent.append("\n");
        }

        if (mPaycardObject.getPdol() != null || mPaycardObject.getPdolConstructed() != null) {
            if (mPaycardObject.getPdol() != null) {
                logContent.append(getString(R.string.pdol)).append(": ").append(HexUtil.bytesToHexadecimal(mPaycardObject.getPdol())).append("\n");
            }
            if (mPaycardObject.getPdolConstructed() != null) {
                logContent.append(getString(R.string.pdol)).append(" Constructed: ").append(HexUtil.bytesToHexadecimal(mPaycardObject.getPdolConstructed())).append("\n");
            }

            logContent.append("\n");
        }

        if (mPaycardObject.getCGpo() != null && mPaycardObject.getRGpo() != null) {
            logContent.append(getString(R.string.gpo)).append(" C-APDU -> \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getCGpo())).append("\n");
            logContent.append(getString(R.string.gpo)).append(" R-APDU <- \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getRGpo())).append("\n");

            logContent.append("\n");
        }

        if (mPaycardObject.getAflData() != null) {
            logContent.append(getString(R.string.afl)).append(": ").append(HexUtil.bytesToHexadecimal(mPaycardObject.getAflData())).append("\n");

            logContent.append("\n");
        }

        if (mPaycardObject.getCAflRecordsList() != null &&! mPaycardObject.getCAflRecordsList().isEmpty()
                &&
                mPaycardObject.getRAflRecordsList() != null && !mPaycardObject.getRAflRecordsList().isEmpty()) {
            int listCSize = mPaycardObject.getCAflRecordsList().size();
            int listRSize = mPaycardObject.getRAflRecordsList().size();

            if (listCSize > 0 && listRSize > 0 && listCSize == listRSize) {
                logContent.append("----").append(getString(R.string.afl)).append(" Record(s) ----\n");

                for (int i = 0; i < listCSize; i++) {
                    byte[] cReadRecord = null, rReadRecord = null;
                    try {
                        cReadRecord = mPaycardObject.getCAflRecordsList().get(i);
                    } catch (Exception e) {
                        LogUtil.e(TAG, e.getMessage());
                        LogUtil.e(TAG, e.toString());

                        e.printStackTrace();
                    }
                    try {
                        rReadRecord = mPaycardObject.getRAflRecordsList().get(i);
                    } catch (Exception e) {
                        LogUtil.e(TAG, e.getMessage());
                        LogUtil.e(TAG, e.toString());

                        e.printStackTrace();
                    }

                    if (cReadRecord != null && rReadRecord != null) {
                        logContent.append("Read Record C-APDU -> \n").append(HexUtil.bytesToHexadecimal(cReadRecord)).append("\n");
                        logContent.append("Read Record R-APDU <- \n").append(HexUtil.bytesToHexadecimal(rReadRecord)).append("\n");

                        if (i < listCSize - 1) {
                            logContent.append("\n");
                        }
                    }
                }

                logContent.append("- ----").append(getString(R.string.afl)).append(" Record(s) ----\n");

                logContent.append("\n");
            }
        }

        if (mPaycardObject.getCdol1() != null || mPaycardObject.getCdol2() != null) {
            if (mPaycardObject.getCdol1() != null) {
                logContent.append(getString(R.string.cdol_1)).append(": \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getCdol1())).append("\n");
            }
            if (mPaycardObject.getCdol2() != null) {
                logContent.append(getString(R.string.cdol_2)).append(": \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getCdol2())).append("\n");
            }

            logContent.append("\n");
        }

        if (mPaycardObject.getCLastOnlineAtcRegister() != null && mPaycardObject.getRLastOnlineAtcRegister() != null) {
            logContent.append("Last Online ").append(getString(R.string.atc)).append(" Register C-APDU -> \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getCLastOnlineAtcRegister())).append("\n");
            logContent.append("Last Online ").append(getString(R.string.atc)).append(" Register R-APDU <- \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getRLastOnlineAtcRegister())).append("\n");

            logContent.append("\n");
        }

        if (mPaycardObject.getCPinTryCounter() != null && mPaycardObject.getRPinTryCounter() != null) {
            logContent.append(getString(R.string.pin)).append(" Try Counter C-APDU -> \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getCPinTryCounter())).append("\n");
            logContent.append(getString(R.string.pin)).append(" Try Counter R-APDU <- \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getRPinTryCounter())).append("\n");

            logContent.append("\n");
        }

        if (mPaycardObject.getCAtc() != null && mPaycardObject.getRAtc() != null) {
            logContent.append(getString(R.string.atc)).append(" C-APDU -> \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getCAtc())).append("\n");
            logContent.append(getString(R.string.atc)).append(" R-APDU <- \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getRAtc())).append("\n");

            logContent.append("\n");
        }

        if (mPaycardObject.getCLogFormat() != null && mPaycardObject.getRLogFormat() != null) {
            logContent.append(getString(R.string.log_format)).append(" C-APDU -> \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getCLogFormat())).append("\n");
            logContent.append(getString(R.string.log_format)).append(" R-APDU <- \n").append(HexUtil.bytesToHexadecimal(mPaycardObject.getRLogFormat())).append("\n");

            logContent.append("\n");
        }

        if (mPaycardObject.getCLogEntryList() != null && !mPaycardObject.getCLogEntryList().isEmpty()
                &&
                mPaycardObject.getRLogEntryList() != null && !mPaycardObject.getRLogEntryList().isEmpty()) {
            int listCSize = mPaycardObject.getCLogEntryList().size();
            int listRSize = mPaycardObject.getRLogEntryList().size();
            if (listCSize == listRSize) {
                logContent.append("----").append(getString(R.string.log_entry)).append(" ----\n");

                for (int i = 0; i < listCSize; i++) {
                    byte[] cReadRecord = null, rReadRecord = null;
                    try {
                        cReadRecord = mPaycardObject.getCLogEntryList().get(i);
                    } catch (Exception e) {
                        LogUtil.e(TAG, e.getMessage());
                        LogUtil.e(TAG, e.toString());

                        e.printStackTrace();
                    }
                    try {
                        rReadRecord = mPaycardObject.getRLogEntryList().get(i);
                    } catch (Exception e) {
                        LogUtil.e(TAG, e.getMessage());
                        LogUtil.e(TAG, e.toString());

                        e.printStackTrace();
                    }

                    if (cReadRecord != null && rReadRecord != null) {
                        logContent.append("Read Record C-APDU -> \n").append(HexUtil.bytesToHexadecimal(cReadRecord)).append("\n");
                        logContent.append("Read Record R-APDU <- \n").append(HexUtil.bytesToHexadecimal(rReadRecord)).append("\n");

                        if (i < listCSize - 1) {
                            logContent.append("\n");
                        }
                    }
                }

                logContent.append("- ----").append(getString(R.string.log_entry)).append(" ----\n");

                logContent.append("\n");
            }
        }

        logContent.append("- ---- Tech Data - ISO 14443-3A, ISO 14443-3B");

        TextView logContentTextView = findViewById(R.id.content_log_content);
        logContentTextView.setText(logContent.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "\"" + TAG + "\": Activity destroy");

        if (mPaycardCardholderNameTextView != null) {
            mPaycardCardholderNameTextView = null;
        }
        if (mPaycardApplicationLabelTextView != null) {
            mPaycardApplicationLabelTextView = null;
        }

        if (mPaycardAddDateTextView != null) {
            mPaycardAddDateTextView = null;
        }
        if (mPaycardExpDateTextView != null) {
            mPaycardExpDateTextView = null;
        }
        if (mPaycardAidTextView != null) {
            mPaycardAidTextView = null;
        }
        if (mPaycardTypeImageView != null) {
            mPaycardTypeImageView = null;
        }
        if (mPaycardTypeTextView != null) {
            mPaycardTypeTextView = null;
        }

        if (mRealm != null) {
            if (mPaycardObject != null) {
                mPaycardObject = null;
            }

            try {
                mRealm.close();
            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage());
                LogUtil.e(TAG, e.toString());

                e.printStackTrace();
            }

            mRealm = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null) {
            getMenuInflater().inflate(R.menu.menu_paycard, menu);

            return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem != null) {
            switch (menuItem.getItemId()) {
                case R.id.action_paycard_host:
                    return true;

                case R.id.action_paycard_snip:
                    return true;

                default:
                    return super.onOptionsItemSelected(menuItem);
            }
        }

        return false;
    }
}
