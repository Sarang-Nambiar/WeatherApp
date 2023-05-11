package com.example.weather_app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.RVHolder> {
    Context context;
    String iconUrl;
    ArrayList<CityModel> arrayList;

    public RVAdapter(Context context, ArrayList<CityModel> arrayList, String iconUrl){
        this.context = context;
        this.arrayList = arrayList;
        this.iconUrl = iconUrl;
    }

    @NonNull
    @Override
    public RVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.rv_item, parent, false);
        return new RVHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RVHolder holder, int position) {
        holder.weatherTextTemp.setText("" + (int) this.arrayList.get(position).getTemp()+ "\u00B0");
        holder.weatherTextCity.setText(this.arrayList.get(position).getCityName());
        if(this.arrayList.get(position).getIconName().contains("d")){ // checking if its day time
            holder.cardView.setCardBackgroundColor(this.context.getResources().getColor(R.color.card_bg_day));
        }else{ // checking if its night time
            holder.cardView.setCardBackgroundColor(this.context.getResources().getColor(R.color.card_bg_night));
        }
        Picasso.get()
                .load(iconUrl + this.arrayList.get(position).getIconName() + "@2x.png")
                .into(holder.weatherIconCity);
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public class RVHolder extends RecyclerView.ViewHolder{
        ImageView weatherIconCity;
        TextView weatherTextCity, weatherTextTemp;

        CardView cardView;
        public RVHolder(@NonNull View itemView) {
            super(itemView);
            weatherIconCity = itemView.findViewById(R.id.weatherIconCity);
            weatherTextCity = itemView.findViewById(R.id.weatherTextCity);
            weatherTextTemp = itemView.findViewById(R.id.weatherTextTemp);
            cardView = itemView.findViewById(R.id.cardViewCity);
        }
    }
}
