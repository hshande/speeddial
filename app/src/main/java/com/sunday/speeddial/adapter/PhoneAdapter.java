package com.sunday.speeddial.adapter;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sunday.speeddial.MainActivity;
import com.sunday.speeddial.R;
import com.sunday.speeddial.bean.Phone;
import com.sunday.speeddial.view.PhoneEditActivity;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by Administrator on 2018/8/5.
 */

public class PhoneAdapter extends RecyclerView.Adapter<PhoneAdapter.MyViewHolder> {

    private List<Phone> phoneList;

    public PhoneAdapter(List<Phone> mData) {
        this.phoneList = mData;
    }

    public void setNewData(List<Phone> mData) {
        phoneList = mData;
        this.notifyDataSetChanged();
    }


    public List<Phone> getPhoneList() {
        return phoneList;
    }

    private Context mContext;

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                parent.getContext()).inflate(R.layout.phone_item, parent,
                false));
        mContext = parent.getContext();
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.iv.setVisibility(View.GONE);
        holder.tv.setVisibility(View.GONE);


        if (phoneList == null || phoneList.isEmpty()) {
            return;
        }

        final Phone phoneBean = phoneList.get(position);
        String photo = phoneBean.getPhoto();
        String name = phoneBean.getName();
        if (photo != null && !photo.isEmpty()) {
            try {
                holder.iv.setImageURI(Uri.fromFile(new File(photo)));
                holder.iv.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                holder.tv.setText(String.valueOf(position + 1));
                holder.tv.setVisibility(View.VISIBLE);
            }

        } else {
            if (name != null && !name.isEmpty()) {

                holder.tv.setText(name);
            } else {

                holder.tv.setText(String.valueOf(position + 1));
            }
            holder.tv.setVisibility(View.VISIBLE);
        }
        final String phone = phoneBean.getPhone();
        if (phone != null && !phone.isEmpty()) {
            holder.rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mContext instanceof MainActivity) {

//                        dialPhone(phone);
                        callPhone(phone);
                    } else if (mContext instanceof PhoneEditActivity) {
//                        deleteItem(phoneBean);
                        ((PhoneEditActivity) mContext).showDialog(phoneBean);
                    }
                }
            });
        }
//        holder.iv.setImageBitmap(phoneList.get(position));
    }

    private void deleteItem(final Phone phoneBean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        //确认按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                int delete = phoneBean.delete();
                if (delete > 0) {
                    phoneList.remove(phoneBean);
                    notifyDataSetChanged();
//                    List<Phone> phoneList = DataSupport.order("sort asc").find(Phone.class);
//                    setNewData(phoneList);
                }
            }
        });
        //取消
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        builder.setTitle("删除");
        builder.setMessage("删除电话号码：" + phoneBean.getPhone());
        AlertDialog dialog = builder.create();
        dialog.show();

    }


    @Override
    public int getItemCount() {
        return phoneList.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView iv;
        TextView tv;
        RelativeLayout rl;

        public MyViewHolder(View view) {
            super(view);
            iv = view.findViewById(R.id.phone_photo);
            tv = view.findViewById(R.id.phone_photo_tv);
            rl = view.findViewById(R.id.click_rl);
        }
    }

    /**
     * 拨打电话（跳转到拨号界面，用户手动点击拨打）
     *
     * @param phoneNum 电话号码
     */
    public void dialPhone(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        mContext.startActivity(intent);
    }

    /**
     * 拨打电话（直接拨打电话）
     *
     * @param phoneNum 电话号码
     */
    public void callPhone(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mContext.startActivity(intent);
    }
}
