package com.quick.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.quick.R;
import com.quick.search.model.Contact;
import com.quick.search.model.Sms;
import com.quick.search.model.info.AppSearchResultInfo;
import com.quick.search.model.info.ContactSearchResultInfo;
import com.quick.search.model.info.SearchResultInfo;
import com.quick.search.model.info.SmsSearchResultInfo;
import com.quick.utils.PinyinUtil;


import java.util.List;

public class SearchRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context mContext;
    private List<SearchResultInfo> mDatas;
    private String inputString;

    public SearchRecyclerAdapter(Context context, List<SearchResultInfo> datas, String inputString) {
        mContext = context;
        mDatas = datas;
        this.inputString = inputString;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        if (mDatas.get(viewType) instanceof AppSearchResultInfo)
            holder = new AppHolder(LayoutInflater.from(mContext).inflate(R.layout.recycler_item_app, parent,false));
        if (mDatas.get(viewType) instanceof ContactSearchResultInfo)
            holder = new ContactHolder(LayoutInflater.from(mContext).inflate(R.layout.recycler_item_contact,parent,false));
        if (mDatas.get(viewType) instanceof SmsSearchResultInfo)
            holder = new SmsHolder(LayoutInflater.from(mContext).inflate(R.layout.recycler_item_sms, parent,false));

        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if (holder instanceof AppHolder)
            initApp((AppHolder) holder, position);

        if (holder instanceof ContactHolder)
            initContact((ContactHolder) holder, position);

        if (holder instanceof SmsHolder)
            initSms((SmsHolder) holder, position);


    }


    private void initApp(AppHolder holder, int position) {
        String str = mDatas.get(position).getTitle();
        SpannableStringBuilder style = new SpannableStringBuilder(str);
        int start = str.indexOf(inputString);
        if (start > -1) {
            int end = start + inputString.length();
            style.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            holder.name.setText(style);
        } else {
            holder.name.setText(str);
        }
        Drawable photo = mDatas.get(position).getIcon();
        if (photo != null)
            holder.icon.setImageDrawable(photo);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = ((AppSearchResultInfo) mDatas.get(position)).getClick();
                mContext.startActivity(intent);
            }
        });
    }


    private void initContact(ContactHolder holder, int position) {
        String str = mDatas.get(position).getTitle();
        SpannableStringBuilder style = new SpannableStringBuilder(str);
        int start = str.indexOf(inputString);
        if (start > -1) {
            int end = start + inputString.length();
            style.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            holder.name.setText(style);
        } else {
            holder.name.setText(style);
        }
        String phone = ((ContactSearchResultInfo) mDatas.get(position)).getPhone();
        SpannableStringBuilder stylePhone = new SpannableStringBuilder(phone);
        int startPhone = phone.indexOf(inputString);
        if (startPhone > -1) {
            int endPhone = startPhone + inputString.length();
            stylePhone.setSpan(new ForegroundColorSpan(Color.RED), startPhone, endPhone, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            holder.phone.setText(stylePhone);
        } else {
            holder.phone.setText(stylePhone);
        }

        Drawable photo = mDatas.get(position).getIcon();
        if (photo != null)
            holder.icon.setImageDrawable(photo);

        holder.icon.setImageResource(R.drawable.ic_sharp_account_circle_24);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = ((ContactSearchResultInfo) mDatas.get(position)).getClick();
                mContext.startActivity(intent);
            }
        });
    }

    private void initSms(SmsHolder holder, int position) {
        String str = mDatas.get(position).getTitle();
        SpannableStringBuilder style = new SpannableStringBuilder(str);
        int start = str.indexOf(inputString);
        if (start > -1) {
            int end = start + inputString.length();
            style.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            holder.phone.setText(style);
        } else {
            holder.phone.setText(style);
        }


        String content = ((SmsSearchResultInfo) mDatas.get(position)).getContent();
        SpannableStringBuilder styleContent = new SpannableStringBuilder(content);
        int startContent = content.indexOf(inputString);
        if (startContent > -1) {
            int endContent = startContent + inputString.length();
            styleContent.setSpan(new ForegroundColorSpan(Color.RED), startContent, endContent, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            holder.content.setText(styleContent);
        } else {
            holder.content.setText(styleContent);
        }

        holder.date.setText(((SmsSearchResultInfo) mDatas.get(position)).getDate());

        Drawable photo = mDatas.get(position).getIcon();
        if (photo != null)
            holder.icon.setImageDrawable(photo);

        holder.icon.setImageResource(R.drawable.ic_baseline_sms_24);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = ((SmsSearchResultInfo) mDatas.get(position)).getClick();
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public class AppHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView name;
        public AppCompatImageView icon;

        public AppHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            icon = itemView.findViewById(R.id.icon);

        }
    }

    public class ContactHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView name;
        public AppCompatImageView icon;
        public AppCompatTextView phone;

        public ContactHolder(View itemView) {
            super(itemView);
            phone = itemView.findViewById(R.id.phone);
            name = itemView.findViewById(R.id.name);
            icon = itemView.findViewById(R.id.icon);

        }
    }

    public class SmsHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView icon;
        public AppCompatTextView phone;
        public AppCompatTextView content;
        public AppCompatTextView date;

        public SmsHolder(View itemView) {
            super(itemView);
            phone = itemView.findViewById(R.id.phone);
            icon = itemView.findViewById(R.id.icon);
            content = itemView.findViewById(R.id.content);
            date = itemView.findViewById(R.id.date);
        }
    }

}
