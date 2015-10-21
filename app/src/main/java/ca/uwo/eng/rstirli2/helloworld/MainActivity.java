package ca.uwo.eng.rstirli2.helloworld;

import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final List<String> urlList = new ArrayList<String>() {{
        add("http://i.imgur.com/CPqbVW8.jpg");
        add("http://i.imgur.com/Ckf5OeO.jpg");
        add("http://i.imgur.com/3jq1bv7.jpg");
        add("http://i.imgur.com/8bSITuc.jpg");
        add("http://i.imgur.com/JfKH8wd.jpg");
        add("http://i.imgur.com/KDfJruL.jpg");
        add("http://i.imgur.com/o6c6dVb.jpg");
        add("http://i.imgur.com/B1bUG2K.jpg");
        add("http://i.imgur.com/AfxvVuq.jpg");
        add("http://i.imgur.com/DSDtm.jpg");
        add("http://i.imgur.com/SAVYw7S.jpg");
        add("http://i.imgur.com/4HznKil.jpg");
        add("http://i.imgur.com/meeB00V.jpg");
        add("http://i.imgur.com/CPh0SRT.jpg");
        add("http://i.imgur.com/8niPBvE.jpg");
        add("http://i.imgur.com/dci41f3.jpg");
    }};
    private ImageFactory imgFactory;
    private ImageView imageView;
    int location=0;
    private CountDownTimer timeClock;
    private TextView timeTxt;
    private TextView imgTxt;
    private int interval=50000;
    private SeekBar timeSetter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        timeTxt = (TextView) findViewById(R.id.timerTxt);
        imgTxt = (TextView) findViewById(R.id.imageLabel);

        timeSetter = (SeekBar) findViewById(R.id.seekBar);

        Button nextBut = (Button) findViewById(R.id.nextBut);
        Button prevBut = (Button) findViewById(R.id.prevBut);
        Button randBut = (Button) findViewById(R.id.randBut);

        makeTimer();

        timeClock.start();
        timeSetter.setMax(55000);
        timeSetter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                interval=progress+5000;
                timeTxt.setText(""+interval/1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                timeClock.cancel();
                timeTxt.setText(""+interval/1000);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                makeTimer();
                timeClock.start();
            }
        });

        nextBut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                timeClock.cancel();
                location++;
                if (location >= urlList.size()) {
                    location = 0;
                }
                getImg(urlList.get(location));
            }
        });

        prevBut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                timeClock.cancel();

                location--;
                if (location<0)
                {
                    location=urlList.size()-1;
                }
                getImg(urlList.get(location));
            }
        });

        randBut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                timeClock.cancel();

                location = new Random().nextInt(urlList.size());
                getImg(urlList.get(location));
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getImg(String imgurURL){
        imgFactory=ImageFactory.getInstance(getApplicationContext());
        imgFactory.fetchImage(imgurURL, new ImageFactory.ImageFoundCallback() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
                timeClock.start();
                imgTxt.setText("Image: " + (location+1) + "/16");
            }

            @Override
            public void onFailure(Throwable reason) {
                imageView.setImageResource(R.drawable.cat_error);
                timeClock.start();
                imgTxt.setText("Kitty couldn't find image: "+(location+1));

            }
        });
    }

    public void makeTimer(){
        timeClock = new CountDownTimer(interval, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeTxt.setText("" + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                location = new Random().nextInt(urlList.size());
                getImg(urlList.get(location));
            }
        };
    }
}
