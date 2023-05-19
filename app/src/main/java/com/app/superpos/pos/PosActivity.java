package com.app.superpos.pos;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.superpos.Constant;
import com.app.superpos.R;
import com.app.superpos.adapter.PosProductAdapter;
import com.app.superpos.adapter.ProductCategoryAdapter;
import com.app.superpos.database.DatabaseAccess;
import com.app.superpos.model.Category;
import com.app.superpos.model.Product;
import com.app.superpos.networking.ApiClient;
import com.app.superpos.networking.ApiInterface;
import com.app.superpos.utils.BaseActivity;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PosActivity extends BaseActivity {


    private RecyclerView recyclerView, categoryRecyclerView;
    PosProductAdapter productAdapter;
    TextView txtNoProducts, txtReset;
    ProductCategoryAdapter categoryAdapter;

    ImageView imgNoProduct, imgScanner, imgCart, imgBack,imgempty,speak,speakno;
    RelativeLayout rspeakno,rspeak;
    ;
    public static EditText etxtSearch;
    public static TextView txtCount;
    private SpeechRecognizer speechRecognizer;
    private ShimmerFrameLayout mShimmerViewContainer;
    DatabaseAccess databaseAccess;
    private Boolean isListening = false;
    private int RecordAudioRequestCode = 1;
    List<Product> productsList;
    MediaPlayer player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);


        getSupportActionBar().setHomeButtonEnabled(true); //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//for back button
        getSupportActionBar().setTitle(R.string.all_product);
        getSupportActionBar().hide();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
            }
        }
        player = MediaPlayer.create(this, R.raw.delete_sound);
        etxtSearch = findViewById(R.id.etxt_search);
        recyclerView = findViewById(R.id.recycler);
        imgNoProduct = findViewById(R.id.image_no_product);
        speak = findViewById(R.id.speak);
        speakno = findViewById(R.id.speakno);
        rspeak = findViewById(R.id.rspeak);
        rspeakno = findViewById(R.id.rspeakno);
        txtNoProducts = findViewById(R.id.txt_no_products);
        imgScanner = findViewById(R.id.img_scanner);
        categoryRecyclerView = findViewById(R.id.category_recyclerview);
        txtReset = findViewById(R.id.txt_reset);
        imgBack = findViewById(R.id.img_back);
        imgCart = findViewById(R.id.img_cart);
        imgempty = findViewById(R.id.img_empty);
        txtCount = findViewById(R.id.txt_count);
        databaseAccess = DatabaseAccess.getInstance(PosActivity.this);

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        SharedPreferences sp = getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String shopID = sp.getString(Constant.SP_SHOP_ID, "");
        String ownerId = sp.getString(Constant.SP_OWNER_ID, "");


        imgScanner.setOnClickListener(v -> {
            Intent intent = new Intent(PosActivity.this, ScannerActivity.class);
            startActivity(intent);
        });

        imgNoProduct.setVisibility(View.GONE);
        txtNoProducts.setVisibility(View.GONE);

        getProductCategory(shopID, ownerId);

        // set a GridLayoutManager with default vertical orientation and 3 number of columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView
        recyclerView.setHasFixedSize(true);

        //Load data from server
        getProductsData("", shopID, ownerId);

        txtReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etxtSearch.setText("");
               // getProductsData("", shopID, ownerId);
            }
        });


        // set a GridLayoutManager with default vertical orientation and 3 number of columns
        LinearLayoutManager linerLayoutManager = new LinearLayoutManager(PosActivity.this, LinearLayoutManager.HORIZONTAL, false);
        categoryRecyclerView.setLayoutManager(linerLayoutManager); // set LayoutManager to RecyclerView


        categoryRecyclerView.setHasFixedSize(true);


        etxtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                Log.d("data", s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 1) {

                    //search data from server
                    getProductsData(s.toString(), shopID, ownerId);
                } else {
                    getProductsData("", shopID, ownerId);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("data", s.toString());
            }


        });


        databaseAccess.open();
        int count = databaseAccess.getCartItemCount();
        if (count == 0) {
            txtCount.setVisibility(View.INVISIBLE);
        } else {
            txtCount.setVisibility(View.VISIBLE);
            txtCount.setText(String.valueOf(count));
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id");
        //MuteAudio();
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                //MuteAudio();
                etxtSearch.setHint("Ucapkan nama barang...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                Log.d("onEndOfSpeech","");
            }

            @Override
            public void onError(int i) {
                //UnMuteAudio();;
                Log.d("onError","");
                speechRecognizer.startListening(speechRecognizerIntent);
            }

            @Override
            public void onResults(Bundle bundle) {

                ArrayList<String> speechData = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
               // txtRecognizedSpeech.setText(speechData.get(0));

                prosesdengar(speechData.get(0));
                speechRecognizer.startListening(speechRecognizerIntent);
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
        imgCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(PosActivity.this, ProductCart.class);
                startActivity(intent);
            }
        });
        imgempty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseAccess.open();
                databaseAccess.emptyCart();
                txtCount.setVisibility(View.INVISIBLE);
            }
        });


        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRecognizer.startListening(speechRecognizerIntent);
                rspeak.setVisibility(View.GONE);
                rspeakno.setVisibility(View.VISIBLE);
                isListening = true;

            }
        });

        speakno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRecognizer.stopListening();
                rspeakno.setVisibility(View.GONE);
                rspeak.setVisibility(View.VISIBLE);
                isListening = false;

            }
        });


    }

    private void prosesdengar(String hasil){

        Product produk;
        switch (hasil){
            case "keranjang":
                if(!productsList.isEmpty()){
                    produk=productsList.get(0);
                    masukkeranjang(produk);

                }

                break;
            case "proses":
                Intent intent = new Intent(PosActivity.this, ProductCart.class);
                startActivity(intent);
                break;
            case "semua":
                etxtSearch.setText("");
                break;
            case "hapus":
                databaseAccess.open();
                databaseAccess.emptyCart();
                txtCount.setVisibility(View.INVISIBLE);
                break;
            default: etxtSearch.setText(hasil);
        }
    }

    private void masukkeranjang(Product product){
        databaseAccess.open();
        int getQty=databaseAccess.getqty(product.getProductId());
        int getStock=Integer.parseInt(product.getProductStock());
        String productId = product.getProductId();
        String productName = product.getProductName();
        String productWeight = product.getProductWeight();
        String productPrice = product.getProductSellPrice();
        String productPriceBefore = product.getProductSellBefore();
        String weightUnit = product.getProductWeightUnit();
        String productImage = product.getProductImage();
        String productStock = product.getProductStock();

        String tax = product.getTax();

        String imageUrl= Constant.PRODUCT_IMAGE_URL+productImage;
        double itemPrice=Double.parseDouble(productPrice);
        double getTax=Double.parseDouble(tax);



        double taxAmount=(itemPrice*getTax)/100;
        if (getStock<=0 || getStock<=getQty )
        {

            Toasty.warning(this, R.string.stock_not_available_please_update_stock, Toast.LENGTH_SHORT).show();
        }

        else {

            databaseAccess.open();

            int check = databaseAccess.addToCart(productId,productName, productWeight, weightUnit, productPrice, 1,productImage,productStock,taxAmount,productPriceBefore);

            databaseAccess.open();
            int count=databaseAccess.getCartItemCount();
            if (count==0)
            {
                PosActivity.txtCount.setVisibility(View.INVISIBLE);
            }
            else
            {
                PosActivity. txtCount.setVisibility(View.VISIBLE);
                PosActivity.txtCount.setText(String.valueOf(count));
            }

            if (check == 1) {
                Toasty.success(this, R.string.product_added_to_cart, Toast.LENGTH_SHORT).show();
                player.start();
            } else if (check == 2) {
                databaseAccess.open();
                int jumlah=databaseAccess.getqty(productId);
                jumlah++;

                databaseAccess.updateQty(productId, String.valueOf(jumlah));
                player.start();
                Toasty.success(this, R.string.product_added_to_cart, Toast.LENGTH_SHORT).show();
                //Toasty.info(context, R.string.product_already_added_to_cart, Toast.LENGTH_SHORT).show();

            } else {

                Toasty.error(this, R.string.product_added_to_cart_failed_try_again, Toast.LENGTH_SHORT).show();

            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }


    public void getProductCategory(String shopId, String ownerId) {

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        Call<List<Category>> call;


        call = apiInterface.getCategory(shopId, ownerId);

        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {


                if (response.isSuccessful() && response.body() != null) {


                    List<Category> productCategory;
                    productCategory = response.body();

                    if (productCategory.isEmpty()) {
                        Toasty.info(PosActivity.this, R.string.no_data_found, Toast.LENGTH_SHORT).show();
                        imgNoProduct.setImageResource(R.drawable.no_data);


                    } else {

                        categoryAdapter = new ProductCategoryAdapter(PosActivity.this, productCategory, recyclerView, imgNoProduct, txtNoProducts, mShimmerViewContainer);

                        categoryRecyclerView.setAdapter(categoryAdapter);

                    }


                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {

                //write own action
            }
        });


    }
    public void MuteAudio(){
        AudioManager mAlramMAnager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
        } else {
            mAlramMAnager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_ALARM, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_RING, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
    }

    public void UnMuteAudio(){
        AudioManager mAlramMAnager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE,0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);
        } else {
            mAlramMAnager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_ALARM, false);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_RING, false);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
        }
    }

    public void getProductsData(String searchText, String shopId, String ownerId) {

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<List<Product>> call;
        call = apiInterface.getProducts(searchText, shopId, ownerId);

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {


                if (response.isSuccessful() && response.body() != null) {

                    productsList = response.body();


                    if (productsList.isEmpty()) {

                        recyclerView.setVisibility(View.GONE);
                        imgNoProduct.setVisibility(View.VISIBLE);
                        txtNoProducts.setVisibility(View.VISIBLE);
                        imgNoProduct.setImageResource(R.drawable.not_found);
                        //Stopping Shimmer Effects
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);


                    } else {


                        //Stopping Shimmer Effects
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                        txtNoProducts.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        imgNoProduct.setVisibility(View.GONE);
                        productAdapter = new PosProductAdapter(PosActivity.this, productsList);


                        recyclerView.setAdapter(productAdapter);

                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {

                Toast.makeText(PosActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                Log.d("Error : ", t.toString());
            }
        });


    }


    //to recheck item count when back this activity
    @Override
    protected void onResume() {
        super.onResume();


        databaseAccess.open();
        int count = databaseAccess.getCartItemCount();
        if (count == 0) {
            txtCount.setVisibility(View.INVISIBLE);
        } else {
            txtCount.setVisibility(View.VISIBLE);
            txtCount.setText(String.valueOf(count));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //we need to destroy the speechRecogniser on Destroy,
        //if we do not do it it will stay on and drain the battery
        speechRecognizer.destroy();
        //UnMuteAudio();
    }
}
