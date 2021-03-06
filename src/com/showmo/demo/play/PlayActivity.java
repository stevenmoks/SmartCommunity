package com.showmo.demo.play;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.showmo.demo.util.spUtil;
import com.showmo.demo.view.DialogPopupCurtain;
import com.showmo.demo.view.DialogPopupSwitch;
import com.way.adapter.CurtianAdapter;
import com.way.adapter.DatabaseAdapter;
import com.way.adapter.SmartSwitchListAdapter;
import com.way.tabui.commonmodule.GosBaseActivity;
import com.way.tabui.gokit.R;
import com.way.util.ControlProtocol;
import com.way.util.ConvertUtil;
import com.way.util.CurtainControlUtils;
import com.way.util.Gizinfo;
import com.way.util.SwitchControlUtils;
import com.way.util.SwitchInfo;
import com.xmcamera.core.model.XmErrInfo;
import com.xmcamera.core.model.XmStreamMode;
import com.xmcamera.core.sys.XmSystem;
import com.xmcamera.core.sysInterface.IXmRealplayCameraCtrl;
import com.xmcamera.core.sysInterface.IXmSystem;
import com.xmcamera.core.sysInterface.IXmTalkManager;
import com.xmcamera.core.sysInterface.IXmTalkManager.TalkState;
import com.xmcamera.core.sysInterface.OnXmBeginTalkListener;
import com.xmcamera.core.sysInterface.OnXmEndTalkListener;
import com.xmcamera.core.sysInterface.OnXmListener;
import com.xmcamera.core.sysInterface.OnXmSimpleListener;
import com.xmcamera.core.sysInterface.OnXmStartResultListener;
import com.xmcamera.core.sysInterface.OnXmTalkVolumListener;
import com.xmcamera.core.view.decoderView.XmGlView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.way.tabui.gokit.R.id.glview;


/**
 * Created by Administrator on 2016/6/20.
 */
public class PlayActivity extends GosBaseActivity implements View.OnClickListener {

