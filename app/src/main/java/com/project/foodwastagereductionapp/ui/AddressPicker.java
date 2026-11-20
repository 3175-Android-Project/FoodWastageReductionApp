package com.project.foodwastagereductionapp.ui;

import android.Manifest;
import android.location.Address;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.project.foodwastagereductionapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Province + city dropdowns, plus "Use my current location" to fill them in.
 * Used by sign-up, Edit Profile and the manager's restaurant setup.
 * Saves the 2-letter province code ("BC") like the existing data.
 */
public final class AddressPicker {

    public static final String DEFAULT_PROVINCE = "BC";

    private static final Map<String, String> PROVINCES = new LinkedHashMap<>(); // code -> name
    private static final Map<String, List<String>> CITIES = new HashMap<>();    // code -> cities

    static {
        PROVINCES.put("BC", "British Columbia");
        PROVINCES.put("AB", "Alberta");
        PROVINCES.put("SK", "Saskatchewan");
        PROVINCES.put("MB", "Manitoba");
        PROVINCES.put("ON", "Ontario");
        PROVINCES.put("QC", "Quebec");
        PROVINCES.put("NB", "New Brunswick");
        PROVINCES.put("NS", "Nova Scotia");
        PROVINCES.put("PE", "Prince Edward Island");
        PROVINCES.put("NL", "Newfoundland and Labrador");
        PROVINCES.put("YT", "Yukon");
        PROVINCES.put("NT", "Northwest Territories");
        PROVINCES.put("NU", "Nunavut");

        CITIES.put("BC", CityPickerBottomSheet.cities());
        CITIES.put("AB", Arrays.asList("Calgary", "Edmonton", "Red Deer", "Lethbridge", "St. Albert", "Airdrie"));
        CITIES.put("SK", Arrays.asList("Saskatoon", "Regina", "Prince Albert", "Moose Jaw"));
        CITIES.put("MB", Arrays.asList("Winnipeg", "Brandon", "Steinbach"));
        CITIES.put("ON", Arrays.asList("Toronto", "Ottawa", "Mississauga", "Brampton", "Hamilton",
                "London", "Markham", "Vaughan", "Kitchener", "Waterloo"));
        CITIES.put("QC", Arrays.asList("Montreal", "Quebec City", "Laval", "Gatineau", "Longueuil", "Sherbrooke"));
        CITIES.put("NB", Arrays.asList("Moncton", "Saint John", "Fredericton"));
        CITIES.put("NS", Arrays.asList("Halifax", "Dartmouth", "Sydney", "Truro"));
        CITIES.put("PE", Arrays.asList("Charlottetown", "Summerside"));
        CITIES.put("NL", Arrays.asList("St. John's", "Mount Pearl", "Corner Brook"));
        CITIES.put("YT", Arrays.asList("Whitehorse", "Dawson City"));
        CITIES.put("NT", Arrays.asList("Yellowknife", "Hay River"));
        CITIES.put("NU", Arrays.asList("Iqaluit", "Rankin Inlet"));
    }

    private final ComponentActivity activity;
    private final EditText etProvince, etCity, etPostalCode, etAddress;
    private final ActivityResultLauncher<String[]> locationPermission;

    /**
     * Call in onCreate. etAddress and btnUseLocation may be null (restaurant has no street field).
     */
    public static AddressPicker attach(ComponentActivity activity, EditText etProvince, EditText etCity,
                                       EditText etPostalCode, EditText etAddress, View btnUseLocation) {
        return new AddressPicker(activity, etProvince, etCity, etPostalCode, etAddress, btnUseLocation);
    }

