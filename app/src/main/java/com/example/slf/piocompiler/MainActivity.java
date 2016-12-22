package com.example.slf.piocompiler;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.example.slf.piocompiler.JieShi.JieShi;
import com.example.slf.piocompiler.JieShi.Main;
import com.example.slf.piocompiler.YuFa.MyError;

public class MainActivity extends AppCompatActivity {

    private TextView controlText;
    private AutoCompleteTextView inputText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }
    private void init() {
        controlText= (TextView) findViewById(R.id.control_text);
        inputText= (AutoCompleteTextView) findViewById(R.id.input_text);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                MyError.errInfo="";
                JieShi.output="";
                Main.run(inputText.getText().toString());
                if (MyError.errInfo.equals("")){
                    controlText.setText("编译成功！\n"+JieShi.output);
                }else {
                    controlText.setText(MyError.errInfo);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            inputText.setText("const a=10;\n" +
                    "var b,c;\n" +
                    "procedure p;\n" +
                    "begin\n" +
                    "  b:=4;\n" +
                    "  c:=b+a\n" +
                    "end;\n" +
                    "\n" +
                    "begin\n" +
                    "   call  p;\n" +
                    "   b:=b-1;\n" +
                    "   write(2*c-b);\n" +
                    "end;");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
