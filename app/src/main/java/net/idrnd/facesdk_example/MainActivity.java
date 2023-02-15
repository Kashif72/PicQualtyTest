package net.idrnd.facesdk_example;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import net.idrnd.android.media.AssetsExtractor;
import net.idrnd.facesdk.DetectEngine;
import net.idrnd.facesdk.DetectionResult;
import net.idrnd.facesdk.FaceException;
import net.idrnd.facesdk.FaceParameters;
import net.idrnd.facesdk.Image;
import net.idrnd.facesdk.InitConfig;
import net.idrnd.facesdk.Meta;
import net.idrnd.facesdk.Pipeline;
import net.idrnd.facesdk.QualityEngine;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FACESDK_LOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Extract FaceSDK initialization data.
        // This method skips extraction if it was already done for this SDK version
        AssetsExtractor assetsExtractor = new AssetsExtractor(getApplicationContext());
        File assetsDir = assetsExtractor.extractAssets();

        // 2. Retrieve init data path in external dir
        String assetsPath = assetsDir.getPath();

        /*
         * 3. Perform face detection
         */

        // Init face detector
        // Engine initialization may be slow, so it should be done once per app launch
        DetectEngine detectEngine = new DetectEngine(
            "BaseNnetDetector",
            new InitConfig(
                new File(assetsPath, AssetsExtractor.DATA_SUBPATH + "/detection/ssd/").toString(),
                "ssd_f8.conf"
            )
        );

        // Perform face detection
        try (Image image = new Image(new File(assetsPath, "real_face.png").toString())) {
            DetectionResult result = detectEngine.detect(image);
            Log.i(TAG, result.toString());

            for (FaceParameters face: result.getFaces()) {
                Log.i(TAG, face.getBoundingBox().toString());
            }
        }

        /*
         * 4. Perform liveness check
         */

        // Init liveness checking pipeline
        // Pipeline initialization may be slow, so it should be done once per app launch
        Pipeline pipeline = new Pipeline(
            "ConfigurablePipeline",
            new InitConfig(
                new File(assetsPath, AssetsExtractor.DATA_SUBPATH + "/pipelines/").toString(),
                "demetra.conf"
            )
        );

        // Perform liveness check
        try (Image image = new Image(new File(assetsPath, "real_face.png").toString())) {
            // As we are doing liveness checks from the Android app, we can use
            // a special calibrarion value to improve liveness checking accuracy
            Meta meta = new Meta();
            meta.setOs(Meta.Os.ANDROID);

            Log.i(TAG, pipeline.checkLiveness(image, meta).toString());
        }

        /*
         * 5. Perform quality check
         */

        // Init quality engine
        // Engine initialization may be slow, so it should be done once per app launch
        QualityEngine qualityEngine = new QualityEngine(
            "BlurColorNnet",
            new InitConfig(
                new File(assetsPath, AssetsExtractor.DATA_SUBPATH + "/quality/BlurColorNnet/").toString(),
                "model_b_v1_f8.conf"
            )
        );

        // Perform quality check
        try (Image image = new Image(new File(assetsPath, "too_many_faces.jpg").toString())) {
            Log.i(TAG, qualityEngine.checkQuality(image).toString());
        }

        /*
         * 6. Handle FaceException during liveness check
         */

        try (Image image = new Image(new File(assetsPath, "face_not_found.jpg").toString())) {
            pipeline.checkLiveness(image);
        } catch (FaceException e) {
            Log.i(TAG, e.toString());
        }

        try (Image image = new Image(new File(assetsPath, "too_many_faces.jpg").toString())) {
            pipeline.checkLiveness(image);
        } catch (FaceException e) {
            Log.i(TAG, e.toString());
        }
    }
}
