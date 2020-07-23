package com.odoo.addons.sales.models;

import android.content.Context;
import android.net.Uri;

import com.odoo.base.addons.res.ResPartner;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

import org.json.JSONArray;

public class SaleOrder extends OModel {

    public static final String TAG = SaleOrder.class.getSimpleName();
    public static final String AUTHORITY = "com.odoo.addons.sales.models.sale_order";

    OColumn name = new OColumn("Name", OVarchar.class);
    OColumn partner_id = new OColumn("Customer", ResPartner.class, OColumn.RelationType.ManyToOne);
    @Odoo.Functional(method = "partnerName", store = true , depends = {"partner_id"})
    OColumn partner_name = new OColumn("Partner Name", OVarchar.class).setLocalColumn();


    public String partnerName(OValues values) {
        try {
            if (!values.getString("partner_id").equals("false")) {
                JSONArray partner_id = new JSONArray(values.getString("partner_id"));
                return partner_id.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "false";
    }


    public SaleOrder(Context context, OUser user) {
        super(context, "sale.order", user);
    }

    @Override
    public Uri uri(){
        return buildURI(AUTHORITY);
    }
}
