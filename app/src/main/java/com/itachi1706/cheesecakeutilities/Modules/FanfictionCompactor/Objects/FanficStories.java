package com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects;

/**
 * Created by Kenneth on 21/4/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.FanfictionCompactor.Objects in CheesecakeUtilities
 */
public class FanficStories {

    // As of Fanfiction Reader app last updated 13 November 2014

    private int id, page_id, author_id;
    private String title, chapters;

    public FanficStories(int id, int page_id, int author_id, String title, String chapters) {
        this.id = id;
        this.page_id = page_id;
        this.author_id = author_id;
        this.title = title;
        this.chapters = chapters;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPage_id() {
        return page_id;
    }

    public void setPage_id(int page_id) {
        this.page_id = page_id;
    }

    public int getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(int author_id) {
        this.author_id = author_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChapters() {
        return chapters;
    }

    public void setChapters(String chapters) {
        this.chapters = chapters;
    }
}
