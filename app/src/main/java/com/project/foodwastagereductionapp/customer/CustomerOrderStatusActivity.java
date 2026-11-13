package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.CartItem;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.AppNotification;
import com.project.foodwastagereductionapp.data.CustomerProfile;


import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.foodwastagereductionapp.R;

/**
 * Live status of an order that is still in progress:
 * Pending -> Accepted -> Preparing -> Ready, with pickup information.
 * The customer can cancel while the order is still Pending; a cancelled or rejected order
 * shows the "Order cancelled" view instead of the steps.
 */
public class CustomerOrderStatusActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_NUMBER = "orderNumber";

    private OrderStore.PlacedOrder placed;

    private ImageView imgStatusIcon;
    private TextView txtStatusTitle, txtStatusDesc, txtOrderStatus, txtCancelNote;
    private LinearLayout llSteps, llPickup;
    private Button btnCancelOrder;
    private TextView[] stepCircles;

    // Re-draws the screen when the restaurant updates the order while it is open
    private final OrderStore.Listener orderListener = this::render;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        placed = OrderStore.find(getIntent().getStringExtra(EXTRA_ORDER_NUMBER));
        if (placed == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_customer_order_status);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Header
        TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtHeaderTitle.setText(R.string.titleOrderStatus);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        imgStatusIcon = findViewById(R.id.imgStatusIcon);
        txtStatusTitle = findViewById(R.id.txtStatusTitle);
        txtStatusDesc = findViewById(R.id.txtStatusDesc);
        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        txtCancelNote = findViewById(R.id.txtCancelNote);
        llSteps = findViewById(R.id.llSteps);
        llPickup = findViewById(R.id.llPickup);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        buildSteps();
        bindOrderDetails();

        btnCancelOrder.setOnClickListener(v -> confirmCancel());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (placed == null) return;
        OrderStore.addListener(orderListener);
        render();
    }

    @Override
    protected void onStop() {
        super.onStop();
        OrderStore.removeListener(orderListener);
    }

    /** The four numbered steps (Pending, Accepted, Preparing, Ready). */
    private void buildSteps() {
        String[] steps = OrderStatus.steps();
        stepCircles = new TextView[steps.length];
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < steps.length; i++) {
            View step = inflater.inflate(R.layout.item_status_step, llSteps, false);
            stepCircles[i] = step.findViewById(R.id.txtStepNumber);
            stepCircles[i].setText(String.valueOf(i + 1));
            ((TextView) step.findViewById(R.id.txtStepLabel)).setText(steps[i]);
            llSteps.addView(step);
        }
    }

    /** Parts of the screen that do not change with the status. */
    private void bindOrderDetails() {
        Order order = placed.order;

        ((TextView) findViewById(R.id.txtOrderNumber))
                .setText(getString(R.string.fmtOrderNumber, order.orderNumber));

        LinearLayout llItems = findViewById(R.id.llItems);
        for (CartItem c : placed.items) {
            TextView line = new TextView(this);
            line.setTextAppearance(R.style.Text_BodyMd);
            line.setText(c.item.name + " x " + c.quantity);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = getResources().getDimensionPixelSize(R.dimen.space_xs);
            llItems.addView(line, lp);
        }

        // Pickup information
        ((TextView) findViewById(R.id.txtPickupAddress))
                .setText(getString(R.string.fmtPickupAt, placed.restaurantAddress));
        ((TextView) findViewById(R.id.txtMethod)).setText(R.string.txtMethodPickup);
        ((TextView) findViewById(R.id.txtPickupTime)).setText(placed.pickupTime);

        ((TextView) findViewById(R.id.txtTotal)).setText(PriceFormatter.format(order.total));
    }

    /** Draws everything that depends on the current status. */
    private void render() {
        String status = placed.order.status;

        txtOrderStatus.setText(status);
        txtOrderStatus.setTextColor(ContextCompat.getColor(this, OrderStatus.textColor(status)));

        if (OrderStatus.isNegative(status)) {
            renderCancelled(status);
        } else {
            renderInProgress(status);
        }
    }

    private void renderInProgress(String status) {
        int current = OrderStatus.stepIndex(status);

        // Icon, title and description
        int icon = R.drawable.icon_package;
        int title, desc;
        switch (status) {
            case OrderStatus.ACCEPTED:
                title = R.string.titleStatusAccepted;  desc = R.string.msgStatusAccepted;  break;
            case OrderStatus.PREPARING:
                title = R.string.titleStatusPreparing; desc = R.string.msgStatusPreparing; break;
            case OrderStatus.READY:
                icon = R.drawable.icon_check;
                title = R.string.titleStatusReady;     desc = R.string.msgStatusReady;     break;
            case OrderStatus.COMPLETED:
                icon = R.drawable.icon_check;
                title = R.string.titleStatusCompleted; desc = R.string.msgStatusCompleted; break;
            default: // Pending
                title = R.string.titleStatusPending;   desc = R.string.msgStatusPending;   break;
        }
        showIcon(icon, R.color.background_accent1, R.color.background_accent2);
        txtStatusTitle.setText(title);
        if (OrderStatus.READY.equals(status)) {
            txtStatusDesc.setText(getString(desc, placed.order.orderNumber));
        } else {
            txtStatusDesc.setText(desc);
        }

        // Steps: everything up to the current step is filled in green
        llSteps.setVisibility(View.VISIBLE);
        for (int i = 0; i < stepCircles.length; i++) {
            boolean reached = i <= current;
            stepCircles[i].setBackgroundTintList(ContextCompat.getColorStateList(this,
                    reached ? R.color.background_accent1 : R.color.background_secondary));
            stepCircles[i].setTextColor(ContextCompat.getColor(this,
                    reached ? R.color.text_inverse : R.color.text_primary));
        }

        llPickup.setVisibility(View.VISIBLE);

        // Cancel only while Pending, otherwise explain why it is not possible
        boolean canCancel = OrderStatus.canCancel(status);
        btnCancelOrder.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        txtCancelNote.setVisibility(
                canCancel || OrderStatus.COMPLETED.equals(status) ? View.GONE : View.VISIBLE);
    }

    private void renderCancelled(String status) {
        showIcon(R.drawable.icon_warning, R.color.text_warning, R.color.background_warning);
        if (OrderStatus.REJECTED.equals(status)) {
            txtStatusTitle.setText(R.string.titleStatusRejected);
            txtStatusDesc.setText(R.string.msgStatusRejected);
        } else {
            txtStatusTitle.setText(R.string.titleStatusCancelled);
            txtStatusDesc.setText(R.string.msgStatusCancelled);
        }
        llSteps.setVisibility(View.GONE);
        llPickup.setVisibility(View.GONE);
        btnCancelOrder.setVisibility(View.GONE);
        txtCancelNote.setVisibility(View.GONE);
    }

    private void showIcon(int iconRes, int iconColor, int backgroundColor) {
        imgStatusIcon.setImageResource(iconRes);
        imgStatusIcon.setImageTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, iconColor)));
        imgStatusIcon.setBackgroundTintList(
                ContextCompat.getColorStateList(this, backgroundColor));
    }

    private void confirmCancel() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialogCancelOrderTitle)
                .setMessage(R.string.dialogCancelOrderMessage)
                .setPositiveButton(R.string.btnYesCancelOrder, (dialog, which) -> cancelOrder())
                .setNegativeButton(R.string.btnKeepOrder, null)
                .show();
    }

    private void cancelOrder() {
        // The restaurant may have accepted it while the dialog was open
        if (!OrderStatus.canCancel(placed.order.status)) {
            Toast.makeText(this, R.string.msgCannotCancel, Toast.LENGTH_SHORT).show();
            render();
            return;
        }
        // TODO: save to database later
        OrderStore.updateStatus(placed.order.orderNumber, OrderStatus.CANCELLED); // re-draws via the listener
        Toast.makeText(this, R.string.msgOrderCancelled, Toast.LENGTH_SHORT).show();
    }
}