          private DatabaseAdapter dbAdapter;
          private SwitchInfo switchInfo;
          //查询方式
          private static final int UPDATA_STATUS = 1;
          private static final int UPDATA_INFO = 0;
          //UI更新广播
          public static final String action = "com.device.status.update.action";
          //发送状态更新广播
          private static final String KUOZHAN = "kuozhan";
          private byte[] SEND_BROAD = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x15};


          IXmSystem xmSystem;
          IXmRealplayCameraCtrl realplayCameraCtrl;
          int cameraid;

          FrameLayout playContent;
          XmGlView glView;

          Button play, stop, HD, SD, AT, capture, Record, StopRecord, btn_rebinder,
               SLrecord, SLrecordclose;
          private Button btn_open, btn_colse, btn_stop, btn_redic;
          // TextView show;
          String logtext = "";

          spUtil sp;
          private GizWifiDevice device;
          /**
           * The device statu.
           */
          private HashMap<String, Object> deviceStatu;

          /**
           * 传输字符
           */
          private static final String KEY_Sendcom = "Send_com";


          protected static final int OPEN = 1;
          protected static final int CLOSE = 0;
          /**
           * 控制指令关键字
           */
          private static final String KEY_Sendair = "Send_aircon";

          /**
           * 指令代码0:开 1：停止  2：关 3：换向
           */
          private int[] con_mes = {14748160, 14748416, 14748672, 14749952};

          int playId;
          IXmTalkManager talkma;
          private ListView list_oc, list_curtain;
          private MyReceiver receiver = null;
          private SmartSwitchListAdapter adapter;
          private LinearLayout ll_curtain;
          private LinearLayout ll_oc_list;
          private LinearLayout ll_show;
          private boolean isshow = false;
          private ScrollView scrollView;
          private TextView tv_show;
          private TextView tx_oc;
          private TextView tx_curtain;
          private CurtianAdapter curtain_adapter;
          private LinearLayout ll_curtain_list;
          private CurtainControlUtils curtainControlUtils;
          private SwitchControlUtils switchControlUtils;


          @Override
          protected void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                    setContentView(R.layout.activity_play_showmo);
                    init();
                    initReceiver();
                    sendbroadcast();
                    initEvent();


          }

          private void init() {
                    //操作数据库
                    dbAdapter = new DatabaseAdapter(this);
                    //数据
                    switchInfo = new SwitchInfo();


                    cameraid = getIntent().getExtras().getInt("cameraId");
                    xmSystem = XmSystem.getInstance();
                    realplayCameraCtrl = xmSystem.xmGetRealplayController();

                    talkma = xmSystem.xmGetTalkManager(cameraid);

                    glView = new XmGlView(this, null);
                    playContent = (FrameLayout) findViewById(glview);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                         LinearLayout.LayoutParams.WRAP_CONTENT,
                         LinearLayout.LayoutParams.WRAP_CONTENT);
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    params.width = dm.widthPixels - 10;
                    params.height = dm.widthPixels - 10;
                    playContent.setLayoutParams(params);
                    playContent.addView((View) glView,
                         FrameLayout.LayoutParams.MATCH_PARENT,
                         FrameLayout.LayoutParams.MATCH_PARENT);

                    scrollView = (ScrollView) findViewById(R.id.scrollview);

                    play = (Button) findViewById(R.id.play);
                    stop = (Button) findViewById(R.id.stop);
                    HD = (Button) findViewById(R.id.HD);
                    SD = (Button) findViewById(R.id.SD);
                    AT = (Button) findViewById(R.id.AT);
                    capture = (Button) findViewById(R.id.capture);
                    Record = (Button) findViewById(R.id.Record);
                    StopRecord = (Button) findViewById(R.id.StopRecord);
                    btn_rebinder = (Button) findViewById(R.id.btn_rebinder);
                    SLrecord = (Button) findViewById(R.id.SLrecord);
                    SLrecordclose = (Button) findViewById(R.id.SLrecordclose);
                    ll_show = (LinearLayout) findViewById(R.id.ll_show);
                    tv_show = (TextView) findViewById(R.id.tv_show);

                    tx_oc = (TextView) findViewById(R.id.tx_oc);
                    tx_curtain = (TextView) findViewById(R.id.tx_curtain);
                    tx_curtain.setVisibility(View.GONE);
                    tx_oc.setVisibility(View.GONE);

                    ll_curtain = (LinearLayout) findViewById(R.id.ll_curtain);
                    ll_curtain.setVisibility(View.GONE);
                    ll_oc_list = (LinearLayout) findViewById(R.id.ll_oc_list);
                    ll_oc_list.setVisibility(View.GONE);
                    ll_curtain_list = (LinearLayout) findViewById(R.id.ll_curtain_list);
                    ll_curtain_list.setVisibility(View.GONE);

                    list_oc = (ListView) findViewById(R.id.list_oc);
                    list_curtain = (ListView) findViewById(R.id.list_curtain);

                    curtain_adapter = new CurtianAdapter(mHander, this);
                    adapter = new SmartSwitchListAdapter(mHander, this);

                    list_oc.setAdapter(adapter);
                    list_curtain.setAdapter(curtain_adapter);

                    btn_open = (Button) findViewById(R.id.btn_open);
                    btn_colse = (Button) findViewById(R.id.btn_colse);
                    btn_stop = (Button) findViewById(R.id.btn_stop);
                    btn_redic = (Button) findViewById(R.id.btn_redic);
                    ll_show.setVisibility(View.GONE);
                    ll_show.setOnClickListener(this);
                    play.setOnClickListener(this);
                    stop.setOnClickListener(this);
                    HD.setOnClickListener(this);
                    SD.setOnClickListener(this);
                    AT.setOnClickListener(this);
                    capture.setOnClickListener(this);
                    Record.setOnClickListener(this);
                    StopRecord.setOnClickListener(this);
                    btn_rebinder.setOnClickListener(this);
                    SLrecord.setOnClickListener(this);
                    SLrecordclose.setOnClickListener(this);
                    //show = (TextView) findViewById(R.id.show);

                    //        mHander.sendEmptyMessage(0x126);
                    sp = new spUtil(this);
          }

          private void initReceiver() {
                    receiver = new MyReceiver();
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("com.way.tabui.actity.GosDeviceListActivityReceviver");
                    filter.addAction("com.way.tabui.actity.GizServiceTOAST");
                    filter.addAction("com.device.control.curtain.action");
                    filter.addAction("com.device.control.switch.action");
                    registerReceiver(receiver, filter);
          }

          private void initEvent() {
                    //窗帘Item点击事件 FIXME
                    list_curtain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                              @Override
                              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        curtainControlUtils = new CurtainControlUtils();
                                        curtainControlUtils.setCurtainAddress(curtain_adapter.getmList().get(position).getAddress());
                                        DialogPopupCurtain popup = new DialogPopupCurtain(PlayActivity.this);
                                        //显示
                                        popup.showPopupWindow(glview);

//                                        String name, address;
//                                        int type;
//                                        //address地址代码
//                                        name = adapter.getmList().get(position).getName();
//                                        //address地址代码
//                                        address = adapter.getmList().get(position).getAddress();
//                                        type = adapter.getmList().get(position).getType();
//                                        Intent intent = new Intent(PlayActivity.this, SmartCurtainActivity.class);
//                                        intent.putExtra("name", name);
//                                        intent.putExtra("address", address);
//                                        Bundle bundle = new Bundle();
//                                        //传设备
//                                        bundle.putParcelable("GizWifiDevice", device);
//                                        intent.putExtras(bundle);
//                                        startActivityForResult(intent, 1000);
                              }
                    });

                    //开关 Item点击事件
                    list_oc.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                              @Override
                              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        switchControlUtils = new SwitchControlUtils();
                                        //设置地址
                                        switchControlUtils.setSwitchAddress(adapter.getmList().get(position).getAddress());
                                        //设置开关类型
                                        switchControlUtils.setSwitchType(adapter.getmList().get(position).getType());
                                        DialogPopupSwitch popup = new DialogPopupSwitch(PlayActivity.this,adapter.getmList().get(position).getType(),adapter.getmList().get(position).getAddress());
                                        switchInfo = dbAdapter.findSwitchInfoStatus(adapter.getmList().get(position).getAddress());
                                        //设置开关状态
                                        popup.setDeviceStutas(switchInfo.getStatus1(),switchInfo.getStatus2(),switchInfo.getStatus3());
                                        //初始化事件
                                        popup.init();
                                        //显示在glview下方
                                        popup.showPopupWindow(glview);
