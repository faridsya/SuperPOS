package com.app.superpos.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.superpos.Constant;
import com.app.superpos.R;
import com.app.superpos.adapter.OrderAdapter;
import com.app.superpos.global.Global;
import com.app.superpos.model.OrderList;
import com.app.superpos.networking.ApiClient;
import com.app.superpos.networking.ApiInterface;
import com.app.superpos.orders.OrdersActivity;
import com.app.superpos.utils.Utils;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoriFragment extends Fragment {
    private RecyclerView recyclerView;

    ImageView imgNoProduct;
    TextView txtNoProducts;
    EditText etxtSearch;
    private View rootView;
    private ShimmerFrameLayout mShimmerViewContainer;
    SwipeRefreshLayout mSwipeRefreshLayout;
    public HistoriFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView= inflater.inflate(R.layout.fragment_history, container, false);
        recyclerView = rootView.findViewById(R.id.recycler);
        imgNoProduct = rootView.findViewById(R.id.image_no_product);
        mShimmerViewContainer = rootView.findViewById(R.id.shimmer_view_container);
        mSwipeRefreshLayout =rootView.findViewById(R.id.swipeToRefresh);

        txtNoProducts=rootView.findViewById(R.id.txt_no_products);
        etxtSearch=rootView.findViewById(R.id.etxt_search_order);

        //set color of swipe refresh
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        imgNoProduct.setVisibility(View.GONE);
        txtNoProducts.setVisibility(View.GONE);
        getActivity().setTitle(R.string.order_history);
        SharedPreferences sp = getActivity().getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String shopID = sp.getString(Constant.SP_SHOP_ID, "");
        String ownerId = sp.getString(Constant.SP_OWNER_ID, "");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager); // set LayoutManager to RecyclerView

        recyclerView.setHasFixedSize(true);

        Utils utils=new Utils();
        mSwipeRefreshLayout.setOnRefreshListener(() -> {

            if (utils.isNetworkAvailable(getActivity()))
            {
                getOrdersData("",shopID,ownerId);
            }
            else
            {
                Toasty.error(getActivity(), R.string.no_network_connection, Toast.LENGTH_SHORT).show();
            }


            //after shuffle id done then swife refresh is off
            mSwipeRefreshLayout.setRefreshing(false);
        });


        if (utils.isNetworkAvailable(getActivity()))
        {
            //Load data from server
            getOrdersData("",shopID,ownerId);
        }
        else
        {
            recyclerView.setVisibility(View.GONE);
            imgNoProduct.setVisibility(View.VISIBLE);
            imgNoProduct.setImageResource(R.drawable.not_found);
            mSwipeRefreshLayout.setVisibility(View.GONE);
            //Stopping Shimmer Effects
            mShimmerViewContainer.stopShimmer();
            mShimmerViewContainer.setVisibility(View.GONE);
            Toasty.error(getActivity(), R.string.no_network_connection, Toast.LENGTH_SHORT).show();
        }


        etxtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                Log.d("data",s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {



                if (s.length() > 1) {

                    //search data from server
                    getOrdersData(s.toString(),shopID,ownerId);
                } else {
                    getOrdersData("",shopID,ownerId);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("data",s.toString());
            }


        });
        return rootView;
    }

    public void getOrdersData(String searchText,String shopId,String ownerId) {

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<List<OrderList>> call;
        call = apiInterface.getOrders(searchText,shopId,ownerId);
        if(Global.vorderList==null) {

            call.enqueue(new Callback<List<OrderList>>() {
                @Override
                public void onResponse(@NonNull Call<List<OrderList>> call, @NonNull Response<List<OrderList>> response) {


                    if (response.isSuccessful() && response.body() != null) {
                        List<OrderList> orderList;
                        orderList = response.body();


                        if (orderList.isEmpty()) {

                            recyclerView.setVisibility(View.GONE);
                            imgNoProduct.setVisibility(View.VISIBLE);
                            imgNoProduct.setImageResource(R.drawable.not_found);
                            //Stopping Shimmer Effects
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);
                            Global.vorderList= new ArrayList<>();


                        } else {


                            //Stopping Shimmer Effects
                            mShimmerViewContainer.stopShimmer();
                            mShimmerViewContainer.setVisibility(View.GONE);

                            recyclerView.setVisibility(View.VISIBLE);
                            imgNoProduct.setVisibility(View.GONE);
                            OrderAdapter orderAdapter = new OrderAdapter(getActivity(), orderList);

                            recyclerView.setAdapter(orderAdapter);
                            Global.vorderList= orderList;
                            //Global.vorderList=orderList;
                        }

                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<OrderList>> call, @NonNull Throwable t) {

                    Toast.makeText(getActivity(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                    Log.d("Error : ", t.toString());
                }
            });
        }
        else {
            //Stopping Shimmer Effects
            mShimmerViewContainer.stopShimmer();
            mShimmerViewContainer.setVisibility(View.GONE);

            recyclerView.setVisibility(View.VISIBLE);
            imgNoProduct.setVisibility(View.GONE);
            OrderAdapter orderAdapter = new OrderAdapter(getActivity(), Global.vorderList);

            recyclerView.setAdapter(orderAdapter);

        }

    }
}