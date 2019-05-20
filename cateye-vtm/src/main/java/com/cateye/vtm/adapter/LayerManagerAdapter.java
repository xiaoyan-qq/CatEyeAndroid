package com.cateye.vtm.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cateye.android.entity.MapSourceFromNet;
import com.cateye.android.vtm.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by xiaoxiao on 2018/6/22.
 */

public class LayerManagerAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private Map<String, List<MapSourceFromNet.DataBean>> dataBeanMap;
    private LayoutInflater inflater;
    private List<String> keyList;

    String translateJson = "{\"L0\":\"基础栅格图层\",\"L1\":\"项目栅格图层\",\"L2\":\"基础矢量图层\",\"L3\":\"项目矢量图层\"}";
    private JSONObject layerGroupNameJsonObject;//用于翻译图层名称的map

    public LayerManagerAdapter(Context mContext, List<MapSourceFromNet.DataBean> dataBeanList) {
        this.mContext = mContext;
        this.dataBeanMap = new TreeMap<>(new MapKeyComparator());
        this.dataBeanMap = sortListDataAndGroup(dataBeanList);
        this.inflater = LayoutInflater.from(mContext);
        this.keyList = new ArrayList<>();
        if (this.dataBeanMap != null) {
            keyList.addAll(dataBeanMap.keySet());
        }
        try {
            layerGroupNameJsonObject = new JSONObject(translateJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getGroupCount() {
        return dataBeanMap.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return dataBeanMap.get(keyList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return dataBeanMap.get(keyList.get(groupPosition));
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return dataBeanMap.get(keyList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition * 1000 + childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.item_layer_manager_group, null);
        TextView tv_groupName = (TextView) convertView.findViewById(R.id.tv_group_name);
        String groupName = keyList.get(groupPosition);
        if (layerGroupNameJsonObject != null) {
            groupName = layerGroupNameJsonObject.optString(groupName, groupName);
        }
        tv_groupName.setText(groupName);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_layer_manager, null);
            holder = new ViewHolder();
            holder.img_icon = (ImageView) convertView.findViewById(R.id.img_item_layer_manager_icon);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_item_layer_manager_name);
            holder.chk_visibile = (CheckBox) convertView.findViewById(R.id.chk_item_layer_manager_visibile);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (dataBeanMap != null && dataBeanMap.size() > groupPosition && dataBeanMap.get(keyList.get(groupPosition)).size() > childPosition) {
            final MapSourceFromNet.DataBean dataBean = dataBeanMap.get(keyList.get(groupPosition)).get(childPosition);
            holder.tv_name.setText(dataBean.getMemo());
            holder.chk_visibile.setChecked(dataBean.isShow());

            //用户点击勾选框，实时修改图层的显隐状态
            holder.chk_visibile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dataBean.setShow(((CompoundButton) view).isChecked());
                }
            });
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    protected class ViewHolder {
        public ImageView img_icon;
        public TextView tv_name;
        public CheckBox chk_visibile;
    }

    /**
     * 对传递进来的数据做分组和排序
     */
    @SuppressLint("NewApi")
    public Map<String, List<MapSourceFromNet.DataBean>> sortListDataAndGroup(List<MapSourceFromNet.DataBean> dataBeanList) {
        //使用map对现有的网络数据进行分组
        dataBeanMap.clear();
        if (dataBeanList != null && !dataBeanList.isEmpty()) {
            for (MapSourceFromNet.DataBean dataBean : dataBeanList) {
                String group = dataBean.getGroup();
                if (!dataBeanMap.containsKey(group)) {
                    List<MapSourceFromNet.DataBean> groupDataList = new ArrayList<>();
                    dataBeanMap.put(group, groupDataList);
                }
                dataBeanMap.get(group).add(dataBean);
            }
        }
        if (keyList!=null){
            keyList.clear();
            keyList.addAll(dataBeanMap.keySet());
        }
        return dataBeanMap;
    }

    private class MapKeyComparator implements Comparator<String> {

        @Override
        public int compare(String str1, String str2) {

            return str1.compareTo(str2);
        }
    }
}
