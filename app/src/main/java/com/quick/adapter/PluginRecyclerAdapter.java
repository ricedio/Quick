package com.quick.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.quick.R;
import com.quick.search.model.info.PluginSearchResultInfo;
import com.quick.search.model.info.SearchResultInfo;

import java.util.List;

public class PluginRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<SearchResultInfo> pluginList;

    public PluginRecyclerAdapter(Context context, List<SearchResultInfo> list) {
        this.context = context;
        this.pluginList = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PluginHolder(LayoutInflater.from(context).inflate(R.layout.recycler_item_plugin, null));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PluginSearchResultInfo item= (PluginSearchResultInfo) pluginList.get(position);

        if (pluginList.get(position).getIcon() != null)
            ((PluginHolder) holder).icon.setImageDrawable(item.getIcon());
        ((PluginHolder) holder).name.setText(item.getTitle());
        ((PluginHolder) holder).content.setText(item.getContent());
    }

    @Override
    public int getItemCount() {
        return pluginList.size();
    }

    class PluginHolder extends RecyclerView.ViewHolder {

        public AppCompatImageView icon;
        public AppCompatTextView name;
        public AppCompatTextView content;


        public PluginHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            content = itemView.findViewById(R.id.content);
        }
    }
}
