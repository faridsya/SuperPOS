package com.app.superpos.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.app.superpos.R;
import com.app.superpos.settings.SettingsActivity;
import com.app.superpos.settings.categories.CategoriesActivity;
import com.app.superpos.settings.payment_method.PaymentMethodActivity;
import com.app.superpos.settings.shop.ShopInformationActivity;

public class SettingsFragment extends Fragment {
    private View rootView;
    CardView cardShopInfo,cardCategory,cardPaymentMethod;
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView=inflater.inflate(R.layout.fragment_settings, container, false);
        cardShopInfo = rootView.findViewById(R.id.card_shop_info);
        cardCategory=rootView.findViewById(R.id.card_category);
        cardPaymentMethod=rootView.findViewById(R.id.card_payment_method);



        cardShopInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ShopInformationActivity.class);
                startActivity(intent);
            }
        });


        cardCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), CategoriesActivity.class);
                startActivity(intent);
            }
        });


        cardPaymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), PaymentMethodActivity.class);
                startActivity(intent);
            }
        });
        getActivity().setTitle(R.string.action_settings);
        return rootView;
    }
}