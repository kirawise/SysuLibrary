package com.project.sysumobilelibrary.adapter;

import java.util.ArrayList;

import me.drakeet.materialdialog.MaterialDialog;

import com.android.volley.toolbox.NetworkImageView;
import com.project.sysumobilelibrary.BookDetailActivity;
import com.project.sysumobilelibrary.LikeFragment;
import com.project.sysumobilelibrary.R;
import com.project.sysumobilelibrary.entity.LikeBook;
import com.project.sysumobilelibrary.entity.SearchBook;
import com.project.sysumobilelibrary.global.MyApplication;
import com.project.sysumobilelibrary.utils.MyDBHelper;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MySearchListViewAdapter extends BaseAdapter {
	private final static String TAG = "MySearchListViewAdapter";
	private Context context;
	private static ArrayList<LikeBook> searchBooks;

	public MySearchListViewAdapter(Context context, ArrayList<LikeBook> searchBooks) {
		this.context = context;
		this.searchBooks = searchBooks;
	} 

	@Override
	public int getCount() {
		return searchBooks.size();
	}

	@Override
	public Object getItem(int index) {
		return searchBooks.get(index);
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
					R.layout.search_book_list_item, null);
			viewHolder = new ViewHolder();
			initViewHolder(viewHolder, convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.nivPic.setDefaultImageResId(R.drawable.default_book_pic);
		viewHolder.nivPic.setErrorImageResId(R.drawable.default_book_pic);
		viewHolder.nivPic.setImageUrl(searchBooks.get(position).getImg_url(),
				MyApplication.getMyVolley().getImageLoader());

		viewHolder.tvNo.setText((position+1)+"");
		viewHolder.tvName.setText(searchBooks.get(position).getName());
		viewHolder.tvAuthor.setText(searchBooks.get(position).getAuthor());
		viewHolder.tvBookNum.setText(searchBooks.get(position).getBook_num());
		viewHolder.tvPublisher.setText(searchBooks.get(position).getPublisher());
		viewHolder.tvYear.setText(searchBooks.get(position).getYear());
		MyDBHelper myDBHelper = new MyDBHelper(context);
		if (myDBHelper.isInLike(MyApplication.getUser().getUsername(), searchBooks.get(position).getDoc_number())){
			viewHolder.ivLike.setImageResource(R.drawable.red_heart);
			viewHolder.ivLike.setTag(R.drawable.red_heart);
		}else{
			viewHolder.ivLike.setImageResource(R.drawable.black_heart);
			viewHolder.ivLike.setTag(R.drawable.black_heart);
		}
		
		
		viewHolder.ivLike.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				final ImageView imageView = (ImageView)view;
				if ((Integer)view.getTag()==R.drawable.black_heart){
					imageView.setImageResource(R.drawable.red_heart);
					imageView.setTag(R.drawable.red_heart);
					//�ղ�
					new Thread(new AddToLike(MyApplication.getUser().getUsername(), searchBooks.get(position))).start();
					
				}else{
					//ɾ���ղ�
					final MaterialDialog dialog = new MaterialDialog(context);
					dialog.setMessage("ȷ��ɾ������ղ���");
					dialog.setPositiveButton("ɾ��", new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							new Thread(new DelLike(MyApplication.getUser().getUsername(), searchBooks.get(position).getDoc_number())).start();
							imageView.setImageResource(R.drawable.black_heart);
							imageView.setTag(R.drawable.black_heart);
							dialog.dismiss();
						}
					}).setNegativeButton("ȡ��", new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							dialog.dismiss();
						}
					});
					dialog.show();
				}
				
			}
		});
		viewHolder.tvDetail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, BookDetailActivity.class);
				intent.putExtra("doc_number", searchBooks.get(position).getDoc_number());
				context.startActivity(intent);
				
			}
		});
		return convertView;
	}

	private void initViewHolder(ViewHolder viewHolder, View convertView) {
		viewHolder.nivPic = (NetworkImageView) convertView
				.findViewById(R.id.niv_pic);
		viewHolder.tvNo = (TextView) convertView.findViewById(R.id.tv_no);
		viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
		viewHolder.tvAuthor = (TextView) convertView
				.findViewById(R.id.tv_author);
		viewHolder.tvBookNum = (TextView) convertView
				.findViewById(R.id.tv_book_num);
		viewHolder.tvPublisher = (TextView) convertView
				.findViewById(R.id.tv_publisher);
		viewHolder.tvYear = (TextView) convertView
				.findViewById(R.id.tv_year);
		viewHolder.ivLike = (ImageView)convertView.findViewById(R.id.iv_like);
		viewHolder.tvDetail = (TextView) convertView
				.findViewById(R.id.tv_detail);
		
	}

	public final class ViewHolder {
		public NetworkImageView nivPic;
		public TextView tvNo;
		public TextView tvName;
		public TextView tvAuthor;
		public TextView tvBookNum;
		public TextView tvPublisher;
		public TextView tvYear;
		public ImageView ivLike;
		public TextView tvDetail;

	}
	public void myToast(String msg){
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	class DelLike implements Runnable{
		private String reader_number;
		private String doc_number;
		public DelLike(String reader_number, String doc_number) {
			this.reader_number = reader_number;
			this.doc_number = doc_number;
		}
		@Override
		public void run() {
			MyDBHelper myDBHelper = new MyDBHelper(context);
			int rows = myDBHelper.deleteLike(reader_number, doc_number);
			if (rows == 1){
//				myToast("ɾ���ղسɹ�");
				Log.e(TAG, "ɾ���ղسɹ�");
				LikeFragment.delLikeBook(doc_number);
				
			}else if (rows == 0){
//				myToast("ɾ���ղ�ʧ��");
				Log.e(TAG, "ɾ���ղ�ʧ��");
			}else{
//				myToast("BUG!");
				Log.e(TAG, "ɾ���ղ�ʧ�� id="+rows);
			}
		}
	}
	class AddToLike implements Runnable{
		private String reader_number;
		private LikeBook likeBook;
		public AddToLike(String reader_number, LikeBook likeBook) {
			this.reader_number = reader_number;
			this.likeBook = likeBook;
		}
		@Override
		public void run() {
			MyDBHelper myDBHelper = new MyDBHelper(context);
			long id = myDBHelper.insertLike(reader_number, likeBook);
			if (id > 0){
//				myToast("�����ղسɹ�");
				LikeFragment.addLikeBookToHead(likeBook);
			}else{
//				myToast("�����ղ�ʧ��");
				Log.e(TAG, "�����ղ�ʧ�� id="+id);
			}
		}
		
	}
}