    private AddressPicker(ComponentActivity activity, EditText etProvince, EditText etCity,
                          EditText etPostalCode, EditText etAddress, View btnUseLocation) {
        this.activity = activity;
        this.etProvince = etProvince;
        this.etCity = etCity;
        this.etPostalCode = etPostalCode;
        this.etAddress = etAddress;

        // Must be registered in onCreate
        locationPermission = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    if (LocationHelper.hasPermission(activity)) {
                        useMyLocation();
                    } else {
                        Toast.makeText(activity, R.string.msgAddressNotFound, Toast.LENGTH_SHORT).show();
                    }
                });

        if (etProvince.getText().toString().trim().isEmpty()) etProvince.setText(DEFAULT_PROVINCE);

        // Province dropdown
        etProvince.setFocusable(false);
        etProvince.setClickable(true);
        etProvince.setOnClickListener(v -> {
            List<String> options = new ArrayList<>();
            for (Map.Entry<String, String> p : PROVINCES.entrySet()) {
                options.add(p.getKey() + " - " + p.getValue());
            }
            CityPickerBottomSheet.showOptions(activity, R.string.titleSelectProvince, options, choice -> {
                String code = choice.substring(0, 2);
                if (!code.equals(provinceCode())) {
                    etProvince.setText(code);
                    // The old city belongs to another province
                    if (!citiesFor(code).contains(etCity.getText().toString().trim())) etCity.setText("");
                }
            });
        });

        // City dropdown: cities of the chosen province
        etCity.setFocusable(false);
        etCity.setClickable(true);
        etCity.setOnClickListener(v -> CityPickerBottomSheet.showOptions(
                activity, R.string.titleSelectCity, citiesFor(provinceCode()), city -> {
                    etCity.setText(city);
                    etCity.setError(null);
                }));

        if (btnUseLocation != null) btnUseLocation.setOnClickListener(v -> useMyLocation());
    }

    /** The province code in the field ("BC" when empty). */
    public String provinceCode() {
        String code = etProvince.getText().toString().trim().toUpperCase(Locale.ROOT);
        return code.isEmpty() ? DEFAULT_PROVINCE : code;
    }

    private static List<String> citiesFor(String code) {
        List<String> cities = CITIES.get(code);
        return cities != null ? cities : CITIES.get(DEFAULT_PROVINCE);
    }

    // ---- "Use my current location" -------------------------------------------------------

    private void useMyLocation() {
        if (!LocationHelper.hasPermission(activity)) {
            locationPermission.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }
        Toast.makeText(activity, R.string.msgFindingAddress, Toast.LENGTH_SHORT).show();
        LocationHelper.getCurrentLocation(activity, location -> {
            if (location == null) {
                Toast.makeText(activity, R.string.msgAddressNotFound, Toast.LENGTH_SHORT).show();
                return;
            }
            double lat = location.getLatitude(), lng = location.getLongitude();
            new Thread(() -> { // address lookup uses the network
                Address address = LocationHelper.addressFrom(activity, lat, lng);
                activity.runOnUiThread(() -> fill(address));
            }).start();
        });
    }

    private void fill(Address a) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        if (a == null) {
            Toast.makeText(activity, R.string.msgAddressNotFound, Toast.LENGTH_SHORT).show();
            return;
        }
        String code = codeFor(a.getAdminArea());
        if (code != null) etProvince.setText(code);

        String city = a.getLocality() != null ? a.getLocality() : a.getSubAdminArea();
        if (city != null) {
            etCity.setText(city);
            etCity.setError(null);
        }
        if (a.getPostalCode() != null) etPostalCode.setText(a.getPostalCode());

        if (etAddress != null && a.getThoroughfare() != null) {
            etAddress.setText(a.getSubThoroughfare() != null
                    ? a.getSubThoroughfare() + " " + a.getThoroughfare()
                    : a.getThoroughfare());
        }
    }

    /** "British Columbia" or "BC" -> "BC"; null if not a Canadian province. */
    private static String codeFor(String adminArea) {
        if (adminArea == null) return null;
        for (Map.Entry<String, String> p : PROVINCES.entrySet()) {
            if (p.getKey().equalsIgnoreCase(adminArea) || p.getValue().equalsIgnoreCase(adminArea)) {
                return p.getKey();
            }
        }
        return null;
    }
}