package com.example.weathertalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    private final List<CityWeather> cityList;
    private final OnCityLongClickListener longClickListener;

    public interface OnCityLongClickListener {
        void onCityLongClick(int position);
    }

    public CityAdapter(List<CityWeather> cityList, OnCityLongClickListener listener) {
        this.cityList = cityList;
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city_weather, parent, false);
        return new CityViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        CityWeather cityWeather = cityList.get(position);
        holder.txtCityName.setText(cityWeather.getCityName());
        holder.txtCityWeather.setText(cityWeather.getWeatherInfo());
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    class CityViewHolder extends RecyclerView.ViewHolder {
        TextView txtCityName, txtCityWeather;

        CityViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCityName = itemView.findViewById(R.id.txtCityName);
            txtCityWeather = itemView.findViewById(R.id.txtCityWeather);

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    int pos = getBindingAdapterPosition(); // preferred over deprecated getAdapterPosition()
                    if (pos != RecyclerView.NO_POSITION) {
                        longClickListener.onCityLongClick(pos);
                        return true;
                    }
                }
                return false;
            });
        }
    }
}
