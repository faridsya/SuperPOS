package com.app.superpos.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.superpos.Constant;
import com.app.superpos.R;
import com.app.superpos.model.Category;
import com.app.superpos.model.Product;
import com.app.superpos.networking.ApiClient;
import com.app.superpos.networking.ApiInterface;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductCategoryAdapter extends RecyclerView.Adapter<ProductCategoryAdapter.MyViewHolder> {


    MediaPlayer player;
    private List<Category> categoryData;
    private Context context;
    RecyclerView recyclerView;
    ImageView imgNoProduct;
    TextView txtNoProducts;
    private ShimmerFrameLayout mShimmerViewContainer;
    SharedPreferences sp;


    public ProductCategoryAdapter(Context context, List<Category> categoryData, RecyclerView recyclerView, ImageView imgNoProduct, TextView txtNoProducts, ShimmerFrameLayout mShimmerViewContainer) {
        this.context = context;
        this.categoryData = categoryData;
        this.recyclerView = recyclerView;
        player = MediaPlayer.create(context, R.raw.delete_sound);

        this.imgNoProduct = imgNoProduct;
        this.txtNoProducts = txtNoProducts;
        this.mShimmerViewContainer = mShimmerViewContainer;
        sp = context.getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);

    }


    @NonNull
    @Override
    public ProductCategoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_category_item, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ProductCategoryAdapter.MyViewHolder holder, int position) {

        final String categoryId = categoryData.get(position).getProductCategoryId();
        String categoryName = categoryData.get(position).getProductCategoryName();
        String shopID = sp.getString(Constant.SP_SHOP_ID, "");


        holder.txtCategoryName.setText(categoryName);
        holder.cardCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                player.start();
                getProductsData(categoryId, shopID);
                mShimmerViewContainer.startShimmer();


            }
        });


    }

    @Override
    public int getItemCount() {
        return categoryData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtCategoryName;
        CardView cardCategory;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtCategoryName = itemView.findViewById(R.id.txt_category_name);
            cardCategory = itemView.findViewById(R.id.card_category);

        }


    }


    public void getProductsData(String categoryId, String shopId) {

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<List<Product>> call;
        call = apiInterface.searchProductByCategory(categoryId, shopId);

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {


                if (response.isSuccessful() && response.body() != null) {
                    List<Product> productsList;
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

                        recyclerView.setVisibility(View.VISIBLE);
                        imgNoProduct.setVisibility(View.GONE);
                        txtNoProducts.setVisibility(View.GONE);
                        PosProductAdapter productAdapter = new PosProductAdapter(context, productsList);

                        recyclerView.setAdapter(productAdapter);

                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {

                Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                Log.d("Error : ", t.toString());
            }
        });


    }


}
