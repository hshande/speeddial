package com.sunday.speeddial.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sunday.speeddial.R;
import com.sunday.speeddial.adapter.DefaultItemTouchHelpCallback;
import com.sunday.speeddial.adapter.DefaultItemTouchHelper;
import com.sunday.speeddial.adapter.PhoneAdapter;
import com.sunday.speeddial.bean.Phone;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhoneEditActivity extends AppCompatActivity {


    private PhoneAdapter phoneAdapter;

    public static void startAction(Context context) {
        Intent intent = new Intent(context, PhoneEditActivity.class);
        context.startActivity(intent);
    }

    public static void startAction(Activity activity) {
        Intent intent = new Intent(activity, PhoneEditActivity.class);
        activity.startActivityForResult(intent, 10001);
    }

    private Context mContext = this;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_edit);
        ButterKnife.bind(this);
        bindView();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private AlertDialog dialog = null;
    private AlertDialog deleteDialog = null;
    private TextView phoneView = null;
    private TextView nameView = null;
    private ImageView imageView = null;
    private Button delView = null;

    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.choose_dialog, null);//获取自定义布局
        builder.setView(layout);

        phoneView = layout.findViewById(R.id.phone);
        nameView = layout.findViewById(R.id.custom);
        imageView = layout.findViewById(R.id.phone_photo);
        delView = layout.findViewById(R.id.del);
        delView.setVisibility(View.GONE);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFromLocal();
            }
        });
        //确认按钮
        builder.setPositiveButton("确定", null);
        //取消
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                dialogClose();
            }
        });
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = phoneView.getText().toString();
                if (phone == null || phone.isEmpty()) {
                    Toast.makeText(mContext, "电话号码不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                String name = nameView.getText().toString();
                Phone phoneBean = new Phone();
                phoneBean.setPhone(phone);
                phoneBean.setName(name);
                phoneBean.setSort(getMaxSort());
                if (imageView != null) {
                    Uri uri = (Uri) imageView.getTag();
                    if (uri != null) {
                        Bitmap bitmap = null;
                        try {
                            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        String realPath = writeFileByBitmap2(bitmap);
                        phoneBean.setPhoto(realPath);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                        byte[] bytes = stream.toByteArray();
                        phoneBean.setPhotoBase64(Base64.encodeToString(bytes, Base64.DEFAULT));
                    }
                }
                boolean save = phoneBean.save();
                if (save) {
                    phoneAdapter.getPhoneList().add(phoneBean);
                    phoneAdapter.notifyDataSetChanged();
                }
                dialogClose();
            }

        });
    }

    private void showDeleteDialog(Phone phoneBean) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("是否确定删除");
        //确认按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Phone> phoneList = phoneAdapter.getPhoneList();
                if(phoneList == null){

                    deleteDialogClose();
                    return;
                }
                int delete = phoneBean.delete();
                if(delete > 0){
                    boolean remove = phoneList.remove(phoneBean);
                    if (remove) {
                        phoneAdapter.notifyDataSetChanged();
                    }
                }
                deleteDialogClose();
            }
        });
        //取消
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                deleteDialogClose();
            }
        });
        deleteDialog = builder.create();
        deleteDialog.setCancelable(false);
        deleteDialog.show();
    }

    public void showDialog(final Phone phoneBean) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.choose_dialog, null);//获取自定义布局
        builder.setView(layout);

        phoneView = layout.findViewById(R.id.phone);
        nameView = layout.findViewById(R.id.custom);
        delView = layout.findViewById(R.id.del);
        delView.setVisibility(View.VISIBLE);
        phoneView.setText(phoneBean.getPhone());
        nameView.setText(phoneBean.getName());
        imageView = layout.findViewById(R.id.phone_photo);
        String photo = phoneBean.getPhotoBase64();
        if (photo != null && !photo.isEmpty()) {
            String base64 =  photo;
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedByte);
        }
        delView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              showDeleteDialog(phoneBean);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFromLocal();
            }
        });
        //确认按钮
        builder.setPositiveButton("确定", null);
        //取消
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                dialogClose();
            }
        });
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = phoneView.getText().toString();
                if (phone == null || phone.isEmpty()) {
                    int delete = phoneBean.delete();
                    if (delete > 0) {

                        Toast.makeText(mContext, "电话号码删除", Toast.LENGTH_LONG).show();
                        phoneAdapter.getPhoneList().remove(phoneBean);
                        phoneAdapter.notifyDataSetChanged();
                        dialogClose();
                    }
                    return;
                }
                phoneBean.setPhone(phone);
                String name = nameView.getText().toString();
                phoneBean.setName(name);
                if (imageView != null) {
                    Uri uri = (Uri) imageView.getTag();
                    if (uri != null) {
                        Bitmap bitmap = null;
                        try {
                            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        String realPath = writeFileByBitmap2(bitmap);
                        phoneBean.setPhoto(realPath);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                        byte[] bytes = stream.toByteArray();
                        phoneBean.setPhotoBase64(Base64.encodeToString(bytes, Base64.DEFAULT));
                    }
                }
                int update = phoneBean.update(phoneBean.getId());
                if (update > 0) {
                    phoneAdapter.notifyDataSetChanged();
                }
                dialogClose();
            }

        });
    }

    private void dialogClose() {
        if (dialog != null) {
            dialog.cancel();
            dialog = null;
            phoneView = null;
            imageView = null;
            delView = null;
        }
    }

    private void deleteDialogClose() {
        if (deleteDialog != null) {
            deleteDialog.cancel();
            deleteDialog = null;
        }
        dialogClose();
    }

    private void bindView() {
        //设置布局管理器
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        //设置adapter
        List<Phone> phoneList = DataSupport.order("sort asc").find(Phone.class);
        phoneAdapter = new PhoneAdapter(phoneList);
        recyclerView.setAdapter(phoneAdapter);
        //设置Item增加、移除动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DefaultItemTouchHelper itemTouchHelper = new DefaultItemTouchHelper(onItemTouchCallbackListener);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        itemTouchHelper.setDragEnable(true);
        itemTouchHelper.setSwipeEnable(true);
    }


    private int getMaxSort() {
        int result = 0;
        Cursor cursor = DataSupport.findBySQL("select MAX(sort) from phone");
        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result + 1;
    }

    private static final int CHOOSE_PICTURE = 1;
    private static final int CROP_PICTURE = 2;

    private void selectFromLocal() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, CHOOSE_PICTURE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHOOSE_PICTURE:
                    startPhotoZoom(data.getData());
                    break;
                case CROP_PICTURE: // 取得裁剪后的图片
                    try {
                        if (imageView != null) {
                            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                            imageView.setImageBitmap(bitmap);
                            imageView.setTag(imageUri);
                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static final String IMAGE_FILE_LOCATION_APP = "/sunday/speeddial";
    private static final String IMAGE_FILE_LOCATION = "file:///" + Environment.getExternalStorageDirectory().getPath() + "/temp.jpg";
    private Uri imageUri = Uri.parse(IMAGE_FILE_LOCATION);

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);

        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        intent.putExtra("outputX", 500);
        intent.putExtra("outputY", 500);

        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        startActivityForResult(intent, CROP_PICTURE);
    }


    /**
     * 以时间戳命名将bitmap写入文件
     *
     * @param bitmap
     */
    public static String writeFileByBitmap2(Bitmap bitmap) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + IMAGE_FILE_LOCATION_APP;//手机设置的存储位置
