package com.app.superpos.settings.categories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.superpos.Constant;
import com.app.superpos.R;
import com.app.superpos.model.Category;
import com.app.superpos.networking.ApiClient;
import com.app.superpos.networking.ApiInterface;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditCategoryActivity extends AppCompatActivity {

    ProgressDialog loading;
    TextView txtEdit, txtUpdate;
    EditText etCategoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_category);

        getSupportActionBar().setHomeButtonEnabled(true); //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//for back button
        getSupportActionBar().setTitle(R.string.update_category);

        txtEdit = findViewById(R.id.txt_edit);
        txtUpdate = findViewById(R.id.txt_update);
        etCategoryName = findViewById(R.id.et_category_name);

        txtUpdate.setVisibility(View.INVISIBLE);

        String categoryId = getIntent().getExtras().getString("category_id");
        String categoryName = getIntent().getExtras().getString("category_name");


        SharedPreferences sp = getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String shopID = sp.getString(Constant.SP_SHOP_ID, "");
        String ownerId = sp.getString(Constant.SP_OWNER_ID, "");

        etCategoryName.setText(categoryName);

        etCategoryName.setEnabled(false);

        txtEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etCategoryName.setEnabled(true);

                etCategoryName.setTextColor(Color.RED);
                txtUpdate.setVisibility(View.VISIBLE);

                txtEdit.setVisibility(View.GONE);

            }
        });


        txtUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String categoryName = etCategoryName.getText().toString().trim();

                if (categoryName.isEmpty()) {
                    etCategoryName.setError(getString(R.string.category_name));
                    etCategoryName.requestFocus();

                } else {
                    updateCategory(categoryId, categoryName,shopID,ownerId);

                }
            }
        });


    }


    private void updateCategory(String categoryId, String categoryName,String shopId, String ownerId) {

        loading = new ProgressDialog(this);
        loading.setCancelable(false);
        loading.setMessage(getString(R.string.please_wait));
        loading.show();

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        Call<Category> call = apiInterface.updateCategory(categoryId, categoryName,shopId,ownerId);
        call.enqueue(new Callback<Category>() {
            @Override
            public void onResponse(@NonNull Call<Category> call, @NonNull Response<Category> response) {


                if (response.isSuccessful() && response.body() != null) {
                    String value = response.body().getValue();

                    if (value.equals(Constant.KEY_SUCCESS)) {

                        loading.dismiss();

                        Toasty.success(EditCategoryActivity.this, R.string.update_successfully, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EditCategoryActivity.this, CategoriesActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    } else if (value.equals(Constant.KEY_FAILURE)) {

                        loading.dismiss();

                        Toasty.error(EditCategoryActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                        finish();

                    } else {
                        loading.dismiss();
                        Toasty.error(EditCategoryActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Category> call, @NonNull Throwable t) {
                loading.dismiss();
                Log.d("Error! ", t.toString());
                Toasty.error(EditCategoryActivity.this, R.string.no_network_connection, Toast.LENGTH_SHORT).show();
            }
        });
    }


    //for back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}