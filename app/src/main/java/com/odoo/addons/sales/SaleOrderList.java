package com.odoo.addons.sales;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.addons.sales.models.SaleOrder;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.addons.fragment.IOnSearchViewChangeListener;
import com.odoo.core.support.addons.fragment.ISyncStatusObserverListener;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.support.list.OCursorListAdapter;
import com.odoo.core.utils.OControls;

import java.util.ArrayList;
import java.util.List;

public class SaleOrderList extends BaseFragment implements OCursorListAdapter.OnViewBindListener,
        ISyncStatusObserverListener, LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, IOnSearchViewChangeListener {

    public static final String KEY = SaleOrderList.class.getSimpleName();

    private View mView;
    private ListView listView;
    private OCursorListAdapter listAdapter = null;
    private boolean syncRequested = false;
    private String mCurFilter = null;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.common_listview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle("Orders");
        mView = view;
        listView = (ListView) mView.findViewById(R.id.listview);
        listAdapter = new OCursorListAdapter(getActivity(), null, R.layout.orderlist_row_item);
        listAdapter.setOnViewBindListener(this);
        listAdapter.setHasSectionIndexers(true, "id");
        listView.setAdapter(listAdapter);
        listView.setFastScrollAlwaysVisible(false);
        setHasSyncStatusObserver(KEY, this, db());
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> items = new ArrayList<>();
        items.add(new ODrawerItem(KEY).setTitle("Sales")
                .setIcon(R.drawable.order_icon)
                .setInstance(new SaleOrderList()));
        return items;

    }

    @Override
    public Class<SaleOrder> database() {
        return SaleOrder.class;
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
        if(!row.getString("id").equals(false)){
            OControls.setText(view, R.id.name, row.getString("name"));
            OControls.setText(view, R.id.partner_name, row.getString("partner_name"));
        }
    }

    @Override
    public void onStatusChange(Boolean refreshing) {
        if(refreshing){
            getLoaderManager().restartLoader(0, null, this);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String where = "";
        List<String> data = new ArrayList<>();

        if (mCurFilter != null) {
            where += " name like ? or partner_name like ? ";
            data.add(mCurFilter + "%");
            data.add(mCurFilter + "%");
        }
        String selection = (data.size() > 0) ? where : null;
        String[] selectionArgs = (data.size() > 0) ? data.toArray(new String[data.size()]) : null;
        return new CursorLoader(getActivity(), db().uri(),null, selection, selectionArgs, "name");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToLast();
        listAdapter.changeCursor(data);
        if (data.getCount() > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setVisible(mView, R.id.swipe_container);
                    OControls.setGone(mView, R.id.data_list_no_item);
                    setHasSwipeRefreshView(mView, R.id.swipe_container, SaleOrderList.this);
                }
            }, 500);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setGone(mView, R.id.swipe_container);
                    OControls.setVisible(mView, R.id.data_list_no_item);
                    setHasSwipeRefreshView(mView, R.id.data_list_no_item, SaleOrderList.this);
                    OControls.setImage(mView, R.id.icon, R.drawable.ic_action_customers);
                    OControls.setText(mView, R.id.title, _s(R.string.label_no_customer_found));
                    OControls.setText(mView, R.id.subTitle, "");
                }
            }, 500);
            if (db().isEmptyTable() && !syncRequested) {
                syncRequested = true;
                onRefresh();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listAdapter.changeCursor(null);
    }

    @Override
    public void onRefresh() {
        if (inNetwork()) {
            parent().sync().requestSync(SaleOrder.AUTHORITY);
            setSwipeRefreshing(true);
        } else {
            hideRefreshingProgress();
            Toast.makeText(getActivity(), _s(R.string.toast_network_required), Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_sale_order, menu);
        setHasSearchView(this, menu, R.id.menu_sale_order_search);
    }

    @Override
    public boolean onSearchViewTextChange(String newFilter) {
        mCurFilter = newFilter;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public void onSearchViewClose() {

    }
}
