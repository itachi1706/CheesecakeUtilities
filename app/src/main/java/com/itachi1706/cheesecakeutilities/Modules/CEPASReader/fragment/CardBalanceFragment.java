/*
 * CardBalanceFragment.java
 *
 * Copyright 2012-2014 Eric Butler <eric@codebutler.com>
 * Copyright 2015 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.itachi1706.cheesecakeutilities.Modules.CEPASReader.fragment;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.SGCardReaderApplication;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.activity.CardInfoActivity;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.card.Card;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.TransitBalance;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.TransitCurrency;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.TransitData;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.ui.ListItem;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.util.TripObfuscator;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.util.Utils;

import org.simpleframework.xml.Serializer;

import java.util.ArrayList;
import java.util.List;

public class CardBalanceFragment extends ListFragment {
    private TransitData mTransitData;
    private static final String TAG = "CardBalanceFragment";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTransitData = getArguments().getParcelable(CardInfoActivity.EXTRA_TRANSIT_DATA);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<Object> combined = new ArrayList<>();
        List<TransitBalance> balances = mTransitData.getBalances();
        if (balances != null)
            combined.addAll(balances);
        setListAdapter(new BalancesAdapter(getActivity(), combined));
    }

    private class BalancesAdapter extends ArrayAdapter<Object> {
        public BalancesAdapter(Context context, List<Object> balances) {
            super(context, 0, balances);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Object item = getItem(position);

            if (item == null) {
                // https://github.com/micolous/metrodroid/issues/28
                Log.w(TAG, "null balance received -- this is an error");
                return getErrorView(convertView, parent, "null");
            }

            if (item instanceof TransitBalance)
                return getBalanceView(convertView, parent, (TransitBalance) item);

            return getErrorView(convertView, parent, item.getClass().getSimpleName());
        }

        private View getErrorView(View convertView, ViewGroup parent, String err) {
            View view = convertView;
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.balance_item, parent, false);
            }

            ((TextView) view.findViewById(R.id.balance)).setText(err);
            return view;
        }

        private View getBalanceView(View convertView,
                                    ViewGroup parent, TransitBalance balance) {
            View view = convertView;
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.balance_item, parent, false);
            }

            TextView validView = view.findViewById(R.id.valid);
            if (balance.getValidFrom() != null && balance.getValidTo() != null) {
                Spanned validFrom = Utils.dateFormat(TripObfuscator.maybeObfuscateTS(balance.getValidFrom()));
                Spanned validTo = Utils.dateFormat(TripObfuscator.maybeObfuscateTS(balance.getValidTo()));
                validView.setText(getString(R.string.valid_format, validFrom, validTo));
                validView.setVisibility(View.VISIBLE);
            } else if (balance.getValidTo() != null) {
                Spanned validTo = Utils.dateFormat(TripObfuscator.maybeObfuscateTS(balance.getValidTo()));
                validView.setText(getString(R.string.valid_to_format, validTo));
                validView.setVisibility(View.VISIBLE);
            } else {
                validView.setVisibility(View.GONE);

                // Get card and see if card expiry is there
                Serializer serializer = SGCardReaderApplication.getInstance().getSerializer();
                Card mCard = Card.fromXml(serializer, getArguments().getString(CardInfoActivity.EXTRA_CARD));
                if (mCard != null) {
                    List<ListItem> cardInfo = mCard.getManufacturingInfo();
                    if (cardInfo != null) {
                        for (ListItem l : cardInfo) {
                            if (l.getText1().toString().equals(getString(R.string.expiry_date))) {
                                validView.setText(getString(R.string.valid_to_format, l.getText2().toString()));
                                validView.setVisibility(View.VISIBLE);
                                break;
                            }
                        }
                    }
                }
            }

            String name = balance.getName();
            TextView nameView = view.findViewById(R.id.name);
            TextView balanceView = view.findViewById(R.id.balance);
            TransitCurrency balanceCur = balance.getBalance();
            if (name != null) {
                nameView.setText(name);
                nameView.setVisibility(View.VISIBLE);
            } else
                nameView.setVisibility(View.GONE);
            if (balanceCur != null) {
                balanceView.setText(balanceCur.maybeObfuscateBalance().formatCurrencyString(true));
                balanceView.setVisibility(View.VISIBLE);
            } else
                balanceView.setVisibility(View.GONE);

            return view;
        }

        @Override
        public boolean isEnabled(int position) {
            Object item = getItem(position);

            if (item == null) {
                return false;
            }

            if (item instanceof TransitBalance) {
                // We don't do anything for balances, yet.
                return false;
            }

            return false;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG, "Clicked " + id + " " + position );
    }
}
