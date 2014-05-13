
package com.android.carair.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.MapView;
import com.android.carair.R;
import com.android.carair.fragments.MainFragment;
import com.android.carair.fragments.Item;
import com.android.carair.fragments.ViewHolder;
import com.android.carair.views.PinnedSectionListView.PinnedSectionListAdapter;

public class MainListApapter extends BaseAdapter implements
        PinnedSectionListAdapter {

    protected final Context mContext;
    protected final LayoutInflater mInflater;
    protected int mResource;
    private MainFragment mFragment;
    private ArrayList<Item> items = new ArrayList<Item>();

    // static class Item {
    //
    // public static final int SECTION = 0;
    // public static final int ITEM_IN_CAR = 1;
    // public static final int ITEM_OUT_CAR = 2;
    // public static final int ITEM_SETTING = 3;
    // public static final int ITEM_MAP = 4;
    // public static final int VIEW_TYPE_COUNT = ITEM_MAP + 1;
    //
    // public final int type;
    // public final String text;
    //
    // ViewHolder holder;
    //
    // Map<String, String> map;
    //
    // public int sectionPosition;
    // public int listPosition;
    //
    // public Item(int type, String text) {
    // this.type = type;
    // this.text = text;
    // }
    //
    // public Item(int type, String text , Map<String, String> map) {
    // this.type = type;
    // this.text = text;
    // this.map = map;
    // }
    //
    // public void setViewHolder (ViewHolder holder) {
    // this.holder = holder;
    // }
    //
    // public ViewHolder getViewHolder () {
    // return holder;
    // }
    //
    // public Map<String, String> getMap() {
    // return map;
    // }
    //
    // @Override
    // public String toString() {
    // return text;
    // }
    //
    // }

    // static class ViewHolder {
    // TextView sectionText;
    // }

    static class ViewHolderInCar extends ViewHolder {
        TextView pm25ValueInCar;
        TextView concentrationOfPoisonousGasesValue;
        TextView timeInCar;
        TextView querying;
        TextView battery;
        TextView connStatus;
    }

    static class ViewHolderOutCar extends ViewHolder {
        TextView pm25ValueOutCar;
        TextView concentrationOfPoisonousGasesValue;
        TextView timeOutCar;
    }

    public MainListApapter(Context context, String[] itemTitles, MainFragment fragment) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mFragment = fragment;
        for (int i = 0; i < itemTitles.length; i++) {
            Item section = new Item(Item.SECTION, itemTitles[i]);
            items.add(section);
            if (mContext.getResources().getString(R.string.in_car)
                    .equals(itemTitles[i])) {
                Item item = new Item(Item.ITEM_IN_CAR, itemTitles[i]);
                items.add(item);
            } else if (mContext.getResources().getString(R.string.out_car)
                    .equals(itemTitles[i])) {
                Item item = new Item(Item.ITEM_OUT_CAR, itemTitles[i]);
                items.add(item);
            } else if (mContext.getResources().getString(R.string.setting)
                    .equals(itemTitles[i])) {
                Item item = new Item(Item.ITEM_SETTING, itemTitles[i]);
                items.add(item);
            } else if (mContext.getResources().getString(R.string.map)
                    .equals(itemTitles[i])) {
                Item item = new Item(Item.ITEM_MAP, itemTitles[i]);
                items.add(item);
            }
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return Item.VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    public Item getItemByType(int type) {
        for (Item item : items) {
            if (type == item.type) {
                return item;
            }
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {
                case Item.SECTION:
                    holder = new ViewHolder();
                    convertView = mInflater.inflate(
                            R.layout.carair_main_list_section, null);
                    holder.sectionText = (TextView) convertView
                            .findViewById(R.id.section_text);
                    holder.sectionText.setText(items.get(position).text);
                    items.get(position).setViewHolder(holder);
                    convertView.setTag(holder);
                    break;
                case Item.ITEM_IN_CAR:
                    ViewHolderInCar holderInCar = new ViewHolderInCar();
                    convertView = mInflater.inflate(
                            R.layout.carair_main_list_item_incar, null);
                    holderInCar.pm25ValueInCar = (TextView) convertView
                            .findViewById(R.id.pm2_5_incar_value);
                    holderInCar.concentrationOfPoisonousGasesValue = (TextView) convertView
                            .findViewById(R.id.concentration_poisonous_gases_value);
                    holderInCar.timeInCar = (TextView) convertView.findViewById(R.id.time_incar);
                    holderInCar.querying = (TextView) convertView.findViewById(R.id.querying);
                    holderInCar.battery = (TextView) convertView.findViewById(R.id.battery);
                    holderInCar.connStatus = (TextView) convertView.findViewById(R.id.conn_status);
                    items.get(position).setViewHolder(holder);
                    holder = holderInCar;
                    convertView.setTag(holder);
                    break;
                case Item.ITEM_OUT_CAR:
                    ViewHolderOutCar holderOutCar = new ViewHolderOutCar();
                    convertView = mInflater.inflate(
                            R.layout.carair_main_list_item_outcar, null);

                    holderOutCar.pm25ValueOutCar = (TextView) convertView
                            .findViewById(R.id.pm2_5_outcar_value);
                    holderOutCar.timeOutCar = (TextView) convertView.findViewById(R.id.time_outcar);
                    holder = holderOutCar;
                    items.get(position).setViewHolder(holder);
                    convertView.setTag(holder);
                    break;
                case Item.ITEM_SETTING:
                    convertView = mInflater.inflate(
                            R.layout.carair_main_list_item_setting, null);
                    convertView.setTag(holder);
                    
                    break;
                case Item.ITEM_MAP:
                    holder = new ViewHolder();
                    convertView = mInflater.inflate(
                            R.layout.carair_main_list_item_map, null);
                    holder.map = (MapView) convertView.findViewById(R.id.map);
                    mFragment.setMap(holder.map);
                    convertView.setTag(holder);
//                    convertView.setOnClickListener(new View.OnClickListener() {
//
//                        @Override
//                        public void onClick(View v) {
//                            Toast.makeText(mContext, "hongwen", 1).show();
//                        }
//                    });
                    break;

                default:
                    break;
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (type) {
            case Item.SECTION:
                break;
            case Item.ITEM_IN_CAR:
                if (item == null || item.getMap() == null) {
                    break;
                }
                ViewHolderInCar holderInCar = (ViewHolderInCar) holder;
                if (holderInCar != null) {
                    if (holderInCar.pm25ValueInCar != null) {
                        String value = item.getMap().get("pm25ValueInCar");
                        holderInCar.pm25ValueInCar.setText(value);
                    }
                    if (holderInCar.concentrationOfPoisonousGasesValue != null) {
                        String value = item.getMap().get("concentrationOfPoisonousGasesValue");
                        holderInCar.concentrationOfPoisonousGasesValue.setText(value);
                    }
                    if (holderInCar.timeInCar != null) {
                        String value = item.getMap().get("timeInCar");
                        holderInCar.timeInCar.setText(value);
                    }
                    if (holderInCar.querying != null) {
                        String value = item.getMap().get("querying");
                        holderInCar.querying.setText(value);
                    }
                    if (holderInCar.battery != null) {
                        String value = item.getMap().get("bettery");
                        holderInCar.querying.setText(value);
                    }
                    if (holderInCar.connStatus != null) {
                        String value = item.getMap().get("conn");
                        if ("1".equals(value)) {
                            value = "连接";
                        } else {
                            value = "未连接";
                        }
                        holderInCar.querying.setText(value);
                    }
                }
                break;
            case Item.ITEM_OUT_CAR:
                if (item == null || item.getMap() == null) {
                    break;
                }
                ViewHolderOutCar holderOutCar = (ViewHolderOutCar) holder;
                if (holderOutCar.pm25ValueOutCar != null) {
                    String value = item.getMap().get("opm25");
                    holderOutCar.pm25ValueOutCar.setText(value);
                }
                break;
            case Item.ITEM_SETTING:
                break;
            case Item.ITEM_MAP:
                break;
            default:
                break;
        }
        return convertView;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == Item.SECTION;
    }

    Item item;

    public void refreshItem(Item item) {
        this.item = item;
        // switch (item.type) {
        // case Item.SECTION:
        // break;
        // case Item.ITEM_IN_CAR:
        // ViewHolderInCar holderInCar = (ViewHolderInCar) item.getViewHolder();
        // if (holderInCar != null) {
        // if (holderInCar.pm25ValueInCar != null) {
        // String value = item.getMap().get("pm25ValueInCar");
        // holderInCar.pm25ValueInCar.setText(value);
        // }
        // if (holderInCar.concentrationOfPoisonousGasesValue != null) {
        // String value =
        // item.getMap().get("concentrationOfPoisonousGasesValue");
        // holderInCar.concentrationOfPoisonousGasesValue.setText(value);
        // }
        // if (holderInCar.timeInCar != null) {
        // String value = item.getMap().get("timeInCar");
        // holderInCar.timeInCar.setText(value);
        // }
        // if (holderInCar.querying != null) {
        // String value = item.getMap().get("querying");
        // holderInCar.querying.setText(value);
        // }
        // }
        // break;
        // case Item.ITEM_OUT_CAR:
        // ViewHolderOutCar holderOutCar = (ViewHolderOutCar)
        // item.getViewHolder();
        // if (holderOutCar != null) {
        // if (holderOutCar.pm25ValueOutCar != null) {
        // String value = item.getMap().get("pm25ValueOutCar");
        // holderOutCar.pm25ValueOutCar.setText(value);
        // }
        // if (holderOutCar.timeOutCar != null) {
        // String value = item.getMap().get("pm25ValueOutCar");
        // holderOutCar.timeOutCar.setText(value);
        // }
        // }
        // break;
        // case Item.ITEM_SETTING:
        // break;
        // case Item.ITEM_MAP:
        // break;
        //
        // default:
        // break;
        // }

        notifyDataSetChanged();
    }

}
