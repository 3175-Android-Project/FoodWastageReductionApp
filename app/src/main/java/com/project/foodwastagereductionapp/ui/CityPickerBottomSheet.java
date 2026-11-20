package com.project.foodwastagereductionapp.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.project.foodwastagereductionapp.R;

import java.util.Arrays;
import java.util.List;

public final class CityPickerBottomSheet {

    public interface OnCitySelectedListener {
        void onCitySelected(String city);
    }

    private static final List<String> CITIES = Arrays.asList(
            "Vancouver",
            "Burnaby",
            "Richmond",
            "Surrey",
            "Coquitlam",
            "North Vancouver",
            "West Vancouver",
            "New Westminster",
            "Langley",
            "Delta",
            "Port Coquitlam",
            "Port Moody"
    );

    private CityPickerBottomSheet() {
    }

    public static List<String> cities() {
        return CITIES;
    }

    public static void show(Context context, OnCitySelectedListener listener) {
        showOptions(context, R.string.titleSelectCity, CITIES, listener);
    }

    /** Same bottom sheet with any title and list (used for cities and food categories). */
    public static void showOptions(Context context, int titleRes, List<String> options,
                                   OnCitySelectedListener listener) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_city_picker, null);
        dialog.setContentView(view);

        android.widget.FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(android.R.color.transparent);
            com.google.android.material.bottomsheet.BottomSheetBehavior<android.widget.FrameLayout> behavior =
                    com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
            behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
        if (dialog.getWindow() != null) {
            dialog.getWindow().setNavigationBarColor(android.graphics.Color.WHITE);
        }

        TextView txtSheetTitle = view.findViewById(R.id.txtSheetTitle);
        if (txtSheetTitle != null) txtSheetTitle.setText(titleRes);

        ImageView btnClose = view.findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        RecyclerView recyclerCities = view.findViewById(R.id.recyclerCities);
        recyclerCities.setLayoutManager(new LinearLayoutManager(context));
        recyclerCities.setAdapter(new CityAdapter(options, selectedCity -> {
            listener.onCitySelected(selectedCity);
            dialog.dismiss();
        }));

        dialog.show();
    }

    private static class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {
        private final List<String> cities;
        private final OnCitySelectedListener listener;

        CityAdapter(List<String> cities, OnCitySelectedListener listener) {
            this.cities = cities;
            this.listener = listener;
        }

        @NonNull
        @Override
        public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_city_option, parent, false);
            return new CityViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
            String city = cities.get(position);
            holder.txtCityName.setText(city);
            holder.itemView.setOnClickListener(v -> listener.onCitySelected(city));
            if (holder.divider != null) {
                holder.divider.setVisibility(position < getItemCount() - 1 ? View.VISIBLE : View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return cities.size();
        }

        static class CityViewHolder extends RecyclerView.ViewHolder {
            final TextView txtCityName;
            final View divider;

            CityViewHolder(@NonNull View itemView) {
                super(itemView);
                txtCityName = itemView.findViewById(R.id.txtCityName);
                divider = itemView.findViewById(R.id.divider);
            }
        }
    }
}
