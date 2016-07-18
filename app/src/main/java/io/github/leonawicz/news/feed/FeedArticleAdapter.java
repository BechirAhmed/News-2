package io.github.leonawicz.news.feed;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

import io.github.leonawicz.news.R;

public class FeedArticleAdapter extends ArrayAdapter<FeedArticle> {

    public FeedArticleAdapter(Activity context, ArrayList<FeedArticle> feedArticles) {
        super(context, 0, feedArticles);
    }

    static class ViewHolder{
        TextView title;
        TextView author;
        TextView date;
        ImageView thumbnail;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.author = (TextView) convertView.findViewById(R.id.authors);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FeedArticle feedArticle = getItem(position);
        holder.title.setText(feedArticle.getTitle());
        holder.author.setText(feedArticle.getAuthor());
        holder.date.setText(feedArticle.getDate());

        new DownloadImageTask(holder.thumbnail).execute(feedArticle.getImageUriString());

        return convertView;
    }
}

