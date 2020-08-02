package com.example.safetyappv1;

import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder>
{

    private List<VideoItem> VideoItemList;

    public VideoAdapter(List<VideoItem> videoItemList) {
        VideoItemList = videoItemList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_videos,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.setVideoData(VideoItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return VideoItemList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder{

        private VideoView videoView;
        TextView textVideoTitle,textVideoDescription;
        ProgressBar videoProgressbar;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            videoView = itemView.findViewById(R.id.VideosDisplay);
            textVideoDescription = itemView.findViewById(R.id.textVideoDescription);;
            textVideoTitle = itemView.findViewById(R.id.textVideoTitle);
            videoProgressbar = itemView.findViewById(R.id.videoProgressBar);
        }

        void setVideoData(VideoItem item)
        {
            textVideoTitle.setText(item.getVideoTitle());
            textVideoDescription.setText(item.getVideoDescription());
            videoView.setVideoPath(item.getVideoUrl());


            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                    videoProgressbar.setVisibility(View.GONE);
                    mp.start();

                    float videoRatio = mp.getVideoWidth() /(float) mp.getVideoHeight();
                    float ScreenRatio = videoView.getWidth() /(float) videoView.getHeight();

                    float scale = videoRatio / ScreenRatio;
                    if(scale >= 1f)
                    {
                        videoView.setScaleX(scale);
                    }
                    else
                    {
                        videoView.setScaleY(1f / scale);
                    }
                }
            });
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.start();
                }
            });
        }
    }
}
