package com.project.foodwastagereductionapp.auth;

import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.project.foodwastagereductionapp.customer.*;
import com.project.foodwastagereductionapp.data.*;
import com.project.foodwastagereductionapp.manager.ManagerDashboardActivity;
import org.junit.*;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SessionIsolationTest {
    private Context context; private SessionManager session;
    @Before public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        session = new SessionManager(context); session.clear();
    }
    @After public void cleanup() { session.clear(); }
    private void put(Class<?> type,String field,Object value) throws Exception {
        Field f=type.getDeclaredField(field); f.setAccessible(true);f.set(null,value);
    }
    private Object get(Class<?> type,String field) throws Exception {
        Field f=type.getDeclaredField(field); f.setAccessible(true);return f.get(null);
    }
    @Test public void switchingAccountsClearsEverySharedCacheAndCart() throws Exception {
        session.start(101,false);
        put(CustomerOrderActivity.class,"cachedOrders",new ArrayList<Order>());
        put(CustomerOrderActivity.class,"cachedCustomerId",101L);
        put(CustomerHomeActivity.class,"cachedHomeUser",new User(101,"first@example.com",User.CUSTOMER,"First","City","BC","V6B1A1",true));
        put(CustomerHomeActivity.class,"cachedHomeRestaurants",new ArrayList<RestaurantListing>());
        put(CustomerBrowseActivity.class,"cachedRestaurants",new ArrayList<RestaurantListing>());
        put(ManagerDashboardActivity.class,"cachedManagerRestaurant",new Restaurant(1,101,"First","City","BC","V6B1A1","6045550100","Food","",""));
        CartManager.add(1,new FoodItem(1,"Food",10,5,2,"8 PM",0,""));
        SessionManager.Snapshot first=session.snapshot();
        session.start(202,false);
        assertFalse(session.isCurrent(first));
        assertNull(get(CustomerOrderActivity.class,"cachedOrders"));
        assertNull(get(CustomerHomeActivity.class,"cachedHomeUser"));
        assertNull(get(CustomerBrowseActivity.class,"cachedRestaurants"));
        assertNull(ManagerDashboardActivity.getCachedRestaurant(101));
        assertEquals(0,CartManager.count());
        SessionManager.Snapshot second=session.snapshot();session.clear();assertFalse(session.isCurrent(second));
        assertEquals(-1,session.getUserId());
    }
    @Test public void logoutAndReloginSameUserStillInvalidatesOldWork() {
        session.start(101,false);SessionManager.Snapshot before=session.snapshot();
        session.clear();session.start(101,false);assertFalse(session.isCurrent(before));
    }
    @Test public void runningTaskCannotPublishAfterAccountSwitch() throws Exception {
        session.start(101,false);
        AtomicBoolean delivered=new AtomicBoolean();CountDownLatch started=new CountDownLatch(1),release=new CountDownLatch(1),finished=new CountDownLatch(1);
        try(ActivityScenario<ForgotPasswordActivity> scenario=ActivityScenario.launch(ForgotPasswordActivity.class)) {
            scenario.onActivity(a->AuthTasks.run(a,null,()->{started.countDown();release.await(5,TimeUnit.SECONDS);finished.countDown();return "old account";},r->delivered.set(true)));
            assertTrue(started.await(5,TimeUnit.SECONDS));session.start(202,false);release.countDown();assertTrue(finished.await(5,TimeUnit.SECONDS));
            CountDownLatch drained=new CountDownLatch(1);
            scenario.onActivity(a->AuthTasks.run(a,null,()->true,result->drained.countDown()));
            assertTrue(drained.await(5,TimeUnit.SECONDS));
            assertFalse(delivered.get());
        } finally {release.countDown();}
    }
}
