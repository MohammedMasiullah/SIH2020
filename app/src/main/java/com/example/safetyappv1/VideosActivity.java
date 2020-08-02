package com.example.safetyappv1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class VideosActivity extends AppCompatActivity {

    private VideoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        setupVideoItems();

        final ViewPager2 VideoViewPager =findViewById(R.id.videoViewPager);
        VideoViewPager.setAdapter(adapter);
    }

    private void setupVideoItems()
    {
        List<VideoItem> VideoItemList = new ArrayList<>();

//        VideoItem videoItemCelebration = new VideoItem();
//        videoItemCelebration.setVideoUrl("android.resource://" + getPackageName() + "/" + R.raw.myvideo);
//        videoItemCelebration.setVideoTitle("Celebration");
//        videoItemCelebration.setVideoDescription("celebrate who you are in your deepest heart, Love your self and the world will love you");


        VideoItem videoItemParty = new VideoItem();
        videoItemParty.setVideoUrl("https://firebasestorage.googleapis.com/v0/b/safetyappv1.appspot.com/o/Video1.mp4?alt=media&token=9117b50d-69d6-46b8-b68b-86231fe934de");
        videoItemParty.setVideoTitle("Safety tip 1");
        videoItemParty.setVideoDescription("When someone grabs your hand forcefully");

        VideoItem newItem = new VideoItem();
        newItem.setVideoUrl("https://firebasestorage.googleapis.com/v0/b/safetyappv1.appspot.com/o/Video2.mp4?alt=media&token=562e11a8-46d1-44ef-b7ed-8027adff3182");
        newItem.setVideoTitle("Safety tip 2");
        newItem.setVideoDescription("When someone forcefully comes closer to you");

//        VideoItem newVideo = new VideoItem();
//        newVideo.setVideoUrl("https://www.infinityandroid.com/videos/video4.mp4");
//        newVideo.setVideoTitle("Excercise");
//        newVideo.setVideoDescription("Excercise is Required");

        //VideoItemList.add(videoItemCelebration);
        VideoItemList.add(videoItemParty);
        VideoItemList.add(newItem);
        //VideoItemList.add(newVideo);

        adapter = new VideoAdapter(VideoItemList);
    }
}