package fuzhiyan.bwei.com.mouthatest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.ex.HttpException;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {
    private MyGridView mUserGv, mOtherGv;
    private List<String> mUserList = new ArrayList<>();
    private List<String> mOtherList = new ArrayList<>();
    private OtherAdapter mUserAdapter, mOtherAdapter;
    private String path= Environment.getExternalStorageDirectory()+ File.separator;
    private ProgressDialog progressdialog;
    private List<MyBean.AppBean> list=new ArrayList<>();
    private boolean flag=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        RequestParams params=new RequestParams(Url.url);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                String s = result.substring(0, result.length() - 1);
                Log.d("EEE", s);
                Gson gson = new Gson();
                MyBean bean = gson.fromJson(s, MyBean.class);
                 list = bean.getApp();
                for (int i = 0; i < list.size(); i++) {
                    if (i < 10) {
                        mUserList.add(list.get(i).getSecCate());
                    } else {
                        mOtherList.add(list.get(i).getSecCate());
                    }

                }
//                mUserAdapter = new OtherAdapter(this, mUserList,true);
//                mOtherAdapter = new OtherAdapter(this, mOtherList,false);
                mUserGv.setAdapter(new OtherAdapter(MainActivity.this, mUserList, true));
                mOtherGv.setAdapter(new OtherAdapter(MainActivity.this, mOtherList, false));
//                mUserGv.setOnItemClickListener(MainActivity.this);
//                mOtherGv.setOnItemClickListener(MainActivity.this);
                mUserGv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("网络选择")
                                .setIcon(R.mipmap.ic_launcher)
                                .setSingleChoiceItems(new String[]{"wifi", "手机流量"}, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case 0:
                                                new AlertDialog.Builder(MainActivity.this)
                                                        .setTitle("版本更新")
                                                        .setMessage("现在检查到新版本，是否更新?")
                                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        })
                                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {



                                                                progressdialog = new ProgressDialog(MainActivity.this);
                                                                RequestParams params = new RequestParams(list.get(position).getUrl());
                                                                Log.e("--->",params.toString());
                                                                params.setSaveFilePath(path + list.get(position).getName() + ".apk");
                                                                x.http().post(params, new ProgressCallback<File>() {
                                                                    @Override
                                                                    public void onSuccess(File result) {

                                                                        Toast.makeText(MainActivity.this, "下载成功", Toast.LENGTH_SHORT).show();

                                                                        Intent intent = new Intent(Intent.ACTION_VIEW);
//                                                                        intent.addCategory("android.intent.category.DEFAULT");
                                                                        intent.setDataAndType(Uri.fromFile(result), "application/vnd.android.package-archive");
                                                                        startActivity(intent);
                                                                    }

                                                                    @Override
                                                                    public void onError(Throwable ex, boolean isOnCallback) {
                                                                        Toast.makeText(MainActivity.this, "下载失败" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        Log.i("rjz", ex.getMessage());
                                                                        if (ex instanceof HttpException) { //网络错误
                                                                            HttpException httpEx = (HttpException) ex;
                                                                            int responseCode = httpEx.getCode();
                                                                            String responseMsg = httpEx.getMessage();
                                                                            String errorResult = httpEx.getResult();
                                                                            //...
                                                                        } else { //其他错误
                                                                            //...
                                                                        }
                                                                    }
                                                                    @Override
                                                                    public void onCancelled(CancelledException cex) {

                                                                    }

                                                                    @Override
                                                                    public void onFinished() {

                                                                    }

                                                                    @Override
                                                                    public void onWaiting() {

                                                                    }

                                                                    @Override
                                                                    public void onStarted() {

                                                                    }

                                                                    @Override
                                                                    public void onLoading(long total, long current, boolean isDownloading) {
                                                                        progressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                                                        progressdialog.setMessage("下载中");
                                                                        progressdialog.show();
                                                                        progressdialog.setMax((int) total);
                                                                        progressdialog.setProgress((int) current);
                                                                    }
                                                                });


                                                        }
                                                        }).show();

                                                break;
                                            case 1:
                                                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                                startActivity(intent);
                                                break;

                                        }
                                    }
                                }).show();
                        return false;
                    }
                });

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }


    public void initView() {
        mUserGv = (MyGridView) findViewById(R.id.userGridView);
        mOtherGv= (MyGridView) findViewById(R.id.otherGridView);
//        mOtherGv = (MyGridView) findViewById(R.id.otherGridView);
//        mUserList.add("推荐");
//        mUserList.add("热点");
//        mUserList.add("上海");
//        mUserList.add("时尚");
//        mUserList.add("科技");
//        mUserList.add("体育");
//        mUserList.add("军事");
//        mUserList.add("财经");
//        mUserList.add("网络");

//        mOtherList.add("汽车");
//        mOtherList.add("房产");
//        mOtherList.add("社会");
//        mOtherList.add("情感");
//        mOtherList.add("女人");
//        mOtherList.add("旅游");
//        mOtherList.add("健康");
//        mOtherList.add("美女");
//        mOtherList.add("游戏");
//        mOtherList.add("数码");
//        mOtherList.add("娱乐");
//        mOtherList.add("探索");
//        mUserAdapter = new OtherAdapter(this, mUserList,true);
//        mOtherAdapter = new OtherAdapter(this, mOtherList,false);
//        mUserGv.setAdapter(mUserAdapter);
//        mOtherGv.setAdapter(mOtherAdapter);
//        mUserGv.setOnItemClickListener(this);
//        mOtherGv.setOnItemClickListener(this);
    }

    /**
     *获取点击的Item的对应View，
     *因为点击的Item已经有了自己归属的父容器MyGridView，所有我们要是有一个ImageView来代替Item移动
     * @param view
     * @return
     */
    private ImageView getView(View view) {
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        Bitmap cache = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(cache);
        return iv;
    }


    /**
     * 获取移动的VIEW，放入对应ViewGroup布局容器
     * @param viewGroup
     * @param view
     * @param initLocation
     * @return
     */
    private View getMoveView(ViewGroup viewGroup, View view, int[] initLocation) {
        int x = initLocation[0];
        int y = initLocation[1];
        viewGroup.addView(view);
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLayoutParams.leftMargin = x;
        mLayoutParams.topMargin = y;
        view.setLayoutParams(mLayoutParams);
        return view;
    }

    /**
     * 创建移动的ITEM对应的ViewGroup布局容器
     * 用于存放我们移动的View
     */
    private ViewGroup getMoveViewGroup() {
        //window中最顶层的view
        ViewGroup moveViewGroup = (ViewGroup) getWindow().getDecorView();
        LinearLayout moveLinearLayout = new LinearLayout(this);
        moveLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        moveViewGroup.addView(moveLinearLayout);
        return moveLinearLayout;
    }
    /**
     * 点击ITEM移动动画
     *
     * @param moveView
     * @param startLocation
     * @param endLocation
     * @param moveChannel
     * @param clickGridView
     */
    private void MoveAnim(View moveView, int[] startLocation, int[] endLocation, final String moveChannel,
                          final GridView clickGridView, final boolean isUser) {
        int[] initLocation = new int[2];
        //获取传递过来的VIEW的坐标
        moveView.getLocationInWindow(initLocation);
        //得到要移动的VIEW,并放入对应的容器中
        final ViewGroup moveViewGroup = getMoveViewGroup();
        final View mMoveView = getMoveView(moveViewGroup, moveView, initLocation);
        //创建移动动画
        TranslateAnimation moveAnimation = new TranslateAnimation(
                startLocation[0], endLocation[0], startLocation[1],
                endLocation[1]);
        moveAnimation.setDuration(300L);//动画时间
        //动画配置
        AnimationSet moveAnimationSet = new AnimationSet(true);
        moveAnimationSet.setFillAfter(false);//动画效果执行完毕后，View对象不保留在终止的位置
        moveAnimationSet.addAnimation(moveAnimation);
        mMoveView.startAnimation(moveAnimationSet);
        moveAnimationSet.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                moveViewGroup.removeView(mMoveView);
                // 判断点击的是DragGrid还是OtherGridView
                if (isUser) {
                    mOtherAdapter.setVisible(true);
                    mOtherAdapter.notifyDataSetChanged();
                    mUserAdapter.remove();
                } else {
                    mUserAdapter.setVisible(true);
                    mUserAdapter.notifyDataSetChanged();
                    mOtherAdapter.remove();
                }
                flag=!flag;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        switch (parent.getId()) {
            case R.id.userGridView:
                //position为 0，1 的不可以进行任何操作
                if (position != 0 && position != 1) {
                    final ImageView moveImageView = getView(view);
                    if (moveImageView != null) {
                        flag=!flag;
                        TextView newTextView = (TextView) view.findViewById(R.id.text_item);
                        final int[] startLocation = new int[2];
                        newTextView.getLocationInWindow(startLocation);
                        final String channel = ((OtherAdapter) parent.getAdapter()).getItem(position);//获取点击的频道内容
                        mOtherAdapter.setVisible(false);
                        //添加到最后一个
                        mOtherAdapter.addItem(channel);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                try {
                                    int[] endLocation = new int[2];
                                    //获取终点的坐标
                                    mOtherGv.getChildAt(mOtherGv.getLastVisiblePosition()).getLocationInWindow(endLocation);
                                    MoveAnim(moveImageView, startLocation, endLocation, channel, mUserGv, true);
                                    mUserAdapter.setRemove(position);
                                } catch (Exception localException) {
                                }
                            }
                        }, 50L);
                    }
                }
                break;
            case R.id.otherGridView:
                final ImageView moveImageView = getView(view);
                if (moveImageView != null) {
                    flag=!flag;
                    TextView newTextView = (TextView) view.findViewById(R.id.text_item);
                    final int[] startLocation = new int[2];
                    newTextView.getLocationInWindow(startLocation);
                    final String channel = ((OtherAdapter) parent.getAdapter()).getItem(position);
                    mUserAdapter.setVisible(false);
                    //添加到最后一个
                    mUserAdapter.addItem(channel);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            try {
                                int[] endLocation = new int[2];
                                //获取终点的坐标
                                mUserGv.getChildAt(mUserGv.getLastVisiblePosition()).getLocationInWindow(endLocation);
                                MoveAnim(moveImageView, startLocation, endLocation, channel, mOtherGv,false);
                                mOtherAdapter.setRemove(position);
                            } catch (Exception localException) {
                            }
                        }
                    }, 50L);// 50 毫秒
                }
                break;
            default:
                break;
        }
    }
}
