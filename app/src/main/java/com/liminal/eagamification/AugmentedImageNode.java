package com.liminal.eagamification;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.concurrent.CompletableFuture;

class AugmentedImageNode extends AnchorNode {

    private static final String TAG = "AUGMENTED_IMAGE_NODE";

    private CompletableFuture<ModelRenderable> objRenderable;
    private ArFragment arFragment;

    // Constructor for AInode
    AugmentedImageNode(Context context, String modelName, ArFragment arFragment) {
        this.arFragment = arFragment;
        objRenderable =
                ModelRenderable.builder()
                        .setSource(context, Uri.parse(modelName))
                        .build();
    }

    // Function to set anchor AR model to the image
    void setImage(AugmentedImage image) {

        // Initialize mazeNode and set its parents and the Renderable. If any of the models are not loaded, process this function until they all are loaded.
        if (!objRenderable.isDone()) {
            CompletableFuture.allOf(objRenderable)
                    .thenAccept((Void aVoid) -> setImage(image))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "3D Model is rendering", throwable);
                                return null;
                            });
            return;
        }

        // Set the anchor based on the center of the image.
        setAnchor(image.createAnchor(image.getCenterPose()));

        Log.d(TAG, String.valueOf(objRenderable));

        // Assign renderable to ObjNode
//        Node objNode = new Node();
//        objNode.setParent(this);
//        objNode.setRenderable(objRenderable.getNow(null));

        //To allow user to interact with 3D model
        TransformableNode objNode = new TransformableNode(arFragment.getTransformationSystem());
        objNode.getTranslationController().setEnabled(false);
        objNode.setRenderable(objRenderable.getNow(null));
        objNode.setParent(this);
    }
}
