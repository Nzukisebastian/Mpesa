package com.example.mpesa;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    EditText phoneTxt,amountTxt;
    String phone,amount;
    Button button;

    /**
     * @param push
     * @return
     */
    private static String generateJsonStringParams(STKPush push) {
        JSONObject postData = new JSONObject();

        try {
            postData.put("BusinessShortCode", push.getBusinessShortCode());
            postData.put("Password", push.getPassword());
            postData.put("Timestamp", push.getTimestamp());
            postData.put("TransactionType", push.getTransactionType());
            postData.put("Amount", push.getAmount());
            postData.put("PartyA", push.getPartyA());
            postData.put("PartyB", push.getPartyB());
            postData.put("PhoneNumber", push.getPhoneNumber());
            postData.put("CallBackURL", push.getCallBackURL());
            postData.put("AccountReference", push.getAccountReference());
            postData.put("TransactionDesc", push.getTransactionDesc());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return postData.toString();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneTxt = findViewById(R.id.editText);
        amountTxt = findViewById(R.id.editText2);
        phone = phoneTxt.getText().toString().trim();
        amount = amountTxt.getText().toString().trim();
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pay();
            }
        });
    }

    public void startTransaction(String result, Context context) {
        if (result == null) {
            Toast.makeText(context, "Error Getting Oauth Key", Toast.LENGTH_LONG).show();

            return;
        }else {
            STK(result);
        }
        return;


    }


    private void STK(String result) {
        try {

            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.get("access_token") != null) {

                String token = jsonObject.get("access_token").toString();

                STKPush stkPush = new
                        STKPush(
                        ApiConstants.safaricom_bussiness_short_code,
                        ApiConstants.DEFAULT_TRANSACTION_TYPE, amount,
                        Utils.sanitizePhoneNumber(phone),
                        ApiConstants.safaricom_party_b,
                        Utils.sanitizePhoneNumber(phone),
                        ApiConstants.callback_url,
                        Utils.sanitizePhoneNumber(phone),
                        "Pay");

                //stkPush.setProduction(ApiConstants.PRODUCTION_RELEASE);

                String url = ApiConstants.BASE_URL + ApiConstants.PROCESS_REQUEST_URL;

                if (stkPush.getProduction() == ApiConstants.PRODUCTION_RELEASE) {
                    url = ApiConstants.PRODUCTION_BASE_URL + ApiConstants.PROCESS_REQUEST_URL;
                }

                new PayService().execute(url, generateJsonStringParams(stkPush), token);

            }

            return;
        } catch (Exception ignored) {

        }
    }

    public void pay() {

            OAuth oAuth = new OAuth(
                    ApiConstants.safaricom_Auth_key,
                    ApiConstants.safaricom_Secret);

            //oAuth.setProduction(ApiConstants.PRODUCTION_RELEASE);

            String url = ApiConstants.BASE_URL + ApiConstants.ACCESS_TOKEN_URL;

            if (oAuth.getProduction() == ApiConstants.PRODUCTION_RELEASE)
                url = ApiConstants.PRODUCTION_BASE_URL + ApiConstants.ACCESS_TOKEN_URL;

            new AuthService(MainActivity.this).execute(url, oAuth.getOauth());

    }

    public class AuthService extends AsyncTask<String, Void, String> {

        final Context context;

        AuthService(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + strings[1]);
            return Request.get(strings[0], headers);
        }

        protected void onPostExecute(String result) {
            startTransaction(result, context);
        }
    }

    public class PayService extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + strings[2]);
            return Request.post(strings[0], strings[1], headers);
        }

        protected void onPostExecute(String result) {

            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();

        }
    }
}
