package com.odoo.addons.sales.services;

import android.content.Context;
import android.os.Bundle;

import com.odoo.addons.sales.models.SaleOrder;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

public class SaleOrderSyncService extends OSyncService {

    public static final String TAG = SaleOrderSyncService.class.getSimpleName();

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(getApplicationContext(), SaleOrder.class, this, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(1000);
    }
}
