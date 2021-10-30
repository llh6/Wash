package com.example.wash.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.wash.Activity_Setting;
import com.example.wash.MainActivity;
import com.example.wash.R;
import com.example.wash.entity.Utility;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.MessageDigest;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_Setting#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class fragment_Setting extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EditText et_username, et_password;
    private Button btn_login, btn_exit;
    private MainActivity mymainActivity;
    private String result = "";
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_Setting.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_Setting newInstance(String param1, String param2) {
        fragment_Setting fragment = new fragment_Setting();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public fragment_Setting() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        mymainActivity = (MainActivity) getActivity();
        et_password = view.findViewById(R.id.et_password);
        et_username = view.findViewById(R.id.et_username);
        btn_exit = view.findViewById(R.id.bt_exit);
        btn_login = view.findViewById(R.id.bt_log);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = et_username.getText().toString();
                String password = getSha1(et_password.getText().toString());
                //mymainActivity.initUsbSerial();
                Intent intent = new Intent(getActivity(), Activity_Setting.class);
                startActivity(intent);




                check_login(username, password);
                if (username.equals(""))
                {
                    Toast.makeText(getActivity(), "请输入账号和密码", Toast.LENGTH_SHORT).show();
                }else {
                    ProgressDialog pd2 = new ProgressDialog(getActivity());
                    pd2.setTitle("提示");
                    pd2.setMessage("登录验证中...");
                    pd2.setCancelable(true);
                    pd2.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pd2.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (result.equals("成功")) {
                            Toast.makeText(getActivity(), "用户名和密码正确", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), Activity_Setting.class);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                            getActivity().finish();
                            et_username.setText("");
                            et_password.setText("");
                            } else if (result.equals("账号或密码错误")) {
                                AlertDialog builder = new AlertDialog.Builder(getActivity())
                                        .setTitle("警告")
                                        .setMessage("账号或密码错误！")
                                        .setNegativeButton("取消", null)
                                        .show();
                                try {
                                    Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
                                    mAlert.setAccessible(true);
                                    Object mAlertController = mAlert.get(builder);

                                    Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
                                    mTitle.setAccessible(true);
                                    TextView mTitleView = (TextView) mTitle.get(mAlertController);
                                    mTitleView.setTextColor(Color.RED);

                                    Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
                                    mMessage.setAccessible(true);
                                    TextView mMessageView = (TextView) mMessage.get(mAlertController);
                                    mMessageView.setTextColor(Color.BLACK);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (NoSuchFieldException e) {
                                    e.printStackTrace();
                                }
                                et_username.setText("");
                                et_password.setText("");
                            }
                            pd2.cancel();
                        }
                    },1000);
                }

            }
        });
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_username.setText("");
                et_password.setText("");
            }
        });
        return view;
    }

    //sha1加密
    public static String getSha1(String str) {

        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));
            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            return null;
        }
    }

    public void check_login(String user, String password) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.1.20.146/api/user?pageNum=1&pageSize=10&search=&uid=" + user + "&pw=" + password)
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
                result = Utility.handleLogin(responseText);
            }
        });
    }
}