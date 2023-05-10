package com.app.superpos.pos;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
//import com.android.volley.Response ;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.superpos.Constant;
import com.app.superpos.R;
import com.app.superpos.adapter.CartAdapter;
import com.app.superpos.database.DatabaseAccess;
import com.app.superpos.global.Global;
import com.app.superpos.model.Customer;
import com.app.superpos.networking.ApiClient;
import com.app.superpos.networking.ApiInterface;
import com.app.superpos.orders.OrderDetailsActivity;
import com.app.superpos.orders.OrdersActivity;
import com.app.superpos.utils.BaseActivity;
import com.app.superpos.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response ;

public class ProductCart extends BaseActivity {


    CartAdapter productCartAdapter;
    ImageView imgNoProduct;
    Button btnSubmitOrder;
    TextView txtNoProduct, txtTotalPrice,txttotaltax,TxtTotalPricewithtax;
    LinearLayout linearLayout,lnket;
    DatabaseAccess databaseAccess = DatabaseAccess.getInstance(ProductCart.this);
    double calculatedTotalCost=0.0;
    double totalTax=0.0;
    AlertDialog.Builder dialog;
    View dialogView;
    List<String> customerNames, orderTypeNames, paymentMethodNames;
    List<Customer> customerData;
    ArrayAdapter<String> customerAdapter, orderTypeAdapter, paymentMethodAdapter;
    SharedPreferences sp;
    String servedBy,staffId,shopTax,currency,shopID,ownerId,ownerCardnumber;
    String cardId="";
    DecimalFormat f;
    EditText mEt1, mEt2, mEt3, mEt4, mEt5, mEt6;
    Button saveButton;
    String pinkartu = "";
    boolean showpin=false;
     AlertDialog alertDialogorder;
    Dialog dialogpin;
    List<HashMap<String, String>> lines;
    private final String[][] techList = new String[][] {
            new String[] {
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(), Ndef.class.getName()
            }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_cart);

        getSupportActionBar().setHomeButtonEnabled(true); //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//for back button
        getSupportActionBar().setTitle(R.string.product_cart);
        f = new DecimalFormat("#0.00");
        sp = getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        servedBy = sp.getString(Constant.SP_STAFF_NAME, "");
        showpin = sp.getBoolean(Constant.SP_SHOWPIN, false);
        staffId = sp.getString(Constant.SP_STAFF_ID, "");
        shopTax= sp.getString(Constant.SP_TAX, "");
        currency= sp.getString(Constant.SP_CURRENCY_SYMBOL, "");

        shopID = sp.getString(Constant.SP_SHOP_ID, "");
        ownerId = sp.getString(Constant.SP_OWNER_ID, "");
        ownerCardnumber = sp.getString(Constant.SP_OWNER_Cardnumber, "");



        getCustomers(shopID,ownerId);

        RecyclerView recyclerView = findViewById(R.id.cart_recyclerview);
        imgNoProduct = findViewById(R.id.image_no_product);
        btnSubmitOrder = findViewById(R.id.btn_submit_order);
        txtNoProduct = findViewById(R.id.txt_no_product);
        linearLayout = findViewById(R.id.linear_layout);
        txtTotalPrice = findViewById(R.id.txt_total_price);
        txttotaltax = findViewById(R.id.txt_total_tax);
        TxtTotalPricewithtax = findViewById(R.id.txt_total_with_tax);
        lnket = findViewById(R.id.linear_layout2);

        txtNoProduct.setVisibility(View.GONE);


