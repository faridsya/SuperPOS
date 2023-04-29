package com.app.superpos.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.app.superpos.R;
import com.app.superpos.report.ExpenseGraphActivity;
import com.app.superpos.report.ExpenseReportActivity;
import com.app.superpos.report.GraphReportActivity;
import com.app.superpos.report.ReportActivity;
import com.app.superpos.report.SalesReportActivity;

public class ReportFragment extends Fragment {
    CardView cardSalesReport, cardGraphReport, cardExpenseReport, cardExpenseGraph;
    private View rootView;
    public ReportFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView= inflater.inflate(R.layout.fragment_report, container, false);
        cardSalesReport = rootView.findViewById(R.id.card_sales_report);
        cardGraphReport = rootView.findViewById(R.id.card_graph_report);
        cardExpenseGraph = rootView.findViewById(R.id.card_expense_graph);
        cardExpenseReport = rootView.findViewById(R.id.card_expense_report);
        getActivity().setTitle(R.string.report);
        cardSalesReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SalesReportActivity.class);
                startActivity(intent);
            }
        });


        cardGraphReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GraphReportActivity.class);
                startActivity(intent);
            }
        });


        cardExpenseReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ExpenseReportActivity.class);
                startActivity(intent);
            }
        });


        cardExpenseGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ExpenseGraphActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }
}