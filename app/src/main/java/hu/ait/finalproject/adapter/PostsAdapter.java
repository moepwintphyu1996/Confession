package hu.ait.finalproject.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.ait.finalproject.R;
import hu.ait.finalproject.Data.Post;


public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    private Context context;
    private List<Post> postList;
    private List<String> postKeys;
    private String uId;
    private int lastPosition = -1;
    private DatabaseReference postsRef;

    public PostsAdapter(Context context, String uId) {
        this.context = context;
        this.uId = uId;
        this.postList = new ArrayList<Post>();
        this.postKeys = new ArrayList<String>();

        postsRef = FirebaseDatabase.getInstance().getReference("posts");
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Post tmpPost = postList.get(position);
        holder.tvAuthor.setText(tmpPost.getAuthor());
        holder.tvTitle.setText(tmpPost.getTitle());
        holder.tvBody.setText(tmpPost.getBody());
        holder.tvTime.setText(tmpPost.getTime());


        if (!TextUtils.isEmpty(tmpPost.getImageUrl())) {
            holder.ivPost.setVisibility(View.VISIBLE);
            Glide.with(context).load(tmpPost.getImageUrl()).into(holder.ivPost);
        } else {
            holder.ivPost.setVisibility(View.GONE);
        }

        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(tmpPost.getUid())) {
            holder.btnDelete.setVisibility(View.VISIBLE);
        }

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePost(position);
            }
        });

        holder.btnUpVote.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                tmpPost.setScore(tmpPost.getScore() + 1);
                holder.tvScore.setText(Integer.toString(tmpPost.getScore()));
            }
        });

        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAuthor;
        public TextView tvTitle;
        public TextView tvBody;
        public ImageView ivPost;
        public ImageButton btnDelete;
        public TextView tvTime;
        public TextView tvScore;
        public ImageButton btnUpVote;

        public ViewHolder(View itemView) {
            super(itemView);
            tvAuthor = (TextView) itemView.findViewById(R.id.tvAuthor);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            ivPost = (ImageView) itemView.findViewById(R.id.ivPost);
            btnDelete = (ImageButton) itemView.findViewById(R.id.btnDelete);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            tvScore = (TextView) itemView.findViewById(R.id.tvScore);
            btnUpVote = (ImageButton) itemView.findViewById(R.id.btnUpVote);
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context,
                    android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }


    public void addPost(Post place, String key) {
        postList.add(place);
        postKeys.add(key);
        notifyDataSetChanged();
    }




    public void updatePost(int index, int score){
        postsRef.child(postKeys.get(index)).child("score").setValue(score);
        notifyDataSetChanged();
    }

    public void updatePostByKey(String key){
        int index = postKeys.indexOf(key);
//        postList.get(index).setScore(score);
        notifyDataSetChanged();

    }

    public void removePost(int index) {
        postsRef.child(postKeys.get(index)).removeValue();
        postList.remove(index);
        postKeys.remove(index);
        notifyItemRemoved(index);
    }

    public void removePostByKey(String key) {
        int index = postKeys.indexOf(key);
        if (index != -1) {
            postList.remove(index);
            postKeys.remove(index);
            notifyItemRemoved(index);
        }
    }

}