        // set a GridLayoutManager with default vertical orientation and 3 number of columns
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager); // set LayoutManager to RecyclerView


        recyclerView.setHasFixedSize(true);


        databaseAccess.open();


        //get data from local database
        List<HashMap<String, String>> cartProductList;
        cartProductList = databaseAccess.getCartProduct();

        Log.d("CartSize", "" + cartProductList.size());

        if (cartProductList.isEmpty()) {

            imgNoProduct.setImageResource(R.drawable.empty_cart);
            imgNoProduct.setVisibility(View.VISIBLE);
            txtNoProduct.setVisibility(View.VISIBLE);
            btnSubmitOrder.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            linearLayout.setVisibility(View.GONE);
            txtTotalPrice.setVisibility(View.GONE);
            lnket.setVisibility(View.GONE);
        } else {


            imgNoProduct.setVisibility(View.GONE);
            productCartAdapter = new CartAdapter(ProductCart.this, cartProductList, txtTotalPrice,txttotaltax,TxtTotalPricewithtax, btnSubmitOrder, imgNoProduct, txtNoProduct,lnket);

            recyclerView.setAdapter(productCartAdapter);


        }


        btnSubmitOrder.setOnClickListener(v -> dialog());

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "1");

        //mTextView.setText("onResume:");
        // creating pending intent:
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);

        }
        else
        {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        }
       // PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // creating intent receiver for NFC events:
        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);

        // enabling foreground dispatch for getting intent from NFC event:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(nfcAdapter!=null)
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{filter}, this.techList);
    }

    @Override
    protected void onPause() {
        super.onPause();

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter!=null)
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent", "1");

        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Log.d("onNewIntent", "2");
            if( alertDialogorder!=null) {
                if ( !alertDialogorder.isShowing()) {
                    Toasty.error(ProductCart.this, "Please submit first", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            else {
                Toasty.error(ProductCart.this, "Please submit first", Toast.LENGTH_SHORT).show();
                return;
            }
            Parcelable[] messages1 = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (messages1 != null) {
                Log.d(TAG, "Found " + messages1.length + " NDEF messages");

                NdefMessage[] ndefMessages = new NdefMessage[messages1.length];
                for (int i = 0; i < messages1.length; i++) {
                    ndefMessages[i] = (NdefMessage) messages1[i];
                }
                NdefRecord record = ndefMessages[0].getRecords()[0];

                byte[] payload = record.getPayload();
                int lng = payload[0] & 0063;
                String textencoding = "UTF-8";
                String text = null;
                try {
                    text = new String(payload, lng + 1, payload.length - lng - 1, textencoding);
                    cardId=text;
                   // Toasty.error(ProductCart.this, text, Toast.LENGTH_SHORT).show();
                    final TextView dialogOrderPaymentMethod = dialogView.findViewById(R.id.dialog_order_status);
                    final TextView dialogOrderType = dialogView.findViewById(R.id.dialog_order_type);
                    final TextView dialogCustomer = dialogView.findViewById(R.id.dialog_customer);

                    final EditText dialogEtxtDiscount = dialogView.findViewById(R.id.etxt_dialog_discount);
                    String orderType1 =  dialogOrderType.getText().toString().trim();
                    String orderPaymentMethod = dialogOrderPaymentMethod.getText().toString().trim();
                    String customerName = dialogCustomer.getText().toString().trim();
                    String discount1 = dialogEtxtDiscount.getText().toString().trim();
                    if (discount1.isEmpty()) {
                        discount1 = "0.00";
                    }



                    String shopCurrency = currency;
                    // String tax = shopTax;

                    double getTax = totalTax;
                    proceedOrder(orderType1, orderPaymentMethod, customerName, getTax, discount1, calculatedTotalCost);
                    alertDialogorder.dismiss();

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }



            } else {
                Log.d(TAG, "Not EXTRA_NDEF_MESSAGES");
            }


        }
    }
    public void proceedOrder(String type, String paymentMethod, String customerName, double tax, String discount, double price) {

        databaseAccess = DatabaseAccess.getInstance(ProductCart.this);
        databaseAccess.open();

        int itemCount = databaseAccess.getCartItemCount();

        databaseAccess.open();
        double orderPrice = databaseAccess.getTotalPrice();
        databaseAccess.open();
        double orderPriceBefore=databaseAccess.getTotalPriceBefore();


        if (itemCount > 0) {

            databaseAccess.open();
            //get data from local database
            //final List<HashMap<String, String>> lines;
            lines = databaseAccess.getCartProduct();

            if (lines.isEmpty()) {
                Toasty.error(ProductCart.this, R.string.no_product_found, Toast.LENGTH_SHORT).show();
            } else {

                //get current timestamp
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());
                String currentYear = new SimpleDateFormat("yyyy", Locale.ENGLISH).format(new Date());
                //H denote 24 hours and h denote 12 hour hour format
                String currentTime = new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date()); //HH:mm:ss a

                //timestamp use for invoice id for unique
                Long tsLong = System.currentTimeMillis() / 1000;
                String timeStamp = tsLong.toString();
                Log.d("Time", timeStamp);
                //Invoice number=INV+StaffID+CurrentYear+timestamp
               // String invoiceNumber="INV"+staffId+currentYear+timeStamp;
                String invoiceNumber="INV"+staffId+shopID;

                final JSONObject obj = new JSONObject();
                try {


                    obj.put("invoice_id", invoiceNumber);
                    obj.put("order_date", currentDate);
                    obj.put("order_time", currentTime);
                    obj.put("order_type", type);
                    obj.put("order_payment_method", paymentMethod);
                    obj.put("customer_name", customerName);

                    obj.put("order_price", String.valueOf(orderPrice));
                    obj.put("order_price_before", String.valueOf(orderPriceBefore));
                    obj.put("tax", String.valueOf(tax));
                    obj.put("discount", discount);
                    obj.put("served_by", servedBy);
                    obj.put("shop_id", shopID);
                    obj.put("owner_id", ownerId);
                    obj.put("card_id", cardId);
                    obj.put("pinkartu", pinkartu);
                    obj.put("owner_cardnumber", ownerCardnumber);

                    JSONArray array = new JSONArray();


                    for (int i = 0; i < lines.size(); i++) {

                        databaseAccess.open();
                        String invoiceId = lines.get(i).get("invoice_id");
                        String productId = lines.get(i).get("product_id");
                        String productName = lines.get(i).get("product_name");
                        String productImage = lines.get(i).get("product_image");
                        String productWeightUnit = lines.get(i).get("product_weight_unit");



                        JSONObject objp = new JSONObject();
                        objp.put("invoice_id", invoiceId);
                        objp.put("product_id", productId);
                        objp.put("product_name", productName);
                        objp.put("product_image", productImage);
                        objp.put("product_weight", lines.get(i).get("product_weight") + " " + productWeightUnit);
                        objp.put("product_qty", lines.get(i).get("product_qty"));
                        objp.put("product_price", lines.get(i).get("product_price"));
                        objp.put("product_price_before", lines.get(i).get("product_price_before"));
                        objp.put("product_order_date", currentDate);
                        objp.put("tax", lines.get(i).get("tax"));

                        array.put(objp);

                    }
                    obj.put("lines", array);


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Utils utils=new Utils();

                if(utils.isNetworkAvailable(ProductCart.this))
                {
                  // orderSubmit(obj);
                    postData(obj);
                }
                else
                {
                    Toasty.error(this, R.string.no_network_connection, Toast.LENGTH_SHORT).show();
                }




            }

        } else {
            Toasty.error(ProductCart.this, R.string.no_product_in_cart, Toast.LENGTH_SHORT).show();
        }
    }

    public void postData(final JSONObject obj){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        String url=Global.urlAPI+"orders_submitbaru.php";

        RequestQueue requstQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, url,obj,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("JSONPost :", response.toString());

                        try {


                            String query_result = response.getString("hasil");

                            if (query_result.equals("sukses")) {
                                alertDialogorder.dismiss();

                                databaseAccess.open();
                                databaseAccess.emptyCart();
                                dialogSuccess(obj);
                                Global.vorderList=null;

                            }

                            else {
                               // Toast.makeText(getApplicationContext(), response.getString("data"), Toast.LENGTH_SHORT).show();
                                Toasty.error(ProductCart.this,response.getString("data"), Toast.LENGTH_SHORT).show();


                            }


                        } catch (final JSONException e) {
                            Log.e("tau", "Json parsing error: " + e.getMessage());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                                    "Json parsing error: " + e.getMessage(),
                                                    Toast.LENGTH_LONG)
                                            .show();
                                }
                            });

                        } //
                        progressDialog.dismiss();


                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.d("Image response", error.toString());
                        Log.d("JSONPost :","ggal");
                    }
                }
        ){
            //here I want to post data to sever
        };
        requstQueue.add(jsonobj);

    }


    public void dialogSuccess(final JSONObject obj) {


        AlertDialog.Builder dialog = new AlertDialog.Builder(ProductCart.this);
         dialogView = getLayoutInflater().inflate(R.layout.dialog_success, null);
        dialog.setView(dialogView);
        dialog.setCancelable(false);

        ImageButton dialogBtnCloseDialog = dialogView.findViewById(R.id.btn_close_dialog);
        Button dialogBtnViewAllOrders = dialogView.findViewById(R.id.btn_view_all_orders);
        Button dialogBtnPrint = dialogView.findViewById(R.id.btn_print);

        AlertDialog alertDialogSuccess = dialog.create();

        dialogBtnCloseDialog.setOnClickListener(v -> {

            alertDialogSuccess.dismiss();

            Intent intent = new Intent(ProductCart.this,PosActivity.class);
            startActivity(intent);
            finish();

        });


        dialogBtnViewAllOrders.setOnClickListener(v -> {

            alertDialogSuccess.dismiss();

            Intent intent = new Intent(ProductCart.this, OrdersActivity.class);
            startActivity(intent);
            finish();

        });
        dialogBtnPrint.setOnClickListener(v -> {
            Log.d("Json",obj.toString());
            //alertDialogSuccess.dismiss();

            Intent i = new Intent(ProductCart.this, OrderDetailsActivity.class);
            i.putExtra(Constant.INVOICE_ID, obj.optString("invoice_id"));
            i.putExtra(Constant.CUSTOMER_NAME, obj.optString("customer_name"));
            i.putExtra(Constant.TAX, obj.optString("tax"));
            i.putExtra(Constant.ORDER_PRICE, obj.optString("order_price"));
            i.putExtra(Constant.ORDER_PRICE_BEFORE, obj.optString("order_price_before"));
            i.putExtra(Constant.DISCOUNT, obj.optString("discount"));
            i.putExtra(Constant.ORDER_DATE, obj.optString("order_date"));
            i.putExtra(Constant.ORDER_TIME,obj.optString("order_time"));
            ProductCart.this.startActivity(i);
            //finish();

        });

        alertDialogSuccess.show();


    }


    //dialog for taking otp code
    public void dialog() {

        databaseAccess.open();
         totalTax = databaseAccess.getTotalTax();


        String shopCurrency = currency;
       // String tax = shopTax;

        double getTax = totalTax;



         dialog = new AlertDialog.Builder(ProductCart.this);
         dialogView = getLayoutInflater().inflate(R.layout.dialog_payment, null);
        dialog.setView(dialogView);
        dialog.setCancelable(false);

        final Button dialogBtnSubmit = dialogView.findViewById(R.id.btn_submit);
        final ImageButton dialogBtnClose = dialogView.findViewById(R.id.btn_close);
        final TextView dialogOrderPaymentMethod = dialogView.findViewById(R.id.dialog_order_status);
        final TextView dialogOrderType = dialogView.findViewById(R.id.dialog_order_type);
        final TextView dialogCustomer = dialogView.findViewById(R.id.dialog_customer);
        final TextView dialogTxtTotal = dialogView.findViewById(R.id.dialog_txt_total);
        final TextView dialogTxtTotalTax = dialogView.findViewById(R.id.dialog_txt_total_tax);
        final TextView dialogTxtLevelTax = dialogView.findViewById(R.id.dialog_level_tax);
        final TextView dialogTxtTotalCost = dialogView.findViewById(R.id.dialog_txt_total_cost);
        final EditText dialogEtxtDiscount = dialogView.findViewById(R.id.etxt_dialog_discount);
        final LottieAnimationView animationView = dialogView.findViewById(R.id.animationView);;

        final ImageButton dialogImgCustomer = dialogView.findViewById(R.id.img_select_customer);
        final ImageButton dialogImgOrderPaymentMethod = dialogView.findViewById(R.id.img_order_payment_method);
        final ImageButton dialogImgOrderType = dialogView.findViewById(R.id.img_order_type);


        dialogTxtLevelTax.setText(getString(R.string.total_tax));
        double totalCost = CartAdapter.totalPrice;
        dialogTxtTotal.setText(shopCurrency + totalCost);


        dialogTxtTotalTax.setText(shopCurrency + f.format(getTax));


        double discount = 0;
         calculatedTotalCost = totalCost + getTax - discount;
        dialogTxtTotalCost.setText(shopCurrency + calculatedTotalCost);


        dialogEtxtDiscount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("data", s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                double discount = 0;
                String getDiscount = s.toString();
                if (!getDiscount.isEmpty() && !getDiscount.equals(".")) {
                     calculatedTotalCost = totalCost + getTax;
                    discount = Double.parseDouble(getDiscount);
                    if (discount > calculatedTotalCost) {
                        dialogEtxtDiscount.setError(getString(R.string.discount_cant_be_greater_than_total_price));
                        dialogEtxtDiscount.requestFocus();

                        dialogBtnSubmit.setVisibility(View.INVISIBLE);

                    } else {

                        dialogBtnSubmit.setVisibility(View.VISIBLE);
                        calculatedTotalCost = totalCost + getTax - discount;
                        dialogTxtTotalCost.setText(shopCurrency + calculatedTotalCost);
                    }
                } else {

                     calculatedTotalCost = totalCost + getTax - discount;
                    dialogTxtTotalCost.setText(shopCurrency + calculatedTotalCost);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("data", s.toString());
            }
        });


        orderTypeNames = new ArrayList<>();
        databaseAccess.open();

        //get data from local database
        final List<HashMap<String, String>> orderType;
        orderType = databaseAccess.getOrderType();

        for (int i = 0; i < orderType.size(); i++) {

            // Get the ID of selected Country
            orderTypeNames.add(orderType.get(i).get("order_type_name"));

        }


        //payment methods
        paymentMethodNames = new ArrayList<>();
        databaseAccess.open();

        //get data from local database
        final List<HashMap<String, String>> paymentMethod;
        paymentMethod = databaseAccess.getPaymentMethod();
        paymentMethodNames.add("CARD");
        for (int i = 0; i < paymentMethod.size(); i++) {

            // Get the ID of selected Country
           // paymentMethodNames.add(paymentMethod.get(i).get("payment_method_name"));

        }


        dialogImgOrderPaymentMethod.setOnClickListener(v -> {

            paymentMethodAdapter = new ArrayAdapter<>(ProductCart.this, android.R.layout.simple_list_item_1);
            paymentMethodAdapter.addAll(paymentMethodNames);

            AlertDialog.Builder dialog1 = new AlertDialog.Builder(ProductCart.this);
            View dialogView1 = getLayoutInflater().inflate(R.layout.dialog_list_search, null);
            dialog1.setView(dialogView1);
            dialog1.setCancelable(false);

            Button dialogButton = (Button) dialogView1.findViewById(R.id.dialog_button);
            EditText dialogInput = (EditText) dialogView1.findViewById(R.id.dialog_input);
            TextView dialogTitle = (TextView) dialogView1.findViewById(R.id.dialog_title);
            ListView dialogList = (ListView) dialogView1.findViewById(R.id.dialog_list);


            dialogTitle.setText(R.string.select_payment_method);
            dialogList.setVerticalScrollBarEnabled(true);
            dialogList.setAdapter(paymentMethodAdapter);

            dialogInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    Log.d("data", s.toString());
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    paymentMethodAdapter.getFilter().filter(charSequence);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d("data", s.toString());
                }
            });


            final AlertDialog alertDialog = dialog1.create();

            dialogButton.setOnClickListener(v1 -> alertDialog.dismiss());

            alertDialog.show();


            dialogList.setOnItemClickListener((parent, view, position, id) -> {

                alertDialog.dismiss();
                String selectedItem = paymentMethodAdapter.getItem(position);
                dialogOrderPaymentMethod.setText(selectedItem);


            });
        });


        dialogImgOrderType.setOnClickListener(v -> {


            orderTypeAdapter = new ArrayAdapter<>(ProductCart.this, android.R.layout.simple_list_item_1);
            orderTypeAdapter.addAll(orderTypeNames);

            AlertDialog.Builder dialog12 = new AlertDialog.Builder(ProductCart.this);
            View dialogView12 = getLayoutInflater().inflate(R.layout.dialog_list_search, null);
            dialog12.setView(dialogView12);
            dialog12.setCancelable(false);

            Button dialogButton = (Button) dialogView12.findViewById(R.id.dialog_button);
            EditText dialogInput = (EditText) dialogView12.findViewById(R.id.dialog_input);
            TextView dialogTitle = (TextView) dialogView12.findViewById(R.id.dialog_title);
            ListView dialogList = (ListView) dialogView12.findViewById(R.id.dialog_list);


            dialogTitle.setText(R.string.select_order_type);
            dialogList.setVerticalScrollBarEnabled(true);
            dialogList.setAdapter(orderTypeAdapter);

            dialogInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    Log.d("data", s.toString());
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    orderTypeAdapter.getFilter().filter(charSequence);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d("data", s.toString());
                }
            });


            final AlertDialog alertDialog = dialog12.create();

            dialogButton.setOnClickListener(v13 -> alertDialog.dismiss());

            alertDialog.show();


            dialogList.setOnItemClickListener((parent, view, position, id) -> {

                alertDialog.dismiss();
                String selectedItem = orderTypeAdapter.getItem(position);


                dialogOrderType.setText(selectedItem);


            });
        });


        dialogImgCustomer.setOnClickListener(v -> {
            customerAdapter = new ArrayAdapter<>(ProductCart.this, android.R.layout.simple_list_item_1);
            customerAdapter.addAll(customerNames);

            AlertDialog.Builder dialog13 = new AlertDialog.Builder(ProductCart.this);
            View dialogView13 = getLayoutInflater().inflate(R.layout.dialog_list_search, null);
            dialog13.setView(dialogView13);
            dialog13.setCancelable(false);

            Button dialogButton = (Button) dialogView13.findViewById(R.id.dialog_button);
            EditText dialogInput = (EditText) dialogView13.findViewById(R.id.dialog_input);
            TextView dialogTitle = (TextView) dialogView13.findViewById(R.id.dialog_title);
            ListView dialogList = (ListView) dialogView13.findViewById(R.id.dialog_list);

            dialogTitle.setText(R.string.select_customer);
            dialogList.setVerticalScrollBarEnabled(true);
            dialogList.setAdapter(customerAdapter);

            dialogInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    Log.d("data", s.toString());
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    customerAdapter.getFilter().filter(charSequence);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d("data", s.toString());
                }
            });


            final AlertDialog alertDialog = dialog13.create();

            dialogButton.setOnClickListener(v12 -> alertDialog.dismiss());

            alertDialog.show();


            dialogList.setOnItemClickListener((parent, view, position, id) -> {

                alertDialog.dismiss();
                String selectedItem = customerAdapter.getItem(position);


                dialogCustomer.setText(selectedItem);


            });
        });


        alertDialogorder = dialog.create();
        alertDialogorder.show();


        dialogBtnSubmit.setOnClickListener(v -> {

            String orderType1 = dialogOrderType.getText().toString().trim();
            String orderPaymentMethod = dialogOrderPaymentMethod.getText().toString().trim();
            String customerName = dialogCustomer.getText().toString().trim();
            String discount1 = dialogEtxtDiscount.getText().toString().trim();
            if (discount1.isEmpty()) {
                discount1 = "0.00";
            }
            if(showpin)   showPin();
            else
            proceedOrder(orderType1, orderPaymentMethod, customerName, getTax, discount1, calculatedTotalCost);



            //alertDialogorder.dismiss();
        });


        dialogBtnClose.setOnClickListener(v -> alertDialogorder.dismiss());

        animationView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                animationView.playAnimation();

            }
        });
    }

    private void showPin() {

        dialogpin = new Dialog(this);
        dialogpin.setContentView(R.layout.dialog_pin);
        TextView clearpin = dialogpin.findViewById(R.id.clearpin);
        mEt1 = dialogpin.findViewById(R.id.otp_edit_text1);
        mEt2 = dialogpin.findViewById(R.id.otp_edit_text2);
        mEt3 = dialogpin.findViewById(R.id.otp_edit_text3);
        mEt4 = dialogpin.findViewById(R.id.otp_edit_text4);
        mEt5 = dialogpin.findViewById(R.id.otp_edit_text5);
        mEt6 = dialogpin.findViewById(R.id.otp_edit_text6);
        addTextWatcher(mEt1);
        addTextWatcher(mEt2);
        addTextWatcher(mEt3);
        addTextWatcher(mEt4);
        addTextWatcher(mEt5);
        addTextWatcher(mEt6);
        //Mengeset judul dialog
        dialogpin.setTitle("Masukkan PIN");
        mEt1.requestFocus();
        dialogpin.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //Mengeset layout


        //Membuat agar dialog tidak hilang saat di click di area luar dialog
        dialogpin.setCanceledOnTouchOutside(false);

        //Membuat dialog agar berukuran responsive
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        dialogpin.getWindow().setLayout((6 * width) / 7, LinearLayout.LayoutParams.WRAP_CONTENT);


         saveButton = (Button) dialogpin.findViewById(R.id.btn_verify);


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView dialogOrderPaymentMethod = dialogView.findViewById(R.id.dialog_order_status);
                final TextView dialogOrderType = dialogView.findViewById(R.id.dialog_order_type);
                final TextView dialogCustomer = dialogView.findViewById(R.id.dialog_customer);

                final EditText dialogEtxtDiscount = dialogView.findViewById(R.id.etxt_dialog_discount);
                String orderType1 =  dialogOrderType.getText().toString().trim();
                String orderPaymentMethod = dialogOrderPaymentMethod.getText().toString().trim();
                String customerName = dialogCustomer.getText().toString().trim();
                String discount1 = dialogEtxtDiscount.getText().toString().trim();
                if (discount1.isEmpty()) {
                    discount1 = "0.00";
                }

                double getTax = totalTax;
                //vpin =txtPin.getText().toString();
                pinkartu=mEt1.getText().toString()+mEt2.getText().toString()+mEt3.getText().toString()+mEt4.getText().toString()+mEt5.getText().toString()+mEt6.getText().toString();
                proceedOrder(orderType1, orderPaymentMethod, customerName, getTax, discount1, calculatedTotalCost);
                dialogpin.dismiss();


            }
        });

        clearpin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEt1.setText("");
                mEt2.setText("");
                mEt3.setText("");
                mEt4.setText("");
                mEt5.setText("");
                mEt6.setText("");
                mEt1.requestFocus();


            }
        });

        //Menampilkan custom dialog
        dialogpin.show();

    }


    private void addTextWatcher(final EditText one) {
        one.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                switch (one.getId()) {
                    case R.id.otp_edit_text1:
                        if (one.length() == 1) {
                            mEt2.requestFocus();
                        }
                        break;
                    case R.id.otp_edit_text2:
                        if (one.length() == 1) {
                            mEt3.requestFocus();
                        } else if (one.length() == 0) {
                            mEt1.requestFocus();
                        }
                        break;
                    case R.id.otp_edit_text3:
                        if (one.length() == 1) {
                            mEt4.requestFocus();
                        } else if (one.length() == 0) {
                            mEt2.requestFocus();
                        }
                        break;
                    case R.id.otp_edit_text4:
                        if (one.length() == 1) {
                            mEt5.requestFocus();
                        } else if (one.length() == 0) {
                            mEt3.requestFocus();
                        }
                        break;
                    case R.id.otp_edit_text5:
                        if (one.length() == 1) {
                            mEt6.requestFocus();
                        } else if (one.length() == 0) {
                            mEt4.requestFocus();
                        }
                        break;
                    case R.id.otp_edit_text6:
                        if (one.length() == 1) {
                            saveButton.requestFocus();
                        } else if (one.length() == 0) {
                            mEt5.requestFocus();
                        }
                        break;
                }
            }
        });
    }
    public void getCustomers(String shopId,String ownerId) {

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        Call<List<Customer>> call;


        call = apiInterface.getCustomers("",shopId,ownerId);

        call.enqueue(new Callback<List<Customer>>() {
            @Override
            public void onResponse(@NonNull Call<List<Customer>> call, @NonNull Response<List<Customer>> response) {


                if (response.isSuccessful() && response.body() != null) {

                    customerData = response.body();

                    customerNames = new ArrayList<>();

                    for (int i = 0; i < customerData.size(); i++) {

                       customerNames.add(customerData.get(i).getCustomerName());

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Customer>> call, @NonNull Throwable t) {

                //write own action
            }
        });


    }




    //for back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

