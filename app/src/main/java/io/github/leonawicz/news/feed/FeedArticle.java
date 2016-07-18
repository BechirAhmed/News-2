package io.github.leonawicz.news.feed;

import android.net.Uri;
import android.widget.ImageView;

public class FeedArticle {
    private String title;
    private String author;
    private String date;
    private String url;
    private String thumbnail;

    public FeedArticle(String title, String author, String date, String url, String thumbnail) {
        this.title = title;
        this.author = author;
        this.date = date;
        this.url = url;
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    public String getImageUriString() {
        return thumbnail;
    }

    public String toString(){
        return "title: " + title + "\nauthor: " + author + "\nDate: "
                + date +"\nWeb url: " + url + "\nImage url: " + thumbnail + "\n";
    }
}
