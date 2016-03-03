package com.project.sysumobilelibrary.adapter;

import java.util.ArrayList;

import me.drakeet.materialdialog.MaterialDialog;

import com.android.volley.toolbox.NetworkImageView;
import com.project.sysumobilelibrary.BookDetailActivity;
import com.project.sysumobilelibrary.R;
import com.project.sysumobilelibrary.entity.LikeBook;
import com.project.sysumobilelibrary.entity.SearchBook;
import com.project.sysumobilelibrary.global.MyApplication;
import com.project.sysumobilelibrary.utils.MyDBHelper;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MyLikeListViewAdapter extends BaseAdapter {
	private Context context;
	private static ArrayList<LikeBook> likeBooks;

	public MyLikeListViewAdapter(Context context, ArrayList<LikeBook> likeBooks) {
		this.context = context;
		this.likeBooks = likeBooks;
	} 

	@Override
	public int getCount() {
		return likeBooks.size();
	}

	@Override
	public Object getItem(int index) {
		return likeBooks.get(index);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.like_book_list_item, parent, false);
			viewHolder = new ViewHolder();
			initViewHolder(viewHolder, convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.nivPic.setDefaultImageResId(R.drawable.default_book_pic);
		viewHolder.nivPic.setErrorImageResId(R.drawable.default_book_pic);
		viewHolder.nivPic.setImageUrl(likeBooks.get(position).getImg_url(),
				MyApplication.getMyVolley().getImageLoader());

		final LikeBook likeBook = likeBooks.get(position);
		viewHolder.tvNo.setText((position+1)+"");
		viewHolder.tvLikeTime.setText(likeBook.getFormatLikeTime());
		viewHolder.tvName.setText(likeBooks.get(position).getName());
		viewHolder.tvAuthor.setText(likeBooks.get(position).getAuthor());
		viewHolder.tvBookNum.setText(likeBooks.get(position).getBook_num());
		viewHolder.tvPublisher.setText(likeBooks.get(position).getPublisher());
		viewHolder.tvYear.setText(likeBooks.get(position).getYear());
			
		viewHolder.ivNote.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final MaterialDialog dialog = new MaterialDialog(context);
				View view = LayoutInflater.from(context).inflate(R.layout.note_dialog, null);
				final EditText etNote = (EditText)view.findViewById(R.id.et_note);
				etNote.setText(likeBook.getNote());
				dialog.setPositiveButton("修改", new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						String note = etNote.getText().toString().trim();
						MyDBHelper myDBHelper = new MyDBHelper(context);
						int rows = myDBHelper.updateNote(MyApplication.getUser().getUsername(), likeBook.getDoc_number(), note);
						if (rows == 1){
							myToast("修改备注成功");
							likeBooks.get(position).setNote(note);
						}else{
							myToast("修改备注失败");
						}
						dialog.dismiss();
					}
				}).setNegativeButton("关闭", new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
					}
				});
				dialog.setView(view).show();
			}
		});
		viewHolder.ivDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final MaterialDialog dialog = new MaterialDialog(context);
				dialog.setMessage("你确定要删除这条收藏吗？");
				dialog.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						MyDBHelper myDBHelper = new MyDBHelper(context);
						int rows = myDBHelper.deleteLike(MyApplication.getUser().getUsername(), likeBook.getDoc_number());
						if (rows == 1){
							myToast("删除成功");
							MyLikeListViewAdapter.this.notifyDataSetChanged();
							dialog.dismiss();
						}else{
							myToast("删除失败");
						}
						likeBooks.remove(position);
					}
				}).setNegativeButton("取消", new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
		viewHolder.tvDetail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, BookDetailActivity.class);
				intent.putExtra("doc_number", likeBooks.get(position).getDoc_number());
				context.startActivity(intent);
			}
		});
		return convertView;
	}

	private void initViewHolder(ViewHolder viewHolder, View convertView) {
		viewHolder.nivPic = (NetworkImageView) convertView
				.findViewById(R.id.niv_pic);
		viewHolder.tvNo = (TextView) convertView.findViewById(R.id.tv_no);
		viewHolder.tvLikeTime = (TextView) convertView.findViewById(R.id.tv_like_time);
		viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
		viewHolder.tvAuthor = (TextView) convertView
				.findViewById(R.id.tv_author);
		viewHolder.tvBookNum = (TextView) convertView
				.findViewById(R.id.tv_book_num);
		viewHolder.tvPublisher = (TextView) convertView
				.findViewById(R.id.tv_publisher);
		viewHolder.tvYear = (TextView) convertView
				.findViewById(R.id.tv_year);
		viewHolder.ivNote = (ImageView)convertView.findViewById(R.id.iv_note);
		viewHolder.ivDelete = (ImageView)convertView.findViewById(R.id.iv_delete);
		viewHolder.tvDetail = (TextView) convertView
				.findViewById(R.id.tv_detail);
		
	}
	private void myToast(String msg){
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	public final class ViewHolder {
		public NetworkImageView nivPic;
		public TextView tvNo;
		public TextView tvLikeTime;
		public TextView tvName;
		public TextView tvAuthor;
		public TextView tvBookNum;
		public TextView tvPublisher;
		public TextView tvYear;
		public ImageView ivNote;
		public TextView tvDetail;
		public ImageView ivDelete;

	}
}
