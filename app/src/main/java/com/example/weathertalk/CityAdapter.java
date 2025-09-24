package com.example.weathertalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    private final List<CityWeather> cityList;
    private final OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public CityAdapter(List<CityWeather> cityList, OnDeleteClickListener listener) {
        this.cityList = cityList;
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        CityWeather cityWeather = cityList.get(position);
        holder.txtCityName.setText(cityWeather.getCityName());
        holder.txtWeatherInfo.setText(cityWeather.getWeatherInfo());

        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView txtCityName, txtWeatherInfo;
        ImageButton btnDelete;

        CityViewHolder(View itemView) {
            super(itemView);
            txtCityName = itemView.findViewById(R.id.txtCityName);
            txtWeatherInfo = itemView.findViewById(R.id.txtWeatherInfo);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
