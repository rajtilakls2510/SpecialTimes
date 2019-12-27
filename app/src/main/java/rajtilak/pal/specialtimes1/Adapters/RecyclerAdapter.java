package rajtilak.pal.specialtimes1.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import rajtilak.pal.specialtimes1.Activities.ShowProfileActivity;
import rajtilak.pal.specialtimes1.models.Posts;
import rajtilak.pal.specialtimes1.R;
import rajtilak.pal.specialtimes1.models.Users;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    ArrayList<Posts> posts;
    Context context;
    ArrayList<Users> users;
    //Users users;

    public RecyclerAdapter(ArrayList<Posts> posts,ArrayList<Users> users){
        this.posts=posts;
        this.users=users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        View view= LayoutInflater.from(context).inflate(R.layout.post_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Posts post1=posts.get(position);
        final Users user1=users.get(position);
        Picasso.get().load(user1.getProfileImageUrl()).fit().centerCrop().placeholder(R.drawable.ic_post_image_black_24dp).into(holder.itemProfileImage);
        Picasso.get().load(post1.getPostImageUrl()).resize(getScreenWidth(context),0).placeholder(R.drawable.ic_post_image_black_24dp).into(holder.itemImage);

        holder.itemProfileName.setText(user1.getName());
        SimpleDateFormat sdfDate=new SimpleDateFormat("dd/MM/yyyy EEE");
        SimpleDateFormat sdfTime=new SimpleDateFormat("h:mm a");
        holder.itemDate.setText(sdfDate.format(post1.getPostDate()));
        holder.itemTime.setText(sdfTime.format(post1.getPostDate()));
        holder.itemDescription.setText(post1.getPostDescription());
        holder.itemProfileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(context, ShowProfileActivity.class);
                i.putExtra("UserId",post1.getUserId());
                context.startActivity(i);
            }
        });
        holder.itemProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(context, ShowProfileActivity.class);
                i.putExtra("UserId",post1.getUserId());
                context.startActivity(i);
            }
        });
        holder.menuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Menu Image Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        if(post1.getPrivacy()==1)
            holder.itemPrivateText.setText("Private");
        else
            holder.itemPrivateText.setText("Public");
        holder.likeBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                    Toast.makeText(context, "Liked", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, "Unliked", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

   public static int getScreenWidth(Context context)
    {
        DisplayMetrics displayMetrics=new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        CircularImageView itemProfileImage;
        TextView itemProfileName,itemDate,itemTime,itemDescription,itemPrivateText;
        ImageView itemImage,menuImage;
        CardView cv;
        ToggleButton likeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemProfileImage=itemView.findViewById(R.id.post_item_profile_image);
            itemProfileName=itemView.findViewById(R.id.post_item_profile_name);
            itemDate=itemView.findViewById(R.id.post_item_date);
            itemTime=itemView.findViewById(R.id.post_item_time);
            itemDescription=itemView.findViewById(R.id.post_item_description);
            itemImage=itemView.findViewById(R.id.post_item_image);
            cv= itemView.findViewById(R.id.post_item_image_card);
            menuImage=itemView.findViewById(R.id.post_menu_image);
            itemPrivateText=itemView.findViewById(R.id.post_private_text);
            likeBtn=itemView.findViewById(R.id.post_item_like_button);
        }
    }
}
