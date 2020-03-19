package com.liminal.eagamification;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.Objects;

class AugmentVideo{

    private ExternalTexture texture;
    private ModelRenderable renderable;
    private SimpleExoPlayer player;
    private AnchorNode anchorNode;
    boolean isTracking = false;

    AugmentVideo(Context context){
        texture = new ExternalTexture();
        player = new SimpleExoPlayer.Builder(context).build();
    }

    void videoAugment(String url, Context context){
        //Initialize player
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, "EasyAugment");
        Uri uri = Uri.parse(url);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        player.prepare(mediaSource, false, false);
        player.setVideoSurface(texture.getSurface());
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.setPlayWhenReady(false);

        //Initialize plane to be augmented
        ModelRenderable
                .builder()
                .setSource(context, R.raw.augmented_video_model)
                .build()
                .thenAccept(modelRenderable -> {
                    modelRenderable.getMaterial().setExternalTexture("videoTexture",
                            texture);

                    renderable = modelRenderable;
                    renderable.setShadowCaster(false);
                    renderable.setShadowReceiver(false);
                });
    }

    // Place plane over marker and set it as video augmentation surface
    void playVideo(Anchor anchor, float extentX, float extentZ, Scene scene) {
        anchorNode = new AnchorNode(anchor);

        texture.getSurfaceTexture().setOnFrameAvailableListener(surfaceTexture -> {
            anchorNode.setRenderable(renderable);
            texture.getSurfaceTexture().setOnFrameAvailableListener(null);
        });

        anchorNode.setWorldScale(new Vector3(extentX, 1f, extentZ));
        scene.addChild(anchorNode);
        player.setPlayWhenReady(true);
    }

    void release(Scene scene) {
        player.release();
        player = null;
        scene.removeChild(anchorNode);
        Objects.requireNonNull(anchorNode.getAnchor()).detach();
        anchorNode.setParent(null);
        anchorNode = null;
    }
}
