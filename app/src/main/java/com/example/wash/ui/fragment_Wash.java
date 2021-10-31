package com.example.wash.ui;

import static android.content.Context.MODE_PRIVATE;
import static com.example.wash.Activity_Setting.address;
import static com.example.wash.Activity_Setting.end_num;
import static com.example.wash.Activity_Setting.getRangeWashers;
import static com.example.wash.Activity_Setting.start_num;
import static com.example.wash.Activity_Setting.url;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wash.Activity_Setting;
import com.example.wash.R;
import com.example.wash.Service.AutoUpdateService;
import com.example.wash.adapter.OnItemClickListener;
import com.example.wash.adapter.myRecyclerAdapter;
import com.example.wash.entity.DataAll;
import com.example.wash.entity.Utility;
import com.example.wash.entity.Wash;
import com.scwang.smartrefresh.header.DropboxHeader;
import com.scwang.smartrefresh.header.FlyRefreshHeader;
import com.scwang.smartrefresh.header.PhoenixHeader;
import com.scwang.smartrefresh.header.WaveSwipeHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_Wash#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_Wash extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public static List<Wash> washerslist =new ArrayList<Wash>();
    public static RecyclerView myrecyclerview;
    public static myRecyclerAdapter myRecyclerAdapter;
    CallBackValue callBackValue;
    private SmartRefreshLayout mySmartRefreshLayout;
    private TextView txt_pulldown;
    private RecyclerView.OnScrollListener loadingListener;
    private Wash wash;
    private int itemposition;
    public fragment_Wash() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_Wash.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_Wash newInstance(String param1, String param2) {
        fragment_Wash fragment = new fragment_Wash();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EventBus.getDefault().register(this);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        try {
                            SharedPreferences pref = getActivity().getSharedPreferences("data", MODE_PRIVATE);
                            getRangeWashers(url, pref.getInt("start_num", 1), pref.getInt("end_num", 100), pref.getString("address", "全部"));
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (myRecyclerAdapter!=null)
                                        myRecyclerAdapter.notifyDataSetChanged();
                                }
                            },1500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        break;
                }
            }
        };
        Intent intent = new Intent(getActivity(), AutoUpdateService.class);
        intent.putExtra("messenger", new Messenger(h));
        getActivity().startService(intent);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callBackValue=(CallBackValue)getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root=inflater.inflate(R.layout.fragment_wash, container, false);
//        initwash_itemData();
        myrecyclerview=root.findViewById(R.id.recyclerview);
        mySmartRefreshLayout =root.findViewById(R.id.item_swipeRefreshLayout);
        txt_pulldown=root.findViewById(R.id.pull_down);
        myrecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        myrecyclerview.setItemAnimator(null);
        myRecyclerAdapter=new myRecyclerAdapter(washerslist,getActivity());
        myrecyclerview.setAdapter(myRecyclerAdapter);
        SharedPreferences pref = getActivity().getSharedPreferences("data",Context.MODE_PRIVATE);
        address = pref.getString("address","全部");
        start_num = pref.getInt("start_num",0);
        end_num = pref.getInt("end_num",0);
        if(washerslist.size()==0&&address.equals("全部"))
        {
            getAllWashers(Activity_Setting.url);
        }else {
            getRangeWashers(Activity_Setting.url,start_num,end_num,address);
        }
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                myRecyclerAdapter.notifyDataSetChanged();
            }
        };
        handler.postDelayed(runnable, 300);

        //添加分割线
        myrecyclerview.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        handleDownPullUpdate();

        myRecyclerAdapter.setmOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                Wash wash= washerslist.get(postion);
                String message=wash.getstatus()+","+wash.getNum();
                callBackValue.SendMessageValue(message);
            }
        });
        loadingListener=new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                /*
                 OnScrollListener.SCROLL_STATE_FLING; //屏幕处于甩动状态
                 OnScrollListener.SCROLL_STATE_IDLE; //停止滑动状态
                 OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;// 手指接触状态
                */
                if (newState==RecyclerView.SCROLL_STATE_IDLE){
                    if(!myrecyclerview.canScrollVertically(1)){
                        //到达底部
//                        Toast.makeText(getActivity(),"你已经触碰到我的底线了",Toast.LENGTH_SHORT).show();
                    }else if(!myrecyclerview.canScrollVertically(-1)){
                        //到达顶部
//                        Toast.makeText(getActivity(),"别划了，已经划不动了",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        myrecyclerview.addOnScrollListener(loadingListener);
        return root;
    }
    /*
        实现下拉刷新
     */
    private void handleDownPullUpdate() {
        mySmartRefreshLayout.setEnabled(true);
        mySmartRefreshLayout.setRefreshHeader(new PhoenixHeader(getContext()));
        mySmartRefreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
        mySmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //结束加载
                        mySmartRefreshLayout.finishRefresh();
                    }
                },1000);
                Toast.makeText(getActivity(), "刷新成功", Toast.LENGTH_SHORT).show();
            }
        });
        mySmartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mySmartRefreshLayout.finishLoadmore();
                    }
                }, 1000);
                Toast.makeText(getActivity(), "加载成功", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public interface CallBackValue{
        public void SendMessageValue(String value);
    }

    @Override
    public void onResume() {
        super.onResume();
        //myRecyclerAdapter.notifyItemChanged(itemposition-1);
        myRecyclerAdapter.notifyDataSetChanged();
       // Toast.makeText(getActivity(),"第"+itemposition+"刷新了",Toast.LENGTH_LONG).show();
    }

/*    @Subscribe
    //接受来自Mainactivity的消息，自动调用
    public void hanldeEvent(String str) {
        //分别为number,time,money
//        String[] sArray=str.split(",");
//        String regEx="[^0-9]";
//        Pattern p=Pattern.compile(regEx);
//        Matcher m = p.matcher(sArray[0]);
//        itemposition=Integer.valueOf(m.replaceAll("").trim()) ;
//        wash= washerslist.get(itemposition-1);
//        wash.setstatus("忙碌");
//        wash.setTime(sArray[1]);
//        wash.setMoney(sArray[2]);
//        washerslist.set(itemposition-1,wash);
//        myRecyclerAdapter.notifyItemChanged(itemposition-1);
    }*/

    //查询历史
    public static void getAllWashers(String url) {
        washerslist.clear();
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://"+url+"/api/washer?pageNum=1&pageSize=1000&search")
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("fail", "onFailure: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                try {
                    DataAll dataAll = Utility.handleWashersResponse(responseText);
                    for (Wash wash : dataAll.washerList) {
                        washerslist.add(wash);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //EventBus.getDefault().unregister(this);
    }

}