//        File file = new File(path);
        String realPath = System.currentTimeMillis() + ".png";
//        File imageFile = new File(file, realPath);


//        if (!file.exists()) {
//            file.mkdirs();
//        }
//        try {
//            imageFile.createNewFile();
//            FileOutputStream outputStream = new FileOutputStream(imageFile);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
//            outputStream.flush();
//            outputStream.close();
            return path + "/" + realPath;
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
    }

    private DefaultItemTouchHelpCallback.OnItemTouchCallbackListener onItemTouchCallbackListener = new DefaultItemTouchHelpCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int adapterPosition) {
            // 滑动删除的时候，从数据源移除，并刷新这个Item。
            if (phoneAdapter.getPhoneList() != null) {
                phoneAdapter.getPhoneList().remove(adapterPosition);
                phoneAdapter.notifyItemRemoved(adapterPosition);
            }
        }

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            if (phoneAdapter.getPhoneList() != null) {
                List<Phone> datas = phoneAdapter.getPhoneList();
                if (srcPosition < targetPosition) {
                    for (int i = srcPosition; i < targetPosition; i++) {
                        Collections.swap(datas, i, i + 1);
                    }
                } else {
                    for (int i = srcPosition; i > targetPosition; i--) {
                        Collections.swap(datas, i, i - 1);
                    }
                }
                phoneAdapter.notifyItemMoved(srcPosition, targetPosition);
                return true;
//                // 更换数据源中的数据Item的位置
//                Collections.swap(phoneAdapter.getPhoneList(), srcPosition, targetPosition);
//                // 更新UI中的Item的位置，主要是给用户看到交互效果
//                phoneAdapter.notifyItemMoved(srcPosition, targetPosition);
//                return true;
            }
            return false;


        }


        @Override
        public void complete() {
            //如果完成之后不刷新会导致删除的时候下标错误的情况
            phoneAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onPause() {
        if (phoneAdapter != null) {
            List<Phone> phoneList = phoneAdapter.getPhoneList();
            if (phoneList != null && !phoneList.isEmpty()) {
                for (int i = 0, len = phoneList.size(); i < len; i++) {
                    Phone phone = phoneList.get(i);
                    phone.setSort(i + 1);
                    phone.update(phone.getId());
                }
//                Intent intent = new Intent();
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("dataList", (Serializable) phoneList);
//                intent.putExtras(bundle);
//                setResult(RESULT_OK, intent);
            }
        }
        super.onPause();
    }

}
