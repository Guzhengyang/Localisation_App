package com.valeo.psa.view;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.valeo.psa.R;
import com.valeo.psa.model.Car;

import java.util.ArrayList;
import java.util.List;

public class CarListAdapter extends RecyclerView.Adapter<CarListAdapter.ViewHolder> {
    private final OnCarSelectionListener mCarSelectionListener;
    private final List<Car> mCars = new ArrayList<>();
    private String selectedCarRegistrationPlate;

    public CarListAdapter(OnCarSelectionListener mCarSelectionListener) {
        super();
        this.mCarSelectionListener = mCarSelectionListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.psa_listitem_car, parent, false);
        return new ViewHolder(v, mCarSelectionListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Car car = mCars.get(position);
        final String brandCar = car.getBrandCar();
        int imgCar = car.getImgCarId();
        holder.brandCar.setText(brandCar);
        holder.imgCar.setImageResource(imgCar);
        // Check if a car is saved
        if (car.getRegPlate().equals(selectedCarRegistrationPlate)) {
            holder.brandCar.setPaintFlags(holder.brandCar.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.brandCar.setPaintFlags(holder.brandCar.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return mCars.size();
    }

    public List<Car> getCars() {
        return mCars;
    }

    public void setCars(List<Car> newCars) {
        mCars.clear();
        mCars.addAll(newCars);
        notifyDataSetChanged();
    }

    public void setSelectedCarRegistrationPlate(String selectedCarRegistrationPlate) {
        this.selectedCarRegistrationPlate = selectedCarRegistrationPlate;
    }

    public interface OnCarSelectionListener {
        void onCarSelection(View carSelected, int position);
    }

    /**
     * Class representing a car list item
     *
     * @author GMAMESSI
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView imgCar;
        private final TextView brandCar;
        private final OnCarSelectionListener mListener;

        public ViewHolder(View itemView, OnCarSelectionListener onCarSelectionListener) {
            super(itemView);
            this.mListener = onCarSelectionListener;
            brandCar = (TextView) itemView.findViewById(R.id.selected_car_model);
            imgCar = (ImageView) itemView.findViewById(R.id.car_model_type);
            brandCar.setOnClickListener(this);
            imgCar.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onCarSelection(v, getAdapterPosition());
        }

        public TextView getBrandCar() {
            return brandCar;
        }
    }
}
