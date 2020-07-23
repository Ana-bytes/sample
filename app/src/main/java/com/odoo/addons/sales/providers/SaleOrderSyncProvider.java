package com.odoo.addons.sales.providers;

import com.odoo.addons.sales.models.SaleOrder;
import com.odoo.core.orm.provider.BaseModelProvider;

public class SaleOrderSyncProvider extends BaseModelProvider {

    public static final String TAG = SaleOrderSyncProvider.class.getSimpleName();

    public String authority(){
        return SaleOrder.AUTHORITY;
    }
}
