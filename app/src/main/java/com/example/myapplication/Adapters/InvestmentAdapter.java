package com.example.myapplication.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Models.Investment;
import com.example.myapplication.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

public class InvestmentAdapter extends RecyclerView.Adapter<InvestmentAdapter.ViewHolder>{
    private static final String TAG = "InvestmentAdapter";

    private ArrayList<Investment> investments = new ArrayList<>();
    private int number = -1;
    private Context context;

    public InvestmentAdapter(Context context) {
        this.context = context;
    }

    public InvestmentAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_investment,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: started");
        holder.name.setText(investments.get(position).getName());
        holder.initDate.setText(investments.get(position).getInit_date());
        holder.finishDate.setText( investments.get(position).getFinish_date());
        holder.amount.setText(String.valueOf(investments.get(position).getAmount()));
        holder.roi.setText(String.valueOf(investments.get(position).getMonthly_roi()));
        holder.profit.setText(String.valueOf(getTotalProfit(investments.get(position))));

        if(number ==-1){
            holder.parent.setCardBackgroundColor(context.getResources().getColor(R.color.light_grey));
            number =1;
        }else{
            holder.parent.setCardBackgroundColor(context.getResources().getColor(R.color.light_blue));
            number=-1;
        }
    }

    private double getTotalProfit(Investment investment) {
        Log.d(TAG, "getTotalProfit: calculating total profit for" + investment.toString());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        double profit = 0.0;


        try {
            calendar.setTime(sdf.parse(investment.getInit_date()));
            int initMonths = calendar.get(Calendar.YEAR)*12 + calendar.get(Calendar.MONTH);

            calendar.setTime(sdf.parse(investment.getFinish_date()));
            int finishMonths = calendar.get(Calendar.YEAR)*12 + calendar.get(Calendar.MONTH);

            int difference = finishMonths-initMonths;

            for (int i=0;i<difference;i++){
                profit += investment.getAmount() * investment.getMonthly_roi()/100;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return profit;
    }

    @Override
    public int getItemCount() {
        return investments.size();
    }

    public void setInvestments(ArrayList<Investment> investments) {
        this.investments = investments;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView name, initDate, finishDate, roi, profit, amount;
        private CardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.txtInvestmentName);
            initDate = (TextView) itemView.findViewById(R.id.txtInitDate);
            finishDate = (TextView) itemView.findViewById(R.id.txtFinishDate);
            roi = (TextView) itemView.findViewById(R.id.txtROI);
            profit = (TextView) itemView.findViewById(R.id.txtProfitAmount);
            amount = (TextView) itemView.findViewById(R.id.txtAmount);
            parent = (CardView) itemView.findViewById(R.id.parent);
        }
    }
}
