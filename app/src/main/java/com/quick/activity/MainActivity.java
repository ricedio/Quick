package com.quick.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quick.Quick;
import com.quick.R;
import com.quick.adapter.PluginRecyclerAdapter;
import com.quick.adapter.SearchRecyclerAdapter;
import com.quick.search.Locate;
import com.quick.search.model.App;
import com.quick.search.model.Contact;
import com.quick.search.model.Plugin;
import com.quick.search.model.Sms;
import com.quick.search.model.info.PluginSearchResultInfo;
import com.quick.search.model.info.SearchResultInfo;
import com.quick.utils.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private AppCompatEditText searchEdit;
    private RecyclerView recyclerView;
    private RecyclerView recyclerView_plugin;//插件功能列表


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setStatusBarMode(this,false,0xFF6200EE);
        setContentView(R.layout.activity_main);


        initView();
    }

    private void initView() {
        searchEdit = findViewById(R.id.search_edit);
        searchEdit.addTextChangedListener(searchWatcher);

        recyclerView=findViewById(R.id.recycler_list);
        recyclerView_plugin=findViewById(R.id.recycler_list_plugin);
        //线性布局
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(this);
        verticalLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(verticalLayoutManager);

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this);
        horizontalLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView_plugin.setLayoutManager(horizontalLayoutManager);


        /*
        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i != 6)
                    return false;
                String text = textView.getText().toString();
                if (text.equals("插件")) {
                    Toast.makeText(MainActivity.this,text,Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this,"未知："+text,Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        */
    }

    private List<SearchResultInfo> list = new ArrayList<>();
    private List<SearchResultInfo> plugin_list = new ArrayList<>();
    private SearchRecyclerAdapter adapter;
    private PluginRecyclerAdapter adapter_plugin;


    private TextWatcher searchWatcher = new TextWatcher() {
        @Override //文本改变之前
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override //文本发生改变时
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            int visibility=charSequence.toString().equals("")?View.GONE: View.VISIBLE;
            if (visibility == View.GONE)
                searchReset();
            else {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView_plugin.setVisibility(View.VISIBLE);
            }
            if (list.size()>0)
                list.clear();
            if (plugin_list.size()>0)
                plugin_list.clear();
            if (!charSequence.toString().equals("")) {
                list.addAll(Locate.getInstance(Quick.getInstance()).search(String.valueOf(charSequence)));
                if (adapter != null)
                    adapter = null;
                adapter = new SearchRecyclerAdapter(Quick.getInstance(), list,String.valueOf(charSequence));
                recyclerView.setAdapter(adapter);

                //插件列表
                plugin_list.addAll(Plugin.getInstance().search(String.valueOf(charSequence)));
                adapter_plugin=new PluginRecyclerAdapter(Quick.getInstance(),plugin_list);
                recyclerView_plugin.setAdapter(adapter_plugin);

            }

        }

        @Override //文本改变之后
        public void afterTextChanged(Editable editable) {

        }
    };

    private void searchReset() {
        searchEdit.removeTextChangedListener(searchWatcher);
        searchEdit.setText("");
        recyclerView.setVisibility(View.GONE);
        recyclerView_plugin.setVisibility(View.GONE);
        searchEdit.addTextChangedListener(searchWatcher);
    }


} 
