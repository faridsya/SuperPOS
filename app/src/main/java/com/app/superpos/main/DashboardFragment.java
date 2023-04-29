package com.app.superpos.main;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype.Slidetop;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.app.superpos.Constant;
import com.app.superpos.HomeActivity;
import com.app.superpos.R;
import com.app.superpos.customers.CustomersActivity;
import com.app.superpos.expense.ExpenseActivity;
import com.app.superpos.login.LoginActivity;
import com.app.superpos.orders.OrdersActivity;
import com.app.superpos.pos.PosActivity;
import com.app.superpos.product.ProductActivity;
import com.app.superpos.report.ReportActivity;
import com.app.superpos.settings.SettingsActivity;
import com.app.superpos.suppliers.SuppliersActivity;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class DashboardFragment extends Fragment {
    CardView cardCustomers, cardProducts, cardSupplier, cardPos, cardOrderList, cardReport, cardSettings, cardExpense, cardLogout;
    //for double back press to exit
    private static final int TIME_DELAY = 2000;
    private static long backPressed;
    private View rootView;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    String userType;
    TextView txtShopName, txtSubText;
    public DashboardFragment() {
        // Required empty public constructor
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView= inflater.inflate(R.layout.fragment_dashboard, container, false);
        cardCustomers = rootView.findViewById(R.id.card_customers);
        cardSupplier = rootView.findViewById(R.id.card_suppliers);
        cardProducts = rootView.findViewById(R.id.card_products);
        cardPos = rootView.findViewById(R.id.card_pos);
        cardOrderList = rootView.findViewById(R.id.card_all_orders);
        cardReport = rootView.findViewById(R.id.card_reports);
        cardSettings = rootView.findViewById(R.id.card_settings);
        cardExpense = rootView.findViewById(R.id.card_expense);
        cardLogout = rootView.findViewById(R.id.card_logout);
        txtShopName = rootView.findViewById(R.id.txt_shop_name);
        txtSubText = rootView.findViewById(R.id.txt_sub_text);
        sp =  getActivity().getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        getActivity().setTitle("Dashboard");
        userType = sp.getString(Constant.SP_USER_TYPE, "");
        String shopName = sp.getString(Constant.SP_SHOP_NAME, "");
        String staffName = sp.getString(Constant.SP_STAFF_NAME, "");
        txtShopName.setText(shopName);
        txtSubText.setText(getString(R.string.hi) + " " + staffName);


        if (SDK_INT >= 23) //Android MarshMellow Version or above
        {
            requestPermission();

        }



        cardCustomers.setOnClickListener(v -> {
            Intent intent = new Intent( getActivity(), CustomersActivity.class);
            startActivity(intent);


        });

        cardSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( getActivity(), SuppliersActivity.class);
                startActivity(intent);


            }
        });


        cardProducts.setOnClickListener(v -> {
            Intent intent = new Intent( getActivity(), ProductActivity.class);
            startActivity(intent);


        });


        cardPos.setOnClickListener(v -> {
            Intent intent = new Intent( getActivity(), PosActivity.class);
            startActivity(intent);


        });

        cardOrderList.setOnClickListener(v -> {
            Intent intent = new Intent( getActivity(), OrdersActivity.class);
            startActivity(intent);


        });


        cardReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userType.equals(Constant.ADMIN) || userType.equals(Constant.MANAGER)) {
                    Intent intent = new Intent( getActivity(), ReportActivity.class);
                    startActivity(intent);
                } else {
                    Toasty.error( getActivity(), R.string.you_dont_have_permission_to_access_this_page, Toast.LENGTH_SHORT).show();
                }

            }
        });


        cardExpense.setOnClickListener(v -> {

            if (userType.equals(Constant.ADMIN) || userType.equals(Constant.MANAGER)) {
                Intent intent = new Intent( getActivity(), ExpenseActivity.class);
                startActivity(intent);
            } else {
                Toasty.error( getActivity(), R.string.you_dont_have_permission_to_access_this_page, Toast.LENGTH_SHORT).show();
            }
        });


        cardSettings.setOnClickListener(v -> {

            if (userType.equals(Constant.ADMIN)) {
                Intent intent = new Intent( getActivity(), SettingsActivity.class);
                startActivity(intent);
            } else {
                Toasty.error( getActivity(), R.string.you_dont_have_permission_to_access_this_page, Toast.LENGTH_SHORT).show();
            }
        });


        cardLogout.setOnClickListener(v -> {


            NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance( getActivity());
            dialogBuilder
                    .withTitle(getString(R.string.logout))
                    .withMessage(R.string.want_to_logout_from_app)
                    .withEffect(Slidetop)
                    .withDialogColor("#43a047") //use color code for dialog
                    .withButton1Text(getString(R.string.yes))
                    .withButton2Text(getString(R.string.cancel))
                    .setButton1Click(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editor.putString(Constant.SP_EMAIL, "");
                            editor.putString(Constant.SP_PASSWORD, "");
                            editor.putString(Constant.SP_USER_NAME, "");
                            editor.putString(Constant.SP_USER_TYPE, "");
                            editor.apply();

                            Intent intent = new Intent( getActivity(), LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            ;
                            dialogBuilder.dismiss();
                        }
                    })
                    .setButton2Click(v1 -> dialogBuilder.dismiss())
                    .show();


        });

        return rootView;
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestPermission() {
        Dexter.withActivity(getActivity())
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {

                            //write your action if needed
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings

                        }
                    }


                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                // withErrorListener(error -> Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show())
                .onSameThread()
                .check();
    }
}