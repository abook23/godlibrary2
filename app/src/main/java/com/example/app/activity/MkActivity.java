package com.example.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.app.R;
import com.god.util.DataManager;

import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.InternalStyleSheet;
import br.tiagohm.markdownview.css.styles.Github;

public class MkActivity extends AppCompatActivity {
    private static final String ASSET_MK_FILE_URL = "ASSET_MK_FILE_URL";
    private MarkdownView mMarkdownView;

    public static void show(Context context, String mk_url) {
        DataManager.getInstance().put(MkActivity.ASSET_MK_FILE_URL, mk_url);
        context.startActivity(new Intent(context, MkActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mk);
        String mkFileUrl = DataManager.getInstance().get(ASSET_MK_FILE_URL);
        if (mkFileUrl != null) {
            mMarkdownView = findViewById(R.id.markdown_view);
            mMarkdownView.addStyleSheet(getCss());
            mMarkdownView.loadMarkdownFromAsset(mkFileUrl);
//        mMarkdownView.loadMarkdown("**MarkdownView**");
//        mMarkdownView.loadMarkdownFromFile(new File());
//        mMarkdownView.loadMarkdownFromUrl("url");
        }
    }

    private InternalStyleSheet getCss() {
        InternalStyleSheet css = new Github();
        css.addRule("body", new String[]{"line-height: 1.2", "padding: 1px"});
        return css;
    }
}
