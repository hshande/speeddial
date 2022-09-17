package com.sunday.speeddial;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.sunday.speeddial.adapter.PhoneAdapter;
import com.sunday.speeddial.bean.Phone;
import com.sunday.speeddial.utils.GsonUtils;
import com.sunday.speeddial.view.PhoneEditActivity;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private PhoneAdapter phoneAdapter;

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    private static final int MY_PERMISSIONS_REQUEST_CALL_CAMERA = 2;

    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE
    };

    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
    List<String> mPermissionList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
            bindView();
        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permissions, MY_PERMISSIONS_REQUEST_CALL_CAMERA);
        }


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void bindView() {
        //设置布局管理器
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        //设置adapter
        phoneAdapter = new PhoneAdapter(new ArrayList<Phone>());
        refresh(null);
        recyclerView.setAdapter(phoneAdapter);
        //设置Item增加、移除动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void refresh(List<Phone> phoneList) {
        if (phoneList == null || phoneList.isEmpty()) {

            phoneList = DataSupport.order("sort asc").find(Phone.class);
        }
        if (phoneAdapter != null) {
            phoneAdapter.setNewData(phoneList);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            PhoneEditActivity.startAction(MainActivity.this);
        } else if (id == R.id.action_export) {

            exportFile();
        } else if (id == R.id.action_import) {

            importFile();
        }
//        else if (id == R.id.action_handler_pic) {
//
//            List<Phone> phoneList = DataSupport.order("sort asc").find(Phone.class);
//            if (phoneList != null && phoneList.size() > 0) {
//                showToast("共"+phoneList.size());
//                int index = 0;
//                List<Phone> updateList = new ArrayList<>();
//                for (Phone phone : phoneList) {
//                    index ++;
//                    String photo = phone.getPhoto();
//                    if (TextUtils.isEmpty(photo)) {
//
//                        continue;
//                    }
//                    Bitmap bitmap = null;
//                    try {
//                        showToast("开始处理"+index);
//                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(new File(photo)));
//                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                        byte[] bytes = stream.toByteArray();
//                        phone.setPhotoBase64(Base64.encodeToString(bytes, Base64.DEFAULT));
//                        updateList.add(phone);
//                    } catch (IOException e) {
//                        continue;
//                    }
//                }
//                if (updateList.size() > 0) {
//
//                    index  = 0;
//
//                    for (Phone phone : updateList) {
//                        index ++;
//                        showToast("开始更新"+index);
//                        phone.update(phone.getId());
//                    }
//                }
//            }
//        }

        return super.onOptionsItemSelected(item);
    }
    private static final String IMAGE_FILE_LOCATION_APP = "/sunday/speeddial";
    private void exportFile() {

        // 判断sd卡是否存在
        boolean hasSDCard = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (!hasSDCard) {

            showToast("SD卡不存在");
            return;
        }

        List<Phone> phoneList = DataSupport.order("sort asc").find(Phone.class);
        if (phoneList == null || phoneList.size() == 0) {

            showToast("没有数据");
            return;
        }
        String jsonStr = GsonUtils.getJsonStr(phoneList);
        String fileName = "speeddial" + System.currentTimeMillis() + ".json";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +IMAGE_FILE_LOCATION_APP + File.separator + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            outStream.write(jsonStr.getBytes());
            outStream.close();
            file.createNewFile();
        } catch (FileNotFoundException e) {
            showToast("导出失败");
            return;
        } catch (IOException e) {
            showToast("导出失败");
            return;
        }
        showToast("导出成功"+filePath);
    }

    private void importFile() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");      //   /*/ 此处是任意类型任意后缀
        //intent.setType(“audio/*”) //选择音频

        //intent.setType(“video/*”) //选择视频 （mp4 3gp 是android支持的视频格式）

        //intent.setType(“video/*;image/*”)//同时选择视频和图片
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 10002);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("权限已申请");
            } else {
                showToast("权限已拒绝");
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_CALL_CAMERA) {
            for (int i = 0; i < grantResults.length; i++) {
                boolean happy = true;
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //判断是否勾选禁止后不再询问
                    boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions[i]);
                    if (showRequestPermission) {
                        happy = false;
                        showToast("权限未申请");
                    }
                }
                if (happy) {
                    bindView();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showToast(String string) {
        Toast.makeText(MainActivity.this, string, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10001) {
            refresh(null);
//            Bundle bundle = data.getExtras();
//            List<Phone> dataList = (List<Phone>) bundle.getSerializable("dataList");
//            refresh(dataList);
        }
        if (requestCode == 10002) {
            File file = null;
            Uri uri = null;
            if(data != null){

                uri = data.getData();
            }
            if (uri != null) {
                if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
                    file = new File(uri.getPath());
                } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                    //把文件复制到沙盒目录
                    ContentResolver contentResolver = getContentResolver();
                    String displayName = System.currentTimeMillis() + Math.round((Math.random() + 1) * 1000)
                            + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));

                    try {
                        InputStream is = contentResolver.openInputStream(uri);
                        File cache = new File(getCacheDir().getAbsolutePath(), displayName);
                        FileOutputStream fos = new FileOutputStream(cache);
                        FileUtils.copyInputStreamToFile(is, cache);
                        file = cache;
                        fos.close();
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (file == null) {
                    showToast("文件地址未找到");
                    return;
                }
                InputStreamReader isr = null;
                try {
                    isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    showToast("文件地址未找到");
                    return;
                } catch (FileNotFoundException e) {
                    showToast("文件地址未找到");
                    return;
                }
                BufferedReader br = new BufferedReader(isr);
                String str = "";
                String mimeTypeLine = null;

                while (true) {
                    try {
                        if (!((mimeTypeLine = br.readLine()) != null)) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    str = str + mimeTypeLine;
                }
                try {
                    List<Phone> list = GsonUtils.jsonToBean(str, new TypeToken<List<Phone>>() {
                    }.getType());
                    if (list == null || list.size() == 0) {

                        showToast("无数据");
                        return;
                    }
                    for (Phone phone : list) {

                        Phone phoneBean = new Phone();
                        phoneBean.setPhone(phone.getPhone());
                        phoneBean.setName(phone.getName());
                        phoneBean.setSort(phone.getSort());
                        phoneBean.setPhotoBase64(phone.getPhotoBase64());
                        Phone oldPhone = DataSupport.find(Phone.class, phone.getId());
                        if (oldPhone != null) {


                            int update = phoneBean.update(phone.getId());
                        }else{

                            boolean save = phoneBean.save();
                            if(save){

                            }
                        }
                    }
                    refresh(null);
                } catch (Exception e) {

                    showToast("解析失败");
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