//                                        String name, address;
//                                        int type;
//                                        //address地址代码
//                                        name = adapter.getmList().get(position).getName();
//                                        //address地址代码
//                                        address = adapter.getmList().get(position).getAddress();
//                                        type = adapter.getmList().get(position).getType();
//                                        Intent intent = new Intent(PlayActivity.this, SmartSwitchActivity.class);
//                                        intent.putExtra("name", name);
//                                        intent.putExtra("address", address);
//                                        intent.putExtra("type", type);
//                                        Bundle bundle = new Bundle();
//                                        //传设备
//                                        bundle.putParcelable("GizWifiDevice", device);
//                                        intent.putExtras(bundle);
//                                        startActivityForResult(intent, 1000);
                              }
                    });


                    btn_open.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {

                                        try {
                                                  sendJson(KEY_Sendair, con_mes[0]);
                                                  //  btn_open.setTextColor(getResources().getColor(R.color.golden));
                                        } catch (JSONException e) {
                                                  Toast.makeText(getApplicationContext(), "发送失败",
                                                       Toast.LENGTH_SHORT).show();
                                        }
                              }
                    });

                    btn_stop.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {

                                        try {
                                                  sendJson(KEY_Sendair, con_mes[1]);

                                                  //         btn_stop.setTextColor(getResources().getColor(R.color.golden));
                                        } catch (JSONException e) {
                                                  Toast.makeText(getApplicationContext(), "发送失败",
                                                       Toast.LENGTH_SHORT).show();
                                        }

                              }
                    });

                    btn_colse.setOnClickListener(new View.OnClickListener() {

                              @Override
                              public void onClick(View v) {
                                        try {
                                                  sendJson(KEY_Sendair, con_mes[2]);
                                                  //      btn_colse.setTextColor(getResources().getColor(R.color.golden));
                                        } catch (JSONException e) {
                                                  Toast.makeText(getApplicationContext(), "发送失败",
                                                       Toast.LENGTH_SHORT).show();
                                        }

                              }
                    });

                    btn_redic.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {
                                        try {
                                                  sendJson(KEY_Sendair, con_mes[3]);
                                                  //   btn_redic.setTextColor(getResources().getColor(R.color.golden));
                                        } catch (JSONException e) {
                                                  Toast.makeText(getApplicationContext(), "发送失败",
                                                       Toast.LENGTH_SHORT).show();
                                        }

                              }
                    });


          }


          public void fixListViewHeight(ListView listView) {
                    // 如果没有设置数据适配器，则ListView没有子项，返回。
                    ListAdapter listAdapter = listView.getAdapter();
                    int totalHeight = 0;
                    if (listAdapter == null) {
                              return;
                    }
                    for (int index = 0, len = listAdapter.getCount(); index < len; index++) {
                              View listViewItem = listAdapter.getView(index, null, listView);
                              // 计算子项View 的宽高
                              listViewItem.measure(0, 0);
                              // 计算所有子项的高度和
                              totalHeight += listViewItem.getMeasuredHeight();
                    }

                    ViewGroup.LayoutParams params = listView.getLayoutParams();
                    // listView.getDividerHeight()获取子项间分隔符的高度
                    // params.height设置ListView完全显示需要的高度
                    params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
                    listView.setLayoutParams(params);
          }

          private void sendJson(String key, Object value) throws JSONException {
                    ConcurrentHashMap<String, Object> hashMap = new ConcurrentHashMap<String, Object>();
                    hashMap.put(key, value);
                    device.write(hashMap, 0);
                    Log.i("==", hashMap.toString());
          }

          private void Play() {
                    if (realplayCameraCtrl.isPlaying()) {
                              mHander.sendEmptyMessage(0x128);
                              return;
                    }
                    realplayCameraCtrl.xmStart(glView, cameraid,
                         new OnXmStartResultListener() {
                                   @Override
                                   public void onStartSuc(boolean isLocalNet, int cameraId,
                                                          int var3) {
                                             playId = var3;
                                             showTV("播放成功！");
                                   }

                                   @Override
                                   public void onStartErr(XmErrInfo errcode) {
//                                 showTV("errId:" + errcode.errId + ",errCode:"
//                                + errcode.errCode + ",errdiscribe:"
//                                + errcode.discribe);
                                             showTV("播放失败");
                                   }
                         });
          }

          private void slrecordclose() {
                    talkma.xmEndTalk(new OnXmEndTalkListener() {

                              @Override
                              public void onTalkClosing() {

                              }

                              @Override
                              public void onSuc() {
                                        mHander.sendEmptyMessage(0x132);
                                        // Toast.makeText(getApplicationContext(), "关闭对讲成功",
                                        // Toast.LENGTH_SHORT).show();
                              }

                              @Override
                              public void onCloseTalkErr(XmErrInfo arg0) {
                                        mHander.sendEmptyMessage(0x130);
                              }

                              @Override
                              public void onAlreadyClosed() {
                                        // TODO Auto-generated method stub

                              }
                    });
          }

          private void slrecord() {
                    talkma.xmBeginTalk(new OnXmBeginTalkListener() {

                              @Override
                              public void onSuc() {
                                        mHander.sendEmptyMessage(0x131);
                              }

                              @Override
                              public void onOpenTalkErr(XmErrInfo arg0) {
                                        mHander.sendEmptyMessage(0x129);
                              }

                              @Override
                              public void onNoRecordPermission() {

                              }

                              @Override
                              public void onIPCIsTalking() {

                              }

                              @Override
                              public void onAlreadyTalking() {
                              }
                    }, new OnXmTalkVolumListener() {

                              @Override
                              public void onVolumeChange(int arg0) {
                              }
                    });

          }

          private void Stop() {
                    if (realplayCameraCtrl.isPlaying()) {
                              realplayCameraCtrl.xmStop(playId);
                              showTV("停止播放！");
                    }
          }

          private void SwitchStream(final XmStreamMode mode) {
                    realplayCameraCtrl.xmSwitchStream(mode, new OnXmSimpleListener() {
                              @Override
                              public void onErr(XmErrInfo info) {
                                        showTV("切换失败！" + info.discribe);
                              }

                              @Override
                              public void onSuc() {
                                        if (mode == XmStreamMode.ModeHd) {
                                                  showTV("切换到HD");
                                        } else if (mode == XmStreamMode.ModeFluency) {
                                                  showTV("切换到SD");
                                        } else if (mode == XmStreamMode.ModeAdapter) {
                                                  showTV("切换到AT");
                                        }
                              }
                    });
          }

          private void Capture() {
                    final long time = System.currentTimeMillis();
                    realplayCameraCtrl.xmCapture("/sdcard/zzj/", "p" + time + ".jpg",
                         new OnXmListener<String>() {
                                   @Override
                                   public void onErr(XmErrInfo info) {
                                             showTV("截图失败");
                                   }

                                   @Override
                                   public void onSuc(String info) {
                                             showTV("截图成功1:" + "/sdcard/zzj/" + "p" + time + ".jpg");
                                             realplayCameraCtrl.xmThumbnail("/sdcard/zzj", "thumb"
                                                       + time + ".jpg", "p" + time + ".jpg",
                                                  new OnXmListener<String>() {
                                                            @Override
                                                            public void onErr(XmErrInfo info) {
                                                                      showTV("截图失败2");
                                                            }

                                                            @Override
                                                            public void onSuc(String info) {
                                                                      showTV("截图成功2:" + "/sdcard/zzj/"
                                                                           + "thumb" + time + ".jpg");
                                                            }
                                                  });
                                   }
                         });
          }

          boolean isRecord = false;

          private void Record() {
                    isRecord = true;
                    long time = System.currentTimeMillis();
                    boolean suc = realplayCameraCtrl.xmRecord("/sdcard/zzj", "v" + time
                         + ".mp4");
                    Toast.makeText(this, suc ? "开始录像" : "录像失败", Toast.LENGTH_LONG).show();
                    showTV(suc ? "开始录像" : "录像失败");
          }

          private void StopRecord() {
                    if (!isRecord) {
                              return;
                    }
                    isRecord = false;
                    String ss = realplayCameraCtrl.xmStopRecord();
                    Toast.makeText(this, ss == null ? "录像失败" : "录像成功：" + ss,
                         Toast.LENGTH_LONG).show();
                    showTV(ss == null ? "录像失败" : "录像成功：" + ss);
          }

          private void btn_rebinder() {
                    new AlertDialog.Builder(this).setTitle("温馨提示")
                         .setMessage("您确定要删除此摄像机吗？")
                         .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which) {
                                             DeleteDevice();
                                   }
                         }).setNegativeButton("取消", null).show();
          }

          private void DeleteDevice() {
                    xmSystem.xmDeleteDevice(cameraid, xmSystem.xmFindDevice(cameraid)
                         .getmUuid(), new OnXmSimpleListener() {
                              @Override
                              public void onErr(XmErrInfo info) {
                                        mHander.sendEmptyMessage(0x124);
                                        showTV("删除失败！" + info.discribe);
                              }

                              @Override
                              public void onSuc() {
                                        mHander.sendEmptyMessage(0x123);
                              }
                    });
          }

          Handler mHander = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                              int position;
                              ArrayList<Gizinfo> gizinfos;
                              switch (msg.what) {
                                        case 0x123:
                                                  Toast.makeText(PlayActivity.this, "删除成功！", Toast.LENGTH_LONG)
                                                       .show();
                                                  setResult(101);
                                                  finish();
                                                  break;
                                        case 0x124:
                                                  Toast.makeText(PlayActivity.this, "删除失败！", Toast.LENGTH_LONG)
                                                       .show();
                                                  break;
                                        case 0x125:
                                                  Toast.makeText(PlayActivity.this, (String) msg.obj,
                                                       Toast.LENGTH_LONG).show();
                                                  break;
                                        case 0x126:
                                                  Toast.makeText(PlayActivity.this, logtext, Toast.LENGTH_LONG)
                                                       .show();
                                        //                    show.setText(logtext);
                                                  break;
                                        case 0x127:
                                                  Toast.makeText(PlayActivity.this, "你没有权限，请先注册登录~",
                                                       Toast.LENGTH_LONG).show();
                                                  break;
                                        case 0x128:
                                                  Toast.makeText(PlayActivity.this, "视频已经在播放中！",
                                                       Toast.LENGTH_LONG).show();
                                                  break;
                                        case 0x129:
                                                  Toast.makeText(PlayActivity.this, "开启错误", Toast.LENGTH_LONG)
                                                       .show();
                                                  break;
                                        case 0x130:
                                                  Toast.makeText(PlayActivity.this, "关闭错误", Toast.LENGTH_LONG)
                                                       .show();
                                                  break;
                                        case 0x131:
                                                  Toast.makeText(PlayActivity.this, "已开启对讲", Toast.LENGTH_SHORT)
                                                       .show();
                                                  break;
                                        case 0x132:
                                                  Toast.makeText(PlayActivity.this, "已关闭对讲", Toast.LENGTH_SHORT)
                                                       .show();
                                                  break;
                              }
                    }

          };

          private void sendbroadcast() {
                    Intent intent = new Intent();
                    intent.setAction("com.way.tabui.actity.GosDeviceListActivity");
                    sendBroadcast(intent);
          }

          /**
           * description:广播接收，响应
           * auther：joahluo
           * time：2017/7/22 11:28
           */
          public class MyReceiver extends BroadcastReceiver {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                              String action = intent.getAction();
                              Message msg = new Message();
                              if (action.equals("com.way.tabui.actity.GosDeviceListActivityReceviver")) {
                                        device = (GizWifiDevice) intent.getParcelableExtra("GizWifiDevice");
                                        adapter.setDate(device.getMacAddress());
                                        curtain_adapter.setDate(device.getMacAddress());
                                        fixListViewHeight(list_oc);
                                        fixListViewHeight(list_curtain);
                                        ll_show.setVisibility(View.VISIBLE);
                                        //开启广播获取状态
                                        initStatusListener();
                              } else if (action.equals("com.device.control.curtain.action")) {
                                        int control = intent.getIntExtra("control", 0);
                                        //窗帘控制响应
                                        try {
                                                  switch (control) {
                                                            case CurtainControlUtils.OPEN:
                                                                      Toast.makeText(PlayActivity.this, "--OPEN", Toast.LENGTH_SHORT)
                                                                           .show();
                                                                      sendJson("kuozhan", curtainControlUtils.getControlCommand(CurtainControlUtils.OPEN));
                                                                      break;
                                                            case CurtainControlUtils.CLOSE:
                                                                      Toast.makeText(PlayActivity.this, "--CLOSE", Toast.LENGTH_SHORT)
                                                                           .show();
                                                                      sendJson("kuozhan", curtainControlUtils.getControlCommand(CurtainControlUtils.CLOSE));
                                                                      break;
                                                            case CurtainControlUtils.REDIC:
                                                                      Toast.makeText(PlayActivity.this, "--REDIC", Toast.LENGTH_SHORT)
                                                                           .show();
                                                                      sendJson("kuozhan", curtainControlUtils.getControlCommand(CurtainControlUtils.REDIC));
                                                                      break;
                                                            case CurtainControlUtils.STOP:
                                                                      Toast.makeText(PlayActivity.this, "--STOP", Toast.LENGTH_SHORT)
                                                                           .show();
                                                                      sendJson("kuozhan", curtainControlUtils.getControlCommand(CurtainControlUtils.STOP));
                                                                      break;
                                                  }
                                        } catch (JSONException e) {
                                                  e.printStackTrace();
                                        }
                              } else if (action.equals("com.device.control.switch.action")) {
                                        boolean isChecked = intent.getBooleanExtra("isChecked", false);
                                        int no_switch = intent.getIntExtra("switch", 1);
                                        try {
                                                  sendJson("kuozhan", switchControlUtils.getControlCommand(isChecked, no_switch));
                                        } catch (JSONException e) {
                                                  e.printStackTrace();
                                        }
                                        Toast.makeText(PlayActivity.this, "switch" + no_switch + "-->" + isChecked, Toast.LENGTH_SHORT)
                                             .show();
                              }
                    }
          }


          private boolean isPlay() {
                    if (!realplayCameraCtrl.isPlaying()) {
                              showTV("视频未开启！");
                    }
                    return realplayCameraCtrl.isPlaying();
          }

          private void showTV(String ss) {
                    logtext = ss;
                    mHander.sendEmptyMessage(0x126);
          }

          @Override
          public void onClick(View v) {
                    switch (v.getId()) {
                              case R.id.play:
                                        Play();
                                        break;
                              case R.id.stop:
                                        Stop();
                                        break;
                              case R.id.HD:
                                        if (sp.getisDemo()) {
                                                  mHander.sendEmptyMessage(0x127);
                                                  break;
                                        }
                                        if (!isPlay()) {
                                                  break;
                                        }
                                        SwitchStream(XmStreamMode.ModeHd);
                                        break;
                              case R.id.SD:
                                        if (sp.getisDemo()) {
                                                  mHander.sendEmptyMessage(0x127);
                                                  break;
                                        }
                                        if (!isPlay()) {
                                                  break;
                                        }
                                        SwitchStream(XmStreamMode.ModeFluency);
                                        break;
                              case R.id.AT:
                                        if (sp.getisDemo()) {
                                                  mHander.sendEmptyMessage(0x127);
                                                  break;
                                        }
                                        if (!isPlay()) {
                                                  break;
                                        }
                                        SwitchStream(XmStreamMode.ModeAdapter);
                                        break;
                              case R.id.capture:
                                        if (sp.getisDemo()) {
                                                  mHander.sendEmptyMessage(0x127);
                                                  break;
                                        }
                                        if (!isPlay()) {
                                                  break;
                                        }
                                        Capture();
                                        break;
                              case R.id.Record:
                                        if (sp.getisDemo()) {
                                                  mHander.sendEmptyMessage(0x127);
                                                  break;
                                        }
                                        if (!isPlay()) {
                                                  break;
                                        }
                                        Record();
                                        break;
                              case R.id.StopRecord:
                                        if (sp.getisDemo()) {
                                                  mHander.sendEmptyMessage(0x127);
                                                  break;
                                        }
                                        if (!isPlay()) {
                                                  break;
                                        }
                                        StopRecord();
                                        break;
                              case R.id.btn_rebinder:
                                        if (sp.getisDemo()) {
                                                  mHander.sendEmptyMessage(0x127);
                                                  break;
                                        }
                                        btn_rebinder();
                                        break;
                              case R.id.SLrecord:
                                        if (talkma.getCurState() == TalkState.NoOpen) {
                                                  slrecord();
                                        } else {
                                                  mHander.sendEmptyMessage(0x129);
                                        }

                                        break;
                              case R.id.SLrecordclose:
                                        if (talkma.getCurState() == TalkState.Opened) {
                                                  slrecordclose();
                                        } else {
                                                  mHander.sendEmptyMessage(0x130);
                                        }

                                        break;

                              case R.id.ll_show:
                                        if (isshow) {
                                                  isshow = false;
                                                  tv_show.setText("-打开实时演示板-");
                                                  if (adapter.getmList().size() != 0)
                                                            tx_curtain.setVisibility(View.GONE);
                                                  tx_oc.setVisibility(View.GONE);
                                                  ll_oc_list.setVisibility(View.GONE);
                                                  ll_curtain.setVisibility(View.GONE);
                                                  ll_curtain_list.setVisibility(View.GONE);
                                                  //scrollView.scrollTo(50,0);
                                        } else {
                                                  isshow = true;
                                                  tv_show.setText("-关闭实时演示板-");
                                                  tx_curtain.setVisibility(View.VISIBLE);
                                                  if (adapter.getmList().size() != 0) {
                                                            tx_oc.setVisibility(View.VISIBLE);
                                                  }
                                                  ll_oc_list.setVisibility(View.VISIBLE);
                                                  //关闭空调按钮
//                                                  ll_curtain.setVisibility(View.VISIBLE);
                                                  ll_curtain_list.setVisibility(View.VISIBLE);
                                        }
                                        break;

                    }
          }

          @Override
          protected void onPause() {
                    super.onPause();
          }

          @Override
          protected void onDestroy() {
                    super.onDestroy();
                    unregisterReceiver(receiver);
          }

          @Override
          public boolean onKeyDown(int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK
                         && event.getAction() == KeyEvent.ACTION_DOWN) {
                              Stop();
                    }
                    return super.onKeyDown(keyCode, event);
          }


          /**
           * description:获取设备状态
           * auther：joahluo
           * time：2017/6/27 15:53
           */
          GizWifiDeviceListener mListener = new GizWifiDeviceListener() {
                    @Override
                    public void didReceiveData(GizWifiErrorCode result, GizWifiDevice device, ConcurrentHashMap<String, Object> dataMap, int sn) {
                              if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                                        // 已定义的设备数据点，有布尔、数值和枚举型数据
                                        if (dataMap.get("data") != null) {
                                                  ConcurrentHashMap<String, Object> map = (ConcurrentHashMap<String, Object>) dataMap.get("data");
                                                  // 获得kuozhan类型数据
                                                  byte[] bytes = (byte[]) map.get("kuozhan");
                                                  byte[] deviceId = new byte[4];
                                                  int sum = 0;
                                                  for (int i = 0; i < bytes.length / 6; i++) {
                                                            if ((bytes[5 + 6 * i] & 0xf0) == 0xf0) {

                                                                      for (int j = 0; j < 4; j++) {
                                                                                deviceId[j] = bytes[j + 1 + i * 6];
                                                                      }
                                                                      String mac = ConvertUtil.byteStringToHexString(deviceId).toUpperCase();
                                                                      //获取数据库数据
                                                                      switchInfo = dbAdapter.findSwitchInfoStatus(mac);
                                                                      //广播intent
                                                                      Intent intent = new Intent(action);
                                                                      //开关类型 状态
                                                                      switch ((int) bytes[0 + 6 * i]) {
                                                                                case (int) ControlProtocol.DevType.SWITCH_THREE:
                                                                                          if ((bytes[5 + 6 * i] & 0x4) == 0x4) {
                                                                                                    switchInfo.setStatus3(1);
                                                                                          } else {
                                                                                                    switchInfo.setStatus3(0);
                                                                                          }
                                                                                case (int) ControlProtocol.DevType.SWITCH_TWO:
                                                                                          if ((bytes[5 + 6 * i] & 0x2) == 0x2) {
                                                                                                    switchInfo.setStatus2(1);
                                                                                          } else {
                                                                                                    switchInfo.setStatus2(0);
                                                                                          }
                                                                                case (int) ControlProtocol.DevType.SWITCH_ONE:
                                                                                          if (mac.equals("00000000")) {
                                                                                                    break;
                                                                                          }
                                                                                          if ((bytes[5 + 6 * i] & 0x1) == 0x1) {
                                                                                                    switchInfo.setStatus1(1);
                                                                                          } else {
                                                                                                    switchInfo.setStatus1(0);
                                                                                          }
                                                                                          sum++;
                                                                                          adapter.updateList(PlayActivity.this.switchInfo, UPDATA_STATUS);
                                                                                          //发送广播，通知UI更新
                                                                                          intent.putExtra("address",switchInfo.getAddress());
                                                                                          intent.putExtra("stutas1",switchInfo.getStatus1());
                                                                                          intent.putExtra("stutas2",switchInfo.getStatus2());
                                                                                          intent.putExtra("stutas3",switchInfo.getStatus3());
                                                                                          sendBroadcast(intent);
                                                                                          break;
                                                                                case (int) ControlProtocol.DevType.PLUG_FIVE:
                                                                                          if ((bytes[5 + 6 * i] & 0x1) == 0x1) {
                                                                                                    switchInfo.setStatus1(1);
                                                                                          } else {
                                                                                                    switchInfo.setStatus1(0);
                                                                                          }
                                                                                          sum++;
                                                                                          adapter.updateList(PlayActivity.this.switchInfo, UPDATA_STATUS);
                                                                                          //发送广播，通知UI更新
                                                                                          intent.putExtra("address",switchInfo.getAddress());
                                                                                          intent.putExtra("stutas4",switchInfo.getStatus1());
                                                                                          sendBroadcast(intent);
                                                                                          break;
                                                                      }
                                                            }

                                                  }
                                                  if (sum != 0) {
                                                            Toast.makeText(getApplicationContext(), "接收到" + sum + "条成功！",
                                                                 Toast.LENGTH_SHORT).show();
                                                  }


                                        }
                              }

                    }

          };


          /**
           * description:设置设备状态监听
           * auther：joahluo
           * time：2017/6/27 16:27
           */
          private void initStatusListener() {
                    //设置设备状态监听
                    device.setListener(mListener);
                    //通知设备上报数据
                    sendBroadreceive();
          }

          /**
           * description：开启广播监听
           * auther：joahluo
           * time：2017/6/27 14:30
           */
          private void sendBroadreceive() {
                    //TODO: 发送状态同步广播给服务器
                    try {
                              sendJson(KUOZHAN, SEND_BROAD);
                    } catch (JSONException e) {
                              e.printStackTrace();
                    }
          }


}
